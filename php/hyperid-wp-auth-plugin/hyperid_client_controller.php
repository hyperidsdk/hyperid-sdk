<?php
/**
 * @package HyperID_Client
 * @version 0.0.1
 */
/*
Plugin Name: HyperID login for WordPress.
Description: Login and authenticate Wordpress users using HyperID
Author: HyperID team
Version: 0.0.1
Author URI: https://hypersecureid.com/
*/

// Layout
require_once __DIR__.'/assets/pages/navigation.php';

// API
require_once __DIR__.'/assets/hyperid/auth/auth.php';
require_once __DIR__.'/assets/hyperid/service/service.php';
require_once __DIR__.'/assets/hyperid/role_manager/role_manager.php';

require_once('role_api.php');

require_once(ABSPATH . 'wp-admin/includes/plugin.php');

define('hyperID', true);

class hyperIdClientController
{
    protected static $controller                = NULL;
    protected static ?Auth          $auth       = NULL;
    protected static ?Service       $service    = NULL;
    protected static ?RoleManager   $roleManager= NULL;

    public static function getController() {
        if (!self::$controller) {
            self::$controller = new self;
        }
        return self::$controller;
    }

    public static function setAuth($refreshToken = '') {
        $clientInfo = null;
        if(get_option('hid_client_id')
            && get_option('hid_client_authorization') != '-1'
            && get_option('hid_client_secret')
        ) {
            $clientAuthorization = get_option('hid_client_authorization');
            switch(intval($clientAuthorization)) {
                case(AuthorizationMethod::BASIC->value) :
                    $clientInfo = new ClientInfoBasic(get_option('hid_client_id'), get_option('hid_client_secret'), home_url());
                    break;
                case(AuthorizationMethod::HS256->value) :
                    $clientInfo = new ClientInfoHS256(get_option('hid_client_id'), get_option('hid_client_secret'), home_url());
                    break;
                case(AuthorizationMethod::RS256->value) :
                    $clientInfo = new ClientInfoRS256(get_option('hid_client_id'), get_option('hid_client_secret'), home_url());
                    break;
            }
        }
        if($clientInfo && $clientInfo->isValid() && get_option('hid_infrastructure') && get_option('hid_user_authorization') != '-1') {
            $infrastructure = get_option('hid_infrastructure');
            switch($infrastructure) {
                case(InfrastructureType::SANDBOX->value) :
                    try {
                        self::$auth = new Auth($clientInfo, $refreshToken, InfrastructureType::SANDBOX);
                    } catch(Exception $e) {
                        self::$auth = new Auth($clientInfo, '', InfrastructureType::SANDBOX);
                    }
                    break;
                case(InfrastructureType::PRODUCTION->value) :
                    try {
                        self::$auth = new Auth($clientInfo, $refreshToken, InfrastructureType::PRODUCTION);
                    } catch(Exception $e) {
                        self::$auth = new Auth($clientInfo, '', InfrastructureType::PRODUCTION);
                    }
                    break;
            }
        }
        if(self::$auth != null) {
            update_option(wp_get_current_user()->ID.'hid_auth', self::$auth);
        }
    }

    public static function getAuth($refreshToken = '') {
        if (!self::$auth) {
            $auth = get_option(wp_get_current_user()->ID.'hid_auth');
            if($auth) self::$auth = $auth;
        }
        if (!self::$auth) {
            self::setAuth($refreshToken);
        }
        return self::$auth;
    }

