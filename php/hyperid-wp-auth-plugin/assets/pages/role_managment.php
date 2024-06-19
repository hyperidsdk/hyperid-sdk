<?php

require_once __DIR__.'/../hyperid/base/enum.php';

function hidRoleManagment() {
?>
    <div class="card_row">
        <div class="card">
            <form id="hidConfig" method="post" action="" style="font-size: 16px;">
                <input type="hidden" name="action" value="hidRolesConfig" />
                <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                Use Service Account for auth
                <input type="checkbox" id="rolesUseSA" name="rolesUseSA" <?php if(get_option('hid_roles_use_sa') == 'on') { ?>checked <?php } ?>/>
                <input class="button-custom" type="submit" id="clientconfig" value="Save" />
            </form>
            <hr style="margin-top: 10px; margin-bottom: 10px; color: black">
            <h3>Roles</h3>
            <button class="button-custom" id="createRoleBtn" onclick="showCreteRole();" style="width: 0%;">
                Create Role
            </button>
            <?php
            if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'roleCreate') {
                $response = get_option('hid_role_operation_result')['response'];
                if($response['result'] == RoleCreateResult::SUCCESS) {
                    echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>Created role id: {$response['role_id']}</p>";
                } else {
                    echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while creating role: {$response['result']->name}</p>";
                }
                delete_option('hid_role_operation_result');
            }
            ?>
            <div class="roleCreatePopup" id="roleCreatePopup" style="display: none">
                <form id="hidConfig" method="post" action="" style="font-size: 16px;">
                    <input type="hidden" name="action" value="hidRoleCreate" />
                    <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                    Role Name: 
                    <input type="text" id="roleName" name="roleName"/>
                    <input class="button-custom" type="button" onclick="resetRoleCreate();" id="roleCreate" value="Cancel"/>
                    <input class="button-custom" type="submit" id="roleCreate" value="Create"/>
                </form>
            </div>
            <hr style="margin-top: 10px; margin-bottom: 10px; color: black">
            <?php
                if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'roleDelete') {
                    $response = get_option('hid_role_operation_result')['response'];
                    if($response['result'] == RoleDeleteResult::SUCCESS) {
                        echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>Role deleted</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while deleting role: {$response['result']->name}</p>";
                    }
                    delete_option('hid_role_operation_result');
                }
                try {
                    echoRoles(RolesApi::get()->getRoles());
                } catch(Exception $e) {
                    if($e instanceof AccessTokenExpiredException) {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Need Authorization</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>$e</p>";
                    }
                }
            ?>
        </div>
    </div>
<script>

    function showCreteRole() {
        let createRolePopup = document.getElementById("roleCreatePopup");
        let createRoleBtn   = document.getElementById("createRoleBtn");
        createRolePopup.style.display = "block"
        createRoleBtn.style.display = "none";
    }

    function resetRoleCreate() {
        let createRolePopup = document.getElementById("roleCreatePopup");
        let createRoleBtn   = document.getElementById("createRoleBtn");
        let roleName        = document.getElementById("roleName");
        createRolePopup.style.display = "none"
        createRoleBtn.style.display = "block";
        roleName.value = "";
    }

</script>

<?php
}

function echoRoles($rolesGetResponse) {
    if(empty($rolesGetResponse)) return;

    $result     = $rolesGetResponse['result'];
    $roleList   = $result == RolesGetResult::SUCCESS ? $rolesGetResponse['roles'] : array();
    if(!empty($roleList)) {
        echo '<h3>Role List</h3>';
        echo '<table class="rolesTable" style="width: 100%;">';
        echo '<tr>';
        echo "<td style='background-color:#5da2b0;'>Name</td><td style='background-color:#5da2b0;'>ID</td>";
        echo '</tr>';
        foreach ($roleList as $roleItem) {
            echo '<tr>';
            echo "<td>{$roleItem['name']}</td><td>{$roleItem['id']}</td>";
            echo "
                <td style='background:transparent;'>
                    <form method='get' action=''>
                        <input type='hidden' name='page' value='HyperID client configuration for authentication' />
                        <input type='hidden' name='tab' value='hidUserRoleManagment' />
                        <input type='hidden' name='roleId' value='{$roleItem['id']}' />
                        <input type='hidden' name='roleName' value='{$roleItem['name']}' />
                        <input class='button-custom' type='submit' id='usersManagment' value='Users Manage' style='margin-top: 0px'/>
                    </form>
                </td>
                <td style='background:transparent;'>
                    <form method='post' action=''>
                        <input type='hidden' name='action' value='hidRoleDelete' />
                        <input type='hidden' name='roleId' value='{$roleItem['id']}' />
                        <input class='button-custom' type='submit' id='roleDeleteBtn' value='Delete' style='margin-top: 0px'/>
                    </form>
                </td>
                ";
            echo '</tr>';
        }
        echo '</table>';
    }
}

?>