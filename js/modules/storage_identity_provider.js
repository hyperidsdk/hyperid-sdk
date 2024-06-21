const UserDataByIdpSetResult = {
	FAIL_BY_KEY_INVALID						: -8,
	FAIL_BY_KEY_ACCESS_DENIED				: -7,
	FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND	: -6,
	FAIL_BY_INVALID_PARAMETERS				: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID		: -4,
	FAIL_BY_ACCESS_DENIED					: -3,
	FAIL_BY_TOKEN_EXPIRED					: -2,
	FAIL_BY_TOKEN_INVALID					: -1,
	SUCCESS									: 0
}

const UserDataByIdpGetResult = {
	FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND	: -6,
	FAIL_BY_INVALID_PARAMETERS				: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID		: -4,
	FAIL_BY_ACCESS_DENIED					: -3,
	FAIL_BY_TOKEN_EXPIRED					: -2,
	FAIL_BY_TOKEN_INVALID					: -1,
	SUCCESS									: 0,
	SUCCESS_NOT_FOUND						: 1
}

const UserDataKeysByIdpGetResult = {
	FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND	: -6,
	FAIL_BY_INVALID_PARAMETERS				: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID		: -4,
	FAIL_BY_ACCESS_DENIED					: -3,
	FAIL_BY_TOKEN_EXPIRED					: -2,
	FAIL_BY_TOKEN_INVALID					: -1,
	SUCCESS									: 0,
	SUCCESS_NOT_FOUND						: 1
}

const UserDataKeysByIdpDeleteResult = {
	FAIL_BY_KEY_ACCESS_DENIED				: -7,
	FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND	: -6,
	FAIL_BY_INVALID_PARAMETERS				: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID		: -4,
	FAIL_BY_ACCESS_DENIED					: -3,
	FAIL_BY_TOKEN_EXPIRED					: -2,
	FAIL_BY_TOKEN_INVALID					: -1,
	SUCCESS									: 0,
	SUCCESS_NOT_FOUND						: 1
}

const UserDataAccessScope = {
	PRIVATE : 0,
	PUBLIC 	: 1
}

class HyperIDIDPStorage {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    async setData(accessToken,
        identityProvider,
        valueKey,
        valueData,
        accessScope = UserDataAccessScope.PUBLIC) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                identity_providers: [identityProvider],
                value_key: valueKey,
                value_data: valueData,
                access_scope: accessScope
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-idp/set", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataByIdpSetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataByIdpSetResult.FAIL_BY_KEY_INVALID: throw new Error("Provided key is invalid.")
                        case UserDataByIdpSetResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataByIdpSetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: throw new Error("Identity provider not found.")
                        case UserDataByIdpSetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByIdpSetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByIdpSetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async getData(accessToken,
                identityProvider,
                valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                identity_providers: [identityProvider],
                value_keys: [valueKey]
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-idp/get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataByIdpGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataByIdpGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByIdpGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByIdpGetResult.SUCCESS_NOT_FOUND: return null;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                const idp = data.identity_providers || [];
                if(idp) {
                    const valueData =idp[0].identity_provider || [];
                    if(valueData) {
                        for(item in valueData) {
                            if(item.value_key === 'valueKey') {
                                return item.value_data;
                            }
                        }
                    }
                }
                return null;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async getKeysList(accessToken, identityProvider) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                identity_providers: [identityProvider]
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-idp/list-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByIdpGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByIdpGetResult.FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND: throw new Error("Identity provider not found.")
                        case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpGetResult.SUCCESS_NOT_FOUND: return null;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                return data;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async getKeysListShared(accessToken, identityProvider) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            let shouldContinue = true;
            const keysShared = [];
            const searchId="";
            
            do {
                const params = {
                    identity_providers: [identityProvider],
                    search_id: searchId,
                    page_size: 100
                };
                const response = await fetch(this.restApiEndpoint + "/user-data/by-idp/shared-list-get", {
                    method: 'POST',
                    headers: {'Accept': 'application/json',
                        'Content-Type':'application/json',
                        'Authorization': "Bearer " + accessToken
                    },
                    body: JSON.stringify(params)
                });
                if(response.status >= 200 && response.status <= 299) {
                    const data = await response.json();
                    if(data.result !== UserDataKeysByIdpGetResult.SUCCESS) {
                        switch (data.result) {
                            case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByIdpGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByIdpGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByIdpGetResult.SUCCESS_NOT_FOUND: return null;
                            default : throw new Error("Server Under maintenance. Please try again later.");
                        }
                    }
                    const idp = data.identity_providers || [];
                    searchId = data.next_search_id;
                    if(idp) {
                        const valueData =idp[0].identity_provider || [];
                        if(valueData) {
                            const ks = data.keys_shared;
                            keysShared.push(ks);
                            if(ks.length < 100) {
                                shouldContinue = false;
                            }
                        }
                    }
                } else {
                    throw new Error("Server Under maintenance. Please try again later.");
                }
            } while(shouldContinue);
            return keysShared;
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async deleteKey(accessToken, identityProvider, valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { identity_providers: [identityProvider], value_keys: [valueKey] };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-idp/delete", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByIdpDeleteResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpDeleteResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByIdpDeleteResult.SUCCESS_NOT_FOUND: return;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }
};

window.hyperIdStorageIDP = HyperIDIDPStorage;