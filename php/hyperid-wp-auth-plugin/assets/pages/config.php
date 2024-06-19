<?php

require_once __DIR__.'/../hyperid/base/enum.php';

function hidClientConfig() {
    if(get_option('hid_auth_tested')) {
?>
    <div class="infoNote" style="margin-left:20px;margin-right:20px;margin-top:20px;max-width:100%;width:80%"><p>You have successfully loged in as : <?php echo get_option('hid_auth_tested') ?></p></div>
    <?php 
    delete_option('hid_auth_tested');
    }
    ?>
    <div class="card_row">
        <div class="card">
            <form id="hidConfig" method="post" action="">
                <input type="hidden" name="action" value="hidConfig" />
                <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                <h3> Configuration for HyperID Client </h3>
                <table class="clientConfigTable">
                    <tr>
                        <td>HyperId Infrastructure</td>
                        <td>
                            <select name="serverType" class="hidConfigSelect" style="max-width:100%">
                                <?php
                                    echo "<option value=\"-1\">Select HyperId Infrastructure</option>";
                                    $links = array_column(InfrastructureType::cases(), 'value');
                                    foreach($links as $link) {
                                        if(get_option('hid_infrastructure') == $link) {
                                            echo "<option value=\"$link\" selected>$link</option>";
                                        } else {
                                            echo "<option value=\"$link\">$link</option>";
                                        }
                                    }
                                ?>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>Client ID</td>
                        <td> <input type="text" id="clientId" name="clientId" style="width:100%;" placeholder="Enter the client ID" value="<?php if (get_option('hid_client_id')) echo esc_attr(get_option('hid_client_id')); ?>" required />
                        </td>
                    </tr>
                    <tr>
                        <td>Client Authorization Type</td>
                        <td>
                            <select name="clientAuthType" onchange="clientAuthCheck()" id="clientAuthType" class="hidConfigSelect" style="max-width:100%">
                                <?php
                                    echo "<option value=\"-1\">Select Client Authorization Type</option>";
                                    $clientAuthTypeDB = get_option('hid_client_authorization');
                                    foreach(AuthorizationMethod::cases() as $clientAuthType) {
                                        if(gettype($clientAuthTypeDB) != 'boolean' && $clientAuthTypeDB == $clientAuthType->value) {
                                            echo "<option value=\"$clientAuthType->value\" selected>$clientAuthType->name</option>";
                                        } else {
                                            echo "<option value=\"$clientAuthType->value\">$clientAuthType->name</option>";
                                        }
                                    }
                                ?>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td id="clientSecretName">Client Secret</td>
                        <td> <input type="text" id="clientSecret" name="clientSecret" style="width:100%;" placeholder="Enter the client secret" value="<?php if (get_option('hid_client_secret')) echo esc_attr(get_option('hid_client_secret')); ?>" required />
                        </td>
                    </tr>
                    <tr>
                        <td>Authorization Method</td>
                        <td>
                            <select name="userAuthMethod" class="hidConfigSelect" style="max-width:100%" onchange="userAuthChange();" id="userAuthMethodSelect">
                                <?php
                                    $userAuthType = get_option('hid_user_authorization');
                                ?>
                                <option value="-1">Select User Authorization Method</option>
                                <option value=<?php printf("%s", AuthorizationFlowMode::SIGN_IN_WEB2->value); if(gettype($userAuthType) != 'boolean' && $userAuthType == AuthorizationFlowMode::SIGN_IN_WEB2->value) echo ' selected';?>>Sign In With Guest Without Wallet</option>
                                <option value=<?php printf("%s", AuthorizationFlowMode::SIGN_IN_WEB3->value); if(gettype($userAuthType) != 'boolean' && $userAuthType == AuthorizationFlowMode::SIGN_IN_WEB3->value) echo ' selected';?>>Sign In With Guest With Wallet</option>
                                <option value=<?php printf("%s", AuthorizationFlowMode::SIGN_IN_GUEST_UPGRADE->value); if(gettype($userAuthType) != 'boolean' && $userAuthType == AuthorizationFlowMode::SIGN_IN_GUEST_UPGRADE->value) echo ' selected';?>>Sign In Without Guest With Wallet</option>
                                <option value=<?php printf("%s", AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER->value); if(gettype($userAuthType) != 'boolean' && $userAuthType == AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER->value) echo ' selected';?>>Sign In By Identity Provider</option>
                            </select>
                        </td>
                    </tr>
                </table>
                <div id="additionalParameters">
                    <hr style="margin-top: 20px; margin-bottom: 20px; ">
                    <div style="display: flex; align-items: center;">
                        <h3 style="display:inline;">
                            Additional parameters (optional)
                        </h3>
                        <button type="button" class="collapsibleButton" style="display: inline;"></button>
                    </div>
                    <div class="collapsibleContent">
                        <table class="clientConfigTable">
                            <tr>
                                <td>Select Account</td>
                                <td>
                                    <input type="checkbox" id="selectAccount" name="selectAccount" <?php if(get_option('hid_auth_select_account') == 'on') { ?>checked <?php } ?>/>
                                </td>
                            </tr>
                            <tr id="trLoginTypesToShow">
                                <td>Login Types To Show</td>
                                <td>
                                    Web2 <input type="checkbox" id="web2" name="web2" <?php if(get_option('hid_login_types_to_show_web2') == 'on') { ?>checked <?php } ?>/>
                                    Web3 <input type="checkbox" id="web3" name="web3" <?php if(get_option('hid_login_types_to_show_web3') == 'on') { ?>checked <?php } ?>/>
                                    Idps <input type="checkbox" id="idps" name="idps" <?php if(get_option('hid_login_types_to_show_idps') == 'on') { ?>checked <?php } ?> onchange="checkForIdpsToShowOnLogin(this);"/>
                                </td>
                            </tr>
                            <tr id="trWalletGetMode">
                                <td>Wallet Get Mode</td>
                                <td>
                                    <select name="walletGetMode" class="hidConfigSelect" style="max-width:100%">
                                        <?php
                                            echo "<option value=\"-1\">Select Wallet Get Mode</option>";
                                            foreach(WalletGetMode::cases() as $walletGetMode) {
                                                $name = ucwords(strtolower(str_replace("_", " ", $walletGetMode->name)));
                                                if(get_option('hid_wallet_get_mode') && get_option('hid_wallet_get_mode') == $walletGetMode->value) {
                                                    echo "<option value=\"$walletGetMode->value\" selected>$name</option>";
                                                } else {
                                                    echo "<option value=\"$walletGetMode->value\">$name</option>";
                                                }
                                            }
                                        ?>
                                    </select>
                                </td>
                            </tr>
                            <tr id="trWalletFamily">
                                <td>Wallet Family</td>
                                <td>
                                    <select name="walletFamily" class="hidConfigSelect" style="max-width:100%">
                                        <?php
                                            echo "<option value=\"-1\">Select Wallet Family</option>";
                                            foreach(WalletFamily::cases() as $walletFamily) {
                                                $name = ucwords(strtolower(str_replace("_", " ", $walletFamily->name)));
                                                if(get_option('hid_wallet_family') && get_option('hid_wallet_family') == $walletFamily->value) {
                                                    echo "<option value=\"$walletFamily->value\" selected>$name</option>";
                                                } else {
                                                    echo "<option value=\"$walletFamily->value\">$name</option>";
                                                }
                                            }
                                        ?>
                                    </select>
                                </td>
                            </tr>
                            <tr id="trIdpForAuth">
                                <td>Identity Provider For Authorization</td>
                                <td>
                                    <?php
                                        $idps = get_option('hid_auth_idps_total');
                                        if($idps) {
                                        ?>
                                            <select name="idpForLogin" class="hidConfigSelect" style="max-width:100%">
                                                <?php
                                                    foreach($idps as $idp) {
                                                        if(get_option('hid_idp_for_login') && get_option('hid_idp_for_login') == $idp) {
                                                            echo "<option value=\"$idp\" selected>$idp</option>";
                                                        } else {
                                                            echo "<option value=\"$idp\">$idp</option>";
                                                        }
                                                    }
                                                ?>
                                            </select>
                                        <?php } else {
                                            echo '<p>(You need to save valid configuration first!)</p>';
                                        } ?>
                                </td>
                            </tr>
                            <tr id="trIdpsToShow">
                                <td>
                                    <div style="display:flex; align-items: center;">
                                        Identity Providers To Show
                                        <img src="<?php echo plugins_url('info.svg', __FILE__)?>" style="display: inline;width: 16px;height: 16px; margin-left: 4px" title="If no option selected, would show all"/>
                                    </div>
                                </td>
                                <td>
                                    <div class="multipleSelection" id="multipleSelection">
                                        <div class="idpSelectBox" onclick="showCheckboxes()" id="idpSelectBox">
                                            <?php
                                            $idps = get_option('hid_auth_idps_total');
                                            $idpsPrev = get_option('hid_idps_list');
                                            if($idps) {
                                            ?>
                                                <select name="idpsListToShow">
                                                    <?php
                                                    if($idpsPrev) {
                                                        echo '<option id="idpsToShow">'.$idpsPrev.' '.'</option>';
                                                    } else {
                                                        echo '<option id="idpsToShow">Select options</option>';
                                                    }
                                                    ?>
                                                </select>
                                            <?php } else {
                                                echo '<p>(You need to save valid configuration first!)</p>';
                                            } ?>
                                            <div class="overIdpSelect"></div>
                                        </div>
                                        <div id="idpsCheckBoxes" class="idpCheckBoxesCard" style="margin-bottom: 20px">
                                            <?php
                                            if($idps) {
                                                for($i = 0; $i < count($idps); $i++) {
                                                    if(str_contains($idpsPrev, $idps[$i])) {
                                                        printf('<label for="idpToShow_%1$s"><input type="checkbox" onchange="idpToShowCheck(\'idpToShow_%1$s\', \'%2$s\')" id="idpToShow_%1$s" checked/>%2$s</label>', $i, $idps[$i]);
                                                    } else {
                                                        printf('<label for="idpToShow_%1$s"><input type="checkbox" onchange="idpToShowCheck(\'idpToShow_%1$s\', \'%2$s\')" id="idpToShow_%1$s"/>%2$s</label>', $i, $idps[$i]);
                                                    }
                                                }
                                            }
                                            ?>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr id="trIdpsOrder">
                                <td>
                                    <div style="display:flex; align-items: center;">
                                        Identity Providers Order
                                        <img src="<?php echo plugins_url('info.svg', __FILE__)?>" style="display: inline;width: 16px;height: 16px; margin-left: 4px" title="Drag and drop to change order"/>
                                    </div>
                                </td>
                                <td>
                                    <ul id="dragList" class="drag-list" style="display:none;">
                                        <?php
                                            $list = null;
                                            if($idpsPrev) {
                                                $list = explode(" ", $idpsPrev);
                                            }
                                            $idpsInOrder = get_option('hid_idps_list_order');
                                            if(!$list && $idpsInOrder) {
                                                $list = explode(" ", $idpsInOrder);
                                            }
                                            if(!$list && $idps) {
                                                $list = $idps;
                                            }
                                            if(!$list) {
                                                echo '<p>(You need to save valid configuration first!)</p>';
                                            } else {
                                                printf('<input type="hidden" name="idpsListToShowInOrder" id="idpsListToShowInOrder" value="%s" />', implode(' ', $list));
                                                for($i = 0; $i < count($list); $i++) {
                                                    echo '<li class="drag-item" draggable="true">'.($i + 1).' : '.$list[$i].'</li>';
                                                }
                                            }
                                        ?>
                                    </ul>
                                    <button class="button-custom" id='idpsListToShowInOrderExpand' onclick='expandListOfIdps(); return false;'>Show providers</button>
                                    <button class="button-custom" style='display:none' id='idpsListToShowInOrderClear' onclick='clearIdpsOrder(); return false;'>Reset providers order to default</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                <hr style="margin-top: 20px; margin-bottom: 20px; ">
                <input class="button-custom" type="submit" id="clientconfig" value="Save Configuration" />
            </form>
            <?php if(get_option('hid_auth_saved') == 'saved') { ?>
            <form id="hidConfig" method="post" action="">
                <input type="hidden" name="action" value="hidConfigTest" />
                <input class="button-custom" type="submit" id="clientconfig" value="Test Configuration" />
                <img src="<?php echo plugins_url('info.svg', __FILE__)?>" style="display: inline;width: 16px;height: 16px; margin-left: 4px" title="Does not save current configuration, save it first!"/>
            </form>
            <?php } ?>
            <p style="font-size: 15px">
                Don't have HyperID developer account? You can create one on <a href="https://dev.hypersecureid.com/" target="_blank">HyperID developer portal</a>.
                <br>For the Base URL and Redirect URI you can past : <a href="<?php echo home_url() ?>" target="_blank"><?php echo home_url() ?></a>.
            </p>
        </div>
    </div>
<script>
    function userAuthChange() {
        additionalParameters.style.display = 'none';
        trLoginTypesToShow.style.display = 'none';
        trWalletGetMode.style.display = 'none';
        trWalletFamily.style.display = 'none';
        trIdpForAuth.style.display = 'none';
        trIdpsToShow.style.display = 'none';
        trIdpsOrder.style.display = 'none';

        var value = userAuthMethodSelect.value;
        if(value != '-1') {
            additionalParameters.style.display = 'block';
        }
        if(value == <?php echo AuthorizationFlowMode::SIGN_IN_WEB2->value ?>) {
            trLoginTypesToShow.style.display = 'table-row';
            trIdpsToShow.style.display = 'table-row';
            trIdpsOrder.style.display = 'table-row';
        } else if(value == <?php echo AuthorizationFlowMode::SIGN_IN_WEB3->value ?>
                || value == <?php echo AuthorizationFlowMode::SIGN_IN_GUEST_UPGRADE->value ?>) {
            trLoginTypesToShow.style.display = 'table-row';
            trIdpsToShow.style.display = 'table-row';
            trIdpsOrder.style.display = 'table-row';
            trWalletGetMode.style.display = 'table-row';
            trWalletFamily.style.display = 'table-row';
        } else if(value == <?php echo AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER->value ?>) {
            trIdpForAuth.style.display = 'table-row';
        }

        checkForIdpsToShowOnLogin(document.getElementById('idps'));
        collapsibleResize();
    }

    var idpsCheckBoxesShow = false;
    function showCheckboxes() {
        var idpsCheckBoxes = document.getElementById("idpsCheckBoxes");
        if (!idpsCheckBoxesShow) {
            idpsCheckBoxes.style.display = "block";
            idpsCheckBoxesShow = true;
        } else {
            idpsCheckBoxes.style.display = "none";
            idpsCheckBoxesShow = false;
        } 
    }

    window.addEventListener("load", async () => {
        var coll = document.getElementsByClassName("collapsibleButton");
        var i;

        for (i = 0; i < coll.length; i++) {
            coll[i].addEventListener("click", function() {
                this.classList.toggle("active");
                var content = document.getElementsByClassName("collapsibleContent")[0];
                if (content.style.maxHeight) {
                    content.style.maxHeight     = null;
                    content.style.marginBottom  = '0px';
                } else {
                    content.style.maxHeight     = content.scrollHeight + "px";
                    content.style.marginBottom  = '16px';
                }
            });
        }

        userAuthChange();
        checkForIdpsToShowOnLogin(document.getElementById('idps'));
    });

    function collapsibleResize() {
        var content = document.getElementsByClassName("collapsibleContent")[0];
        if (content.style.maxHeight) {
            content.style.maxHeight     = content.scrollHeight + "px";
            content.style.marginBottom  = '16px';
        }
    }

    window.addEventListener('click', function(e) {
        if(!document.getElementById('multipleSelection').contains(e.target)) {
            if(idpsCheckBoxesShow) {
                document.getElementById("idpsCheckBoxes").style.display = "none";
                idpsCheckBoxesShow = false;
            }
        }
    });

    function clientAuthCheck() {
        clientAuth = document.getElementById("clientAuthType").value;
        clientSecretName = document.getElementById("clientSecretName");
        if(clientAuth == <?php echo AuthorizationMethod::RS256->value ?>) {
            clientSecretName.innerText = "Private Key";
        } else {
            clientSecretName.innerText = "Client Secret";
        }
    }

    function idpToShowCheck(elementId, idp) {
        idpToShow = document.getElementById(elementId).checked;
        idpsToShow = document.getElementById("idpsToShow");

        if(idpToShow) {
            if(idpsToShow.innerHTML == 'Select options') {
                idpsToShow.innerHTML = '';
                document.getElementById('dragList').innerHTML = '<input type="hidden" name="idpsListToShowInOrder" id="idpsListToShowInOrder" value="" />';
            }
            if(!idpsToShow.innerHTML.includes(idp)) {
                idpsToShow.innerHTML += idp + ' ';
                isInOrderList = false;
                dragElements = document.getElementsByClassName('drag-item');
                for(i = 0; i < dragElements.length; i++) {
                    if(dragElements[i].innerHTML.includes(idp)) {
                        isInOrderList = true;
                        break;
                    }
                }
                if(!isInOrderList) {
                    dragElements = document.getElementsByClassName('drag-item');
                    document.getElementById('dragList').innerHTML += '<li class="drag-item" draggable="true">' + (dragElements.length + 1) + ' : ' + idp + '</li>';
                    document.getElementById('idpsListToShowInOrder').value += (dragElements.length == 0 ? '' : ' ') + idp;
                }
            }
        } else {
            if(idpsToShow.innerHTML.includes(idp + ' ')) {
                idpsToShow.innerHTML = idpsToShow.innerHTML.substring(0, idpsToShow.innerHTML.indexOf((idp + ' ')))
                                        + idpsToShow.innerHTML.substring(idpsToShow.innerHTML.indexOf((idp + ' ')) + (idp + ' ').length);
                if(document.getElementById('dragList').innerHTML.includes(idp)) {
                    dragList = document.getElementById('dragList');
                    dragElements = document.getElementsByClassName('drag-item');
                    for(i = 0; i < dragElements.length; i++) {
                        if(dragElements[i].innerHTML.includes(idp)) {
                            dragList.removeChild(dragElements[i]);
                        }
                    }
                    document.getElementById('idpsListToShowInOrder').value = '';
                    for(i = 0; i < dragElements.length; i++) {
                        dragElements[i].style.boxShadow = '';

                        dragElements[i].innerHTML = (i + 1) + dragElements[i].innerHTML.substring(dragElements[i].innerHTML.indexOf(' : '));
                        idpInElement = dragElements[i].innerHTML.substring(dragElements[i].innerHTML.indexOf(' : ') + ' : '.length);
                        document.getElementById('idpsListToShowInOrder').value += idpInElement + (i == dragElements.length - 1 ? '' : ' ');
                    }
                }
            }
            if(idpsToShow.innerHTML == '') {
                idpsToShow.innerHTML = 'Select options';
                clearIdpsOrder();
            }
        }
        collapsibleResize()
    }

    //idpOrderedList

    let listShowed = false;

    let draggedItem = null;
    let draggParent = null;

    dragList.addEventListener('dragstart',  handleDragStart);
    dragList.addEventListener('dragover',   handleDragOver);
    dragList.addEventListener('drop',       handleDrop);
    dragList.addEventListener('dragend',    handleDragEnd);
    dragList.addEventListener('dragleave',  handleDragLeave);

    function handleDragStart(event) {
        draggedItem = event.target;
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/html', draggedItem.innerHTML);
        event.target.style.opacity = '0.5';
    }

    function handleDragOver(event) {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
        const targetItem = event.target;
        if (targetItem !== draggedItem && targetItem.classList.contains('drag-item')) {
            const boundingRect = targetItem.getBoundingClientRect();
            const offset = boundingRect.y + (boundingRect.height / 2);
            if (event.clientY - offset > 0) {
                targetItem.style.boxShadow = 'inset 0px -2px #000';
            } else {
                targetItem.style.boxShadow = 'inset 0px 2px #000';
            }
        }
    }

    function handleDrop(event) {
        event.preventDefault();
        const targetItem = event.target;
        if (targetItem !== draggedItem && targetItem.classList.contains('drag-item')) {
            if (event.clientY > targetItem.getBoundingClientRect().top + (targetItem.offsetHeight / 2)) {
                targetItem.parentNode.insertBefore(draggedItem, targetItem.nextSibling);
            } else {
                targetItem.parentNode.insertBefore(draggedItem, targetItem);
            }
        }
    }

    function handleDragLeave(event) {
        event.target.style.boxShadow = '';
    }

    function handleDragEnd(event) {
        draggedItem.style.opacity = '';
        draggedItem = null;

        dragElements = document.getElementsByClassName('drag-item');
        document.getElementById('idpsListToShowInOrder').value = '';
        document.getElementById("idpsToShow").innerHTML = '';
        for(i = 0; i < dragElements.length; i++) {
            dragElements[i].style.boxShadow = '';

            dragElements[i].innerHTML = (i + 1) + dragElements[i].innerHTML.substring(dragElements[i].innerHTML.indexOf(' : '));
            idp = dragElements[i].innerHTML.substring(dragElements[i].innerHTML.indexOf(' : ') + ' : '.length);
            document.getElementById('idpsListToShowInOrder').value += idp + (i == dragElements.length - 1 ? '' : ' ');
            document.getElementById("idpsToShow").innerHTML += idp + (i == dragElements.length - 1 ? '' : ' ');
        }
    }

    function expandListOfIdps() {
        if(listShowed) {
            document.getElementById('dragList').style.display = 'none';
            document.getElementById('idpsListToShowInOrderClear').style.display = 'none';
            document.getElementById('idpsListToShowInOrderExpand').innerHTML = 'Show providers';
        } else {
            document.getElementById('dragList').style.display = 'block';
            document.getElementById('idpsListToShowInOrderClear').style.display = 'inline';
            document.getElementById('idpsListToShowInOrderExpand').innerHTML = 'Hide providers';
            collapsibleResize();
        }
        listShowed = !listShowed;
    }

    function clearIdpsOrder() {
        idpsToShow = document.getElementById("idpsToShow");
        selectedIdps = idpsToShow.innerHTML;

        if(selectedIdps == 'Select options') {
            document.getElementById('idpsListToShowInOrder').value = '';
            document.getElementById('dragList').innerHTML = '<?php
                $list = null;
                if(!$list && $idps) {
                    $list = $idps;
                }
                if(!$list) {
                    echo '<p>(You need to save valid configuration first!)</p>';
                } else {
                    printf('<input type="hidden" name="idpsListToShowInOrder" id="idpsListToShowInOrder" value="" />');
                    for($i = 0; $i < count($list); $i++) {
                        echo '<li class="drag-item" draggable="true">'.($i + 1).' : '.$list[$i].'</li>';
                    }
                }
            ?>';
        } else {
            document.getElementById('dragList').innerHTML = '<input type="hidden" name="idpsListToShowInOrder" id="idpsListToShowInOrder" value="" />';
            listOfSelectedIdps  = selectedIdps.split(' ');
            listOfTotalIdps     = <?php if($idps) {echo '"'.implode(' ', $idps).'"';} else { echo '""';} ?>.split(' ');
            document.getElementById("idpsToShow").innerHTML = '';
            number = 1;
            for(i = 0; i < listOfTotalIdps.length; i++) {
                if(selectedIdps.includes(listOfTotalIdps[i])) {
                    document.getElementById('dragList').innerHTML += '<li class="drag-item" draggable="true">' + number + ' : ' + listOfTotalIdps[i] + '</li>';
                    document.getElementById('idpsListToShowInOrder').value += (number == 1 ? '' : ' ') + listOfTotalIdps[i];
                    document.getElementById("idpsToShow").innerHTML += listOfTotalIdps[i] + ' ';
                    number++;
                }
            }
            document.getElementById("idpsToShow").innerHTML = document.getElementById("idpsToShow").innerHTML.trim();
        }

        collapsibleResize();
    }

    function checkForIdpsToShowOnLogin(checkBox) {
        var value = userAuthMethodSelect.value;
        if(value != '-1' && value != <?php echo AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER->value; ?>) {
            if(checkBox.checked) {
                trIdpsToShow.style.display = 'table-row';
                trIdpsOrder.style.display = 'table-row';
            } else {
                trIdpsToShow.style.display = 'none';
                trIdpsOrder.style.display = 'none';
            }
            collapsibleResize();
        }
    }

</script>

<?php
}
?>