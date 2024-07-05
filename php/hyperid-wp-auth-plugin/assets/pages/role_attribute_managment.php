<?php

require_once __DIR__.'/../hyperid/base/enum.php';

function hidRoleAttributeManagment() {
    $roleId             = isset($_GET['roleId'])    ? $_GET['roleId']               : "";
    $roleName           = isset($_GET['roleName'])  ? $_GET['roleName']             : "";
    $pageNumber         = isset($_GET['pageNumber'])? intval($_GET['pageNumber'])   : 0;
    $attributesCount    = 0;
?>
    <div class="card_row">
        <div class="card">
            <div id="attributeEditOverlay" class="overlay-container">
                <div class="popup-box" id="attributeEditBox">
                    <input hidden type="text" id="attributeKeyEdit" name="attributeKeyEdit"/>
                    <textarea type="text" id="attributeValueEdit" name="attributeValueEdit" class="attributeValueEdit"></textarea>
                    <input class="button-custom" type="button" onclick="saveAttributeChange();" value="Save" style="display: block; margin:auto; margin-top: 8px"/>
                    <input class="button-custom" type="button" onclick="cancelAttributeChange();" value="Cancel" style="display: block; margin:auto; margin-top: 8px"/>
                </div>
            </div>
            <?php
                if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'attributeReplace') {
                    $response = get_option('hid_role_operation_result')['response'];
                    if($response['result'] == RoleAttributeReplaceResult::SUCCESS) {
                        echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>Attribute changed successfully</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while attribute change: {$response['result']->name}</p>";
                    }
                    delete_option('hid_role_operation_result');
                }
                if(get_option('hid_role_operation_result') && get_option('hid_role_operation_result')['operation'] == 'attributeDelete') {
                    $response = get_option('hid_role_operation_result')['response'];
                    if($response['result'] == RoleAttributeDeleteResult::SUCCESS) {
                        echo "<p class='infoNote' style='font-size:16px;margin-left:0px'>Attribute deleted successfully</p>";
                    } else {
                        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while attribute deleted: {$response['result']->name}</p>";
                    }
                    delete_option('hid_role_operation_result');
                }
            ?>
            <h3>Role name: <?php echo "<b style='color: #5da2b0;'>$roleName</b>" ?></h3>
            <h3>Role ID: <?php echo "<b style='color: #5da2b0;'>$roleId</b>" ?></h3>
            <hr style="margin-top: 10px; margin-bottom: 10px; color: black">
            <button class="button-custom" id="attributeCreateBtn" onclick="showAttributeCreate();" style="width: 0%;">
                Create Attribute
            </button>
            <div id="attributeCreatePopup" style="display: none">
                <form id="hidConfig" method="post" action="" style="font-size: 16px;">
                    <input type="hidden" name="action" value="hidRoleAttributeReplace" />
                    <input type="hidden" name="roleId" value="<?php echo $roleId ?>"/>
                    <input type="hidden" name="pageNumber" value="<?php echo $pageNumber ?>"/>
                    <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                    <p>Attribute Name:</p>
                    <input type="text" id="attributeKey" name="attributeKey"/>
                    <p>Attribute Value:</p>
                    <textarea type="text" id="attributeValue" name="attributeValue" class="attributeValueCreate"></textarea>
                    <br>
                    <input class="button-custom" type="button" onclick="resetAttributeCreate();" value="Cancel"/>
                    <input class="button-custom" type="submit" value="Create"/>
                </form>
            </div>
            <h3>Attributes</h3>
            <?php
                if(!empty($roleId)) {
                    $attributesCount = echoAttributes(RolesApi::get()->attributesGet($roleId, $pageNumber), $roleId);
                }
            ?>
            <?php if($pageNumber != 0 || $attributesCount == RolesApi::ATTRIBUTES_PER_PAGE) {?>
            <div style="margin: auto; width: fit-content;">
                <?php if($pageNumber != 0) {?>
                    <form method='get' action='' style='display: inline-block;'>
                        <input type='hidden' name='page'        value='HyperID client configuration for authentication' />
                        <input type='hidden' name='tab'         value='hidRoleAttributeManagment' />
                        <input type='hidden' name='roleId'      value='<?php echo $roleId ?>' />
                        <input type='hidden' name='roleName'    value='<?php echo $roleName ?>' />
                        <input type='hidden' name='pageNumber'  value='<?php echo ($pageNumber-1) ?>' />
                        <button type='submit' class="button-custom pageButton"><</button>
                    </form>
                <?php } ?>
                <p style='display: inline-block;'><?php echo $pageNumber ?></p>
                <?php if($attributesCount == RolesApi::ATTRIBUTES_PER_PAGE) {?>
                    <form method='get' action='' style='display: inline-block;'>
                        <input type='hidden' name='page'        value='HyperID client configuration for authentication' />
                        <input type='hidden' name='tab'         value='hidRoleAttributeManagment' />
                        <input type='hidden' name='roleId'      value='<?php echo $roleId ?>' />
                        <input type='hidden' name='roleName'    value='<?php echo $roleName ?>' />
                        <input type='hidden' name='pageNumber'  value='<?php echo ($pageNumber+1) ?>' />
                        <button type='submit' class="button-custom pageButton">></button>
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
    function showAttributeCreate() {
        let attributeCreatePopup = document.getElementById("attributeCreatePopup");
        let attributeCreateBtn   = document.getElementById("attributeCreateBtn");
        attributeCreatePopup.style.display  = "block"
        attributeCreateBtn.style.display    = "none";
    }

    function resetAttributeCreate() {
        let attributeCreatePopup    = document.getElementById("attributeCreatePopup");
        let attributeCreateBtn      = document.getElementById("attributeCreateBtn");
        let attributeKey            = document.getElementById("attributeKey");
        let attributeValue          = document.getElementById("attributeValue");
        attributeCreatePopup.style.display  = "none"
        attributeCreateBtn.style.display    = "inline-block";
        attributeKey.value                  = "";
        attributeValue.value                = "";
    }

    function togglePopup() { 
        const overlay = document.getElementById('attributeEditOverlay'); 
        overlay.classList.toggle('show'); 
    }

    function editAttribute(key, value) {
        let attributeKey    = document.getElementById("attributeKeyEdit");
        attributeKey.value  = key;
        let attributeValue  = document.getElementById("attributeValueEdit");
        attributeValue.value= value;

        togglePopup();

        document.addEventListener('click', outsideClickListener);
    }

    function saveAttributeChange() {
        let attributeKeyEdit    = document.getElementById("attributeKeyEdit");
        let attributeValueEdit  = document.getElementById("attributeValueEdit");
        let attributeKey        = document.getElementById("attributeKey");
        let attributeValue      = document.getElementById("attributeValue");

        attributeKey.value  = attributeKeyEdit.value;
        attributeValue.value= attributeValueEdit.value;

        let form = document.getElementById("hidConfig");
        form.submit();

        togglePopup();
    }

    function cancelAttributeChange() {
        let attributeKey    = document.getElementById("attributeKeyEdit");
        attributeKey.value  = "";
        let attributeValue  = document.getElementById("attributeValueEdit");
        attributeValue.value= "";
        togglePopup();
        removeClickListener();
    }

    clickCounter = 0;
    const outsideClickListener = event => {
        let element = document.getElementById('attributeEditBox');
        if (!element.contains(event.target) && isVisible(element) && clickCounter >= 1) {
            console.log('outside');
            cancelAttributeChange();
            return;
        }
        clickCounter++;
    }

    const removeClickListener = () => {
        clickCounter = 0;
        document.removeEventListener('click', outsideClickListener);
    }

    const isVisible = elem => !!elem && !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );

</script>

<?php
}

