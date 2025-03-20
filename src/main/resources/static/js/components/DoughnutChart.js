Vue.component('doughnut-chart', {
    extends: VueChartJs.Doughnut,
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
                cutoutPercentage: 70,
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        fontSize: 10,
                        padding: 5
                    }
                },
                animation: {
                    animateScale: true,
                    animateRotate: true
                },
                tooltips: {
                    titleFontSize: 10,
                    bodyFontSize: 10,
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var dataset = data.datasets[tooltipItem.datasetIndex];
                            var total = dataset.data.reduce(function(previousValue, currentValue) {
                                return previousValue + currentValue;
                            });
                            var currentValue = dataset.data[tooltipItem.index];
                            var percentage = Math.floor(((currentValue/total) * 100) + 0.5);
                            return data.labels[tooltipItem.index] + ': ' + currentValue + ' (' + percentage + '%)';
                        }
                    }
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