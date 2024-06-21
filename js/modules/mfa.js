const MfaAvailabilityCheckResult = {
	FAIL_BY_TOKEN_INVALID				: -5,
	FAIL_BY_TOKEN_EXPIRED				: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_INVALID_PARAMETERS			: -2,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -1,
	SUCCESS								: 0
};

const MfaTransactionStartResult = {
	FAIL_BY_TEMPLATE_NOT_FOUND			: -8,
	FAIL_BY_USER_DEVICE_NOT_FOUND		: -7,
	FAIL_BY_TOKEN_INVALID				: -5,
	FAIL_BY_TOKEN_EXPIRED				: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_INVALID_PARAMETERS			: -2,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -1,
	SUCCESS								: 0
};

const MfaTransactionStatusGetResult = {
	FAIL_BY_TRANSACTION_NOT_FOUND		: -6,
	FAIL_BY_TOKEN_INVALID				: -5,
	FAIL_BY_TOKEN_EXPIRED				: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_INVALID_PARAMETERS			: -2,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -1,
	SUCCESS								: 0
};

const MfaTransactionCancelResult = {
	FAIL_BY_ALREADY_CANCELED			: -10,
	FAIL_BY_TRANSACTION_COMPLETED		: -9,
	FAIL_BY_TRANSACTION_EXPIRED			: -8,
	FAIL_BY_TRANSACTION_NOT_FOUND		: -6,
	FAIL_BY_TOKEN_INVALID				: -5,
	FAIL_BY_TOKEN_EXPIRED				: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_INVALID_PARAMETERS			: -2,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -1,
	SUCCESS								: 0
};

const MfaTransactionStatus = {
	PENDING 	: 0,
	COMPLETED 	: 1,
	EXPIRED		: 2,
	CANCELED	: 4
};

const MfaTransactionCompleteResult = {
	APPROVED : 0,
	DENIED : 1
};

class HyperIDMfa {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    async checkAvailability(accessToken) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const response = await fetch(this.restApiEndpoint + "/mfa-client/availability-check", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                }
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== MfaAvailabilityCheckResult.SUCCESS) {
                    switch (data.result) {
                        case MfaAvailabilityCheckResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case MfaAvailabilityCheckResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case MfaAvailabilityCheckResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                return data.is_available;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error ) {
            throw error
        }
    }

    async startTransaction(accessToken, code, question) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        let c = String(code);
        if (c.length > 2) {
            throw new Error("The code must be exactly two digits long.");
        }
        if (c.length === 1) {
            c = "0" + c;
        }

        const action = { type: "question", action_info: question };
        const value = { version: 1, action: action };
        const params = {
            template_id: 4,
            values: JSON.stringify(value),
            code: c
        };
        try {
            const response = await fetch(this.restApiEndpoint + "/mfa-client/transaction/start/v2", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== MfaTransactionStartResult.SUCCESS) {
                    switch (data.result) {
                        case MfaTransactionStartResult.FAIL_BY_USER_DEVICE_NOT_FOUND: throw new Error("HyperId Authenticator not install, please install it first.")
                        case MfaTransactionStartResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionStartResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionStartResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                return data.transaction_id;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error ) {
            throw error
        }
    }

    async getTransactionStatus(accessToken, transactionId) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const params = {
                transaction_id: transactionId
            };
            const response = await fetch(this.restApiEndpoint + "/mfa-client/transaction/status-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== MfaTransactionStatusGetResult.SUCCESS) {
                    switch (data.result) {
                        case MfaTransactionStatusGetResult.FAIL_BY_TRANSACTION_NOT_FOUND: throw new Error("Transaction not found.")
                        case MfaTransactionStatusGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionStatusGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionStatusGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                return data;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error ) {
            throw error
        }
    }

    async cancelTransaction(accessToken, transactionId) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }
        try {
            const params = {
                transaction_id: transactionId
            };
            const response = await fetch(this.restApiEndpoint + "/mfa-client/transaction/cancel", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== MfaTransactionCancelResult.SUCCESS) {
                    switch (data.result) {
                        case MfaTransactionCancelResult.FAIL_BY_ALREADY_CANCELED: return
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_EXPIRED: return
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_COMPLETED: throw new Error("Transaction already completed.")
                        case MfaTransactionCancelResult.FAIL_BY_TRANSACTION_NOT_FOUND: throw new Error("Transaction not found.")
                        case MfaTransactionCancelResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionCancelResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case MfaTransactionCancelResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
                        default : throw new Error("Server Under maintenance. Please try again later.");
                    }
                }
                return;
            } else {
                throw new Error("Server Under maintenance. Please try again later.");
            }
        } catch (error ) {
            throw error
        }
    }
}

window.hyperIdMfa = HyperIDMfa;