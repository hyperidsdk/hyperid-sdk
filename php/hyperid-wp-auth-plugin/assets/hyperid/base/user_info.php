<?php

class Wallet {
    public ?string $wallet_address;
    public ?string $wallet_chain_id;
    public ?string $wallet_source;
    public ?string $is_wallet_verified;
    public ?string $wallet_family;

    function __construct(array $jsonData) {
        $this->wallet_address       = isset($jsonData['wallet_address'])        ? $jsonData['wallet_address']       : null;
        $this->wallet_chain_id      = isset($jsonData['wallet_chain_id'])       ? $jsonData['wallet_chain_id']      : null;
        $this->wallet_source        = isset($jsonData['wallet_source'])         ? $jsonData['wallet_source']        : null;
        $this->is_wallet_verified   = isset($jsonData['is_wallet_verified'])    ? $jsonData['is_wallet_verified']   : null;
        $this->wallet_family        = isset($jsonData['wallet_family'])         ? $jsonData['wallet_family']        : null;
    }

    function __toString() {
        $wallet = "";
        foreach ($this as $key => $value) {
            if($value) {
                $wallet .= "<p>$key => $value</p>";
            }
        }
        return $wallet;
    }

    function isEmpty() : bool {
        foreach ($this as $key => $value) {
            if($value) {
                return false;
            }
        }
        return true;
    }
}

class UserInfo {
    public ?string  $user_id;
    public ?bool    $is_guest;
    public ?string  $user_email;
    public ?array   $user_roles;
    public ?string  $ip;
    public ?Wallet  $wallet;

    function __construct(array $jsonData) {
        $this->user_id      = isset($jsonData['sub'])       ? $jsonData['sub']          : null;
        $this->is_guest     = isset($jsonData['is_guest'])  ? true                      : (isset($jsonData['email']) ? false : null);
        $this->user_email   = isset($jsonData['email'])     ? $jsonData['email']        : null;
        $this->user_roles   = isset($jsonData['user_roles'])? $jsonData['user_roles']   : null;
        $this->ip           = isset($jsonData['ip'])        ? $jsonData['ip']           : null;
        $this->wallet       = new Wallet($jsonData);
        if($this->wallet->isEmpty()) {
            $this->wallet = null;
        }
    }

    function __toString() {
        $userInfo = "";
        foreach ($this as $key => $value) {
            if($value && !empty($value)) {
                $userInfo .= "<p>$key => $value</p>";
            }
        }
        return $userInfo;
    }
}

?>