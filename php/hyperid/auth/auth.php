<?php

require_once 'client_info.php';
require_once __DIR__.'/../base/auth_token.php';
require_once __DIR__.'/../base/discover.php';
require_once __DIR__.'/../base/enum.php';
require_once __DIR__.'/../base/user_info.php';
require_once __DIR__.'/../utils.php';
require_once __DIR__.'/../error.php';
require_once __DIR__.'/../error_rfc6749.php';

$DISCOVER_URI = '/auth/realms/HyperID/.well-known/openid-configuration';

class Auth {
    public InfrastructureType   $infrastructureType;
    public int                  $requestTimeout;
    public ClientInfo           $clientInfo;
    public Discover             $discover;
    public ?AuthToken           $accessToken    = null;
    public ?AuthToken           $refreshToken   = null;

    function __construct(ClientInfo         $clientInfo,
                         string             $refreshToken       = '',
                         InfrastructureType $infrastructureType = InfrastructureType::SANDBOX,
                         int                $requestTimeout     = 10) {
        $this->infrastructureType = $infrastructureType;
        $this->requestTimeout     = $requestTimeout;
        if(!$clientInfo || !$clientInfo->isValid()) {
            throw new WrongCredentialsException();
        }
        $this->clientInfo = $clientInfo;
    
        $this->updateDiscoverConfiguration();
    
        if($refreshToken != null && !empty($refreshToken)) {
            $this->refreshToken = new AuthToken($refreshToken);
            try {
                $this->refreshTokens();
            } catch(RefreshTokenExpiredException $e) {
                $this->refreshToken = null;
            } catch(Exception $e) {
                throw $e;
            }
        }
    }

    private function updateDiscoverConfiguration() {
        $response = httpGet($this->infrastructureType->value . $GLOBALS['DISCOVER_URI'], $this->requestTimeout);
        if($response['status'] == 200) {
            $this->discover = new Discover($response['response'], true);
        } else {
            throw new ServerErrorException();
        }
    }

    function getDiscoverConfiguration() {
        return $this->discover;
    }

    function getAuthUrl(AuthorizationFlowMode $flowMode                   = AuthorizationFlowMode::SIGN_IN_WEB2,
                        WalletGetMode         $walletGetMode              = null,
                        WalletFamily          $walletFamily               = null,
                        VerificationLevel     $verificationLevel          = null,
                        string                $identityProvider           = null,
                        bool                  $isIdentityProviderRequired = false,
                        bool                  $switchAccount              = true) : string {
        $params = [
            'response_type' => 'code',
            'client_id'     => $this->clientInfo->clientId,
            'redirect_uri'  => $this->clientInfo->redirectUri,
            'scope'         => $this->discover->getScopes(),
            'flow_mode'     => $flowMode->value
        ];

        if($walletGetMode) {
            $params['wallet_get_mode'] = $walletGetMode->value;
        }
        if($walletFamily) {
            $params['wallet_family'] = $walletFamily->value;
        }
        if($verificationLevel) {
            $params['verification_level'] = $verificationLevel->value;
        }
        if($identityProvider) {
            $params['identity_provider'] = $identityProvider;
            if($isIdentityProviderRequired) {
                $params['identity_provider_required'] = '1';
            }
        }
        if($switchAccount) {
            $params['prompt'] = 'select_account';
        }
        return $this->discover->authorizationEndpoint.'?'.http_build_query($params);
    }

    function getAuthWeb2Url() {
        return $this->getAuthUrl(AuthorizationFlowMode::SIGN_IN_WEB2);
    }

    function getAuthWeb3Url(WalletFamily $walletFamily = WalletFamily::ETHEREUM) {
        return $this->getAuthUrl(AuthorizationFlowMode::SIGN_IN_WEB3, null, $walletFamily);
    }

    function getAuthGuestUpgradeUrl() {
        return $this->getAuthUrl(AuthorizationFlowMode::SIGN_IN_GUEST_UPGRADE);
    }

    function getAuthWalletGetUrl(WalletGetMode $walletGetMode = null, WalletFamily $walletFamily = null) {
        return $this->getAuthUrl(AuthorizationFlowMode::SIGN_IN_WALLET_GET, $walletGetMode, $walletFamily);
    }

    function getAuthByIdentityProviderUrl(string $identityProvider = 'google', bool $isIdentityProviderRequired = true) {
        return $this->getAuthUrl(AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER, null, null, null, $identityProvider, $isIdentityProviderRequired);
    }

