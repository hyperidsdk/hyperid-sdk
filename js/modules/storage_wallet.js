const UserWalletsGetResult = {
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
}

const UserDataByWalletSetResult = {
	FAIL_BY_KEY_INVALID					: -8,
	FAIL_BY_KEY_ACCESS_DENIED			: -7,
	FAIL_BY_WALLET_NOT_EXISTS			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
}

const UserDataByWalletGetResult = {
	FAIL_BY_WALLET_NOT_EXISTS			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
	SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByWalletGetResult = {
	FAIL_BY_WALLET_NOT_EXISTS			: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0,
	SUCCESS_NOT_FOUND					: 1
}

const UserDataKeysByWalletDeleteResult = {
	FAIL_BY_WALLET_NOT_EXISTS			: -6,
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

class WalletData {
    constructor(address,
                chain,
                isPublic = true
    ) {
        this.address = address;
        this.chain = chain;
        this.isPublic = isPublic;
    }
}

class HyperIDWalletStorage {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    async getWallets(accessToken) {
        try {
            const response = await fetch(this.restApiEndpoint + "/user-wallets/get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                }
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserWalletsGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserWalletsGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserWalletsGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserWalletsGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                let wallets = new Array();
                for(let i = 0; i < data.wallets_public.length; i++) {
                    wallets.push(new WalletData(data.wallets_public[i].address, data.wallets_public[i].chain));
                }
                for(let i = 0; i < data.wallets_private.length; i++) {
                    wallets.push(new WalletData(data.wallets_private[i].address, data.wallets_private[i].chain, false));
                }
                return wallets;
            }
        } catch (error) {
            console.log(error);
            throw error;
        }
    }

    async setData(accessToken,
        walletAddress,
        valueKey,
        valueData,
        accessScope = UserDataAccessScope.PUBLIC) {
        try {
            const params = {
                wallet_address: walletAddress,
                value_key: valueKey,
                value_data: valueData,
                access_scope: accessScope
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-wallet/set", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataByWalletSetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataByWalletSetResult.FAIL_BY_KEY_INVALID: throw new Error("Provided key is invalid.")
                        case UserDataByWalletSetResult.FAIL_BY_KEY_ACCESS_DENIED: throw new Error("Key access violation: Your permissions are not sufficient.")
                        case UserDataByWalletSetResult.FAIL_BY_WALLET_NOT_EXISTS: throw new Error("Specified wallet not found.")
                        case UserDataByWalletSetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByWalletSetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByWalletSetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
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
                walletAddress,
                valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                wallet_address: walletAddress,
                value_keys: [valueKey]
            };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-wallet/get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataByWalletGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataByWalletGetResult.FAIL_BY_WALLET_NOT_EXISTS: throw new Error("Specified wallet not found.")
                        case UserDataByWalletGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByWalletGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataByWalletGetResult.SUCCESS_NOT_FOUND: return null;
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                const values = data.values;
                if (values) {
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

    async getKeysList(accessToken, walletAddress) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { wallet_address: walletAddress };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-wallet/list-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByWalletGetResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByWalletGetResult.FAIL_BY_WALLET_NOT_EXISTS: throw new Error("Specified wallet not found.")
                        case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletGetResult.SUCCESS_NOT_FOUND: return null;
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

    async getKeysListShared(accessToken, walletAddress) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            let shouldContinue = true;
            const keysShared = [];
            const searchId="";
            
            do {
                const params = {
                    wallet_address: walletAddress,
                    search_id: searchId,
                    page_size: 100
                };
                const response = await fetch(this.restApiEndpoint + "/user-data/by-wallet/shared-list-get", {
                    method: 'POST',
                    headers: {'Accept': 'application/json',
                        'Content-Type':'application/json',
                        'Authorization': "Bearer " + accessToken
                    },
                    body: JSON.stringify(params)
                });
                if(response.status >= 200 && response.status <= 299) {
                    const data = await response.json();
                    if(data.result !== UserDataKeysByWalletGetResult.SUCCESS) {
                        switch (data.result) {
                            case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByWalletGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByWalletGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                            case UserDataKeysByWalletGetResult.SUCCESS_NOT_FOUND: return null;
                            default : throw new Error("Server Under maintenance. Please try again later.");
                        }
                    }
                    const ks = data.keys_shared;
                    searchId = data.next_search_id;
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

    async deleteKey(accessToken, walletAddress, valueKey) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = { wallet_address: walletAddress, value_keys: [valueKey] };
            const response = await fetch(this.restApiEndpoint + "/user-data/by-wallet/delete", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== UserDataKeysByWalletDeleteResult.SUCCESS) {
                    switch (data.result) {
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_WALLET_NOT_EXISTS: throw new Error("Specified wallet not found.")
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletDeleteResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        case UserDataKeysByWalletDeleteResult.SUCCESS_NOT_FOUND: return;
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

window.hyperIdStorageWallet= HyperIDWalletStorage;