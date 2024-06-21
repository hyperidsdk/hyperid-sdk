const KycUserStatus = {
    NONE					: 0,
    PENDING					: 1,
    COMPLETE_SUCCESS		: 2,
    COMPLETE_FAIL_RETRYABLE	: 3,
    COMPLETE_FAIL_FINAL		: 4,
    DELETED					: 5
};

const KycUserStatusGetResult = {
	FAIL_BY_USER_KYC_DELETED			: -8,
	FAIL_BY_USER_NOT_FOUND				: -7,
	FAIL_BY_BILLING						: -6,
	FAIL_BY_INVALID_PARAMETERS			: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
};

const KycUserStatusTopLevelGetResult = {
	FAIL_BY_INVALID_PARAMETERS			: -7,
	FAIL_BY_USER_KYC_DELETED			: -6,
	FAIL_BY_BILLING						: -5,
	FAIL_BY_SERVICE_TEMPORARY_NOT_VALID	: -4,
	FAIL_BY_ACCESS_DENIED				: -3,
	FAIL_BY_TOKEN_EXPIRED				: -2,
	FAIL_BY_TOKEN_INVALID				: -1,
	SUCCESS								: 0
};

class HyperIDKyc {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    async getUserStatus(accessToken,
                        verificationLevel = VerificationLevel.KYC_FULL) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }
        try {
            const params = {
                verification_level: verificationLevel
            };
            const response = await fetch(this.restApiEndpoint + "/kyc/user/status-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                },
                body: JSON.stringify(params)
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== KycUserStatusGetResult.SUCCESS) {
                    switch (data.result) {
                        case KycUserStatusGetResult.FAIL_BY_USER_KYC_DELETED: return null
                        case KycUserStatusGetResult.FAIL_BY_USER_NOT_FOUND: return null
                        case KycUserStatusGetResult.FAIL_BY_BILLING: return null
                        case KycUserStatusGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case KycUserStatusGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case KycUserStatusGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
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

    async getUserStatusTopLevel(accessToken) {
        if(!accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        try {
            const response = await fetch(this.restApiEndpoint + "/kyc/user/status-top-level-get", {
                method: 'POST',
                headers: {'Accept': 'application/json',
                    'Content-Type':'application/json',
                    'Authorization': "Bearer " + accessToken
                }
            });
            if(response.status >= 200 && response.status <= 299) {
                const data = await response.json();
                if(data.result !== KycUserStatusTopLevelGetResult.SUCCESS) {
                    switch (data.result) {
                        case KycUserStatusTopLevelGetResult.FAIL_BY_USER_KYC_DELETED: return null
                        case KycUserStatusTopLevelGetResult.FAIL_BY_BILLING: return null
                        case KycUserStatusTopLevelGetResult.FAIL_BY_TOKEN_INVALID: throw new Error("Access token is expired. Please sign in first.");
                        case KycUserStatusTopLevelGetResult.FAIL_BY_TOKEN_EXPIRED: throw new Error("Access token is expired. Please sign in first.");
                        case KycUserStatusTopLevelGetResult.FAIL_BY_ACCESS_DENIED: throw new Error("Access token is expired. Please sign in first.");
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
}