    function exchangeAuthCodeToToken(string $authCode) {
        $payload = [
            'grant_type'    => 'authorization_code',
            'code'          => $authCode,
            'redirect_uri'  => $this->clientInfo->redirectUri,
        ];
        $this->paramsPrepare($payload);
        $response       = httpPost($this->discover->tokenEndpoint, $payload, null, $this->requestTimeout);
        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus >= 200 && $responseStatus < 300) {
            $this->accessToken  = new AuthToken($responseJson['access_token']);
            $this->refreshToken = new AuthToken($responseJson['refresh_token']);
        } else if($responseStatus >= 400 && $responseStatus < 500) {
            $this->handleOAuthError($responseJson['error']);
        } else if($responseStatus >= 500 && $responseStatus < 600) {
            throw new ServerErrorException();
        } else {
            throw new UnknownErrorException();
        }
    }

    function refreshTokens() {
        if(!$this->refreshToken || $this->refreshToken->isExpired()) {
            throw new RefreshTokenExpiredException();
        }

        $payload = [
            'grant_type'    => 'refresh_token',
            'refresh_token' => $this->refreshToken->token,
        ];
        $this->paramsPrepare($payload);
        $response       = httpPost($this->discover->tokenEndpoint, $payload, null, $this->requestTimeout);
        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        $this->accessToken  = null;
        $this->refreshToken = null;
        if($responseStatus >= 200 && $responseStatus < 300) {
            $this->accessToken = new AuthToken($responseJson['access_token']);
            $this->refreshToken = new AuthToken($responseJson['refresh_token']);
        } else if($responseStatus >= 400 && $responseStatus < 500) {
            $this->handleOAuthError($responseJson['error']);
        } else if($responseStatus >= 500 && $responseStatus < 600) {
            throw new ServerErrorException();
        } else {
            throw new UnknownErrorException();
        }
    }

    function getRefreshToken() {
        if($this->refreshToken && !$this->refreshToken->isExpired()) {
            return $this->refreshToken->token;
        }
        throw new RefreshTokenExpiredException();
    }

    function getAccessToken() {
        if($this->accessToken && !$this->accessToken->isExpired()) {
            return $this->accessToken->token;
        }
        throw new AccessTokenExpiredException();
    }

    function getUserInfo() : UserInfo {
        if(!$this->accessToken) throw new AccessTokenExpiredException();
        if($this->accessToken->isExpired()) $this->refreshTokens();
        return new UserInfo($this->accessToken->getDecodedToken());
    }

    function logout() {
        if(!$this->refreshToken || $this->refreshToken->isExpired()) {
            $this->accessToken  = null;
            $this->refreshToken = null;
            return;
        }

        $payload = [
            'token' => $this->refreshToken->token,
        ];
        $this->paramsPrepare($payload);

        $response       = httpPost($this->discover->revocationEndpoint, $payload, null, $this->requestTimeout);
        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if ($responseStatus >= 200 && $responseStatus < 300) {
            $this->refreshToken = null;
            $this->accessToken  = null;
            return;
        } else if($responseStatus >= 400 && $responseStatus < 500) {
            $this->handleOAuthError($responseJson['error']);
        } else if($responseStatus >= 500 && $responseStatus < 600) {
            throw new ServerErrorException();
        }
        throw new UnknownErrorException();
    }

    private function paramsPrepare(array &$payload) {
        if($this->clientInfo instanceof ClientInfoBasic) {
            $payload['client_id']       = $this->clientInfo->clientId;
            $payload['client_secret']   = $this->clientInfo->clientSecret;
        } else {
            $payload['client_assertion']        = $this->clientAssertion();
            $payload['client_assertion_type']   = 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer';
        }
    }

    private function clientAssertion() : string {
        $body = [
            'iss'   => $this->clientInfo->clientId,
            'sub'   => $this->clientInfo->clientId,
            'aud'   => $this->discover->issuer,
            'exp'   => time() + 60 * 5,
            'iat'   => time(),
            'jti'   => $this->generateJti(),
        ];
        if($this->clientInfo instanceof ClientInfoHS256
            && $this->clientInfo->authMethod == AuthorizationMethod::HS256) {
            return generateJWT($body, $this->clientInfo->authMethod, $this->clientInfo->clientSecret);
        }
        if($this->clientInfo instanceof ClientInfoRS256
            && $this->clientInfo->authMethod == AuthorizationMethod::RS256) {
            return generateJWT($body, $this->clientInfo->authMethod, $this->clientInfo->privateKey);
        }
        throw new InvalidClientException();
    }

    private function handleOAuthError($errorCode) {
        $errorClasses = [
            'invalid_request'           => 'InvalidRequestException',
            'invalid_client'            => 'InvalidClientException',
            'unauthorized_client'       => 'UnauthorizedClientException',
            'access_denied'             => 'AccessDeniedException',
            'unsupported_response_type' => 'UnsupportedResponseTypeException',
            'invalid_scope'             => 'InvalidScopeException',
            'invalid_grant'             => 'InvalidGrantException',
        ];
        $errorClass = $errorClasses[$errorCode];
        if($errorClass != null) {
            throw new $errorClass();
        }
        throw new ServerErrorException();
    }

    private function generateJti() {
        $data = $data ?? random_bytes(16);
        assert(strlen($data) == 16);

        $data[6] = chr(ord($data[6]) & 0x0f | 0x40);
        $data[8] = chr(ord($data[8]) & 0x3f | 0x80);

        return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($data), 4));
    }
}

?>