<?php

require_once __DIR__.'/../hyperid/base/enum.php';

function hidServiceAccountConfig() {
?>
    <div class="card_row">
        <div class="card">
            <form id="hidSaConfig" method="post" action="">
                <input type="hidden" name="action" value="hidSaConfig" />
                <?php wp_nonce_field('HIDConfigNonce', 'HIDConfigNonce') ?>
                <h3> Configuration for HyperID Service Account </h3>
                <table class="clientConfigTable">
                    <tr>
                        <td>Client ID</td>
                        <td> <input type="text" id="saClientId" name="saClientId" style="width:100%;" placeholder="Enter the client ID" value="<?php if (get_option('hid_sa_client_id')) echo esc_attr(get_option('hid_sa_client_id')); ?>" required />
                        </td>
                    </tr>
                    <tr>
                        <td>Is Self Signed</td>
                        <td>
                            <input type="checkbox" id="saSelfSigned" name="saSelfSigned" <?php if(get_option('hid_sa_self_signed') == 'on') { ?>checked <?php } ?>/>
                        </td>
                    </tr>
                    <tr>
                        <td>Client Authorization Type</td>
                        <td>
                            <select name="saClientAuthType" onchange="saClientAuthCheck()" id="saClientAuthType" class="hidConfigSelect" style="max-width:100%">
                                <?php
                                    echo "<option value=\"-1\">Select Client Authorization Type</option>";
                                    $clientAuthTypeDB = get_option('hid_sa_client_authorization');
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
                        <td id="saClientSecretName">Client Secret</td>
                        <td> <input type="text" id="saClientSecret" name="saClientSecret" style="width:100%;" placeholder="Enter the client secret" value="<?php if (get_option('hid_sa_client_secret')) echo esc_attr(get_option('hid_sa_client_secret')); ?>" required />
                        </td>
                    </tr>
                </table>
                <hr style="margin-top: 20px; margin-bottom: 20px; ">
                <input class="button-custom" type="submit" id="clientconfig" value="Save Configuration" />
            </form>
            <button class="button-custom" id="tokensBtn" onclick="toggleShowTokens();" style="width: 0%;">
                Show Tokens
            </button>
            <?php
                $refreshToken   = hyperIdClientController::getSaRefreshToken();
                $accessToken    = hyperIdClientController::getSaAccessToken();
                if(!empty($accessToken) && (get_option('hid_sa_self_signed') === 'on' || !empty($refreshToken))) {
            ?>
                <div id="tokensInfo" style="display: none; width: 80%;">
                    <?php if(get_option('hid_sa_self_signed') === 'off') { ?>
                        <p>Refresh token:</p>
                        <p style="word-break: break-all;"><?php echo $refreshToken; ?></p>
                    <?php } ?>
                    <p>Access token:</p>
                    <p style="word-break: break-all;"><?php echo $accessToken; ?></p>
                </div>
            <?php } ?>
            <p style="font-size: 15px">
                Don't have HyperID developer account? You can create one on <a href="https://dev.hypersecureid.com/" target="_blank">HyperID developer portal</a>.
            </p>
        </div>
    </div>
<script>
    function saClientAuthCheck() {
        clientAuth = document.getElementById("saClientAuthType").value;
        clientSecretName = document.getElementById("saClientSecretName");
        if(clientAuth == <?php echo AuthorizationMethod::RS256->value ?>) {
            clientSecretName.innerText = "Private Key";
        } else {
            clientSecretName.innerText = "Client Secret";
        }
    }
    <?php
        if(!empty($accessToken) && (get_option('hid_sa_self_signed') === 'on' || !empty($refreshToken))) {
    ?>
        let tokensShow = false;
        function toggleShowTokens() {
            if(tokensShow) {
                tokensInfo.style.display = "none";
                tokensBtn.innerHTML = "Show Tokens"
                tokensShow = false;
            } else {
                tokensInfo.style.display = "block";
                tokensBtn.innerHTML = "Hide Tokens"
                tokensShow = true;
            }
        }
    <?php } ?>
</script>

<?php
}
?>