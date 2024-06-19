<?php

class OAuth2Error extends Exception {
    function __construct(string $message = 'An OAuth2 error occurred', int $code = 0, Throwable $previous = null) {
        parent::__construct($message, $code, $previous);
    }

    public function __toString() {
        return get_class($this) . " => Code : {$this->code}, Message : {$this->message}, Stack trace : {$this->getTraceAsString()}\n";
    }
}

class InvalidRequestException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed.', -1, $previous);
    }
}

class InvalidClientException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The client credentials is invalid.', -2, $previous);
    }
}

class UnauthorizedClientException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The client is not authorized to request an authorization code using this method.', -3, $previous);
    }
}

class AccessDeniedException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The resource owner or authorization server denied the request.', -4, $previous);
    }
}

class UnsupportedResponseTypeException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The authorization server does not support obtaining an authorization code using this method.', -5, $previous);
    }
}

class InvalidScopeException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The requested scope is invalid, unknown, or malformed.', -6, $previous);
    }
}

class InvalidGrantException extends OAuth2Error {
    function __construct(Throwable $previous = null) {
        parent::__construct('The requested grant is invalid, token or code may be stale or invalid.', -7, $previous);
    }
}

?>