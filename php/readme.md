# HyperId SDK documentation

This repository contains the HyperID SDK.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To use HyperId SDK copy source files in your project.

You need to have php v8.1 or higher.

### How to integrate HyperId into your project:

```php
$auth = null;
$clientInfo = new ClientInfoBasic('your.client.id', 'your.client.secret', 'your.redirect.url');
try {
    $auth = new Auth($clientInfo, '', InfrastructureType::SANDBOX);
} catch(Exception $e) {
    echo '<h1>Auth not started</h1>';
}
```

> InfrastructureType is enum of two values, PRODUCTION and SANDBOX. \
> Use according to your needs

## Authentication
There are couple of possible authentication methods avaible:
* web2
* web3
* sing in with wallet
* sign in with guest upgrade
* sign in with indentity provider

### The following examples will demonstarte how to use them

Authentication example:\
Each returns sign-in url you need to redirect to.
```php
//...
header('Location:' . $auth->getAuthWeb2Url());
// or
header('Location:' . $auth->getAuthWeb3Url());
// or
header('Location:' . $auth->getAuthWalletGetUrl());
```

Sign in using identity provider:
```php
header('Location:' . $auth->getAuthByIdentityProviderUrl('google'));
```

In this case you can obtain full list of identity providers using next method:
```php
$idps = $auth->getDiscoverConfiguration()->identityProviders;
$url = null;
if(in_array('twitter', $idps)) {
    $url = $auth->getAuthByIdentityProviderUrl('twitter');
}
header('Location:' . $url);
```
## Additional options for sign in:

There are two types of wallet get modes: WalletGetMode::WALLET_GET_FAST and WalletGetMode::WALLET_GET_FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```php
$url = $auth->getAuthWalletGetUrl(WalletGetMode::WALLET_GET_FULL);
```

Next additional parameter is walletFamily. This parameter allows you to specify chain family: evm or solana.
Here is quick example:
```php
$url = $auth->getAuthWalletGetUrl(WalletGetMode::WALLET_GET_FULL, WalletFamily::ETHEREUM);
```

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verificationLevel parameter in the next sign in actions. It could be VerificationLevel::KYC_BASIC or VerificationLevel::KYC_FULL

```php
$url = $auth->getAuthUrl(AuthorizationFlowMode::SIGN_IN_WEB2,
                         null, /*WalletGetMode*/
                         null, /*WalletFamily*/
                         VerificationLevel::KYC_BASIC);
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it (from integration):
```php
if ($auth && isset($_GET['code'])) {
    try {
        $auth->exchangeAuthCodeToToken($_GET['code']);
    } catch (Exception $e) {
        echo 'Exchange code to token raised exception : ', $e;
    }
}
```

## Minimun working example of page:

```php
<!-- public/index.html -->
<!DOCTYPE html>
<html lang="en">

<head>
    <title>SDK Test App</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" />
</head>

<body style="background: #191E25; color: white;">

<?php
    require_once __DIR__.'/hyperid/auth/auth.php';

    session_start();

    $auth = null;
    if(!isset($_SESSION['auth'])) {
        $clientInfo = new ClientInfoBasic('your.client.id', 'your.client.secret', 'your.redirect.url');
        try {
            $auth = new Auth($clientInfo, '', InfrastructureType::PRODUCTION);
            $_SESSION['auth'] = $auth;
        } catch(Exception $e) {
            echo '<h1>Auth not started</h1>';
        }
    } else {
        if($_SESSION['auth'] instanceof Auth) {
            $auth = $_SESSION['auth'];
        }
    }

    if ($auth && isset($_SESSION['login']) && isset($_GET['code'])) {
        try {
            $auth->exchangeAuthCodeToToken($_GET['code']);
        } catch (Exception $e) {
            echo 'Exchange code to token raised exception : ', $e;
        }
        unset($_SESSION['login']);
    }

    if (isset($_GET['login']) && $auth) {
        $_SESSION['login'] = true;
        header('Location:' . $auth->getAuthWeb2Url());
    }

    if (isset($_POST['logout'])) {
        $auth->logout();
        session_destroy();
        echo '<meta http-equiv="refresh" content="0">';
        return;
    }
?>
    <form action="" method="post">
        <input type="hidden" name="logout" value="1"/>
        <button type="submit" style="margin:auto;">
            Sign out
        </button>
    </form>
    <br>

    <form action="" method="get">
        <input type="hidden" name="login" value="1"/>
        <button type="submit" style="margin:auto;">
            Sign in
        </button>
    </form>
    <br>

</body>
```                                                                                                                                                                                                                      