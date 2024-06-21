<?php

require_once __DIR__.'/../error.php';
require_once __DIR__.'/../utils.php';

enum UserWalletsGetResult : int {
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
}

enum UserDataByWalletSetResult : int {
    case FAIL_BY_KEY_INVALID                    = -8;
    case FAIL_BY_KEY_ACCESS_DENIED              = -7;
    case FAIL_BY_WALLET_NOT_EXISTS              = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
}

enum UserDataByWalletGetResult : int {
    case FAIL_BY_WALLET_NOT_EXISTS              = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByWalletGetResult : int {
    case FAIL_BY_WALLET_NOT_EXISTS              = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByWalletDeleteResult : int {
    case FAIL_BY_WALLET_NOT_EXISTS              = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataAccessScope : int {
    case PRIVATE    = 0;
    case PUBLIC     = 1;
}

class WalletData {
    public string $address;
    public string $chain;
    public bool   $isPublic;

    function __construct(string $address,
                         string $chain,
                         bool   $isPublic = true
    ) {
        $this->address  = $address;
        $this->chain    = $chain;
        $this->isPublic = $isPublic;
    }
}

class HyperIDWalletStorage {
    public string $restApiEndpoint;

    function __construct(string $restApiEndpoint) {
        $this->restApiEndpoint = $restApiEndpoint;
    }

    function getWallets(string $accessToken) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-wallets/get", [], $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserWalletsGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserWalletsGetResult::FAIL_BY_TOKEN_INVALID->value:        throw new AccessTokenExpiredException();
                    case UserWalletsGetResult::FAIL_BY_TOKEN_EXPIRED->value:        throw new AccessTokenExpiredException();
                    case UserWalletsGetResult::FAIL_BY_ACCESS_DENIED->value:        throw new AccessTokenExpiredException();
                    default:                                                        throw new ServerErrorException();
                }
            }
            $wallets = [];
            foreach($data['wallets_public'] as $wallet) {
                array_push($wallets, new WalletData($wallet['address'], $wallet['chain']));
            }
            foreach($data['wallets_private'] as $wallet) {
                array_push($wallets, new WalletData($wallet['address'], $wallet['chain'], false));
            }
            return $wallets;
        } else {
            throw new ServerErrorException();
        }
    }

    function setData(string $accessToken,
                     string $walletAddress,
                     string $valueKey,
                     string $valueData,
                     UserDataAccessScope $accessScope = UserDataAccessScope::PUBLIC) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'wallet_address'=> $walletAddress,
            'value_key'     => $valueKey,
            'value_data'    => $valueData,
            'access_scope'  => $accessScope
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-wallet/set", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataByWalletSetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataByWalletSetResult::FAIL_BY_KEY_INVALID->value:         throw new HyperIdException("Provided key is invalid.");
                    case UserDataByWalletSetResult::FAIL_BY_KEY_ACCESS_DENIED->value:   throw new HyperIdException("Key access violation: Your permissions are not sufficient.");
                    case UserDataByWalletSetResult::FAIL_BY_WALLET_NOT_EXISTS->value:   throw new HyperIdException("Specified wallet not found.");
                    case UserDataByWalletSetResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                    case UserDataByWalletSetResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                    case UserDataByWalletSetResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                    default:                                                            throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }

    function getData(string $accessToken,
                     string $walletAddress,
                     string $valueKey) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'wallet_address'=> $walletAddress,
            'value_keys'    => [$valueKey]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-wallet/get", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataByWalletGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataByWalletGetResult::FAIL_BY_WALLET_NOT_EXISTS->value:   throw new HyperIdException("Specified wallet not found.");
                    case UserDataByWalletGetResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                    case UserDataByWalletGetResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                    case UserDataByWalletGetResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                    case UserDataByWalletGetResult::SUCCESS_NOT_FOUND->value:           return null;
                    default:                                                            throw new ServerErrorException();
                }
            }
            if(empty($data['values'])) {
                return null;
            }
            $values = $data['values'];
            return $values[0]['value_data'];
        } else {
            throw new ServerErrorException();
        }
    }

    function getKeys(string $accessToken, string $walletAddress) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'wallet_address'=> $walletAddress
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-wallet/list-get", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByWalletGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByWalletGetResult::FAIL_BY_WALLET_NOT_EXISTS->value:   throw new HyperIdException("Specified wallet not found.");
                    case UserDataKeysByWalletGetResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletGetResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletGetResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletGetResult::SUCCESS_NOT_FOUND->value:           return null;
                    default:                                                                throw new ServerErrorException();
                }
            }
            $keys = [];
            $keys['public'] = $data['keys_public'];
            $keys['private']= $data['keys_private'];
            return $keys;
        } else {
            throw new ServerErrorException();
        }
    }

    function getKeysListShared(string $accessToken, string $walletAddress) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $shouldContinue = true;
        $keysShared = [];
        $searchId = "";
        $pageSize = 100;

        do {
            $params = [
                'wallet_address'=> $walletAddress,
                'search_id'     => $searchId,
                'page_size'     => $pageSize
            ];
            $header[]   = 'Authorization: Bearer '.$accessToken;
            $request = httpPostJSON($this->restApiEndpoint."/user-data/by-wallet/shared-list-get", $params, $header);

            if($request['status'] >= 200 && $request['status'] <= 299) {
                $data = $request['response'];
                if($data['result'] !== UserDataKeysByWalletGetResult::SUCCESS->value) {
                    switch ($data['result']) {
                        case UserDataKeysByWalletGetResult::FAIL_BY_WALLET_NOT_EXISTS->value:   throw new HyperIdException("Specified wallet not found.");
                        case UserDataKeysByWalletGetResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                        case UserDataKeysByWalletGetResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                        case UserDataKeysByWalletGetResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                        case UserDataKeysByWalletGetResult::SUCCESS_NOT_FOUND->value:           return null;
                        default:                                                                throw new ServerErrorException();
                    }
                }
                $searchId = $data['next_search_id'];
                $ks = $data['keys_shared'];
                array_push($keysShared, $ks);
                if(count($ks) < $pageSize) {
                    $shouldContinue = false;
                }
            } else {
                throw new ServerErrorException();
            }
        } while($shouldContinue);
    }

    function deleteKey(string $accessToken,
                       string $walletAddress,
                       string $valueKey) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'wallet_address'=> $walletAddress,
            'value_keys'    => [$valueKey]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-wallet/delete", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByWalletDeleteResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByWalletDeleteResult::FAIL_BY_WALLET_NOT_EXISTS->value:    throw new HyperIdException("Specified wallet not found.");
                    case UserDataKeysByWalletDeleteResult::FAIL_BY_TOKEN_INVALID->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletDeleteResult::FAIL_BY_TOKEN_EXPIRED->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletDeleteResult::FAIL_BY_ACCESS_DENIED->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByWalletDeleteResult::SUCCESS_NOT_FOUND->value:            return;
                    default:                                                                    throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }
}

?>