function echoAttributes($attributesGetResponse, $roleId) {
    $result     = $attributesGetResponse['result'];
    $attributesList= $result == RoleAttributesGetResult::SUCCESS ? $attributesGetResponse['attributes'] : array();
    if(!empty($attributesList)) {
        $htmlOutput = "";
        $htmlOutput.='<table class="rolesTable" width="100%">';
        $htmlOutput.='<tr>';
        $htmlOutput.="<td width='15%' style='background-color:#5da2b0;'>Name</td><td width='55%' style='background-color:#5da2b0;'>Value</td>";
        $htmlOutput.='</tr>';
        foreach($attributesList as $attribute) {
            $key    = $attribute['key'];
            $value  = $attribute['value'];

            $htmlOutput.='<tr>';
            $htmlOutput.="<td>$key</td>";
            $htmlOutput.="<td><p class='attributeValue'>".str_replace("\n", "<br>", htmlspecialchars(stripslashes($value)))."</p></td>";
            $htmlOutput.="
            <td width='15%' style='background:transparent;'>
                <input class='button-custom' type='button' value='Edit' onclick='editAttribute(\"$key\", ".htmlspecialchars(json_encode(stripslashes($value))).")' style='margin-top: 0px'/>
            </td>
            ";
            $htmlOutput.="
                <td width='15%' style='background:transparent;'>
                    <form method='post' action=''>
                        <input type='hidden' name='action' value='hidRoleAttributeDelete' />
                        <input type='hidden' name='roleId' value='$roleId' />
                        <input type='hidden' name='attributeKey' value='$key' />
                        <input class='button-custom' type='submit' value='Delete' style='margin-top: 0px'/>
                    </form>
                </td>
                ";
            $htmlOutput.='</tr>';
        }
        $htmlOutput.='</table>';
        echo $htmlOutput;
    } else if($result == RoleAttributesGetResult::SUCCESS) {
        echo '<p>No attributes found</p>';
    } else {
        echo "<p class='errorNote' style='font-size:16px;margin-left:0px'>Error while users get: {$result->name}</p>";
    }
    return count($attributesList);
}
?>