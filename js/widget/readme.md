# HyperId Widget documentation

This repository contains the HyperID Widget.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To import HyperId Widget into your webpage use:
```html
<script src="https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdWidget.js"></script>
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

- Insert future widget element

```js
<div id="hyperid-websdk-container"></div>
```
- Add script with parameters

```js
<script async src="https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdWidget.js"
        data-client-id="your.client.id"
        data-client-secret="your.client.secret"
        data-redirect-url="your.redirect.uri"
        data-container-id="hyperid-websdk-container"
        data-flow-mode="desired.flow.mod"       /*web2 | web3*/
        data-infrastructure-type="production"   /*production | sandbox*/
        data-css-styles=""                      /*your css styles for container elements*/
    >
</script>
```
## Authentication

User can authenticate in HyperId with created button. After user successfuly authenticated, button will be replaced with contex menu where user can see information about connected account depending on given flow mode type.

## SDK usage

After user successfuly authenticated, you can use global obejct `hyperId`. It consist of modules of SDK (`auth`, `kyc`, `mfa`, `storages`) to access `user info`, get user `KYC`, send `mfa transaction`, etc.

### The following examples will briefly demonstarte how to use them

Show user email in message:
```js
if(await hyperId.auth.checkSession()) {
    alert(hyperId.auth.userInfo().email);
}
```

Check if mfa available and send transaction:
 ```js
let isAvailable     = null;
let transactionId   = null;
if(!await hyperId.auth.checkSession()) return;

try{
    isAvailable = await hyperId.mfa.checkAvailability(hyperId.auth.getAccessToken());
    if(isAvailable) {
        transactionId = await hyperId.mfa.startTransaction(hyperId.auth.getAccessToken(), "code", "Your question here");
    }
}catch(error) {
    // error handle
}
```

Check KYC user status:
 ```js
let kycData = null;
if(!await hyperId.auth.checkSession()) return;

try{
    kycData = await hyperId.kyc.getUserStatus(hyperId.auth.getAccessToken(), verificationLevel.KYC_FULL);
} catch(error) {
    // error handle
}
```

#### For more examples of usage check HyperId SDK documentation

## Minimun working example of page:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Widget</title>
</head>
<body style="background-color: #191E25;">
    <div id="hyperid-websdk-container"></div>
    <script async src="https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdWidget.js"
            data-client-id="your.client.id"
            data-client-secret="your.client.secret"
            data-redirect-url="your.redirect.uri"
            data-container-id="hyperid-websdk-container"
            data-flow-mode="web3"
            data-infrastructure-type="production">
    </script>

</body>
</html>
```