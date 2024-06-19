# HyperId SDK documentation

This repository contains the HyperID SDK.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To import HyperId SDK into your webpage use:
```html
<script src="https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdSdk.js"></script>
```

To install the HyperID SDK, run the following at the command-line:

```terminal
npm install
npm run build
```

For running samples use nodeJS:
- node
- npm

### How to integrate HyperId into your project:

```js
let clientInfo = new clientInfoBasic("your.client.id",
                                     "your.client.secret",
                                     "redirect.url");
let sdk = null;
(async () => {
    sdk = new hyperIdSdk(clientInfo, infrastructureType.PRODUCTION);
    await sdk.init();
    window.onload = sdk.auth.handleOAuthCallback();
    sdk.on('tokensChanged', async(rt, at) => {
        // changes with tokens goes here
    });
})()
```

> InfrastructureType is enum of two values, PRODUCTION and SANDBOX. \
> Use according to your needs

## Authentication
There are couple of possible authentication methods avaible:
* web2
* web3
* sing in with wallet
* sign in with indentity provider
* sign in with kyc verification

### The following examples will demonstarte how to use them

Authentication example:\
Each returns sign-in url you need to redirect to.
```js
//...
let url = sdk.auth.startSignInWeb2();
// or
let url = sdk.auth.startSignInWeb3();
// or
let url = sdk.auth.startSignInWalletGet();

location.href = url;
```

Sign in using identity provider:
 ```js
let urlIdp = sdk.auth.startSignInByIdentityProvider('google');
window.location.href = urlIdp;
```

In this case you can obtain full list of identity providers using next method:
 ```js
    let idps = sdk.auth.getDiscover().identity_providers;
    let url = null;
    if(idps.indexOf('twitter') != -1) {
        url = sdk.auth.startSignInByIdentityProvider('twitter');
    }
    // other code
```
## Additional options for sign in:

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verificationLevel parameter in the next sign in actions. It could be verificationLevel.KYC_BASIC or verificationLevel.KYC_FULL

```js
//...
let url = sdk.auth.startSignInWeb2(verificationLevel.KYC_FULL);
// or
let url = sdk.auth.startSignInWeb3(verificationLevel.KYC_FULL);
// or
let url = sdk.auth.startSignInByIdentityProvider('google', verificationLevel.KYC_FULL);
```

There are two types of wallet get modes: walletGetMode.WALLET_GET_FAST and walletGetMode.WALLET_GET_FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```js
let url = sdk.auth.startSignInWalletGet(walletGetMode.WALLET_GET_FULL);
```

Next additional parameter is walletFamily. This parameter allows you to specify chain family: evm or solana.
Here is quick example:
```js
let url = sdk.auth.startSignInWeb3(null/*verificationLevel*/, walletGetMode.SOLANA);
// or
let url = sdk.auth.startSignInWalletGet(null/*walletGetMode*/, walletGetMode.ETHEREUM);
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it (from integration):
  ```js
