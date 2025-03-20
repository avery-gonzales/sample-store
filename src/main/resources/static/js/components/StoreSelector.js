Vue.component('store-selector', {
    template: `
        <div class="card">
            <div class="card-header">
                Select Organization and Store
            </div>
            <div class="card-body">
                <div v-if="isLoading" class="text-center py-3">
                    <div class="spinner"></div>
                    <p>Loading organizations...</p>
                </div>
                <div v-else-if="error" class="alert alert-danger">
                    {{ error }}
                </div>
                <div v-else>
                    <div class="form-group">
                        <label for="organization">Organization</label>
                        <select class="form-control" id="organization" v-model="selectedOrganizationId" @change="loadStores">
                            <option value="">Select an organization</option>
                            <option v-for="org in organizations" :value="org.id" :key="org.id">
                                {{ org.name || 'Unnamed Organization #' + org.id }}
                            </option>
                        </select>
                        <small v-if="organizations.length === 0" class="text-danger">
                            No organizations found.
                        </small>
                    </div>
                    <div class="form-group">
                        <label for="store">Store</label>
                        <select class="form-control" id="store" v-model="selectedStoreId" @change="storeSelected" :disabled="!stores.length">
                            <option value="">Select a store</option>
                            <option v-for="store in stores" :value="store.id" :key="store.id">
                                {{ store.name || 'Unnamed Store #' + store.id }}
                            </option>
                        </select>
                        <small v-if="stores.length === 0 && selectedOrganizationId" class="text-danger">
                            No stores found for this organization.
                        </small>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            organizations: [],
            stores: [],
            selectedOrganizationId: '',
            selectedStoreId: '',
            isLoading: false,
            error: null,
            debug: {
                apiResponse: null,
                parsedData: null
            }
        }
    },
    created() {
        console.log('StoreSelector component created');
        this.testDirectFetch(); // Test the direct fetch first
        this.loadOrganizations();
    },
    methods: {
        testDirectFetch() {
            console.log('Testing direct fetch...');
            fetch('/api/organizations/dropdown')
                .then(response => response.json())
                .then(data => {
                    console.log('Direct fetch data (raw):', JSON.stringify(data));
                    this.debug.apiResponse = data;
                    
                    if (Array.isArray(data)) {
                        console.log('Direct fetch returned an array of length:', data.length);
                        data.forEach((item, index) => {
                            if (index < 5) { // Log first 5 items
                                console.log(`Item ${index}:`, item);
                            }
                        });
                    } else {
                        console.log('Direct fetch did not return an array but:', typeof data);
                    }
                })
                .catch(error => {
                    console.error('Direct fetch error:', error);
                });
        },
        loadOrganizations() {
            console.log('Loading organizations...');
            this.isLoading = true;
            this.error = null;
            
            // Use the simplified dropdown endpoint
            axios.get('/api/organizations/dropdown')
                .then(response => {
                    console.log('Organizations axios response:', response);
                    console.log('Organizations data:', response.data);
                    console.log('Is array?', Array.isArray(response.data));
                    console.log('Type:', typeof response.data);
                    console.log('Stringified:', JSON.stringify(response.data).substring(0, 100) + '...');
                    
                    if (!response.data) {
                        this.error = 'Invalid response format from server';
                        this.organizations = [];
                        return;
                    }
                    
                    try {
                        // This should be a simple array of {id, name} objects
                        let orgs = response.data;
                        if (Array.isArray(orgs)) {
                            this.organizations = orgs.filter(org => org && org.id && org.name).map(org => ({
                                id: org.id,
                                name: org.name
                            }));
                        } else {
                            throw new Error('Response is not an array');
                        }
                        
                        console.log('Final organizations for display:', this.organizations);
                        
                        if (this.organizations.length === 0) {
                            this.error = 'No valid organizations found';
                        }
                    } catch (err) {
                        console.error('Error processing organizations:', err);
                        this.error = 'Failed to process organization data: ' + err.message;
                        this.organizations = [];
                    }
                    
                    this.isLoading = false;
                    
                    if (this.organizations.length > 0) {
                        this.selectedOrganizationId = this.organizations[0].id;
                        this.loadStores();
                    }
                })
                .catch(error => {
                    console.error('Error loading organizations:', error);
                    this.isLoading = false;
                    this.error = 'Failed to load organizations. Please try again.';
                    this.organizations = [];
                });
        },
        loadStores() {
            if (!this.selectedOrganizationId) {
                this.stores = [];
                return;
            }
            
            console.log('Loading stores for organization ID:', this.selectedOrganizationId);
            this.selectedStoreId = '';
            this.isLoading = true;
            this.error = null;
            
            // Use the simplified dropdown endpoint
            axios.get(`/api/stores/organization/${this.selectedOrganizationId}/dropdown`)
                .then(response => {
                    console.log('Stores axios response:', response);
                    console.log('Stores data:', response.data);
                    console.log('Is array?', Array.isArray(response.data));
                    console.log('Type:', typeof response.data);
                    console.log('Stringified:', JSON.stringify(response.data).substring(0, 100) + '...');
                    
                    if (!response.data) {
                        this.error = 'Invalid store data format from server';
                        this.stores = [];
                        return;
                    }
                    
                    try {
                        // This should be a simple array of {id, name} objects
                        let storeData = response.data;
                        if (Array.isArray(storeData)) {
                            this.stores = storeData.filter(store => store && store.id && store.name).map(store => ({
                                id: store.id,
                                name: store.name
                            }));
                        } else {
                            throw new Error('Stores response is not an array');
                        }
                        
                        console.log('Final stores for display:', this.stores);
                        
                        if (this.stores.length === 0) {
                            console.log('No stores found for this organization');
                        }
                    } catch (err) {
                        console.error('Error processing stores:', err);
                        this.error = 'Failed to process store data: ' + err.message;
                        this.stores = [];
                    }
                    
                    this.isLoading = false;
                    
                    if (this.stores.length > 0) {
                        this.selectedStoreId = this.stores[0].id;
                        this.storeSelected();
                    }
                })
                .catch(error => {
                    console.error('Error loading stores:', error);
                    this.isLoading = false;
                    this.error = 'Failed to load stores. Please try again.';
                    this.stores = [];
                });
        },
        storeSelected() {
            if (this.selectedStoreId) {
                this.$emit('store-selected', this.selectedStoreId);
            }
        }
    }
}); 