    public static function setService($refreshToken = '') {
        $serviceInfo = null;
        if(get_option('hid_sa_client_id')
            && get_option('hid_sa_client_authorization') != '-1'
            && get_option('hid_sa_client_secret')
        ) {
            $serviceAuthorization = get_option('hid_sa_client_authorization');
            $isSelfSigned = get_option('hid_sa_self_signed');
            switch(intval($serviceAuthorization)) {
                case(AuthorizationMethod::BASIC->value) :
                    $isSelfSigned == 'on'
                        ? $serviceInfo = new ServiceInfoSelfSignedHS256(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'))
                        : $serviceInfo = new ServiceInfoBasic(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'));
                    break;
                case(AuthorizationMethod::HS256->value) :
                    $isSelfSigned == 'on'
                        ? $serviceInfo = new ServiceInfoSelfSignedHS256(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'))
                        : $serviceInfo = new ServiceInfoHS256(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'));
                    break;
                case(AuthorizationMethod::RS256->value) :
                    $isSelfSigned == 'on'
                        ? $serviceInfo = new ServiceInfoSelfSignedRS256(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'))
                        : $serviceInfo = new ServiceInfoRS256(get_option('hid_sa_client_id'), get_option('hid_sa_client_secret'));
                    break;
            }
        }
        if($serviceInfo && $serviceInfo->isValid() && get_option('hid_infrastructure')) {
            $infrastructure = get_option('hid_infrastructure');
            switch($infrastructure) {
                case(InfrastructureType::SANDBOX->value) :
                    try {
                        self::$service = new Service($serviceInfo, $refreshToken, InfrastructureType::SANDBOX);
                    } catch(Exception $e) {
                        self::$service = new Service($serviceInfo, '', InfrastructureType::SANDBOX);
                    }
                    break;
                case(InfrastructureType::PRODUCTION->value) :
                    try {
                        self::$service = new Service($serviceInfo, $refreshToken, InfrastructureType::PRODUCTION);
                    } catch(Exception $e) {
                        self::$service = new Service($serviceInfo, '', InfrastructureType::PRODUCTION);
                    }
                    break;
            }
        }
        if(self::$service != null) {
            update_option('hid_service', self::$service);
        }
    }

    public static function getService($refreshToken = '') {
        if (!self::$service) {
            $service = get_option('hid_service');
            if($service) self::$service = $service;
        }
        if (!self::$service) {
            self::setService($refreshToken);
            if(!empty($refreshToken) && self::$service != null) {
                update_option('hid_sa_access_token',  self::$service->getAccessToken());
                update_option('hid_sa_refresh_token', self::$service->getRefreshToken());
            }
        }
        return self::$service;
    }

    public static function getRoleManager() {
        if (!self::$roleManager) {
            $apiUrl     = "";
            if(get_option('hid_roles_use_sa') === 'on') {
                $service = self::getService(get_option('hid_sa_refresh_token'));
                if(!$service) return;
                $apiUrl = $service->getDiscoverConfiguration()->restApiTokenEndpoint;
            } else {
                $auth = self::getAuth();
                if(!$auth) return;
                $apiUrl = $auth->getDiscoverConfiguration()->restApiTokenEndpoint;
            }
            self::$roleManager = new RoleManager($apiUrl);
        }
        return self::$roleManager;
    }

    public function __construct() {
        add_action('admin_menu',    array($this, 'addMenuPage'));
        add_action('init',          array($this, 'performeAction'));
        add_action('login_form',    array($this, 'loginWithHIDButton'));
        add_action('wp_logout',     array($this, 'logoutFromHID'));
        register_uninstall_hook(__FILE__, 'deletePluginDB');

        if(!get_option('hid_login_types_to_show_web2')) update_option('hid_login_types_to_show_web2',   'on');
        if(!get_option('hid_login_types_to_show_web3')) update_option('hid_login_types_to_show_web3',   'on');
        if(!get_option('hid_login_types_to_show_idps')) update_option('hid_login_types_to_show_idps',   'on');
        if(!get_option('hid_auth_select_account'))      update_option('hid_auth_select_account',        'on');
    }

    function addMenuPage() {
        add_menu_page('HyperIdClient',
                      'HyperID Client',
                      'manage_options',
                      'HyperID client configuration for authentication',
                      'hidNavigation',
                      'data:image/svg+xml;base64,' . base64_encode(file_get_contents(__DIR__.'/assets/logo.svg')));
    }

    function performeAction() {
        if (isset($_GET['hidLoginAction']) && 'hidLogin' == $_GET['hidLoginAction'] || isset($_POST['action']) && 'hidConfigTest' == $_POST['action']) {
            self::performeTestLogin();
            return;
        }

        if (isset($_GET['code'])) {
            self::performeLogin();
            return;
        }

        if (self::isSiteAdmin() && isset($_POST['action'])) {
            if ($_POST['action'] == 'hidConfig') {
                self::saveClientConfig();
                return;
            }
            if ($_POST['action'] == 'hidSaConfig') {
                self::saveSaConfig();
                return;
            }
            if ($_POST['action'] == 'hidRolesConfig') {
                self::saveRolesConfig();
                return;
            }
            if ($_POST['action'] == 'hidRoleCreate' && isset($_POST['roleName'])) {
                RolesApi::get()->roleCreate();
                return;
            }
            if ($_POST['action'] == 'hidRoleDelete' && isset($_POST['roleId'])) {
                RolesApi::get()->roleDelete();
                return;
            }
            if ($_POST['action'] == 'hidUserAttach' && isset($_POST['roleId']) && isset($_POST['userEmail'])) {
                RolesApi::get()->userAttachToRole();
                return;
            }
            if ($_POST['action'] == 'hidUserDetach' && isset($_POST['roleId']) && isset($_POST['userId'])) {
                RolesApi::get()->userDetachFromRole();
                return;
            }
        }
    }

    function performeTestLogin() {
        $sessionState = 'hidLogin';
        if(isset($_POST['action']) && $_POST['action'] == 'hidConfigTest') {
            self::logoutFromHID(wp_get_current_user()->ID);
            $sessionState = 'hidLoginConfigTest';
        }
        if (session_id() == '' || !isset($session) && !headers_sent())
            session_start();

        $userAuthType       = AuthorizationFlowMode::from(intval(get_option('hid_user_authorization')));
        $walletGetMode      = get_option('hid_wallet_get_mode') ? WalletGetMode::from(intval(get_option('hid_wallet_get_mode'))) : null;
        $walletFamily       = get_option('hid_wallet_family') ? WalletFamily::from(intval(get_option('hid_wallet_family'))) : null;
        $identityProvider   = get_option('hid_idp_for_login');
        $selectAccount      = get_option('hid_auth_select_account') == 'on';

        $authUrl = self::getAuth()->getAuthUrl($userAuthType,
                                               $walletGetMode,
                                               $walletFamily,
                                               null,
                                               $identityProvider,
                                               $selectAccount);
        $loginTypesToShow = '';
        if(get_option('hid_login_types_to_show_web2') == 'on') $loginTypesToShow .= 'web2 ';
        if(get_option('hid_login_types_to_show_web3') == 'on') $loginTypesToShow .= 'web3 ';
        if(get_option('hid_login_types_to_show_idps') == 'on') $loginTypesToShow .= 'idp ';
        $loginTypesToShow = trim($loginTypesToShow);
        if($loginTypesToShow) {
            $authUrl .= '&login_types_to_show=' . $loginTypesToShow;
        }
        if(get_option('hid_idps_list')) {
            $authUrl .= '&idp_show=' . get_option('hid_idps_list');
        }
        if(get_option('hid_idps_list_order')) {
            $authUrl .= '&idp_order=' . get_option('hid_idps_list_order');
        }
        header('Location: ' . $authUrl . '&state=' . $sessionState);
        exit;
    }

    function performeLogin() {
        try {
            self::getAuth()->exchangeAuthCodeToToken($_GET['code']);
        } catch (Exception $e) {
            self::errorMsg("Exchange code to token raised exception : ".$e);
            return;
        }
        $userInfo = self::getAuth()->getUserInfo();
        
        $users = get_users(array(
            'meta_key'      => 'hid_user_id',
            'meta_value'    => $userInfo->user_id,
        ));
        if($users != null) {
            wp_set_current_user($users[0]->data->{'ID'});
            wp_set_auth_cookie($users[0]->data->{'ID'});
            $user  = get_user_by('ID', $users[0]->data->{'ID'});
            do_action('wp_login', $user->user_login, $user);
        } else {
            global $wpdb;
            $userId = $wpdb->get_var($wpdb->prepare("SELECT * FROM $wpdb->users WHERE user_email= %s", $userInfo->user_email));
            $wpUserInfo = array();
            $wpUserInfo['user_email']   = $userInfo->user_email;
            $wpUserInfo['user_login']   = $userInfo->user_email;

            if ($userId) {
                $wpUserInfo['ID'] = $userId;
                $user = wp_update_user($wpUserInfo);
            } else {
                $wpUserInfo['user_pass'] =  '';
                $user = wp_insert_user($wpUserInfo);
            }
            update_user_meta($user, 'hid_user_id', $userInfo->user_id);

            wp_set_current_user($user);
            wp_set_auth_cookie($user);
            $user  = get_user_by('ID', $user);
            do_action('wp_login', $user->user_login, $user);
        }
        update_user_meta(wp_get_current_user()->ID, 'hid_user_is_guest',            self::getAuth()->getUserInfo()->is_guest);
        update_user_meta(wp_get_current_user()->ID, 'hid_user_access_token',        self::getAuth()->getAccessToken());
        update_user_meta(wp_get_current_user()->ID, 'hid_user_refresh_token',       self::getAuth()->getRefreshToken());
        if(self::getAuth()->getUserInfo()->wallet) {
            update_user_meta(wp_get_current_user()->ID, 'hid_user_wallet_address',      self::getAuth()->getUserInfo()->wallet->wallet_address);
            update_user_meta(wp_get_current_user()->ID, 'hid_user_wallet_is_verified',  self::getAuth()->getUserInfo()->wallet->is_wallet_verified);
        }
        if(isset($_GET['state']) && $_GET['state'] == 'hidLoginConfigTest') {
            update_option('hid_auth_tested', $user->user_email);
            wp_redirect(admin_url('admin.php?page=HyperID+client+configuration+for+authentication'));
        } else {
            wp_redirect(home_url());
        }
        update_option(wp_get_current_user()->ID.'hid_auth', self::$auth);
        exit;
    }

    function saveClientConfig() {
        if (isset($_POST['HIDConfigNonce']) && !empty($_POST['HIDConfigNonce']) && wp_verify_nonce(sanitize_key($_POST['HIDConfigNonce']), 'HIDConfigNonce')) {
            if(!isset($_POST['serverType']) || sanitize_text_field($_POST['serverType']) == '-1') {
                self::errorMsg('Please choose correct HyperID infrastructure.');
                return;
            }
            if(!isset($_POST['clientId']) || sanitize_text_field($_POST['clientId']) == '') {
                self::errorMsg('Please type correct HyperID clientId.');
                return;
            }
            if(!isset($_POST['clientAuthType']) || sanitize_text_field($_POST['clientAuthType']) == '-1') {
                self::errorMsg('Please choose correct client authorization method.');
                return;
            }
            if(!isset($_POST['clientSecret']) || sanitize_text_field($_POST['clientSecret']) == '') {
                self::errorMsg('Please type correct HyperID client secret.');
                return;
            }
            if(!isset($_POST['userAuthMethod']) || sanitize_text_field($_POST['userAuthMethod']) == '-1') {
                self::errorMsg('Please choose correct authorization method.');
                return;
            }

            update_option('hid_infrastructure',         isset($_POST['serverType'])     ? sanitize_text_field($_POST['serverType'])     : '');
            update_option('hid_client_id',              isset($_POST['clientId'])       ? sanitize_text_field($_POST['clientId'])       : '');
            update_option('hid_client_authorization',   isset($_POST['clientAuthType']) ? sanitize_text_field($_POST['clientAuthType']) : '');
            update_option('hid_client_secret',          isset($_POST['clientSecret'])   ? sanitize_text_field($_POST['clientSecret'])   : '');
            update_option('hid_user_authorization',     isset($_POST['userAuthMethod']) ? sanitize_text_field($_POST['userAuthMethod']) : '');

            // Additional Options
            update_option('hid_wallet_get_mode',    isset($_POST['walletGetMode'])      && $_POST['walletGetMode'] != '-1'      ? sanitize_text_field($_POST['walletGetMode'])      : '');
            update_option('hid_wallet_family',      isset($_POST['walletFamily'])       && $_POST['walletFamily'] != '-1'       ? sanitize_text_field($_POST['walletFamily'])       : '');
            update_option('hid_idp_for_login',      isset($_POST['idpForLogin'])                                                ? sanitize_text_field($_POST['idpForLogin'])        : '');

            update_option('hid_login_types_to_show_web2',   isset($_POST['web2'])           ? 'on'  : 'off');
            update_option('hid_login_types_to_show_web3',   isset($_POST['web3'])           ? 'on'  : 'off');
            update_option('hid_login_types_to_show_idps',   isset($_POST['idps'])           ? 'on'  : 'off');
            update_option('hid_auth_select_account',        isset($_POST['selectAccount'])  ? 'on'  : 'off');

            update_option('hid_idps_list',          isset($_POST['idpsListToShow']) && $_POST['idpsListToShow'] != 'Select options' ? sanitize_text_field($_POST['idpsListToShow'])         : '');
            update_option('hid_idps_list_order',    isset($_POST['idpsListToShowInOrder'])                                          ? sanitize_text_field($_POST['idpsListToShowInOrder'])  : '');

            self::setAuth();
            if(self::getAuth()) {
                update_option('hid_auth_saved', 'saved');
                self::successMsg('Successfully saved the HyperID configuration.');
                update_option('hid_auth_idps_total', self::getAuth()->getDiscoverConfiguration()->identityProviders);
            } else {
                self::errorMsg('Please fill up all fields in config.');
            }
        }
    }

    function saveSaConfig() {
        if (isset($_POST['HIDConfigNonce']) && !empty($_POST['HIDConfigNonce']) && wp_verify_nonce(sanitize_key($_POST['HIDConfigNonce']), 'HIDConfigNonce')) {
            if(get_option('hid_infrastructure') === '' || get_option('hid_infrastructure') === '-1') {
                self::errorMsg('Please fill HyperId Infrastructure on Auth Configuration Tab.');
                return;
            }
            if(!isset($_POST['saClientId']) || sanitize_text_field($_POST['saClientId']) == '') {
                self::errorMsg('Please type correct Service Account clientId.');
                return;
            }
            if(!isset($_POST['saClientAuthType']) || sanitize_text_field($_POST['saClientAuthType']) == '-1') {
                self::errorMsg('Please choose correct Service Account authorization method.');
                return;
            }
            if(!isset($_POST['saClientSecret']) || sanitize_text_field($_POST['saClientSecret']) == '') {
                self::errorMsg('Please type correct Service Account client secret.');
                return;
            }

            update_option('hid_sa_client_id',           isset($_POST['saClientId'])         ? sanitize_text_field($_POST['saClientId'])         : '');
            update_option('hid_sa_self_signed',         isset($_POST['saSelfSigned'])       ? 'on'                                              : 'off');
            update_option('hid_sa_client_authorization',isset($_POST['saClientAuthType'])   ? sanitize_text_field($_POST['saClientAuthType'])   : '');
            update_option('hid_sa_client_secret',       isset($_POST['saClientSecret'])     ? sanitize_text_field($_POST['saClientSecret'])     : '');

            self::setService();
            if(self::getService()) {
                update_option('hid_service_saved', 'saved');
                self::successMsg('Successfully saved the HyperID Service Account configuration.');
                update_option('hid_sa_access_token',  self::getService()->getAccessToken());
                update_option('hid_sa_refresh_token', self::getService()->getRefreshToken());
            } else {
                self::errorMsg('Please fill up all fields in config.');
            }
        }
    }

    function saveRolesConfig() {
        if (isset($_POST['HIDConfigNonce']) && !empty($_POST['HIDConfigNonce']) && wp_verify_nonce(sanitize_key($_POST['HIDConfigNonce']), 'HIDConfigNonce')) {
            update_option('hid_roles_use_sa', isset($_POST['rolesUseSA']) ? 'on' : 'off');

            if(self::getRoleManager()) {
                update_option('hid_role_manadger_saved', 'saved');
                self::successMsg('Successfully saved the HyperID Role Manager configuration.');
            } else {
                self::errorMsg('Please fill up all fields in config.');
            }
        }
    }

    public static function refreshAuthTokens() {
        try {
            self::getAuth(self::getAuthRefreshToken())->refreshTokens();
            update_option(wp_get_current_user()->ID.'hid_auth', self::$auth);
        } catch(Exception $e) {
        } catch(Error $e) {
        }
    }

    public static function getAuthRefreshToken() {
        try {
            $userId = wp_get_current_user()->ID;
            return get_user_meta($userId, 'hid_user_refresh_token', true);
        } catch(Exception $e) {
            return "";
        } catch(Error $e) {
            return "";
        }
    }

    public static function getAuthAccessToken() {
        try {
            $userId = wp_get_current_user()->ID;
            return get_user_meta($userId, 'hid_user_access_token', true);
        } catch(Exception $e) {
            return "";
        } catch(Error $e) {
            return "";
        }
    }

    public static function refreshSaTokens() {
        try {
            self::getService(get_option('hid_sa_refresh_token'))->refreshTokens();
        } catch(Exception $e) {
        } catch(Error $e) {
        }
    }

    public static function getSaRefreshToken() {
        try {
            return self::getService(get_option('hid_sa_refresh_token'))->getRefreshToken();
        } catch(Exception $e) {
            return "";
        } catch(Error $e) {
            return "";
        }
    }

    public static function getSaAccessToken() {
        try {
            return self::getService(get_option('hid_sa_refresh_token'))->getAccessToken();
        } catch(Exception $e) {
            return "";
        } catch(Error $e) {
            return "";
        }
    }

    public static function successMsg($message) {
        $width = '70%';
        printf('<div class="%1$s " style="margin-left:13rem;margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('infoNote'), $width, esc_html($message));
    }

    public static function errorMsg($message) {
        $width = '70%';
        printf('<div class="%1$s" style="margin-left:13rem; margin-top:2rem; width:%2$s;"><p>%3$s</p></div>', esc_attr('errorNote'), $width, esc_html($message));
    }

    function loginWithHIDButton() {
        try {
            if(self::getAuth()) {
            ?>
            <a style="color:#FFF; width:100%; text-align:center; margin-bottom:1em;" class="button button-primary" href="<?php echo site_url('?hidLoginAction=hidLogin'); ?>"><?php echo esc_html('HyperID Sign In'); ?></a>
            <div style="clear:both;"></div>
            <?php
            }
        } catch(Exception $e) {
        }
    }

    function logoutFromHID($userId) {
        try {
            $auth = self::getAuth(self::getAuthRefreshToken());
            if($auth) {
                $auth->logout();
                delete_option(wp_get_current_user()->ID.'hid_auth');
            }
        } catch(Exception $e) {
        }
    }

    function isSiteAdmin() {
        return in_array('administrator', wp_get_current_user()->roles);
    }

    function deletePluginDB() {
        delete_option('hid_infrastructure');
        delete_option('hid_client_id');
        delete_option('hid_client_authorization');
        delete_option('hid_client_secret');
        delete_option('hid_user_authorization');
        delete_option('hid_wallet_get_mode');
        delete_option('hid_wallet_family');
        delete_option('hid_idp_for_login');
        delete_option('hid_idps_list');
        delete_option('hid_idps_list_order');
        delete_option('hid_login_types_to_show_web2');
        delete_option('hid_login_types_to_show_web3');
        delete_option('hid_login_types_to_show_idps');
        delete_option('hid_auth_select_account');
        delete_option('hid_auth_saved');
        delete_option('hid_auth_tested');
        
        delete_option('hid_sa_client_id');
        delete_option('hid_sa_self_signed');
        delete_option('hid_sa_client_authorization');
        delete_option('hid_sa_client_secret');
        delete_option('hid_sa_access_token');
        delete_option('hid_sa_refresh_token');
        delete_option('hid_service_saved');

        delete_option('hid_roles_use_sa');
        delete_option('hid_role_manadger_saved');
        delete_option('hid_role_operation_result');

        delete_option('hid_auth_idps_total');
    }
}

$HyperIdController = hyperIdClientController::getController();