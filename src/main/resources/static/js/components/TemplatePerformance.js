Vue.component('template-performance', {
    template: `
        <div class="card">
            <div class="card-header">
                Template Performance
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
            startHeight: 0
        };
    },
    computed: {
        hasData() {
            return this.analytics && 
                   this.analytics.ctrByTemplate && 
                   Object.keys(this.analytics.ctrByTemplate).length > 0;
        },
        chartData() {
            if (!this.hasData) {
                return {
                    labels: [],
                    datasets: [{
                        data: [],
                        backgroundColor: []
                    }]
                };
            }

            // Limit to top 10 templates if there are more
            let templateIds = Object.keys(this.analytics.ctrByTemplate);
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