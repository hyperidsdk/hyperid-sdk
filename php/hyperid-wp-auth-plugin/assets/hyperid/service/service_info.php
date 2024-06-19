<?php

require_once __DIR__.'/../base/enum.php';

class ServiceInfo {
    public string               $clientId;
    public AuthorizationMethod  $authMethod;
    public bool                 $isSelfSigned;

    function __construct(string $clientId, AuthorizationMethod $authMethod, bool $isSelfSigned = false) {
        $this->clientId     = $clientId;
        $this->authMethod   = $authMethod;
        $this->isSelfSigned = $isSelfSigned;
    }

    function isValid() : bool {
        return isset($this->clientId)   && !empty($this->clientId)
            && isset($this->authMethod);
    }
}

class ServiceInfoSelfSignedHS256 extends ServiceInfo {
    public string $clientSecret;

    function __construct(string $clientId, string $clientSecret) {
        $this->clientSecret = $clientSecret;
        parent::__construct($clientId, AuthorizationMethod::HS256, true);
    }

    function isValid() : bool {
        return isset($this->clientSecret) && !empty($this->clientSecret)
            && parent::isValid();
    }
}

class ServiceInfoSelfSignedRS256 extends ServiceInfo {
    public string $privateKey;

    function __construct(string $clientId, string $privateKey) {
        $this->privateKey = $privateKey;
        parent::__construct($clientId, AuthorizationMethod::RS256, true);
    }

    function isValid() : bool {
        return isset($this->privateKey) && !empty($this->privateKey)
            && parent::isValid();
    }
}

class ServiceInfoBasic extends ServiceInfo {
    public string $clientSecret;

    function __construct(string $clientId, string $clientSecret) {
        $this->clientSecret = $clientSecret;
        parent::__construct($clientId, AuthorizationMethod::BASIC);
    }

    function isValid() : bool {
        return isset($this->clientSecret) && !empty($this->clientSecret)
            && parent::isValid();
    }
}

class ServiceInfoHS256 extends ServiceInfo {
    public string $clientSecret;

    function __construct(string $clientId, string $clientSecret) {
        $this->clientSecret = $clientSecret;
        parent::__construct($clientId, AuthorizationMethod::HS256);
    }

    function isValid() : bool {
        return isset($this->clientSecret) && !empty($this->clientSecret)
            && parent::isValid();
    }
}

class ServiceInfoRS256 extends ServiceInfo {
    public string $privateKey;

    function __construct(string $clientId, string $privateKey) {
        $this->privateKey = $privateKey;
        parent::__construct($clientId, AuthorizationMethod::RS256);
    }

    function isValid() : bool {
        return isset($this->privateKey) && !empty($this->privateKey)
            && parent::isValid();
    }
}

?>