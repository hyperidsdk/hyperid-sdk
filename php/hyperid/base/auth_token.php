<?php

class AuthToken {
    public string $token;

    function __construct(string $token) {
        $this->token = $token;
    }

    function setToken(string $token) {
        $this->token = $token;
    }

    function getToken() {
        return $this->token;
    }

    function getDecodedToken() : array {
        if($this->token == null || empty($this->token)) return [];
        list($header, $payload, $signature) = explode('.', $this->token);
        return json_decode(base64_decode(strtr($payload, '-_', '+/')), true);
    }

    function isExpired() : bool {
        if($this->token == null || empty($this->token)) return true;
        $decodedToken = $this->getDecodedToken();
        $exp_timestamp = $decodedToken['exp'];
        if($exp_timestamp === null) return false;
        return time() > $exp_timestamp;
    }
}

?>