window.onload = sdk.auth.handleOAuthCallback();
```

## API calls
All API call will generate Error in case of anything goes wrong.

### KYC

User status get:\
Function takes user access token from auth and verificationLevel.
Returns a object with kyc user info with valid info or object with empty fields if user do not have accociated kyc info.

```js
let kycData = null;
try{
    kycData = await sdk.kyc.getUserStatus(sdk.auth.getAccessToken(), verificationLevel.KYC_FULL);
} catch(error) {
    // error handle
}
```

User status top level get:
Function returns structure with kyc user info. Use this when user pass both kyc level, you will recieve top one.
Returns a object with kyc user info or oject with empty fields if user do not have accociated kyc info.\
```js
let kycData = null;
try{
    kycData = await sdk.kyc.getUserStatusTopLevel(sdk.auth.getAccessToken());
} catch(error) {
    // error handle
}
```

### MFA

Check whether HyperId Authenticator installed or not:
Returns bool.
```js
let isAvailable = null;
try{
    isAvailable = await sdk.mfa.checkAvailability(sdk.auth.getAccessToken());
}catch(error) {
    // error handle
}
```

Start MFA transaction:
Function takes 3 arguments: accessToken, code(string) and question(string). Code is 2 digit integer. Code is used for identification of request by user. Both question and code will appear in HyperID Authenticator.
Returns transaction id. You will need it in the next request to check the user response.
```js
let transactionId = null;
try {
    transactionId = await sdk.mfa.startTransaction(sdk.auth.getAccessToken(), "code", "Your question here");
} catch(error) {
    // error handle
}
```

Check transaction status:
Function takes access token and integer argument: transactionId.
Returns the information about transaction, status and complete result.
```js
let status = null;
try{
    status = await sdk.mfa.getTransactionStatus(sdk.auth.getAccessToken(), transactionId);
}catch(error) {
    // error handle
}
```

Cancel transaction:
Function takes access token and integer argument: transactionId.
Does not return anything in case of success.
```js
try{
    await sdk.mfa.cancelTransaction(sdk.auth.getAccessToken(), transactionId);
}catch(error) {
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
```js
try{
    await sdk.storageEmail.setData(sdk.auth.getAccessToken(), "key", "value");
} catch(error) {
    // error handle
}
```

User data get by email:\
Function takes 1 additional string argument: key(str).
Returns the data under given key or null if data not found.
```js
let data = null;
try{
    data = await sdk.storageEmail.getData(sdk.auth.getAccessToken(), "key");
} catch(error) {
    // error handle
}
```

Get keys list by email:\
Returns a object with assosiated keys or null on empty.
```js
let data = null;
try{
    data = await sdk.storageEmail.getKeysList(sdk.auth.getAccessToken());
} catch(error) {
    // error handle
}
```

Get shared keys list by email:\
Returns list with assosiated keys or null in case of keys not found.
```js
let data = null;
try{
    data = await sdk.storageEmail.getKeysListShared(sdk.auth.getAccessToken());
} catch(error) {
    // error handle
}
```

Data delete by email:\
Function takes 1 additional string argument: key(str).
Deletes a specified key.
Does not return anything in case of success.
```js
try{
    await sdk.storageEmail.deleteKey(sdk.auth.getAccessToken(), "key");
} catch(error) {
    // error handle
}
```

### Storage by user id

Allows you to setup any data assosiated with user id.\
Function takes 3 additional arguments: key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.
User data set by user id:
```js
try{
    await sdk.storageUserId.setData(sdk.auth.getAccessToken(), "key", "value");
} catch(error) {
    // error handle
}
```

User data get by user id:\
Function takes 1 additional string argument: key(str).
Returns the data under given key or null if data not found.
```js
let data = null;
try{
    data = await sdk.storageUserId.getData(sdk.auth.getAccessToken(), "key");
} catch(error) {
    // error handle
}
```

Get keys list by user id:\
Returns a object with assosiated keys or null on empty.
```js
let data = null;
try{
    data = await sdk.storageUserId.getKeysList(sdk.auth.getAccessToken());
} catch(error) {
    // error handle
}
```

Get shared keys list by user id:\
Returns list with assosiated keys or null in case of keys not found.
```js
let data = null;
try{
    data = await sdk.storageUserId.getKeysListShared(sdk.auth.getAccessToken());
} catch(error) {
    // error handle
}
```

Data delete by user id:\
Function takes 1 additional string argument: key(str).
Deletes a specified key.
Does not return anything in case of success.
```js
try{
    await sdk.storageUserId.deleteKey(sdk.auth.getAccessToken(), "key");
} catch(error) {
    // error handle
}
```

### Storage by identity provider

Allows you to setup any data assosiated with identity provider.\
Function takes 4 additional arguments: identityProvider(str), key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.
User data set by identity provider:
```js
try{
    await sdk.storageIdp.setData(sdk.auth.getAccessToken(), 'google', "key", "value");
} catch(error) {
    // error handle
}
```

User data get by identity provider:\
Function takes 2 additional arguments: identityProvider(str), key(str).
Returns the data under given key or null if data not found.
```js
let data = null;
try{
    data = await sdk.storageIdp.getData(sdk.auth.getAccessToken(), 'google', "key");
} catch(error) {
    // error handle
}
```

Get keys list by identity provider:\
Function takes 1 additional argument: identityProvider(str).
Returns list with assosiated keys or null in case of keys not found.
```js
let data = null;
try{
    data = await sdk.storageIdp.getKeysList(sdk.auth.getAccessToken(), 'google');
} catch(error) {
    // error handle
}
```

Get shared keys list by identity provider:\
Function takes 1 additional argument: identityProvider(str).
Returns object with assosiated keys or null in case of keys not found.
```js
let data = null;
try{
    data = await sdk.storageIdp.getKeysListShared(sdk.auth.getAccessToken(), 'google');
} catch(error) {
    // error handle
}
```

Data delete by identity provider:\
Function takes 2 additional arguments: identityProvider(str), key(str).
Deletes a specified key.
Does not return anything in case of success.
```js
try{
    await sdk.storageIdp.deleteKey(sdk.auth.getAccessToken(), 'google', "key");
} catch(error) {
    // error handle
}
```

### Storage by wallet

Allows you to setup any data assosiated with wallet.\
Function takes 4 additional arguments: wallet(str), key(str), value(str) and accessScope(userDataAccessScope). You can specify two types of access scope: public(1) or private(0).
Does not return anything in case of success.
User data set by wallet:
```js
try{
    await sdk.storageWallet.setData(sdk.auth.getAccessToken(), '0xAABBCC', "key", "value");
} catch(error) {
    // error handle
}
```

User data get by wallet:\
Function takes 2 additional arguments: wallet(str), key(str).
Returns the data under given key or null if data not found.
```js
let data = null;
try{
    data = await sdk.storageWallet.getData(sdk.auth.getAccessToken(), '0xAABBCC', "key");
} catch(error) {
    // error handle
}
```

Get keys list by wallet:\
Function takes 1 additional argument: wallet(str).
Returns object with assosiated keys or null in case of keys not found.
```js
let data = null;
try{
    data = await sdk.storageWallet.getKeysList(sdk.auth.getAccessToken(), '0xAABBCC');
} catch(error) {
    // error handle
}
```

Get shared keys list by wallet:\
Function takes 1 additional argument: wallet(str).
Returns a object with assosiated keys or null if keys not found.
```js
let data = null;
try{
    data = await sdk.storageWallet.getKeysListShared(sdk.auth.getAccessToken(), '0xAABBCC');
} catch(error) {
    // error handle
}
```

Data delete by wallet:\
Function takes 2 additional arguments: wallet(str), key(str).
Deletes a specified key.
Does not return anything in case of success.
```js
try{
    await sdk.storageWallet.deleteKey(sdk.auth.getAccessToken(), '0xAABBCC', "key");
} catch(error) {
    // error handle
}
```

## Minimun working example of page:

```html
<!-- public/index.html -->
<!DOCTYPE html>
<html>
<head>
    <title>HyperIdJS SDK Example</title>
    <script src="https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdSdk.js"></script>
</head>
<body>
    <form onclick="loginWeb2()" style="width: 25%;">
        <button type="button" style="width: 100%;">
            Web 2.0 Sign in 
        </button>
    </form>
    <script>
        let clientInfo = new clientInfoBasic("your.client.id",
                                             "your.client.secret",
                                             "your.redirect.uri");
        let sdk = null;
        (async () => {
            sdk = new hyperIdSdk(clientInfo, infrastructureType.PRODUCTION);
            await sdk.init();
            window.onload = sdk.auth.handleOAuthCallback();
            sdk.on('tokensChanged', async(rt, at) => {
                console.log("accessToken:\n", at);
                console.log("refreshToken:\n", rt);
            });
        })()

        function loginWeb2() {
            window.location.href = sdk.auth.startSignInWeb2();
        }
    </script>
</body>
```