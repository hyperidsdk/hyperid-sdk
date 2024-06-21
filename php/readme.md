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
$hidSDK = null;
$clientInfo = new ClientInfoBasic('your.client.id', 'your.client.secret', 'your.redirect.url');
try {
    $hidSDK = new HyperIDSDK($clientInfo, '', InfrastructureType::SANDBOX);
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
header('Location:' . $hidSDK->auth->getAuthWeb2Url());
// or
header('Location:' . $hidSDK->auth->getAuthWeb3Url());
// or
header('Location:' . $hidSDK->auth->getAuthWalletGetUrl());
```

Sign in using identity provider:
```php
header('Location:' . $hidSDK->auth->getAuthByIdentityProviderUrl('google'));
```

Sign in with wallet get without user interaction (return default wallet for user):
 ```php
header('Location:' . $hidSDK->auth->getAuthAutoWalletGetUrl());
```

Sign in with transaction, returns `transaction_result` and `transaction_hash` (*if result == 0*) as `GET` parameters.\
Mandatory parameters is:
- `addressTo` - wallet address where to send tokens (hex string)
- `chain` - chain of token
Optional:
- `addressFrom` - user wallet address to transfer tokens from (hex string)
- `value` - value of tokens to transfer (value or data should be present)
- `data` - data to make transaction in hex (value or data should be present)
- `gas`
- `nonce`

```php
header('Location:' . $hidSDK->auth->getAuthSignInWithTransactionUrl("0x501Fc2e1854cef866A084bCCbABbf68401FCaCb0", "1", null/*addressFrom*/, "10"));
```

To check transaction status, after receiving redirected url use:

 ```php
$transactionResult = $_GET['transaction_result'];
$transactionResultDesc = $_GET['transaction_result_description'];
$transactionHash = null;
if(isset($_GET['transaction_hash'])) { /* Success */
    $transactionHash = $_GET['transaction_hash'];
}
```

In this case you can obtain full list of identity providers using next method:
```php
$idps = $hidSDK->auth->getDiscoverConfiguration()->identityProviders;
$url = null;
if(in_array('twitter', $idps)) {
    $url = $hidSDK->auth->getAuthByIdentityProviderUrl('twitter');
}
header('Location:' . $url);
```
## Additional options for sign in:

There are two types of wallet get modes: WalletGetMode::WALLET_GET_FAST and WalletGetMode::WALLET_GET_FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```php
$url = $hidSDK->auth->getAuthWalletGetUrl(WalletGetMode::WALLET_GET_FULL);
```

Next additional parameter is walletFamily. This parameter allows you to specify chain family: evm or solana.
Here is quick example:
```php
$url = $hidSDK->auth->getAuthWalletGetUrl(WalletGetMode::WALLET_GET_FULL, WalletFamily::ETHEREUM);
```

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verificationLevel parameter in the next sign in actions. It could be VerificationLevel::KYC_BASIC or VerificationLevel::KYC_FULL

```php
$url = $hidSDK->auth->getAuthUrl(AuthorizationFlowMode::SIGN_IN_WEB2,
                                 null, /*WalletGetMode*/
                                 null, /*WalletFamily*/
                                 VerificationLevel::KYC_BASIC);
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it (from integration):
```php
if ($auth && isset($_GET['code'])) {
    try {
        $hidSDK->auth->exchangeAuthCodeToToken($_GET['code']);
    } catch (Exception $e) {
        echo 'Exchange code to token raised exception : ', $e;
    }
}
```

## API calls
All API call will generate Error in case of anything goes wrong.

### KYC
User status get:\
Function takes user access token from auth and verificationLevel.
Returns a object with kyc user info with valid info or object with empty fields if user do not have accociated kyc info.

```php
$kycData = null;
try{
    $kycData = $hidSDK->kyc->getUserStatus($hidSDK->auth->getAccessToken(), VerificationLevel::KYC_FULL);
} catch(Exception $e) {
    // error handle
}
```

User status top level get:
Function returns structure with kyc user info. Use this when user pass both kyc level, you will recieve top one.
Returns a object with kyc user info or oject with empty fields if user do not have accociated kyc info.\
```php
$kycData = null;
try{
    $kycData = $hidSDK->kyc->getUserStatusTopLevel($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

### MFA
Check whether HyperId Authenticator installed or not:
Returns bool.
```php
$isAvailable = null;
try{
    $isAvailable = $hidSDK->mfa->checkAvailability($hidSDK->auth->getAccessToken());
}catch(Exception $e) {
    // error handle
}
```

Start MFA transaction:
Function takes 3 arguments: accessToken, code(string) and question(string). Code is 2 digit integer. Code is used for identification of request by user. Both question and code will appear in HyperID Authenticator.
Returns transaction id. You will need it in the next request to check the user response.
```php
$transactionId = null;
try {
    $transactionId = $hidSDK->mfa->startTransaction($hidSDK->auth->getAccessToken(), "code", "Your question here");
} catch(Exception $e) {
    // error handle
}
```

Check transaction status:
Function takes access token and integer argument: transactionId.
Returns the information about transaction, status and complete result.
```php
$status = null;
try{
    $status = $hidSDK->mfa->getTransactionStatus($hidSDK->auth->getAccessToken(), $transactionId);
}catch(Exception $e) {
    // error handle
}
```

Cancel transaction:
Function takes access token and integer argument: transactionId.
Does not return anything in case of success.
```php
try{
    $hidSDK->mfa->cancelTransaction($hidSDK->auth->getAccessToken(), $stransactionId);
}catch(Exception $e) {
    // error handle
}
```

## Storage

### There four types of storages: email, user id, identity provider and wallet

All functions require access token of authorized user;

### Storage by email
Allows you to setup any data assosiated with email.\
Email is taken from auth token.
Function takes 3 additional arguments: key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.

User data set by email:
```php
try{
    $storageEmail->setData($hidSDK->auth->getAccessToken(), 'key', 'value');
} catch(Exception $e) {
    // error handle
}
```

User data get by email:\
Function takes 1 additional string argument: key(str).
Returns the data under given key or null if data not found.
```php
$data = null;
try{
    $data = $hidSDK->storageEmail->getData($hidSDK->auth->getAccessToken(), "key");
} catch(Exception $e) {
    // error handle
}
```

Get keys list by email:\
Returns a object with assosiated keys or null on empty.
```php
$data = null;
try{
    $data = $hidSDK->storageEmail->getKeys($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

Get shared keys list by email:\
Returns list with assosiated keys or null in case of keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageEmail->getKeysListShared($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

Data delete by email:\
Function takes 1 additional string argument: key(str).
Deletes a specified key.
Does not return anything in case of success.
```php
try{
    $hidSDK->storageEmail->deleteKey($hidSDK->auth->getAccessToken(), "key");
} catch(Exception $e) {
    // error handle
}
```

### Storage by user id

Allows you to setup any data assosiated with user id.\
Function takes 3 additional arguments: key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.

User data set by user id:
```php
try{
    $hidSDK->storageUserId->setData($hidSDK->auth->getAccessToken(), "key", "value");
} catch(Exception $e) {
    // error handle
}
```

User data get by user id:\
Function takes 1 additional string argument: key(str).
Returns the data under given key or null if data not found.
```php
$data = null;
try{
    $data = $hidSDK->storageUserId->getData($hidSDK->auth->getAccessToken(), "key");
} catch(Exception $e) {
    // error handle
}
```

Get keys list by user id:\
Returns a object with assosiated keys or null on empty.
```php
$data = null;
try{
    $data = $hidSDK->storageUserId->getKeys($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

Get shared keys list by user id:\
Returns list with assosiated keys or null in case of keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageUserId->getKeysListShared($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

Data delete by user id:\
Function takes 1 additional string argument: key(str).
Deletes a specified key.
Does not return anything in case of success.
```php
try{
    $hidSDK->storageUserId->deleteKey($hidSDK->auth->getAccessToken(), "key");
} catch(Exception $e) {
    // error handle
}
```

### Storage by identity provider

Allows you to setup any data assosiated with identity provider.\
Function takes 4 additional arguments: identityProvider(str), key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.

User data set by identity provider:
```php
try{
    $hidSDK->storageIdp->setData($hidSDK->auth->getAccessToken(), 'google', "key", "value");
} catch(Exception $e) {
    // error handle
}
```

User data get by identity provider:\
Function takes 2 additional arguments: identityProvider(str), key(str).
Returns the data under given key or null if data not found.
```php
$data = null;
try{
    $data = $hidSDK->storageIdp->getData($hidSDK->auth->getAccessToken(), 'google', "key");
} catch(Exception $e) {
    // error handle
}
```

Get keys list by identity provider:\
Function takes 1 additional argument: identityProvider(str).
Returns list with assosiated keys or null in case of keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageIdp->getKeys($hidSDK->auth->getAccessToken(), 'google');
} catch(Exception $e) {
    // error handle
}
```

Get shared keys list by identity provider:\
Function takes 1 additional argument: identityProvider(str).
Returns object with assosiated keys or null in case of keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageIdp->getKeysListShared($hidSDK->auth->getAccessToken(), 'google');
} catch(Exception $e) {
    // error handle
}
```

Data delete by identity provider:\
Function takes 2 additional arguments: identityProvider(str), key(str).
Deletes a specified key.
Does not return anything in case of success.
```php
try{
    $hidSDK->storageIdp->deleteKey($hidSDK->auth->getAccessToken(), 'google', "key");
} catch(Exception $e) {
    // error handle
}
```

### Storage by wallet
Get Wallets:\
Allows you to get all the wallets associated with user and current client.
```php
$wallets = null;
try{
    $wallets = $hidSDK->storageWallet->getWallets($hidSDK->auth->getAccessToken());
} catch(Exception $e) {
    // error handle
}
```

Allows you to setup any data assosiated with wallet.\
Function takes 4 additional arguments: wallet(str), key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.
User data set by wallet:
```php
try{
    $hidSDK->storageWallet->setData($hidSDK->auth->getAccessToken(), '0xAABBCC', "key", "value");
} catch(Exception $e) {
    // error handle
}
```

User data get by wallet:\
Function takes 2 additional arguments: wallet(str), key(str).
Returns the data under given key or null if data not found.
```php
$data = null;
try{
    $data = $hidSDK->storageWallet->getData($hidSDK->auth->getAccessToken(), '0xAABBCC', "key");
} catch(Exception $e) {
    // error handle
}
```

Get keys list by wallet:\
Function takes 1 additional argument: wallet(str).
Returns object with assosiated keys or null in case of keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageWallet->getKeys($hidSDK->auth->getAccessToken(), '0xAABBCC');
} catch(Exception $e) {
    // error handle
}
```

Get shared keys list by wallet:\
Function takes 1 additional argument: wallet(str).
Returns a object with assosiated keys or null if keys not found.
```php
$data = null;
try{
    $data = $hidSDK->storageWallet->getKeysListShared($hidSDK->auth->getAccessToken(), '0xAABBCC');
} catch(Exception $e) {
    // error handle
}
```

Data delete by wallet:\
Function takes 2 additional arguments: wallet(str), key(str).
Deletes a specified key.
Does not return anything in case of success.
```php
try{
    $hidSDK->storageWallet->deleteKey($hidSDK->auth->getAccessToken(), '0xAABBCC', "key");
} catch(Exception $e) {
    // error handle
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
    require_once __DIR__.'/hyperid/sdk.php';

    session_start();

    $hidSDK = null;
    if(!isset($_SESSION['hidSDK'])) {
        $clientInfo = new ClientInfoBasic('your.client.id', 'your.client.secret', 'your.redirect.url');
        try {
            $hidSDK = new HyperIDSDK($clientInfo, '', InfrastructureType::PRODUCTION);
            $_SESSION['hidSDK'] = $hidSDK;
        } catch(Exception $e) {
            echo '<h1>SDK not started</h1>';
        }
    } else {
        if($_SESSION['hidSDK'] instanceof HyperIDSDK) {
            $hidSDK = $_SESSION['hidSDK'];
        }
    }

    if ($hidSDK && isset($_SESSION['login']) && isset($_GET['code'])) {
        try {
            $hidSDK->auth->exchangeAuthCodeToToken($_GET['code']);
        } catch (Exception $e) {
            echo 'Exchange code to token raised exception : ', $e;
        }
        unset($_SESSION['login']);
    }

    if (isset($_GET['login']) && $hidSDK) {
        $_SESSION['login'] = true;
        header('Location:' . $hidSDK->auth->getAuthWeb2Url());
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
