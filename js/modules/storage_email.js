const UserDataSetByEmailResult = {
	FAIL_BY_KEY_INVALID					: -7,
	FAIL_BY_KEY_ACCESS_DENIED			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
}

const UserDataGetByEmailResult = {
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
	SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByEmailGetResult = {
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
    SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByEmailDeleteResult = {
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

class HyperIDEmailStorage {
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
            const response = await fetch(this.restApiEndpoint + "/user-data/by-email/set", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataSetByEmailResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataSetByEmailResult.FAIL_BY_KEY_INVALID: throw new Error("Provided key is invalid.")
                        case UserDataSetByEmailResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataSetByEmailResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataSetByEmailResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataSetByEmailResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
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

    async getData(accessToken, valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { value_keys: [valueKey] };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-email/get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataGetByEmailResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataGetByEmailResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByEmailResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByEmailResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataGetByEmailResult.SUCCESS_NOT_FOUND: return null;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                const values = data.values || [];
                if (values.length > 0) {
                    return values[0].value_data;
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

    async getKeysList(accessToken) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const response = await fetch(this.restApiEndpoint + "/user-data/by-email/list-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                }
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByEmailGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByEmailGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailGetResult.SUCCESS_NOT_FOUND: return null;
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
            const searchId = "";
            do {
                const params = {
                    search_id: searchId,
                    page_size: 100
                };
                const response = await fetch(this.restApiEndpoint + "/user-data/by-email/shared-list-get", {
                    method: 'POST',
                    headers: {'Accept': 'application/json',
                        'Content-Type':'application/json',
                        'Authorization': "Bearer " + accessToken
                    },
                    body: JSON.stringify(params)
                });
                if(response.status >= 200 && response.status <= 299) {
                    const data = await response.json();
                    if(data.result !== UserDataKeysByEmailGetResult.SUCCESS) {
                        switch (data.result) {
                            case UserDataKeysByEmailGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByEmailGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByEmailGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByEmailGetResult.SUCCESS_NOT_FOUND: return null;
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
            const response = await fetch(this.restApiEndpoint + "/user-data/by-email/delete", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByEmailDeleteResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByEmailDeleteResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataKeysByEmailDeleteResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailDeleteResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailDeleteResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByEmailDeleteResult.SUCCESS_NOT_FOUND: return;
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

window.hyperIdStorageEmail = HyperIDEmailStorage;