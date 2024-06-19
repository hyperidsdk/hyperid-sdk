<!DOCTYPE html>
<html lang="en">

<head>
    <title>SDK Test App</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" />
    <link rel="stylesheet" type="text/css" href="./styles/styles.css">
</head>

<body style="background: #191E25; color: white;">

<?php
    require_once __DIR__.'/../hyperid/auth/auth.php';

    session_start();

    $auth = null;
    if(!isset($_SESSION['auth'])) {
        $clientInfo = new ClientInfoBasic('your.client.id', 'your.client.secret', 'your.redirect.url');
        try {
            $auth = new Auth($clientInfo, '', InfrastructureType::SANDBOX);
            $_SESSION['auth'] = $auth;
        } catch(Exception $e) {
            echo '<h1>Auth not started</h1>';
        }
    } else {
        if($_SESSION['auth'] instanceof Auth) {
            $auth = $_SESSION['auth'];
        }
    }

    $user = null;
    $accessToken = null;
    $refreshToken = null;

    if ($auth
        && isset($_SESSION['login'])
        && isset($_GET['code'])) {
        try {
            $auth->exchangeAuthCodeToToken($_GET['code']);
        } catch (Exception $e) {
            echo 'Exchange code to token raised exception : ', $e;
        }
        unset($_SESSION['login']);
    }

    if (isset($_GET['login']) && $auth) {
        $_SESSION['login'] = true;
        $loginMethod = $_GET['login'];
        switch($loginMethod) {
        case AuthorizationFlowMode::SIGN_IN_WEB2->value :
            header('Location:' . $auth->getAuthWeb2Url());
            break;
        case AuthorizationFlowMode::SIGN_IN_WEB3->value :
            header('Location:' . $auth->getAuthWeb3Url());
            break;
        case AuthorizationFlowMode::SIGN_IN_WALLET_GET->value :
            header('Location:' . $auth->getAuthWalletGetUrl());
            break;
        case AuthorizationFlowMode::SIGN_IN_GUEST_UPGRADE->value :
            header('Location:' . $auth->getAuthGuestUpgradeUrl());
            break;
        case AuthorizationFlowMode::SIGN_IN_IDENTITY_PROVIDER->value :
            header('Location:' . $auth->getAuthByIdentityProviderUrl());
            break;
        }
    }

    if (isset($_POST['destroy_session'])) {
        $auth->logout();
        session_destroy();
        echo '<meta http-equiv="refresh" content="0">';
        return;
    }

    if (isset($_GET['refreshTokens']) && $auth) {
        $auth->refreshTokens();
        $accessToken  = $auth->getAccessToken();
        $refreshToken = $auth->getRefreshToken();
    }

    if (isset($_GET['showTokens']) && $auth) {
        $accessToken  = $auth->getAccessToken();
        $refreshToken = $auth->getRefreshToken();
    }

    if (isset($_GET['userInfo']) && $auth) {
        $user = $auth->getUserInfo();
    }

    if (isset($_GET['logout']) && $auth) {
        $auth = $auth;
        $auth->logout();
    }

    $refreshTokenAuth = '';
    try {
        if($auth) {
            $refreshTokenAuth = $auth->getRefreshToken();
        }
    } catch (Exception $e) {
    }
    $accessTokenAuth = '';
    try {
        if($auth) {
            $accessTokenAuth = $auth->getAccessToken();
        }
    } catch (Exception $e) {
    }
