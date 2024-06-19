import './polyfill.js';
const jwt = require('jsonwebtoken');
const uuid = require('uuid'); 

const AUTO_DISCOVER_URI = "/auth/realms/HyperID/.well-known/openid-configuration"

const AuthorizationFlowMode = {
    SIGN_IN_WEB2                : 0,
    SIGN_IN_WEB3                : 3,
    SIGN_IN_WALLET_GET          : 4,
    SIGN_IN_GUEST_UPGRADE       : 6,
    SIGN_IN_IDENTITY_PROVIDER   : 9
};

const AuthorizationMethod = {
    BASIC : 0,
    HS256 : 1,
    RS256 : 2
}

const VerificationLevel = {
    KYC_BASIC : 3,
    KYC_FULL : 4
};

const WalletFamily = {
    ETHEREUM : 0,
    SOLANA : 1
};

const WalletGetMode = {
    WALLET_GET_FAST : 2,
    WALLET_GET_FULL : 3
};

const InfrastructureType = {
    SANDBOX : "https://login-sandbox.hypersecureid.com",
    PRODUCTION : "https://login.hypersecureid.com"
};

class Wallet {
    constructor(jsonData) {
        this.address = jsonData.wallet_address
        this.chain_id = jsonData.wallet_chain_id
        this.source = jsonData.wallet_source
        this.is_verified = jsonData.is_wallet_verified
        this.family = jsonData.wallet_family
    }
}

class UserInfo {
    constructor(jsonData) {
        this.user_id = jsonData.sub
        this.is_guest = false;
        if(jsonData.is_guest) {
            this.is_guest = true;
        }
        this.email = jsonData.email
        this.device_id = jsonData.device_id
        this.ip = jsonData.ip
        if(jsonData.wallet_address) {
            this.wallet = new Wallet(jsonData)    
        }
    }
}

class ClientInfo {
    constructor(clientId, redirectUri, authMethod) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.authMethod = authMethod;
    }
    isValidStr(str) {
        return str !== null && str !== undefined && str !== '';
    }

    isValid() {
        return this.isValidStr(this.clientId) 
            && this.isValidStr(this.redirectUri);
    }
}

class ClientInfoBasic extends ClientInfo {
    constructor(clientId, clientSecret, redirectUri) {
        super(clientId, redirectUri, AuthorizationMethod.BASIC);
        this.clientSecret = clientSecret;
    }

    idValid() {
        return this.isValidStr(clientSecret) && super.isValid();
    }
}

class ClientInfoHS256 extends ClientInfo {
    constructor(clientId, clientSecret, redirectUri) {
        super(clientId, redirectUri, AuthorizationMethod.HS256);
        this.clientSecret = clientSecret;
    }

    isValid() {
        return this.isValidStr(clientSecret) && super.isValid();
    }
}

class ClientInfoRS256 extends ClientInfo {
    constructor(clientId, privateKey, redirectUri) {
        super(clientId, redirectUri, AuthorizationMethod.RS256);
        this.privateKey = privateKey;
    }

    isValid() {
        return this.isValidStr(privateKey) && super.isValid();
    }
}

class HyperIDSDK{
    constructor(clientInfo, infrastructureType) {
        this.auth = new Auth(clientInfo, infrastructureType);
        this.mfa = null;
        this.kyc = null;
        this.storageEmail = null;
        this.storageUserId = null;
        this.storageIdp = null;
        this.storageWallet = null;
    }

    async init(refreshToken=null) {
        await this.auth.init(refreshToken);
        this.mfa = new HyperIDMfa(this.auth.getDiscover().rest_api_token_endpoint);
        this.kyc = new HyperIDKyc(this.auth.getDiscover().rest_api_token_endpoint);
        this.storageEmail = new HyperIDStorageEmail(this.auth.getDiscover().rest_api_token_endpoint);
        this.storageUserId = new HyperIDStorageUserId(this.auth.getDiscover().rest_api_token_endpoint);
        this.storageIdp = new HyperIDStorageIdp(this.auth.getDiscover().rest_api_token_endpoint);
        this.storageWallet = new HyperIDStorageWallet(this.auth.getDiscover().rest_api_token_endpoint);
    }

    on(eventName, callback) {
        if (!this.auth.events[eventName]) {
          this.auth.events[eventName] = [];
        }
        this.auth.events[eventName].push(callback);
      }
};

class Auth {
    constructor(clientInfo, infrastructureType) {
        this.clientInfo = clientInfo;
        this.infrastructureType = infrastructureType;
        this.accessToken = "";
        this.refreshToken = "";
        this.discover = null;
        this.events = {};
    }

