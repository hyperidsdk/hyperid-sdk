<?php

require_once __DIR__.'/../base/enum.php';

class ClientInfo {
    public string               $clientId;
    public string               $redirectUri;
    public AuthorizationMethod  $authMethod;

    function __construct(string $clientId, string $redirectUri, AuthorizationMethod $authMethod) {
        $this->clientId     = $clientId;
        $this->redirectUri  = $redirectUri;
        $this->authMethod   = $authMethod;
    }

    function isValid() : bool {
        return isset($this->clientId)       && !empty($this->clientId)
            && isset($this->redirectUri)    && !empty($this->redirectUri)
            && isset($this->authMethod);
    }
}

class ClientInfoBasic extends ClientInfo {
    public string $clientSecret;

    function __construct(string $clientId, string $clientSecret, string $redirectUri) {
        $this->clientSecret = $clientSecret;
        parent::__construct($clientId, $redirectUri, AuthorizationMethod::BASIC);
    }

    function isValid() : bool {
        return isset($this->clientSecret) && !empty($this->clientSecret)
            && parent::isValid();
    }
}

class ClientInfoHS256 extends ClientInfo {
    public string $clientSecret;

    function __construct(string $clientId, string $clientSecret, string $redirectUri) {
        $this->clientSecret = $clientSecret;
        parent::__construct($clientId, $redirectUri, AuthorizationMethod::HS256);
    }

    function isValid() : bool {
        return isset($this->clientSecret) && !empty($this->clientSecret)
            && parent::isValid();
    }
}

class ClientInfoRS256 extends ClientInfo {
    public string $privateKey;

    function __construct(string $clientId, string $privateKey, string $redirectUri) {
        $this->privateKey = $privateKey;
        parent::__construct($clientId, $redirectUri, AuthorizationMethod::RS256);
    }

    function isValid() : bool {
        return isset($this->privateKey) && !empty($this->privateKey)
            && parent::isValid();
    }
}

?>