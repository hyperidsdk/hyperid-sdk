<?php

require_once 'config.php';
require_once 'sa_config.php';
require_once 'role_managment.php';
require_once 'user_role_managment.php';

function hidNavigation() {
    wp_enqueue_style("css_styles", plugins_url('css/layout.css', __FILE__));

    isset($_GET['tab']) ? $active_tab = sanitize_text_field($_GET['tab']) : $active_tab = 'hidClientConfig';
?>

    <div class="wrap">
        <h2 class="nav-tab-wrapper" style="border:none;">
            <a <?php if($active_tab !== 'hidClientConfig') {?> href="<?php echo esc_url_raw(add_query_arg(array('tab' => 'hidClientConfig', 'roleId' => null, 'roleName' => null),    $_SERVER['REQUEST_URI'])); ?>"<?php }?>
                class="nav-tab <?php echo $active_tab == 'hidClientConfig'   ? 'nav-tab-active' : ''; ?>"><?php echo esc_html('Authentication'); ?></a>
            <a <?php if($active_tab !== 'hidSAConfig') {?> href="<?php echo esc_url_raw(add_query_arg(array('tab' => 'hidSAConfig', 'roleId' => null, 'roleName' => null),   $_SERVER['REQUEST_URI'])); ?>"<?php }?>
                class="nav-tab <?php echo $active_tab == 'hidSAConfig'  ? 'nav-tab-active' : ''; ?>"><?php echo esc_html('Service'); ?></a>
            <a <?php if($active_tab !== 'hidRoleManagment') {?> href="<?php echo esc_url_raw(add_query_arg(array('tab' => 'hidRoleManagment', 'roleId' => null, 'roleName' => null),   $_SERVER['REQUEST_URI'])); ?>"<?php }?>
                class="nav-tab <?php echo $active_tab == 'hidRoleManagment' || $active_tab == 'hidUserRoleManagment' ? 'nav-tab-active' : ''; ?>"><?php echo esc_html('Role Managment'); ?></a>
        </h2>
    </div>

<?php
    if     ($active_tab === 'hidClientConfig')      hidClientConfig();
    else if($active_tab === 'hidSAConfig')          hidServiceAccountConfig();
    else if($active_tab === 'hidRoleManagment')     hidRoleManagment();
    else if($active_tab === 'hidUserRoleManagment') hidUserRoleManagment();
}

?>
