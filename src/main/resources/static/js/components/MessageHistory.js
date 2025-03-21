Vue.component('message-history', {
    template: `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Message History</span>
                <div class="btn-group">
                    <button class="btn btn-sm btn-outline-secondary" :class="{ active: dateRange === '30' }" @click="setDateRange('30')">30 Days</button>
                    <button class="btn btn-sm btn-outline-secondary" :class="{ active: dateRange === '90' }" @click="setDateRange('90')">90 Days</button>
                    <button class="btn btn-sm btn-outline-secondary" :class="{ active: dateRange === '180' }" @click="setDateRange('180')">6 Months</button>
                    <button class="btn btn-sm btn-outline-secondary" :class="{ active: dateRange === 'all' }" @click="setDateRange('all')">All Time</button>
                </div>
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
            startHeight: 0,
            dateRange: '90' // Default to 90 days
        };
    },
    computed: {
        totalMessages() {
            if (!this.analytics || !this.filteredMessagesByMonth) return 0;
            return this.filteredMessagesByMonth.reduce((sum, item) => sum + parseInt(item.count), 0);
        },
        totalClicks() {
            if (!this.analytics || !this.filteredClicksByMonth) return 0;
            return this.filteredClicksByMonth.reduce((sum, item) => sum + parseInt(item.count), 0);
        },
        filteredMessagesByMonth() {
            if (!this.analytics || !this.analytics.messagesByMonth) return [];
            if (this.dateRange === 'all') return this.analytics.messagesByMonth;
            
            const cutoffDate = this.getCutoffDate();
            return this.analytics.messagesByMonth.filter(item => {
                const itemDate = new Date(parseInt(item.year), parseInt(item.month) - 1, 1);
                return itemDate >= cutoffDate;
            });
        },
        filteredClicksByMonth() {
            if (!this.analytics || !this.analytics.clicksByMonth) return [];
            if (this.dateRange === 'all') return this.analytics.clicksByMonth;
            
            const cutoffDate = this.getCutoffDate();
            return this.analytics.clicksByMonth.filter(item => {
                const itemDate = new Date(parseInt(item.year), parseInt(item.month) - 1, 1);
                return itemDate >= cutoffDate;
            });
        },
        chartData() {
            if (!this.analytics || !this.filteredMessagesByMonth.length === 0 || !this.filteredClicksByMonth.length === 0) {
                return {
                    labels: [],
                    datasets: []
                };
            }

            // Process messages by month data
            const messagesByMonth = this.filteredMessagesByMonth;
            const clicksByMonth = this.filteredClicksByMonth;
            
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
            
            // Extract data for chart
            const labels = sortedDates.map(dateKey => {
                const [year, month] = dateKey.split('-');
                return `${this.getMonthName(parseInt(month))} ${year}`;
            });
            
            const messageData = sortedDates.map(dateKey => allMonthsMap.get(dateKey).messages);
            const clickData = sortedDates.map(dateKey => allMonthsMap.get(dateKey).clicks);
            
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
        setDateRange(range) {
            this.dateRange = range;
        },
        getCutoffDate() {
            const now = new Date();
            const days = parseInt(this.dateRange);
            const cutoffDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - days);
            return cutoffDate;
        },
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