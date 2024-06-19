<?php

class RolesApi
{
    const               USERS_PER_PAGE          = 10;
    const               GET_ROLES_CACHE_NAME    = 'hid_project_roles';

    protected static $manager       = NULL;
    protected static $accessToken   = NULL;
    protected static $isService     = NULL;

    public static function get() {
        if (!self::$manager) {
            self::$manager = new self;
        }
        if(get_option('hid_roles_use_sa') === 'on') {
            self::$accessToken  = hyperIdClientController::getSaAccessToken();
            self::$isService    = true;
        } else {
            self::$accessToken  = hyperIdClientController::getAuthAccessToken();
            self::$isService    = false;
        }
        return self::$manager;
    }

    function getRoles() {
        $roles = get_transient(self::GET_ROLES_CACHE_NAME);
        if(false !== $roles)  return $roles;

        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                $width = '70%';
                printf('<div class="%1$s" style="margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('errorNote'), $width, 'Need admin authorization');
            } else {
                $width = '70%';
                printf('<div class="%1$s" style="margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('errorNote'), $width, 'Service Account Config not valid');
            }
            return array();
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;
        $response = $roleManager->rolesGet(self::$accessToken, self::$isService);
        if($response['result'] == RolesGetResult::SUCCESS) set_transient(self::GET_ROLES_CACHE_NAME, $response, 60);
        return $response;
    }

    function roleCreate() {
        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                hyperIdClientController::errorMsg("Need admin authorization first");
            } else {
                hyperIdClientController::errorMsg("Service Account config not valid");
            }
            return;
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;
        $roleName   = $_POST['roleName'];
        $response   = $roleManager->roleCreate(self::$accessToken, $roleName, self::$isService);
        if($response['result'] == RoleCreateResult::SUCCESS) {
            delete_transient(self::GET_ROLES_CACHE_NAME);
        }
        $data = array('operation' => 'roleCreate', 'response' => $response);
        update_option('hid_role_operation_result', $data);
    }

    function roleDelete() {
        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                hyperIdClientController::errorMsg("Need admin authorization first");
            } else {
                hyperIdClientController::errorMsg("Service Account config not valid");
            }
            return;
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;
        $roleId = $_POST['roleId'];
        $response   = $roleManager->roleDelete(self::$accessToken, $roleId, self::$isService);
        if($response['result'] == RoleDeleteResult::SUCCESS) {
            delete_transient(self::GET_ROLES_CACHE_NAME);
        }
        $data = array('operation' => 'roleDelete', 'response' => $response);
        update_option('hid_role_operation_result', $data);
    }

    function userAttachToRole() {
        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                hyperIdClientController::errorMsg("Need admin authorization first");
            } else {
                hyperIdClientController::errorMsg("Service Account config not valid");
            }
            return;
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;

        $roleId     = $_POST['roleId'];
        $userEmail  = $_POST['userEmail'];

        $user = get_user_by('email', $userEmail);

        if(!$user) return;

        $hidUsers = get_users(array(
            'meta_key' => 'hid_user_id'
        ));
        $usersHidData = array();
        foreach($hidUsers as $hidUser) {
            $hidId                      = get_user_meta($hidUser->ID, 'hid_user_id', true);
            $hidEmail                   = $hidUser->data->user_email;
            $usersHidData[$hidEmail]    = $hidId;
        }

        $hidUserId  = isset($usersHidData[$userEmail]) ? $usersHidData[$userEmail] : '';
        $response   = $roleManager->userRoleAttach(self::$accessToken, $hidUserId, $userEmail, $roleId, self::$isService);
        if($response['result'] == UserRoleAttachResult::SUCCESS) {
            $userId = $response['user_id'];
            update_user_meta($user->ID, 'hid_user_id', $userId);
        }
        $data = array('operation' => 'userAttach', 'response' => $response);
        update_option('hid_role_operation_result', $data);

        if(hyperIdClientController::getAuth()->getUserInfo()->user_email == $userEmail) hyperIdClientController::refreshAuthTokens();
    }

    function userDetachFromRole() {
        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                hyperIdClientController::errorMsg("Need admin authorization first");
            } else {
                hyperIdClientController::errorMsg("Service Account config not valid");
            }
            return;
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;

        $roleId     = $_POST['roleId'];
        $hidUserId  = $_POST['userId'];
        $response   = $roleManager->userRoleDetach(self::$accessToken, $hidUserId, $roleId, self::$isService);
        $data = array('operation' => 'userDetach', 'response' => $response);
        update_option('hid_role_operation_result', $data);

        if(hyperIdClientController::getAuth()->getUserInfo()->user_id == $hidUserId) hyperIdClientController::refreshAuthTokens();
    }

    function usersByRoleGet(string $roleId, int $pageNumber) {
        if(empty(self::$accessToken)) {
            if(get_option('hid_roles_use_sa') !== 'on') {
                $width = '70%';
                printf('<div class="%1$s" style="margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('errorNote'), $width, 'Need admin authorization');
            } else {
                $width = '70%';
                printf('<div class="%1$s" style="margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('errorNote'), $width, 'Need admin authorization');
            }
            return;
        }
        $roleManager = hyperIdClientController::getRoleManager();
        if(!$roleManager) return;
        return $roleManager->usersByRoleGet(self::$accessToken, $roleId, $pageNumber*self::USERS_PER_PAGE, self::USERS_PER_PAGE, self::$isService);
    }
}