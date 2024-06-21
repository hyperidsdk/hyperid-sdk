const UserDataSetByUserIdResult = {
	FAIL_BY_KEY_INVALID					: -7,
	FAIL_BY_KEY_ACCESS_DENIED			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
}

const UserDataGetByUserIdResult = {
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
    SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByUserIdGetResult = {
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
    SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByUserIdDeleteResult = {
	FAIL_BY_KEY_ACCESS_DENIED			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
    SUCCESS_NOT_FOUND					: 1
}

const UserDataAccessScope = {
	PRIVATE : 0,
	PUBLIC 	: 1
}

class HyperIDUserIdStorage {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    async setData(accessToken,
        valueKey,
        valueData,
        accessScope = UserDataAccessScope.PUBLIC) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                value_key: valueKey,
                value_data: valueData,
                access_scope: accessScope
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-user-id/set", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataSetByUserIdResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataSetByUserIdResult.FAIL_BY_KEY_INVALID: throw new Error("Provided key is invalid.")
                        case UserDataSetByUserIdResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataSetByUserIdResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataSetByUserIdResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataSetByUserIdResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async getData(accessToken, valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { value_keys: [valueKey] };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-user-id/get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataGetByUserIdResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataGetByUserIdResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByUserIdResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByUserIdResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByUserIdResult.SUCCESS_NOT_FOUND: return null;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                const values = data.values || [];
                if (values.length > 0) {
                    return values[0].value_data;
                  }
                return null;
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async getKeysList(accessToken) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const response = await fetch(this.restApiEndpoint + "/user-data/by-user-id/list-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                }
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByUserIdGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdGetResult.SUCCESS_NOT_FOUND: return null;
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

    async getKeysListShared(accessToken) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            let shouldContinue = true;
            const keysShared = [];
            const searchId="";
            do {
                const params = {
                    search_id: searchId,
                    page_size: 100
                };
                const response = await fetch(this.restApiEndpoint + "/user-data/by-user-id/shared-list-get", {
                    method: 'POST',
                    headers: {'Accept': 'application/json',
                        'Content-Type':'application/json',
                        'Authorization': "Bearer " + accessToken
                    },
                    body: JSON.stringify(params)
                });
                if(response.status >= 200 && response.status <= 299) {
                    const data = await response.json();
                    if(data.result !== UserDataKeysByUserIdGetResult.SUCCESS) {
                        switch (data.result) {
                            case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByUserIdGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByUserIdGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByUserIdGetResult.SUCCESS_NOT_FOUND: return null;
                            default : throw new Error("Server Under maintenance. Please try again later.");
                        }
                    }
                    searchId = data.next_search_id;
                    const ks = data.keys_shared;
                    keysShared.push(ks);
                    if(ks.length < 100) {
                        shouldContinue = false;
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

    async deleteKey(accessToken, valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { value_keys: [valueKey] };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-user-id/delete", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByUserIdDeleteResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdDeleteResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByUserIdDeleteResult.SUCCESS_NOT_FOUND: return;
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

window.hyperIdStorageUserId = HyperIDUserIdStorage;