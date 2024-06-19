<?php

if ( !defined( 'WP_UNINSTALL_PLUGIN' ) ) 
    exit();

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

?>