    #getDecodedToken(_token) {
        const base64Url = _token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
        );
        return jsonPayload;
    }
    
    
    #clientAssertion() {
        const now = Math.floor(Date.now() / 1000);
        const token = {
            'iss': clientInfo.client_id,
            'sub': clientInfo.client_id,
            'aud': this.discover.issuer,
            'exp': now + 5 * 60,
            'iat': now,
            'jti': uuid.v4(),
        };
        let clientAssertion = null;
        if (this.clientInfo.authMethod == AuthorizationMethod.HS256) {
            clientAssertion = jwt.sign(token, this.clientInfo.clientSecret, { algorithm: 'HS256' });
        }
        if(this.clientInfo.authMethod == AuthorizationMethod.RS256) {
            clientAssertion = jwt.sign(token, this.clientInfo.privateKey, { algorithm: 'RS256' });
        }
        return clientAssertion;
    }
    
    #emit(eventName, ...args) {
        if (this.events[eventName]) {
            this.events[eventName].forEach(callback => callback(...args));
        }
    }
    
    #prepareParams(params) {
        if (this.clientInfo.authMethod == AuthorizationMethod.BASIC) {
            params.client_id = this.clientInfo.clientId;
            params.client_secret = this.clientInfo.clientSecret;
        } else {
            params.client_assertion_type = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
            params.client_assertion = this.#clientAssertion();
        }
    }

    isTokenExpired(_token) {
        if(!_token || _token === undefined || _token === 'undefined') {
            return true;
        }

        try {
            const token = this.#getDecodedToken(_token);
            const timestamp = Date.now();
            return timestamp > token.exp
        } catch (error) {
            console.error(error);
            return true;
        }
    }
    
    async init(refreshToken=null) {
        if (!this.clientInfo.isValid()){
            throw new Error("Wrong credentials");
        }
        
        try {
            const response = await fetch(`${this.infrastructureType}${AUTO_DISCOVER_URI}`)
            this.discover = await response.json();
        } catch (error){
            console.error(error);
            throw error;
        }
        if(refreshToken) {
            try {
                this.refreshToken = refreshToken;
                await this.refreshTokens();
            } catch (error) {
                console.error(error);
            }
        }
    }

    getDiscover() {
        return this.discover;
    }
    
    getAccessToken() {
        return this.accessToken;
    }

    getSessionRestoreInfo() {
        return this.refreshToken;
    }
    
    getIdpProvides() {
        return this.discover.identity_providers
    }

    getAuthorizationUrl(flowMode,
                        scopes=null,
                        state=null,
                        verificationLevel=null,
                        walletGetMode=null,
                        walletFamily=null,
                        identityProvider=null) {
        const params = {
            response_type: "code",
            client_id: this.clientInfo.clientId,
            redirect_uri: this.clientInfo.redirectUri,
            scope: scopes
        };
        
        if(scopes) {
            params.scope = scopes;
        } else {
            const combinedScopes = new Set([...this.discover.client_scopes_optional, ...this.discover.client_scopes_default]);
            params.scope = Array.from(combinedScopes).join(' '); 
        }

        if(state) {
            params.state = state;
        }

        if(flowMode.flowMode !== undefined) {
            params.flow_mode = flowMode.flowMode;
        } else {
            params.flow_mode = flowMode;
        }

        if(verificationLevel) {
            if(verificationLevel.verificationLevel !== undefined) {
                params.verification_level = verificationLevel.verificationLevel;
            } else {
                params.verification_level = verificationLevel;
            }
        }

        if(walletGetMode) {
            if(walletGetMode.walletGetMode !== undefined) {
                params.wallet_get_mode = walletGetMode.walletGetMode;
            } else {
                params.wallet_get_mode = walletGetMode;
            }
        }

        if(walletFamily) {
            if(walletFamily.walletFamily !== undefined) {
                params.wallet_family = walletFamily.walletFamily;
            } else {
                params.wallet_family = walletFamily;
            }
        }

        if(identityProvider) {
            params.identity_provider = identityProvider;
        }

        const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join('&');
        return `${this.discover.authorization_endpoint}?${queryString}`;
    }

    startSignInWeb2(verificationLevel=null, state=null, scopes=null) {
        return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_WEB2,
            scopes,
            state,
            verificationLevel,
            null,
            null,
            null);
    }
    
    startSignInWeb3(verificationLevel=null,
        walletFamily=WalletFamily.ETHEREUM,
        state=null,
        scopes=null) {
        return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_WEB3,
            scopes,
            state,
            verificationLevel,
            null,
            walletFamily,
            null);
    }

    startSingInGuestUpgrade(state=null, scopes=null) {
        return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_GUEST_UPGRADE,
            scopes,
            state,
            null,
            null,
            null,
            null);
    }

    startSignInWalletGet(walletGetMode=WalletGetMode.WALLET_GET_FAST,
        walletFamily=WalletFamily.ETHEREUM,
        state=null,
        scopes=null) {
            return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_WALLET_GET,
                scopes,
                state,
                null,
                walletGetMode,
                walletFamily,
                null)
    }
            
    startSignInByIdentityProvider(identityProvider,
                                  verificationLevel=null,
                                  state=null,
                                  scopes=null) {
        return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_IDENTITY_PROVIDER,
            scopes,
            state,
            verificationLevel,
            null,
            null,
            identityProvider)
    }

    async exchangeCodeToToken(authorization_code) {
        const params = {
            grant_type: 'authorization_code',
            code: authorization_code,
            redirect_uri: this.clientInfo.redirectUri
        };
        this.#prepareParams(params);
        const formBody = Object.keys(params).map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key])).join('&');
        try {
            const response = await fetch(this.discover.token_endpoint, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formBody
            });
            const data = await response.json();
            this.accessToken = data.access_token;
            this.refreshToken = data.refresh_token;
            this.#emit('tokensChanged', this.accessToken, this.refreshToken)
            if (history.pushState) {
                var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname;
                window.history.pushState({path:newurl},'',newurl);
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

   async logout() {
        if (!this.refreshToken) {
            return;
        }

        const params = {
            refresh_token: this.refreshToken
        };
        this.#prepareParams(params);
        const formBody = Object.keys(params).map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key])).join('&');
        try {
            await fetch(this.discover.end_session_endpoint, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formBody
            });
            this.accessToken = null;
            this.refreshToken = null;
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async introspectToken() {
        if (!this.accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        if(this.isTokenExpired(this.accessToken)) {
            try{
                this.refresh_tokens();
            } catch(error) {
                throw error;
            }
        }

        const params = {
            token_type_hint: "access_token",
            token: this.accessToken
        };
        this.#prepareParams(params);
        const formBody = Object.keys(params).map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key])).join('&');
        try {
            const response = await fetch(this.discover.introspection_endpoint, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formBody
            });
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async checkSession() {
        try {
            const data = await this.introspectToken();
            return data.active;
        } catch (error){
            console.error(error);
            return false;
        }
    }

    async refreshTokens() {
        if (!this.refreshToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        if(this.isTokenExpired(this.refreshToken)) {
            throw new Error("Authorization required. Please sign in.");
        }

        const params = {
            grant_type: "refresh_token",
            refresh_token: this.refreshToken
        };
        this.#prepareParams(params);
        const formBody = Object.keys(params).map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key])).join('&');
        try {
            const response = await fetch(this.discover.token_endpoint, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formBody
            });
            const data = await response.json();
            this.accessToken = data.access_token;
            this.refreshToken = data.refresh_token;
            this.#emit('tokensChanged', this.accessToken, this.refreshToken)
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    userInfo() {
        if (!this.accessToken) {
            try {
                this.refreshTokens();
            }
            catch (error) {
                console.error(error);
                throw new Error("Authorization required. Please sign in.")
            }
        }

        return new UserInfo(JSON.parse(this.#getDecodedToken(this.accessToken)));
    }
    
    handleOAuthCallback() {
        const queryString = window.location.search;
        const urlParams = new URLSearchParams(queryString);
        const error = urlParams.get('error');
        if (error) {
            throw error;
        }
        const code = urlParams.get('code');
        if (code) {
            try {
                this.exchangeCodeToToken(code);
            } catch(error) {
                console.error('Error:', error);
                throw error;
            }
        }
    }
}

async function getHyperIDAuth(clientInfo, infrastructureType, refreshToken=null) {
    let auth = new Auth(clientInfo, infrastructureType);
    await auth.init();
    if(refreshToken) {
        try {
            auth.refreshToken = refreshToken;
            await auth.refreshTokens();
        } catch (error) {
            console.error(error);
        }
    }
    return auth;
}

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

class HyperIDStorageEmail {
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
            throw error;
        }
    }
};

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

