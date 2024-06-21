<?php

require_once __DIR__.'/../error.php';
require_once __DIR__.'/../utils.php';

enum UserDataSetByUserIdResult : int {
    case FAIL_BY_KEY_INVALID                    = -7;
    case FAIL_BY_KEY_ACCESS_DENIED              = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
}

enum UserDataGetByUserIdResult : int {
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByUserIdGetResult : int {
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByUserIdDeleteResult : int {
    case FAIL_BY_KEY_ACCESS_DENIED              = -6;
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

class HyperIDUserIdStorage {
    public string $restApiEndpoint;

    function __construct(string $restApiEndpoint) {
        $this->restApiEndpoint = $restApiEndpoint;
    }

    function setData(string $accessToken,
                     string $valueKey,
                     string $valueData,
                     UserDataAccessScope $accessScope = UserDataAccessScope::PUBLIC) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'value_key'     => $valueKey,
            'value_data'    => $valueData,
            'access_scope'  => $accessScope
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-user-id/set", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataSetByUserIdResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataSetByUserIdResult::FAIL_BY_KEY_INVALID->value:         throw new HyperIdException("Provided key is invalid.");
                    case UserDataSetByUserIdResult::FAIL_BY_KEY_ACCESS_DENIED->value:   throw new HyperIdException("Key access violation: Your permissions are not sufficient.");
                    case UserDataSetByUserIdResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                    case UserDataSetByUserIdResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                    case UserDataSetByUserIdResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                    default:                                                            throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }

    function getData(string $accessToken,
                     string $valueKey) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'value_keys' => [$valueKey]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-user-id/get", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataGetByUserIdResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataGetByUserIdResult::FAIL_BY_TOKEN_INVALID->value:   throw new AccessTokenExpiredException();
                    case UserDataGetByUserIdResult::FAIL_BY_TOKEN_EXPIRED->value:   throw new AccessTokenExpiredException();
                    case UserDataGetByUserIdResult::FAIL_BY_ACCESS_DENIED->value:   throw new AccessTokenExpiredException();
                    case UserDataGetByUserIdResult::SUCCESS_NOT_FOUND->value:       return null;
                    default:                                                        throw new ServerErrorException();
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

    function getKeys(string $accessToken) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-user-id/list-get", [], $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByUserIdGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByUserIdGetResult::FAIL_BY_TOKEN_INVALID->value:   throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdGetResult::FAIL_BY_TOKEN_EXPIRED->value:   throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdGetResult::FAIL_BY_ACCESS_DENIED->value:   throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdGetResult::SUCCESS_NOT_FOUND->value:       return null;
                    default:                                                            throw new ServerErrorException();
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

    function getKeysListShared(string $accessToken) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $shouldContinue = true;
        $keysShared = [];
        $searchId = "";
        $pageSize = 100;

        do {
            $params = [
                'search_id' => $searchId,
                'page_size' => $pageSize
            ];
            $header[]   = 'Authorization: Bearer '.$accessToken;
            $request = httpPostJSON($this->restApiEndpoint."/user-data/by-user-id/shared-list-get", $params, $header);

            if($request['status'] >= 200 && $request['status'] <= 299) {
                $data = $request['response'];
                if($data['result'] !== UserDataKeysByUserIdGetResult::SUCCESS->value) {
                    switch ($data['result']) {
                        case UserDataKeysByUserIdGetResult::FAIL_BY_TOKEN_INVALID->value:   throw new AccessTokenExpiredException();
                        case UserDataKeysByUserIdGetResult::FAIL_BY_TOKEN_EXPIRED->value:   throw new AccessTokenExpiredException();
                        case UserDataKeysByUserIdGetResult::FAIL_BY_ACCESS_DENIED->value:   throw new AccessTokenExpiredException();
                        case UserDataKeysByUserIdGetResult::SUCCESS_NOT_FOUND->value:       return null;
                        default:                                                            throw new ServerErrorException();
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
                       string $valueKey) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'value_keys' => [$valueKey]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-user-id/delete", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByUserIdDeleteResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByUserIdDeleteResult::FAIL_BY_KEY_ACCESS_DENIED->value:    throw new HyperIdException("Key access violation: Your permissions are not sufficient.");
                    case UserDataKeysByUserIdDeleteResult::FAIL_BY_TOKEN_INVALID->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdDeleteResult::FAIL_BY_TOKEN_EXPIRED->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdDeleteResult::FAIL_BY_ACCESS_DENIED->value:        throw new AccessTokenExpiredException();
                    case UserDataKeysByUserIdDeleteResult::SUCCESS_NOT_FOUND->value:            return;
                    default:                                                                    throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }
}

?>