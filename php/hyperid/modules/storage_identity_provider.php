<?php

require_once __DIR__.'/../error.php';
require_once __DIR__.'/../utils.php';

enum UserDataByIdpSetResult : int {
    case FAIL_BY_KEY_INVALID                    = -8;
    case FAIL_BY_KEY_ACCESS_DENIED              = -7;
    case FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND   = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
}

enum UserDataByIdpGetResult : int {
    case FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND   = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByIdpGetResult : int {
    case FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND   = -6;
    case FAIL_BY_INVALID_PARAMETERS             = -5;
    case FAIL_BY_SERVICE_TEMPORARY_NOT_VALID    = -4;
    case FAIL_BY_ACCESS_DENIED                  = -3;
    case FAIL_BY_TOKEN_EXPIRED                  = -2;
    case FAIL_BY_TOKEN_INVALID                  = -1;
    case SUCCESS                                = 0;
    case SUCCESS_NOT_FOUND                      = 1;
}

enum UserDataKeysByIdpDeleteResult : int {
    case FAIL_BY_KEY_ACCESS_DENIED              = -7;
    case FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND   = -6;
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

class HyperIDIDPStorage {
    public string $restApiEndpoint;

    function __construct(string $restApiEndpoint) {
        $this->restApiEndpoint = $restApiEndpoint;
    }

    function setData(string $accessToken,
                     string $identityProvider,
                     string $valueKey,
                     string $valueData,
                     UserDataAccessScope $accessScope = UserDataAccessScope::PUBLIC) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'identity_providers'=> [$identityProvider],
            'value_key'         => $valueKey,
            'value_data'        => $valueData,
            'access_scope'      => $accessScope
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-idp/set", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataByIdpSetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataByIdpSetResult::FAIL_BY_KEY_INVALID->value:                    throw new HyperIdException("Provided key is invalid.");
                    case UserDataByIdpSetResult::FAIL_BY_KEY_ACCESS_DENIED->value:              throw new HyperIdException("Key access violation: Your permissions are not sufficient.");
                    case UserDataByIdpSetResult::FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND->value:   throw new HyperIdException("Identity provider not found.");
                    case UserDataByIdpSetResult::FAIL_BY_TOKEN_INVALID->value:                  throw new AccessTokenExpiredException();
                    case UserDataByIdpSetResult::FAIL_BY_TOKEN_EXPIRED->value:                  throw new AccessTokenExpiredException();
                    case UserDataByIdpSetResult::FAIL_BY_ACCESS_DENIED->value:                  throw new AccessTokenExpiredException();
                    default:                                                                    throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }

    function getData(string $accessToken,
                     string $identityProvider,
                     string $valueKey) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'identity_providers'=> [$identityProvider],
            'value_keys'        => [$valueKey]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-idp/get", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataByIdpGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataByIdpGetResult::FAIL_BY_IDENTITY_PROVIDERS_NOT_FOUND->value:   throw new HyperIdException("Identity provider not found.");
                    case UserDataByIdpGetResult::FAIL_BY_TOKEN_INVALID->value:                  throw new AccessTokenExpiredException();
                    case UserDataByIdpGetResult::FAIL_BY_TOKEN_EXPIRED->value:                  throw new AccessTokenExpiredException();
                    case UserDataByIdpGetResult::FAIL_BY_ACCESS_DENIED->value:                  throw new AccessTokenExpiredException();
                    case UserDataByIdpGetResult::SUCCESS_NOT_FOUND->value:                      return null;
                    default:                                                                    throw new ServerErrorException();
                }
            }
            if(empty($data['identity_providers'])) return null;
            $idp = $data['identity_providers'];
            
            if(empty($idp[0]['identity_provider'])) return null;
            $valueData = $idp[0]['identity_provider'];

            foreach($valueData as $item) {
                if($item['value_key'] == $valueKey) {
                    return $item['value_data'];
                }
            }
            return null;
        } else {
            throw new ServerErrorException();
        }
    }

    function getKeys(string $accessToken, string $identityProvider) {
        if(empty($accessToken)) {
            throw new AuthorizationRequiredException();
        }

        $params = [
            'identity_providers'=> [$identityProvider]
        ];
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-email/list-get", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByEmailGetResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByEmailGetResult::FAIL_BY_TOKEN_INVALID->value:    throw new AccessTokenExpiredException();
                    case UserDataKeysByEmailGetResult::FAIL_BY_TOKEN_EXPIRED->value:    throw new AccessTokenExpiredException();
                    case UserDataKeysByEmailGetResult::FAIL_BY_ACCESS_DENIED->value:    throw new AccessTokenExpiredException();
                    case UserDataKeysByEmailGetResult::SUCCESS_NOT_FOUND->value:        return null;
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

    function getKeysListShared(string $accessToken, string $identityProvider) {
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
                'page_size' => $pageSize,
                'identity_providers' => [$identityProvider]
            ];
            $header[]   = 'Authorization: Bearer '.$accessToken;
            $request = httpPostJSON($this->restApiEndpoint."/user-data/by-email/shared-list-get", $params, $header);

            if($request['status'] >= 200 && $request['status'] <= 299) {
                $data = $request['response'];
                if($data['result'] !== UserDataKeysByEmailGetResult::SUCCESS->value) {
                    switch ($data['result']) {
                        case UserDataKeysByEmailGetResult::FAIL_BY_TOKEN_INVALID->value:    throw new AccessTokenExpiredException();
                        case UserDataKeysByEmailGetResult::FAIL_BY_TOKEN_EXPIRED->value:    throw new AccessTokenExpiredException();
                        case UserDataKeysByEmailGetResult::FAIL_BY_ACCESS_DENIED->value:    throw new AccessTokenExpiredException();
                        case UserDataKeysByEmailGetResult::SUCCESS_NOT_FOUND->value:        return null;
                        default:                                                            throw new ServerErrorException();
                    }
                }
                $searchId = $data['next_search_id'];
                if(!isset($data['identity_providers'])) {
                    $idp = $data['identity_providers'];
                    if(!isset($idp[0]['identity_provider'])) {
                        $valueData = $idp[0]['identity_provider'];
                        $ks = $valueData['keys_shared'];
                        array_push($keysShared, $ks);
                        if(count($ks) < $pageSize) {
                            $shouldContinue = false;
                        }
                    }
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
        $request = httpPostJSON($this->restApiEndpoint."/user-data/by-email/delete", $params, $header);

        if($request['status'] >= 200 && $request['status'] <= 299) {
            $data = $request['response'];
            if($data['result'] !== UserDataKeysByIdpDeleteResult::SUCCESS->value) {
                switch ($data['result']) {
                    case UserDataKeysByIdpDeleteResult::FAIL_BY_KEY_ACCESS_DENIED->value:   throw new HyperIdException("Key access violation: Your permissions are not sufficient.");
                    case UserDataKeysByIdpDeleteResult::FAIL_BY_TOKEN_INVALID->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByIdpDeleteResult::FAIL_BY_TOKEN_EXPIRED->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByIdpDeleteResult::FAIL_BY_ACCESS_DENIED->value:       throw new AccessTokenExpiredException();
                    case UserDataKeysByIdpDeleteResult::SUCCESS_NOT_FOUND->value:           return;
                    default:                                                                throw new ServerErrorException();
                }
            }
        } else {
            throw new ServerErrorException();
        }
    }
}

?>