class HyperIDStorageUserId {
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
            throw error;
        }
    }
};

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

class HyperIDStorageIdp {
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
            throw error;
        }
    }
};

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

class HyperIDStorageWallet {
    constructor(restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
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
            console.error(error);
            throw error;
        }
    }
};

window['getHyperIDAuth'] = getHyperIDAuth;
window.auth = Auth;
window.authorizationFlowMode = AuthorizationFlowMode;
window.authorizationMethod = AuthorizationMethod;
window.verificationLevel = VerificationLevel;
window.kycUserStatus = KycUserStatus;
window.walletFamily = WalletFamily;
window.walletGetMode = WalletGetMode;
window.infrastructureType = InfrastructureType;
window.userDataAccessScope = UserDataAccessScope;
window.wallet = Wallet;
window.userInfo = UserInfo;
window.clientInfo = ClientInfo;
window.clientInfoBasic = ClientInfoBasic;
window.clientInfoHS256 = ClientInfoHS256;
window.clientInfoRS256 = ClientInfoRS256;
window.hyperIdKyc = HyperIDKyc;
window.hyperIdMfa = HyperIDMfa;
window.hyperIdStorageEmail = HyperIDStorageEmail;
window.hyperIdStorageUserId = HyperIDStorageUserId;
window.hyperIdStorageIDP = HyperIDStorageIdp;
window.hyperIdStorageWallet= HyperIDStorageWallet;
window.hyperIdSdk = HyperIDSDK;