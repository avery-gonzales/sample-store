Vue.component('analytics-summary', {
    template: `
        <div class="row">
            <div class="col-md-4 col-sm-12 mb-3">
                <div class="card summary-card">
                    <div class="number">{{ analytics.totalMessages || 0 }}</div>
                    <div class="label">Total Messages Sent</div>
                </div>
            </div>
            <div class="col-md-4 col-sm-6 mb-3">
                <div class="card summary-card">
                    <div class="number">{{ analytics.totalClicks || 0 }}</div>
                    <div class="label">Total Link Clicks</div>
                </div>
            </div>
            <div class="col-md-4 col-sm-6 mb-3">
                <div class="card summary-card">
                    <div class="number">{{ clickThroughRate }}%</div>
                    <div class="label">Overall Click-Through Rate</div>
                </div>
            </div>
        </div>
    `,
    props: {
        analytics: {
            type: Object,
            required: true
        }
    },
    computed: {
        clickThroughRate() {
            if (!this.analytics.totalMessages || this.analytics.totalMessages === 0) {
                return '0.00';
            }
            
            const rate = (this.analytics.totalClicks / this.analytics.totalMessages) * 100;
            return rate.toFixed(2);
        }
    }
}); 