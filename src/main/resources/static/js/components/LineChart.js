Vue.component('line-chart', {
    extends: VueChartJs.Line,
    props: {
        chartData: {
            type: Object,
            required: true
        },
        chartOptions: {
            type: Object,
            default: () => ({
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            maxTicksLimit: 6
                        },
                        gridLines: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    }],
                    xAxes: [{
                        ticks: {
                            maxTicksLimit: 10,
                            maxRotation: 45,
                            minRotation: 0
                        },
                        gridLines: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    }]
                },
                legend: {
                    labels: {
                        boxWidth: 12
                    }
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                    titleFontSize: 10,
                    bodyFontSize: 10
                }
            })
        }
    },
    watch: {
        chartData: {
            handler() {
                this.renderChart(this.chartData, this.chartOptions);
            },
            deep: true
        }
    },
    mounted() {
        this.renderChart(this.chartData, this.chartOptions);
    }
}); 