<?php

require_once 'enum_results.php';

require_once __DIR__.'/../utils.php';
require_once __DIR__.'/../error.php';
require_once __DIR__.'/../error_rfc6749.php';

class RoleManager {
    public int      $requestTimeout;
    public string   $restApiURI;

    function __construct(string $restApiURI, int $requestTimeout = 10) {
        $this->restApiURI       = $restApiURI;
        $this->requestTimeout   = $requestTimeout;
    }

    /**
     * @return response{'result' => RoleCreateResult, 'role_id'=>'some_role_id'}
     */
    function roleCreate(string $accessToken, string $roleName, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_name'=> $roleName,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/create' : '/admin/project-role/create');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => RoleCreateResult::from($responseJson['result']), 'role_id' => $responseJson['role_id']];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RolesGetResult, 'roles' => [{'id'=>'role_id', 'name'=>'role_name'}, ...]}
     */
    function rolesGet(string $accessToken, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $requestUrl = $this->restApiURI.($isService ? '/service/project-roles/get' : '/admin/project-roles/get');
        $response   = httpPostJSON($requestUrl, [], $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => RolesGetResult::from($responseJson['result']), 'roles' => $responseJson['roles']];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RoleDeleteResult}
     */
    function roleDelete(string $accessToken, string $roleId, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'=> $roleId,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/delete' : '/admin/project-role/delete');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => RoleDeleteResult::from($responseJson['result'])];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => UserRoleAttachResult}
     */
    function userRoleAttach(string $accessToken, string $userId, string $userEmail, string $roleId, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'user_id'   => $userId,
            'user_email'=> $userEmail,
            'role_id'   => $roleId,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/user/attach' : '/admin/project-role/user/attach');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result'    => UserRoleAttachResult::from($responseJson['result']),
                    'user_id'   => UserRoleAttachResult::from($responseJson['result']) == UserRoleAttachResult::SUCCESS ? $responseJson['user_id'] : null];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => UserRoleDetachResult}
     */
    function userRoleDetach(string $accessToken, string $userId, string $roleId, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'user_id'=> $userId,
            'role_id'=> $roleId,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/user/detach' : '/admin/project-role/user/detach');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => UserRoleDetachResult::from($responseJson['result'])];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => UsersByRoleGetResult, 'next_page_offset' => 0, 'next_page_size' => 0, 'user_ids' => ['user_id1', ...]}
     */
    function usersByRoleGet(string $accessToken, string $roleId, int $pageOffset = 0, int $pageSize = 100, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'       => $roleId,
            'page_offset'   => $pageOffset,
            'page_size'     => $pageSize,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/users/get' : '/admin/project-role/users/get');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return [
                'result'            => UsersByRoleGetResult::from($responseJson['result']),
                'next_page_offset'  => $responseJson['next_page_offset'],
                'next_page_size'    => $responseJson['next_page_size'],
                'user_ids'          => $responseJson['user_ids'],
            ];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RoleAttributeReplaceResult}
     */
    function roleAttributeReplace(string $accessToken, string $roleId, string $attributeKey, string $attributeValue, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'   => $roleId,
            'key'       => $attributeKey,
            'value'     => $attributeValue,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/attribute/replace' : '/admin/project-role/attribute/replace');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => RoleAttributeReplaceResult::from($responseJson['result'])];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RoleAttributeGetResult, 'value' => "some_value"}
     */
    function roleAttributeGet(string $accessToken, string $roleId, string $attributeKey, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'   => $roleId,
            'key'       => $attributeKey,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/attribute/get' : '/admin/project-role/attribute/get');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return [
                'result'=> RoleAttributeGetResult::from($responseJson['result']),
                'value' => $responseJson['value'],
            ];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RoleAttributesGetResult, 'next_page_offset' => 0, 'next_page_size' => 100, 'attributes' => {'key'=>'value', ...}}
     */
    function roleAttributesGet(string $accessToken, string $roleId, int $pageOffset = 0, int $pageSize = 100, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'       => $roleId,
            'page_offset'   => $pageOffset,
            'page_size'     => $pageSize,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/attributes/get' : '/admin/project-role/attributes/get');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return [
                'result'            => RoleAttributesGetResult::from($responseJson['result']),
                'next_page_offset'  => $responseJson['next_page_offset'],
                'next_page_size'    => $responseJson['next_page_size'],
                'attributes'        => $responseJson['attributes'],
            ];
        }
        throw new ServerErrorException();
    }

    /**
     * @return response{'result' => RoleAttributeDeleteResult}
     */
    function roleAttributeDelete(string $accessToken, string $roleId, string $attributeKey, bool $isService = true) {
        $header[]   = 'Authorization: Bearer '.$accessToken;
        $payload    = [
            'role_id'   => $roleId,
            'key'       => $attributeKey,
        ];
        $requestUrl = $this->restApiURI.($isService ? '/service/project-role/attribute/delete' : '/admin/project-role/attribute/delete');
        $response   = httpPostJSON($requestUrl, $payload, $header, $this->requestTimeout);

        $responseStatus = $response['status'];
        $responseJson   = $response['response'];

        if($responseStatus == 0 && $responseJson == null) {
            throw new ServerErrorException();
        }

        if($responseStatus == 200 || $responseStatus == 400) {
            return ['result' => RoleAttributeDeleteResult::from($responseJson['result'])];
        }
        throw new ServerErrorException();
    }
}

?>