?>
    <h2 style="color: white; margin-left: 50px;">
        Sign in / Sign up options
    </h2>
    <div>
        <form action="" method="post">
            <input type="hidden" name="destroy_session" value="1"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Destroy test session
            </button>
        </form>
        <br>

        <button type="button" class="button-custom" style="width: 20%;" onclick="goToRoles();">
            Go to Role Managment
        </button>
        <br>
        <br>

        <form action="" method="get">
            <input type="hidden" name="login" value="0"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Web 2.0 Sign in 
            </button>
        </form>
        <br>

        <form action="" method="get">
            <input type="hidden" name="login" value="4"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Web 2.0 Sign in + Connect Wallet
            </button>
        </form>
        <br>

        <form action="" method="get">
            <input type="hidden" name="login" value="6"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Web 3.0 Sign In
            </button>
        </form>
        <br>

        <form action="" method="get">
            <input type="hidden" name="login" value="3"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Sign in Auto Guest
            </button>
        </form>
        <br>

        <form action="" method="get">
            <input type="hidden" name="login" value="9"/>
            <button type="submit" class="button-custom" style="width: 20%;">
                Sign In with identity provider
            </button>
        </form>
        <br>

        <h2 style="color: white; margin-left: 80px;">
            HyperID Features
        </h2>

        <?php
        if($refreshTokenAuth) {
            ?>
            <form action="" method="get">
                <input type="hidden" name="refreshTokens" value="1"/>
                <button type="submit" class="button-custom" style="width: 20%;">
                    Refresh Tokens
                </button>
            </form>
            <br>
        <?php } else { ?>
            <form action="" method="get">
                <input type="hidden" name="refreshTokens" value="1"/>
                <button type="submit" disabled class="button-custom" style="width: 20%;">
                    Refresh Tokens
                </button>
                <div style="color: orange; margin-left: 10px;">
                    Please sign in first to make requests
                </div>
            </form>
            <br>
        <?php } ?>

        <?php
        if($accessTokenAuth) {
        ?>
            <form action="" method="get">
                <input type="hidden" name="showTokens" value="1"/>
                <button type="submit" class="button-custom" style="width: 20%;">
                    Show Tokens
                </button>
            </form>
            <br>
            <form action="" method="get">
                <input type="hidden" name="userInfo" value="1"/>
                <button type="submit" class="button-custom" style="width: 20%;">
                    Show User Info
                </button>
            </form>
            <br>
            <form action="" method="get">
                <input type="hidden" name="logout" value="1"/>
                <button type="submit" class="button-custom" style="width: 20%;">
                    Logout
                </button>
            </form>
            <br>
        <?php } else { ?>
            <form action="" method="get">
                <input type="hidden" name="showTokens" value="1"/>
                <button disabled type="submit" class="button-custom" style="width: 20%;">
                    Show Tokens
                </button>
                <div style="color: orange; margin-left: 10px;">
                    Please sign in first to make requests
                </div>
            </form>
            <br>
            <form action="" method="get">
                <input type="hidden" name="userInfo" value="1"/>
                <button disabled type="submit" class="button-custom" style="width: 20%;">
                    Show User Info
                </button>
                <div style="color: orange; margin-left: 10px;">
                    Please sign in first to make requests
                </div>
            </form>
            <br>
            <form action="" method="get">
                <input type="hidden" name="logout" value="1"/>
                <button disabled type="submit" class="button-custom" style="width: 20%;">
                    Logout
                </button>
                <div style="color: orange; margin-left: 10px;">
                    Please sign in first to make requests
                </div>
            </form>
            <br>
        <?php } ?>

        <?php
        if($accessToken && $refreshToken) {
        ?>
            <br>
            <div>
                <p>==================================================================</p>
                <p>refresh_token:</p><p><?php echo $refreshToken ?></p>
                <p>access_token:</p><p><?php echo $accessToken ?></p>
                <p>==================================================================</p>
            </div>
        <?php } ?>

            <?php
        if($user) {
            ?>
            <br>
            <div>
                <p>==================================================================</p>
                <p>User Id: <?php echo $user->user_id ?></p>
                <p>Email: <?php echo $user->user_email ?></p>
                <p>Is Guest: <?php echo $user->is_guest ? "true" : "false" ?></p>
                <?php if($user->ip) {?>
                <p>Ip: <?php echo $user->ip ?></p>
                <?php }?>
                <?php if($user->wallet) {?>
                <p>Wallet: <?php echo $user->wallet->wallet_address ?></p>
                <?php }?>
                <p>==================================================================</p>
            </div>
        <?php } ?>
    </div>
    <script>
        <?php
        $url = '';
        if (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] == 'on') {
            $url.='https://';
        } else {
            $url.='http://';
        }
        $url.=$_SERVER['HTTP_HOST'].'/roles';
        ?>
        function goToRoles() {
            window.location.href = '<?php echo $url?>';
        }
    </script>
</body>

</html>