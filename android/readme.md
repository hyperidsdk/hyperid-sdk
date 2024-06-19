# HyperId SDK documentation

This repository contains the HyperID SDK.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To using the HyperID SDK, add the following command to your builde gradle dependecies:

```terminal
implementation("com.hyperid.sdk:sdk:1.0.0")
```

For running samples:
- [Android Studio](https://developer.android.com/studio)

### How to integrate HyperId into your project:

```kotlin
import ai.hyper_id.sdk.IHyperIdSDK


val client_info = ClientInfo(client_id="your.client.id",
                              client_secret="your.client.secret if available for your client",
                              redirect_uri="redirect.url",
                              privateRSAKey="RSAKey if available for your client"
                              authorizationMethod="your.client.authorization_method")
val sdk = HyperIdSDKInstance()
sdk.init("your HyperId infrastructure type",
        clientInfo,
        "instance of your completeListener")
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
* sign in with transaction start

### The following examples will demonstarte how to use them

Authentication example:\
Each returns sign-in url you need to redirect to.

```kotlin
val sdkAuth = sdk.getAuth()
val url = sdkAuth.startSignInWeb2("params")
val url = sdkAuth.startSignInWeb3("params")
val url = sdkAuth.startSignInWalletGet("params")
val url = sdkAuth.startSignInGuestUpgrade("params")
val url = sdkAuth.startSignInIdentityProvider("params")
val url = sdkAuth.startSignInWithTransaction("params")

```
Sign in using identity provider:
 ```kotlin
val url = sdkAuth.startSignInIdentityProvider("params")
```

In this case you can obtain full list of identity providers using next method:
 ```kotlin
val sdkAuth = sdk.getAuth()
val idpList = sdk.identityProviders()
if("google" in idpList)
    val url = sdk.startSignInIdentityProvider("google")
....
```
## Additional options for sign in:

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verificationLevel parameter in the next sign in actions. It could be KycVerificationLevel.BASIC or KycVerificationLevel.FULL

```kotlin
#...
sdkAuth.startSignInWeb2(verificationLevel=KycVerificationLevel.FULL, "your result listener")
# or
sdkAuth.startSignInWeb3(verificationLevel=KycVerificationLevel.BASIC, "your result listener")
# or
sdkAuth.startSignInIdentityProvider(verificationLevel=KycVerificationLevel.FULL,
        identity_provider=...,
        "your result listener")
```

There are two types of wallet get modes: WalletGetMode.FAST and WalletGetMode.FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```kotlin
sdkAuth.startSignInWalletGet(walletGetMode=WalletGetMode.FAST, "your result listener")
```

Next additional parameter is walletFamily. This parameter allows you to specify chain family: ethereum or solana.
Here is quick example:
```Kotlin
sdkAuth.startSignInWeb3(walletFamily=WalletGetMode.SOLANA, "your result listener")
# or
sdkAuth.startSignInWalletGet(walletFamily=WalletGetMode.ETHEREUM, "your result listener")
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it:
  ```Kotlin
sdkAuth.completeSignIn("entire redirect URL",
                        "your result listener")
```

If authorization start with transaction for completing you should  use:
```Kotlin
sdkAuth.completeSignInWithTransaction("entire redirect URL",
                                      "your result listener")
```
### KYC

User status get:\
Function takes 1 (KycVerificationLevel) argument: verificationLevel.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\

```Kotlin
val kycSDK = sdk.getKYC()
kycSDK.getUserStatus(kycVerificationLevel=KycVerificationLevel.FULL,
                    "your result listener")
```

User status top level get:\
Function returns structure with kyc user info. Use this when user pass both kyc level, you will recieve top one.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\
```Kotlin
kycSDK.getUserStatusTopLevel("your result listener")
```

### MFA

Check whether HyperId Authenticator installed or not:

```Kotlin
val mfaSDK = sdk.getMFA()
val isAvailable = mfaSDK.availabilityCheck("your result listener")
```

Start MFA transaction:
Function takes 2 string arguments: question and code. Code is 2 digit string. Code is used for identification. Both question and code will appear in HyperID Authenticator.
Returns transaction id. You will need it in the next request to check the user response.
```Kotlin
val transactionId = mfaSDK.transactionStart(question="Your question here",
                                            code="code",
                                            "your result listener")
```

Check stansaction status:
Function takes 1 integer argument: transactionId.
Returns the information about transaction, status and complete result.
```Kotlin
mfaSDK.transactionStatusCheck(transactionId=your_transaction_id,
                                "your result listener")
```

Cancel transaction:
Function takes 1 integer argument: transactionId.
Does not return anything in case of success.
```Kotlin
mfaSDK.transactionCancel(transactionId=your_transaction_id,
                            "your result listener")
```

## Storage

### There four types of storages: email, user id, identity provider and wallet

### Storage by email
Allows you to setup any data assosiated with email. Email is taken from public user info you can get after authorization.
Function takes 3 arguments: key(str), value(str) and accessType(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by email:
```Kotlin
val storageByEmail = sdk.getStorage().StorageByEmail()
storageByEmail.dataSet(key, value, UserDataAccessScope.PUBLIC,"your result listener")
# or
storageByEmail.dataSet(key, value, aUserDataAccessScope.PRIVATE, "your result listener")
```

User data get by email:
Function takes 1 string argument: key(String).
Returns the data under given key or None if data not found.
```Kotlin
storageByEmail.dataGet(key, "your result listener")
```

Get keys list by email:
Returns a structure with assosiated keys.
```Kotlin
storageByEmail.keysGet("your result listener")
```

Get shared keys list by email:
Returns list with assosiated keys or None in case of keys not found.
```Kotlin
storageByEmail.keysSharedGet("your result listener")
```

Data delete by email:
Function takes 1 string argument: keys(List<String>).
Delete a specified key.
Does not return anything in case of success.
```Kotlin
storageByEmail.dataDelete(keys, "your result listener")
```
### Storage by user id

Allows you to setup any data assosiated with user id. User id is taken from public user info you can get after authorization.
Function takes 3 arguments: key(String), value(String) and accessType(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by user id:
```Kotlin
val storageByUserId = sdk.getStorage().StorageByUserId()
storageByUserId.dataSet(key, value, UserDataAccessScope.PUBLIC, "your result listener")
# or
storageByUserId.dataSet(key, value, UserDataAccessScope.PRIVATE, "your result listener")
```

User data get by user id:
Function takes 1 string argument: key(String).
Returns the data under given key or None if data not found.
```Kotlin
storageByUserId.dataGet(key, "your result listener")
```

Get keys list by user id:
Returns a structure with assosiated keys.
```Kotlin
storageByUserId.keysGet("your result listener")
```

Get shared keys list by user id:
Returns list with assosiated keys or None in case of keys not found.
```Kotlin
storageByUserId.keysSharedGet("your result listener")
```

Data delete by user id:
Function takes 1 string argument: keys(List<String>).
Delete a specified key.
Does not return anything in case of success.
```Kotlin
storageByUserId.dataDelete(keys, "your result listener")
```

### Storage by identity provider

Allows you to setup any data assosiated with identity provider.
Function takes 4 arguments: identityProvider(String), key(String), value(String) and accessType(UserDataAccessScope). You can specify two types of accessType: public or private.
Does not return anything in case of success.
User data set by identity provider:
```Kotlin
val storageByIdentityProvider = sdk.getStorage().StorageByIdentityProvider()
storageByIdentityProvider.dataSet("google", key, value, UserDataAccessScope.PUBLIC, "your result listener")
# or
storageByIdentityProvider.dataSet('google', key, value, UserDataAccessScope.PRIVATE, "your result listener")
```

User data get by identity provider:
Function takes 2 arguments: identityProvider(String), key(String).
Returns the data under given key or None if data not found.
```Kotlin
storageByIdentityProvider.dataGet("google", key, "your result listener")
```

Get keys list by identity provider:
Function takes 1 argument: identityProvider(String).
(str).
Returns a structure with assosiated keys.
```Kotlin
storageByIdentityProvider.keysGet('google', "your result listener")
```

Get shared keys list by identity provider:
Function takes 1 argument: identityProvider(String).
Returns a structure with assosiated keys or None if keys not found.
```Kotlin
storageByIdentityProvider.keysSharedGet('google', "your result listener")
```

Data delete by identity provider:
Function takes 2 arguments: identityProvider(String), keys(List<String>).
Delete a specified key.
Does not return anything in case of success.
```Kotlin
storageByIdentityProvider.dataDelete("google", keys, "your result listener")
```

### Storage by wallet

Allows you to setup any data assosiated with wallet.
Function takes 4 arguments: walletAddress(String), key(String), value(String) and accessType(UserDataAccessScope). You can specify two types of accessType: public or private.
Does not return anything in case of success.
User data set by wallet:
```Kotlin
val storageByWallet = sdk.getStorage().StorageByWallet()
storageByWallet.dataSet(walletAddress='0xAABBCC', key, value, UserDataAccessScope.PUBLIC, "your result listener")
# or
storageByWallet.dataSet(walletAddress='0xAABBCC', key, value, UserDataAccessScope.PRIVATE, "your result listener")
```

User data get by wallet:
Function takes 2 arguments: walletAddress(String), key(String).
Returns the data under given key or None if data not found.
```Kotlin
storageByWallet.dataGet(walletAddress='0xAABBCC', key, "your result listener")
```

Get keys list by wallet:
Function takes 1argument: walletAddress(String).
Returns a structure with assosiated keys.
```Kotlin
storageByWallet.keysGet(wallet='0xAABBCC', "your result listener")
```

Get shared keys list by wallet:
Function takes 1 argument: walletAddress(String).
Returns a structure with assosiated keys or None if keys not found.
```Kotlin
storageByWallet.keysSharedGet(walletAddress='0xAABBCC', "your result listener")
```

Data delete by wallet:
Function takes 2 arguments: walletAddress(String), key(List<String>).
Delete a specified key.
Does not return anything in case of success.
```Kotlin
storageByWallet.dataDelete(walletAddress='0xAABBCC', keys, "your result listener")
```

Wallets get:
Function do not takes additional arguments and return list of ```WalletInfo```.
```Kotlin
storageByWallet.walletsGet("your result listener")
```

## License

### Code
The code in this repository, including all code samples in the notebooks listed above, is released under the [MIT license](LICENSE-CODE). Read more at the [Open Source Initiative](https://opensource.org/licenses/MIT).