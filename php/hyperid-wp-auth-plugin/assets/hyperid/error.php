<?php

class HyperIdException extends Exception {
    function __construct(string $message = 'HyperIdException', int $code = 0, Throwable $previous = null) {
        parent::__construct($message, $code, $previous);
    }

    function __toString() {
        return get_class($this) . " => Code : {$this->code}, Message : {$this->message}, Stack trace : {$this->getTraceAsString()}\n";
    }
}

class AuthorizationRequiredException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Authorization required. Please sign in.', -1, $previous);
    }
}

class AccessTokenExpiredException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Access token is expired. Please sign in first.', -2, $previous);
    }
}

class RefreshTokenExpiredException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Refresh token is expired. Re-authorization required.', -3, $previous);
    }
}

class ServerErrorException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Server Under maintenance. Please try again later.', -4, $previous);
    }
}

class UnknownErrorException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Unknow error.', -5, $previous);
    }
}

class WrongCredentialsException extends HyperIdException {
    function __construct(Throwable $previous = null) {
        parent::__construct('Wrong credentials.', -6, $previous);
    }
}

?>