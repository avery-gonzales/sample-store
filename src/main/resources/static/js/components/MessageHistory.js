Vue.component('message-history', {
    template: `
        <div class="card">
            <div class="card-header">
                Message History
            </div>
            <div class="card-body">
                <div v-if="isLoading" class="loading">
                    <div class="spinner"></div>
                </div>
                <div v-else class="chart-container" ref="chartContainer">
                    <line-chart :chart-data="chartData"></line-chart>
                    <div class="chart-resize-handle" @mousedown="startResize"></div>
                </div>
                <div v-if="!isLoading && analytics && analytics.messagesByMonth" class="d-flex justify-content-between text-muted mt-3">
                    <small>Total Messages: {{ totalMessages }}</small>
                    <small>Total Clicks: {{ totalClicks }}</small>
                </div>
            </div>
            <div class="card-footer">
                <small class="text-muted">Messages sent and clicks over time</small>
            </div>
        </div>
    `,
    props: {
        analytics: {
            type: Object,
            required: true
        },
        isLoading: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            resizing: false,
            startY: 0,
            startHeight: 0
        };
    },
    computed: {
        totalMessages() {
            if (!this.analytics || !this.analytics.messagesByMonth) return 0;
            return this.analytics.messagesByMonth.reduce((sum, item) => sum + parseInt(item.count), 0);
        },
        totalClicks() {
            if (!this.analytics || !this.analytics.clicksByMonth) return 0;
            return this.analytics.clicksByMonth.reduce((sum, item) => sum + parseInt(item.count), 0);
        },
        chartData() {
            if (!this.analytics || !this.analytics.messagesByMonth || !this.analytics.clicksByMonth) {
                return {
                    labels: [],
                    datasets: []
                };
            }

            // Process messages by month data
            const messagesByMonth = this.analytics.messagesByMonth;
            const clicksByMonth = this.analytics.clicksByMonth;
            
            // Create a map of all months from both datasets
            const allMonthsMap = new Map();
            
            messagesByMonth.forEach(item => {
                const month = parseInt(item.month);
                const year = parseInt(item.year);
                const dateKey = `${year}-${month.toString().padStart(2, '0')}`;
                allMonthsMap.set(dateKey, { 
                    messages: parseInt(item.count),
                    clicks: 0
                });
            });
            
            clicksByMonth.forEach(item => {
                const month = parseInt(item.month);
                const year = parseInt(item.year);
                const dateKey = `${year}-${month.toString().padStart(2, '0')}`;
                
                if (allMonthsMap.has(dateKey)) {
                    const data = allMonthsMap.get(dateKey);
                    data.clicks = parseInt(item.count);
                    allMonthsMap.set(dateKey, data);
                } else {
                    allMonthsMap.set(dateKey, {
                        messages: 0,
                        clicks: parseInt(item.count)
                    });
                }
            });
            
            // Sort by date
            const sortedDates = Array.from(allMonthsMap.keys()).sort();
            
            // Get relevant time range - limit to 12 most recent months if more exist
            const limitedDates = sortedDates.length > 12 ? sortedDates.slice(-12) : sortedDates;
            
            // Extract data for chart
            const labels = limitedDates.map(dateKey => {
                const [year, month] = dateKey.split('-');
                return `${this.getMonthName(parseInt(month))} ${year}`;
            });
            
            const messageData = limitedDates.map(dateKey => allMonthsMap.get(dateKey).messages);
            const clickData = limitedDates.map(dateKey => allMonthsMap.get(dateKey).clicks);
            
            return {
                labels: labels,
                datasets: [
                    {
                        label: 'Messages Sent',
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        data: messageData,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                    },
                    {
                        label: 'Link Clicks',
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        data: clickData,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                    }
                ]
            };
        }
    },
    methods: {
        startResize(e) {
            this.resizing = true;
            this.startY = e.clientY;
            this.startHeight = this.$refs.chartContainer.clientHeight;
            
            document.addEventListener('mousemove', this.onResize);
            document.addEventListener('mouseup', this.stopResize);
            e.preventDefault();
        },
        
        onResize(e) {
            if (!this.resizing) return;
            
            const container = this.$refs.chartContainer;
            const newHeight = this.startHeight + (e.clientY - this.startY);
            
            if (newHeight >= 200 && newHeight <= 800) {
                container.style.height = `${newHeight}px`;
            }
        },
        
        stopResize() {
            this.resizing = false;
            document.removeEventListener('mousemove', this.onResize);
            document.removeEventListener('mouseup', this.stopResize);
        },
        
        getMonthName(monthNumber) {
            const months = [
                'January', 'February', 'March', 'April', 'May', 'June', 
                'July', 'August', 'September', 'October', 'November', 'December'
            ];
            return months[monthNumber - 1];
        }
    }
}); 