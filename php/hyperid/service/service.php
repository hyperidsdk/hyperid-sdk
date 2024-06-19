<?php

require_once 'service_info.php';
require_once __DIR__.'/../base/auth_token.php';
require_once __DIR__.'/../base/discover.php';
require_once __DIR__.'/../base/enum.php';
require_once __DIR__.'/../base/user_info.php';
require_once __DIR__.'/../utils.php';
require_once __DIR__.'/../error.php';
require_once __DIR__.'/../error_rfc6749.php';

$DISCOVER_URI = '/auth/realms/HyperID/.well-known/openid-configuration';

class Service {
    public InfrastructureType   $infrastructureType;
    public int                  $requestTimeout;
    public ServiceInfo          $serviceInfo;
    public Discover             $discover;
    public ?AuthToken           $accessToken    = null;
    public ?AuthToken           $refreshToken   = null;

    function __construct(ServiceInfo        $serviceInfo,
                         string             $refreshToken       = '',
                         InfrastructureType $infrastructureType = InfrastructureType::SANDBOX,
                         int                $requestTimeout     = 10) {
        $this->infrastructureType   = $infrastructureType;
        $this->requestTimeout       = $requestTimeout;
        if(!$serviceInfo || !$serviceInfo->isValid()) {
            throw new WrongCredentialsException();
        }
        $this->serviceInfo = $serviceInfo;

        $this->updateDiscoverConfiguration();

        if(!$this->serviceInfo->isSelfSigned && $refreshToken != null && !empty($refreshToken)) {
            $this->refreshToken = new AuthToken($refreshToken);
            try {
                $this->refreshTokens();
            } catch(RefreshTokenExpiredException $e) {
                $this->refreshToken = null;
            } catch(Exception $e) {
                throw $e;
            }
        }
        if(!$this->accessToken) {
            $this->authenticate();
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

    function authenticate() {
        if($this->serviceInfo->isSelfSigned) {
            $this->accessToken = new AuthToken($this->generateSelfSignedToken());
            return;
        }
        $this->logout();

        $response = null;
        if($this->serviceInfo instanceof ServiceInfoBasic) {
            $payload = [
                'grant_type'=> 'client_credentials',
                'scope'     => $this->discover->getScopes(),
            ];
            $header[] = 'Authorization: Basic '.base64_encode($this->serviceInfo->clientId.":".$this->serviceInfo->clientSecret);
            $response = httpPost($this->discover->tokenEndpoint, $payload, $header, $this->requestTimeout);
        } else {
            $payload = [
                'grant_type'            => 'client_credentials',
                'client_assertion_type' => 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
                'client_assertion'      => $this->clientAssertion(),
                'scope'                 => $this->discover->getScopes(),
            ];
            $response       = httpPost($this->discover->tokenEndpoint, $payload, null, $this->requestTimeout);
        }
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

    function refreshTokens() {
        if(!$this->refreshToken || $this->refreshToken->isExpired()) {
            $this->authenticate();
            return;
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
        if($this->serviceInfo->isSelfSigned) {
            return null;
        }
        if($this->refreshToken && !$this->refreshToken->isExpired()) {
            return $this->refreshToken->token;
        }
        $this->authenticate();
        return $this->refreshToken->token;
    }

    function getAccessToken() {
        if($this->accessToken && !$this->accessToken->isExpired()) {
            return $this->accessToken->token;
        } else {
            $this->refreshTokens();
            return $this->accessToken->token;
        }
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

    private function generateSelfSignedToken() {
        $body = [
            'iss'   => $this->serviceInfo->clientId,
            'sub'   => $this->serviceInfo->clientId,
            'azp'   => $this->serviceInfo->clientId,
            'aud'   => $this->discover->issuer,
            'jti'   => $this->generateJti(),
            'typ'   => 'Bearer',
            'iat'   => time(),
            'exp'   => time() + 60 * 5,
            'scope' => $this->discover->getScopes(),
        ];
        if($this->serviceInfo instanceof ServiceInfoSelfSignedHS256 || $this->serviceInfo instanceof ServiceInfoHS256) {
            return generateJWT($body, $this->serviceInfo->authMethod, $this->serviceInfo->clientSecret);
        }
        if($this->serviceInfo instanceof ServiceInfoSelfSignedRS256 || $this->serviceInfo instanceof ServiceInfoRS256) {
            return generateJWT($body, $this->serviceInfo->authMethod, $this->serviceInfo->privateKey);
        }
    }

    private function paramsPrepare(array &$payload) {
        if($this->serviceInfo instanceof ServiceInfoBasic) {
            $payload['client_id']       = $this->serviceInfo->clientId;
            $payload['client_secret']   = $this->serviceInfo->clientSecret;
        } else {
            $payload['client_assertion']        = $this->clientAssertion();
            $payload['client_assertion_type']   = 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer';
        }
    }

    private function clientAssertion() : string {
        $body = [
            'iss'   => $this->serviceInfo->clientId,
            'sub'   => $this->serviceInfo->clientId,
            'aud'   => $this->discover->issuer,
            'jti'   => $this->generateJti(),
            'iat'   => time(),
            'exp'   => time() + 60 * 5,
        ];
        if($this->serviceInfo instanceof ServiceInfoSelfSignedHS256 || $this->serviceInfo instanceof ServiceInfoHS256) {
            return generateJWT($body, $this->serviceInfo->authMethod, $this->serviceInfo->clientSecret);
        }
        if($this->serviceInfo instanceof ServiceInfoSelfSignedRS256 || $this->serviceInfo instanceof ServiceInfoRS256) {
            return generateJWT($body, $this->serviceInfo->authMethod, $this->serviceInfo->privateKey);
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