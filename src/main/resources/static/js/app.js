new Vue({
    el: '#app',
    template: `
        <div>
            <nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-4">
                <div class="container">
                    <a class="navbar-brand" href="#">Store Analytics Dashboard</a>
                </div>
            </nav>
            
            <div class="container">
                <div class="row mb-4">
                    <div class="col-lg-4 col-md-5 mb-4">
                        <store-selector @store-selected="loadStoreAnalytics"></store-selector>
                    </div>
                    <div class="col-lg-8 col-md-7" v-if="analytics">
                        <analytics-summary :analytics="analytics"></analytics-summary>
                    </div>
                </div>
                
                <div v-if="isLoading" class="row">
                    <div class="col-12 text-center py-5">
                        <div class="spinner"></div>
                        <p class="mt-3">Loading analytics data...</p>
                    </div>
                </div>
                
                <div v-else-if="error" class="row">
                    <div class="col-12">
                        <div class="alert alert-danger">
                            <strong>Error:</strong> {{ error }}
                            <button class="btn btn-sm btn-outline-danger float-right" @click="clearError">Dismiss</button>
                        </div>
                    </div>
                </div>
                
                <div v-else-if="analytics" class="row">
                    <div class="col-lg-6 col-12 mb-4">
                        <message-history :analytics="analytics" :is-loading="isLoading"></message-history>
                    </div>
                    <div class="col-lg-6 col-12 mb-4">
                        <template-performance :analytics="analytics" :is-loading="isLoading"></template-performance>
                    </div>
                    
                    <!-- CTR Prediction Section -->
                    <div class="col-12 mt-2">
                        <h4 class="mb-3">
                            <i class="fas fa-brain mr-2"></i>
                            AI-Powered Insights
                        </h4>
                        <div class="row">
                            <div class="col-12">
                                <ctr-prediction-chart 
                                    :store-id="selectedStoreId" 
                                    :days="predictionDays"
                                    @update:days="updatePredictionDays">
                                </ctr-prediction-chart>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div v-else-if="!isLoading && !error" class="row">
                    <div class="col-12 text-center py-5">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle mr-2"></i>
                            Select an organization and store to view analytics
                        </div>
                    </div>
                </div>
            </div>
            
            <footer class="container mt-5 mb-3 text-center text-muted">
                <p>&copy; 2023 Store Analytics Dashboard</p>
            </footer>
        </div>
    `,
    data: {
        analytics: null,
        isLoading: false,
        error: null,
        selectedStoreId: null,
        predictionDays: 30
    },
    methods: {
        loadStoreAnalytics(storeId) {
            if (!storeId) return;
            
            this.selectedStoreId = storeId;
            this.isLoading = true;
            this.analytics = null;
            this.error = null;
            
            axios.get(`/api/analytics/store/${storeId}`)
                .then(response => {
                    if (!response.data) {
                        this.error = 'No analytics data returned for this store';
                        return;
                    }
                    this.analytics = response.data;
                })
                .catch(error => {
                    console.error('Error loading analytics:', error);
                    this.error = error.response?.data?.message || 
                                 'Failed to load analytics data. Please try again later.';
                })
                .finally(() => {
                    this.isLoading = false;
                });
        },
        clearError() {
            this.error = null;
        },
        updatePredictionDays(days) {
            this.predictionDays = days;
        }
    }
}); 