/**
 * CTR Prediction Chart Component
 * Displays CTR predictions using Chart.js
 */
Vue.component('ctr-prediction-chart', {
    template: `
        <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">CTR Prediction for Next {{ days }} Days</h5>
                <div class="d-flex align-items-center">
                    <label class="mr-2 mb-0">Days:</label>
                    <select class="form-control form-control-sm" v-model="selectedDays" @change="updateDays">
                        <option value="7">7</option>
                        <option value="14">14</option>
                        <option value="30">30</option>
                        <option value="60">60</option>
                        <option value="90">90</option>
                    </select>
                </div>
            </div>
            <div class="card-body">
                <div v-if="loading" class="text-center py-5">
                    <div class="spinner"></div>
                    <p class="mt-2">Loading predictions...</p>
                </div>
                <div v-else-if="error" class="alert alert-danger">
                    <i class="fas fa-exclamation-circle mr-2"></i>
                    {{ error }}
                </div>
                <div v-else-if="!predictionData || Object.keys(predictionData.predictions || {}).length === 0" class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle mr-2"></i>
                    Not enough historical data to generate predictions
                </div>
                <div v-else>
                    <div class="chart-container">
                        <canvas ref="chart" height="300"></canvas>
                    </div>
                    <div class="row mt-3">
                        <div class="col-md-4 text-center">
                            <div class="card bg-light">
                                <div class="card-body py-2">
                                    <h6 class="mb-0">Current CTR</h6>
                                    <div class="h3 mb-0">{{ (predictionData.currentCTR * 100).toFixed(2) }}%</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4 text-center">
                            <div class="card" :class="predictionTrendClass">
                                <div class="card-body py-2">
                                    <h6 class="mb-0">Average Predicted CTR</h6>
                                    <div class="h3 mb-0">{{ (predictionData.averagePredictedCTR * 100).toFixed(2) }}%</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4 text-center">
                            <div class="card" :class="predictionTrendClass">
                                <div class="card-body py-2">
                                    <h6 class="mb-0">Trend</h6>
                                    <div class="h3 mb-0">
                                        <i :class="trendIcon"></i>
                                        {{ formattedPercentChange }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    props: {
        storeId: {
            type: [Number, String],
            required: true
        },
        days: {
            type: Number,
            default: 30
        }
    },
    data() {
        return {
            loading: false,
            error: null,
            predictionData: null,
            chart: null,
            selectedDays: 30
        };
    },
    computed: {
        trendIcon() {
            if (!this.predictionData) return '';
            const change = this.getPercentChange();
            if (change > 0) return 'fas fa-arrow-up';
            if (change < 0) return 'fas fa-arrow-down';
            return 'fas fa-equals';
        },
        formattedPercentChange() {
            const change = this.getPercentChange();
            return (change > 0 ? '+' : '') + change.toFixed(2) + '%';
        },
        predictionTrendClass() {
            if (!this.predictionData) return 'bg-light';
            const change = this.getPercentChange();
            if (change > 5) return 'bg-success text-white';
            if (change < -5) return 'bg-danger text-white';
            return 'bg-warning';
        }
    },
    methods: {
        getPercentChange() {
            if (!this.predictionData || !this.predictionData.averagePredictedCTR || !this.predictionData.currentCTR) return 0;
            
            // Calculate percent change between current CTR and predicted average CTR
            const current = this.predictionData.currentCTR;
            const predicted = this.predictionData.averagePredictedCTR;
            const percentChange = ((predicted - current) / current) * 100;
            return percentChange;
        },
        updateDays() {
            this.$emit('update:days', parseInt(this.selectedDays));
            this.fetchPredictions();
        },
        fetchPredictions() {
            if (!this.storeId) return;
            
            this.loading = true;
            this.error = null;
            
            axios.get(`/api/predictions/ctr/${this.storeId}?days=${this.selectedDays}`)
                .then(response => {
                    this.predictionData = response.data;
                    // Wait for DOM to update, then wait a bit more to ensure canvas is ready
                    this.$nextTick(() => {
                        setTimeout(() => {
                            this.renderChart();
                        }, 50);
                    });
                })
                .catch(error => {
                    console.error('Error fetching CTR predictions:', error);
                    this.error = error.response?.data?.message || 
                                 'Failed to load prediction data. Please try again later.';
                })
                .finally(() => {
                    this.loading = false;
                });
        },
        renderChart() {
            if (!this.predictionData || !this.predictionData.predictions || !this.$refs.chart) {
                console.warn('Cannot render chart: missing data or canvas element');
                return;
            }
            
            if (this.chart) {
                this.chart.destroy();
            }
            
            const ctx = this.$refs.chart.getContext('2d');
            const dates = Object.keys(this.predictionData.predictions);
            const values = dates.map(date => this.predictionData.predictions[date]);
            
            // Sort dates chronologically
            const sortedIndices = dates.map((date, i) => i)
                .sort((a, b) => new Date(dates[a]) - new Date(dates[b]));
            
            const sortedDates = sortedIndices.map(i => dates[i]);
            const sortedValues = sortedIndices.map(i => values[i]);
            
            this.chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: sortedDates,
                    datasets: [{
                        label: 'Predicted CTR',
                        data: sortedValues.map(val => val * 100), // Convert to percentage
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 2,
                        pointRadius: 3,
                        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
                        tension: 0.3
                    }, {
                        label: 'Current CTR',
                        data: Array(sortedDates.length).fill(this.predictionData.currentCTR * 100), // Convert to percentage
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 2,
                        borderDash: [5, 5],
                        pointRadius: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        yAxes: [{
                            ticks: {
                                beginAtZero: true,
                                callback: function(value) {
                                    return value + '%';
                                }
                            },
                            scaleLabel: {
                                display: true,
                                labelString: 'Click-Through Rate (%)'
                            }
                        }],
                        xAxes: [{
                            scaleLabel: {
                                display: true,
                                labelString: 'Date'
                            }
                        }]
                    },
                    tooltips: {
                        callbacks: {
                            label: function(tooltipItem, data) {
                                return data.datasets[tooltipItem.datasetIndex].label + ': ' + 
                                       tooltipItem.yLabel.toFixed(2) + '%';
                            }
                        }
                    }
                }
            });
        }
    },
    watch: {
        storeId: {
            handler(newVal) {
                if (newVal) {
                    this.selectedDays = this.days;
                    this.fetchPredictions();
                }
            },
            immediate: true
        }
    },
    mounted() {
        if (this.storeId) {
            this.fetchPredictions();
        }
    }
}); 