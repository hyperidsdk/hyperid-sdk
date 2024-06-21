import './polyfill';
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
    AUTO_WALLET_GET : 0,
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

class TransactionData {
    constructor(addressTo,
                chainId,
                addressFrom = null,
                value = null,
                data = null,
                gas = null,
                nonce = null
    ) {
        this.to = addressTo;
        this.chain = chainId;
        if(addressFrom != null) {
            this.from = addressFrom;
        }
        if(value) {
            this.value = value;
        }
        if(data) {
            this.data = data;
        }
        if(gas) {
            this.gas = gas;
        }
        if(nonce) {
            this.nonce = nonce;
        }
    }

    isValid() {
        if(!parseInt(this.to)) return false;
        if(this.from != undefined && !parseInt(this.from)) return false;
        if(this.data == undefined && this.value == undefined) return false;
        if(this.value != undefined && isNaN(this.value)) return false;
        if(this.data != undefined && !parseInt(this.data)) return false;
        return true;
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
        await this.auth.init();
        if(refreshToken) {
            try {
                this.auth.refreshToken = refreshToken;
                await this.auth.refreshTokens();
            } catch (error) {
                console.error(error);
            }
        }
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
        this.transactionResult = null;
        this.transactionResultDesc = null;
        this.transactionHash = null;
    }

    #getDecodedToken(_token) {
        const base64Url = _token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
        );
        return jsonPayload;
    }
    
    #isTokenExpired(_token) {
        try {
            const token = this.#getDecodedToken(_token);
            const timestamp = Date.now();
            return timestamp > token.exp
        } catch (error) {
            console.error(error);
            return true;
        }
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
    
    #prepareParams(params) {
        if (this.clientInfo.authMethod == AuthorizationMethod.BASIC) {
            params.client_id = this.clientInfo.clientId;
            params.client_secret = this.clientInfo.clientSecret;
        } else {
            params.client_assertion_type = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
            params.client_assertion = this.#clientAssertion();
        }
    }
    
    async init() {
        if (!this.clientInfo.isValid()){
            throw new Error("Wrong credentials");
        }
        
        try {
            const response = await fetch(`${this.infrastructureType}${AUTO_DISCOVER_URI}`)
            this.discover = await response.json();
        } catch (error){
            console.error('There has been a problem with your fetch operation:', error);
            throw error;
        }
    }
    
    #emit(eventName, ...args) {
        if (this.events[eventName]) {
          this.events[eventName].forEach(callback => callback(...args));
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
            scope: scopes,
            flow_mode: flowMode
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

        if(verificationLevel && Object.values(VerificationLevel).includes(verificationLevel)) {
            params.verification_level = verificationLevel;
        }

        if(walletGetMode && Object.values(WalletGetMode).includes(walletGetMode)) {
            params.wallet_get_mode = walletGetMode;
        }

        if(walletFamily && Object.values(WalletFamily).includes(walletFamily)) {
            params.wallet_family = walletFamily;
        }

        if(identityProvider) {
            params.identity_provider = identityProvider;
        }

        const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join('&');
        return `${this.discover.authorization_endpoint}?${queryString}`;
    }

    startSignInWithTransaction(addressTo,
        chain,
        addressFrom=null,
        value=null,
        data=null,
        gas=null,
        nonce=null,
        scopes=null,
        state=null,
    ) {
        const params = {
            response_type: "code",
            client_id: this.clientInfo.clientId,
            redirect_uri: this.clientInfo.redirectUri,
            scope: scopes,
            flow_mode: AuthorizationFlowMode.SIGN_IN_WEB2
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

        let transaction = new TransactionData(addressTo, chain, data, addressFrom, value, gas, nonce);
        if(!transaction.isValid()) throw new Error("Transaction not valid.");
        params.transaction = JSON.stringify(transaction);

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

    startSignInAutoWalletGet(state=null,
        scopes=null) {
            return this.getAuthorizationUrl(AuthorizationFlowMode.SIGN_IN_WALLET_GET,
                scopes,
                state,
                null,
                WalletGetMode.AUTO_WALLET_GET,
                null,
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
            console.log("this.at", this.accessToken)
            this.#emit('tokensChanged', this.accessToken, this.refreshToken)
            if (history.pushState) {
                var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname;
                window.history.pushState({path:newurl},'', newurl);
            }
        } catch (error) {
            console.error(error);
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
            console.error(error);
            throw error;
        }
    }

    async introspectToken() {
        console.log("AT:", this.accessToken);
        if (!this.accessToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        if(this.#isTokenExpired(this.accessToken)) {
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
            console.error(error);
            throw error;
        }
    }

    async checkSession() {
        try {
            const data = await this.introspectToken();
            return data.active;
        } catch {
            return false;
        }
    }

    async refreshTokens() {
        if (!this.refreshToken) {
            throw new Error("Authorization required. Please sign in.");
        }

        if(this.#isTokenExpired(this.refreshToken)) {
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
            console.error(error);
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
        if(urlParams.get('transaction_result')) {
            this.transactionResult = urlParams.get('transaction_result');
            this.transactionResultDesc = urlParams.get('transaction_result_description');
            if(this.transactionResult == '0') {
                this.transactionHash = urlParams.get('transaction_hash');
            }
        } else {
            this.transactionResult = null;
            this.transactionResultDesc = null;
            this.transactionHash = null;
        }
        if (code) {
            try {
                this.exchangeCodeToToken(code);
            } catch(error) {
                console.error(error);
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
            console.log(error);
        }
    }
    return auth;
}

window['getHyperIDAuth'] = getHyperIDAuth;
window.auth = Auth;
window.authorizationFlowMode = AuthorizationFlowMode;
window.authorizationMethod = AuthorizationMethod;
window.verificationLevel = VerificationLevel;
window.walletFamily = WalletFamily;
window.walletGetMode = WalletGetMode;
window.infrastructureType = InfrastructureType;
window.wallet = Wallet;
window.userInfo = UserInfo;
window.clientInfo = ClientInfo;
window.clientInfoBasic = ClientInfoBasic;
window.clientInfoHS256 = ClientInfoHS256;
window.clientInfoRS256 = ClientInfoRS256;