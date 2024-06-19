<?php

class Discover {
    public string $issuer;
    public string $authorizationEndpoint;
    public string $tokenEndpoint;
    public string $introspectionEndpoint;
    public string $userinfoEndpoint;
    public string $endSessionEndpoint;
    public string $jwksUri;
    public array $responseTypesSupported;
    public array $codeChallengeMethodsSupported;
    public string $revocationEndpoint;
    public string $restApiTokenEndpoint;
    public string $restApiPublicEndpoint;
    public array $identityProviders;
    public array $clientScopesDefault;
    public array $clientScopesOptional;
    public array $walletFamily;
    public array $walletSource;
    public array $walletChain;

    function __construct(array $data) {
        $this->issuer                           = $data['issuer'];
        $this->authorizationEndpoint            = $data['authorization_endpoint'];
        $this->tokenEndpoint                    = $data['token_endpoint'];
        $this->introspectionEndpoint            = $data['introspection_endpoint'];
        $this->userinfoEndpoint                 = $data['userinfo_endpoint'];
        $this->endSessionEndpoint               = $data['end_session_endpoint'];
        $this->jwksUri                          = $data['jwks_uri'];
        $this->responseTypesSupported           = $data['response_types_supported'];
        $this->codeChallengeMethodsSupported    = $data['code_challenge_methods_supported'];
        $this->revocationEndpoint               = $data['revocation_endpoint'];
        $this->restApiTokenEndpoint             = $data['rest_api_token_endpoint'];
        $this->restApiPublicEndpoint            = $data['rest_api_public_endpoint'];
        $this->identityProviders                = $data['identity_providers'];
        $this->clientScopesDefault              = $data['client_scopes_default'];
        $this->clientScopesOptional             = $data['client_scopes_optional'];
        $this->walletFamily                     = $data['wallet_family'];
        $this->walletSource                     = $data['wallet_source'];
        $this->walletChain                      = $data['wallet_chain'];
    }

    function getScopes() : string {
        $scopes = '';
        foreach ($this->clientScopesDefault as $scope){
            $scopes .= $scope.' ';
        }
        foreach ($this->clientScopesOptional as $scope){
            $scopes .= $scope.' ';
        }
        return trim($scopes);
    }
}

?>