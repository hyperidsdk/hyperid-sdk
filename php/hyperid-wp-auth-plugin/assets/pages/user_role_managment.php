<?php

require_once __DIR__.'/../hyperid/base/enum.php';

function hidUserRoleManagment() {
    $roleId     = isset($_GET['roleId'])    ? $_GET['roleId']               : "";
    $roleName   = isset($_GET['roleName'])  ? $_GET['roleName']             : "";
    $pageNumber = isset($_GET['pageNumber'])? intval($_GET['pageNumber'])   : 0;
    $usersCount = 0;
?>
    <div class="card_row">
        <div class="card">
            <?php
                if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'userAttach') {
                    $response = get_option('hid_role_operation_result')['response'];
                    if($response['result'] == UserRoleAttachResult::SUCCESS) {
                        echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>User attached successfully</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while user attach: {$response['result']->name}</p>";
                    }
                    delete_option('hid_role_operation_result');
                }
                if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'userDetach') {
                    $response = get_option('hid_role_operation_result')['response'];
                    if($response['result'] == UserRoleDetachResult::SUCCESS) {
                        echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>User detached successfully</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while user detach: {$response['result']->name}</p>";
                    }
                    delete_option('hid_role_operation_result');
                }
            ?>
            <h3>Role name: <?php echo "<b style='color: #5da2b0;'>$roleName</b>" ?></h3>
            <h3>Role ID: <?php echo "<b style='color: #5da2b0;'>$roleId</b>" ?></h3>
            <hr style="margin-top: 10px; margin-bottom: 10px; color: black">
            <button class="button-custom" id="userAttachBtn" onclick="showUserAttach();" style="width: 0%;">
                Attach user
            </button>
            <div id="userAttachPopup" style="display: none">
                <form id="hidConfig" method="post" action="" style="font-size: 16px;">
                    <input type="hidden" name="action" value="hidUserAttach" />
                    <input type="hidden" name="roleId" value="<?php echo $roleId ?>"/>
                    <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                    User Email: 
                    <input type="text" id="userEmail" name="userEmail"/>
                    <input class="button-custom" type="button" onclick="resetUserAttach();" value="Cancel"/>
                    <input class="button-custom" type="submit" value="Attach"/>
                </form>
            </div>
            <h3>Users</h3>
            <?php
                if(!empty($roleId)) {
                    $usersCount = echoUsers(RolesApi::get()->usersByRoleGet($roleId, $pageNumber), $roleId);
                }
            ?>
            <?php if($pageNumber != 0 || $usersCount == RolesApi::USERS_PER_PAGE) {?>
            <div style="margin: auto; width: fit-content;">
                <?php if($pageNumber != 0) {?>
                    <form method='get' action='' style='display: inline-block;'>
                        <input type='hidden' name='page'        value='HyperID client configuration for authentication' />
                        <input type='hidden' name='tab'         value='hidUserRoleManagment' />
                        <input type='hidden' name='roleId'      value='<?php echo $roleId ?>' />
                        <input type='hidden' name='roleName'    value='<?php echo $roleName ?>' />
                        <input type='hidden' name='pageNumber'  value='<?php echo ($pageNumber-1) ?>' />
                        <button type='submit'><</button>
                    </form>
                <?php } ?>
                <p style='display: inline-block;'><?php echo $pageNumber ?></p>
                <?php if($usersCount == RolesApi::USERS_PER_PAGE) {?>
                    <form method='get' action='' style='display: inline-block;'>
                        <input type='hidden' name='page'        value='HyperID client configuration for authentication' />
                        <input type='hidden' name='tab'         value='hidUserRoleManagment' />
                        <input type='hidden' name='roleId'      value='<?php echo $roleId ?>' />
                        <input type='hidden' name='roleName'    value='<?php echo $roleName ?>' />
                        <input type='hidden' name='pageNumber'  value='<?php echo ($pageNumber+1) ?>' />
                        <button type='submit'>></button>
                    </form>
                <?php } ?>
            </div>
            <?php } ?>
            <hr style="margin-top: 10px; margin-bottom: 10px; color: black">
            <form method='get' action='' style="display: inline-block">
                <input type='hidden' name='page' value='HyperID client configuration for authentication' />
                <input type='hidden' name='tab' value='hidRoleManagment' />
                <input class='button-custom' type='submit' value='Return To Roles' style='margin-top: 0px'/>
            </form>
        </div>
    </div>
<script>
    document.addEventListener("DOMContentLoaded", async () => {
        let userEmail = document.getElementById("userEmail");
        userEmail.addEventListener('input', () => {
            sortEmails();
        });
    });

    function showUserAttach() {
        let userAttachPopup = document.getElementById("userAttachPopup");
        let userAttachBtn   = document.getElementById("userAttachBtn");
        userAttachPopup.style.display = "block"
        userAttachBtn.style.display = "none";
    }

    function resetUserAttach() {
        let userAttachPopup = document.getElementById("userAttachPopup");
        let userAttachBtn   = document.getElementById("userAttachBtn");
        let userEmail       = document.getElementById("userEmail");
        userAttachPopup.style.display = "none"
        userAttachBtn.style.display = "inline-block";
        userEmail.value = "";
    }
</script>

<?php
}

function echoUsers($usersGetResponse, $roleId) {
    $result     = $usersGetResponse['result'];
    $usersIdList= $result == UsersByRoleGetResult::SUCCESS ? $usersGetResponse['user_ids'] : array();
    if(!empty($usersIdList)) {
        $htmlOutput         = "";
        $usersFoundCount    = 0;
        $htmlOutput.='<table class="rolesTable" width="100%">';
        $htmlOutput.='<tr>';
        $htmlOutput.="<td width='60%' style='background-color:#5da2b0;'>HID User Id</td><td width='20%' style='background-color:#5da2b0;'>User Email</td>";
        $htmlOutput.='</tr>';
        $users = get_users(array(
            'meta_key' => 'hid_user_id'
        ));
        $usersHidData = array();
        foreach($users as $user) {
            $hidId          = get_user_meta($user->ID, 'hid_user_id', true);
            $hidEmail       = $user->data->user_email;
            $usersHidData[$hidId] = $hidEmail;
        }
        foreach($usersIdList as $hidUserId) {
            if(isset($usersHidData[$hidUserId])) {
                $htmlOutput.='<tr>';
                $htmlOutput.="<td>$hidUserId</td>";
                $email = $usersHidData[$hidUserId];
                $htmlOutput.="<td>$email</td>";
                $htmlOutput.="
                    <td width='20%' style='background:transparent;'>
                        <form method='post' action=''>
                            <input type='hidden' name='action' value='hidUserDetach' />
                            <input type='hidden' name='roleId' value='$roleId' />
                            <input type='hidden' name='userId' value='$hidUserId' />
                            <input class='button-custom' type='submit' value='User Detach' style='margin-top: 0px'/>
                        </form>
                    </td>
                    ";
                $htmlOutput.='</tr>';
                $usersFoundCount++;
            }
        }
        $htmlOutput.='</table>';
        if($usersFoundCount == 0) {
            echo '<p>No users match those in database</p>';
        } else {
            echo $htmlOutput;
        }
    } else if($result == UsersByRoleGetResult::SUCCESS) {
        echo '<p>No user attached</p>';
    } else {
        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while users get: {$result->name}</p>";
    }
    return count($usersIdList);
}

?>