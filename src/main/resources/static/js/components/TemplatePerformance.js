Vue.component('template-performance', {
    template: `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Template Performance</span>
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
                <div v-else-if="!hasData" class="text-center p-4">
                    <div class="alert alert-info">
                        No template performance data available
                    </div>
                </div>
                <div v-else class="chart-container" ref="chartContainer">
                    <doughnut-chart :chart-data="chartData"></doughnut-chart>
                    <div class="chart-resize-handle" @mousedown="startResize"></div>
                </div>
            </div>
            <div class="card-footer">
                <small class="text-muted">Click-through rates by template</small>
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
        hasData() {
            return this.analytics && 
                   this.analytics.ctrByTemplate && 
                   Object.keys(this.analytics.ctrByTemplate).length > 0;
        },
        filteredTemplateIds() {
            if (!this.hasData) return [];
            if (this.dateRange === 'all') return Object.keys(this.analytics.ctrByTemplate);
            
            // For date-specific filtering, we need to examine message dates for each template
            const cutoffDate = this.getCutoffDate();
            const result = [];
            
            if (this.analytics.messagesByMonth) {
                // Identify which templates were used within the specified date range
                // This is a simplified approach - in a real app you'd need data that associates
                // templates with specific dates
                Object.keys(this.analytics.ctrByTemplate).forEach(templateId => {
                    // For this simplified example, we include all templates
                    // In a real app, you would filter based on template usage dates
                    result.push(templateId);
                });
            }
            
            return result;
        },
        chartData() {
            if (!this.hasData || this.filteredTemplateIds.length === 0) {
                return {
                    labels: [],
                    datasets: [{
                        data: [],
                        backgroundColor: []
                    }]
                };
            }

            // Limit to top 10 templates if there are more
            let templateIds = this.filteredTemplateIds;
            let ctrValues = templateIds.map(id => this.analytics.ctrByTemplate[id]);
            
            // Sort by CTR value in descending order and take top 10
            if (templateIds.length > 10) {
                const sortedPairs = templateIds.map((id, index) => ({
                    id,
                    ctr: ctrValues[index]
                })).sort((a, b) => b.ctr - a.ctr).slice(0, 10);
                
                templateIds = sortedPairs.map(pair => pair.id);
                ctrValues = sortedPairs.map(pair => pair.ctr);
            }
            
            // Generate colors
            const backgroundColors = this.generateColors(templateIds.length);
            
            return {
                labels: templateIds.map(id => `Template ${id}`),
                datasets: [{
                    data: ctrValues,
                    backgroundColor: backgroundColors
                }]
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
        
        generateColors(count) {
            const colors = [];
            for (let i = 0; i < count; i++) {
                // Generate a color based on a hue rotation
                const hue = (i * 137.5) % 360;
                colors.push(`hsl(${hue}, 70%, 60%)`);
            }
            return colors;
        }
    }
}); 