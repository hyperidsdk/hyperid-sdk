# HyperId SDK documentation

This repository contains the HyperID SDK.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements

For running samples:
- [Visual Studio](https://visualstudio.microsoft.com/vs/)

### How to integrate HyperId into your project:

```C#
using HyperId.SDK;

# in Program.cs
var builder = WebApplication.CreateBuilder(args);
builder.Services.AddSingleton<IHyperIDSDK>(HyperIDSDKFactory.Instance());

#than you can use instance of SDK as DI in your constructors
 public YourModel(IHyperIDSDK hyperIDSDK)
 {
     this.hyperIdSDK = hyperIDSDK;
     this.sdkAuth = hyperIDSDK.GetAuth();
 }

....
ClientInfo client_info = ClientInfo(client_id="your.client.id",
                              client_secret="your.client.secret if available for your client",
                              redirect_uri="redirect.url",
                              privateRSAKey="RSAKey if available for your client"
                              authorizationMethod="your.client.authorization_method");

try
{
        await authSDK.InitAsync("your HyperId infrastructure type",
                        clientInfo,
                        ....);
}
catch(Exception ex)
{
        #error processing
}
....
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

```C#
try
{
        string url = sdkAuth.StartSignInWeb2("params");
        //or
        string url = sdkAuth.StartSignInWeb3("params");
        //or
        string url = sdkAuth.StartWalletGet("params");
}
catch(Exception ex)
{
        //error processing
}

```
Sign in using identity provider:
 ```C#
 try
 {
        string url = sdkAuth.StartSignInIdentityProvider("params");
 }
catch(Exception ex)
{
        //error processing
}
```

In this case you can obtain full list of identity providers using next method:
 ```C#
try
{
        List<string> idpList = sdkAuth.IdentityProviders();
        if("google" in idpList)
        {
                string url = sdkAuth.StartSignInIdentityProvider("google");
                ....
        }
}
catch(Exception ex)
{
        //error processing
}
....
```
## Additional options for sign in:

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verificationLevel parameter in the next sign in actions. It could be KycVerificationLevel.BASIC or KycVerificationLevel.FULL

```C#
try
{
        #...
        string url = sdkAuth.StartSignInWeb2(verificationLevel=KycVerificationLevel.FULL);
        # or
        string url = sdkAuth.StartSignInWeb3(verificationLevel=KycVerificationLevel.BASIC);
        # or
        string url = sdkAuth.StartSignInIdentityProvider(verificationLevel=KycVerificationLevel.FULL,
                identity_provider=...);
}
catch(Exception ex)
{
        //error processing
}
```

There are two types of wallet get modes: WalletGetMode.FAST and WalletGetMode.FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```C#
try
{
        string url = sdkAuth.startSignInWalletGet(walletGetMode=WalletGetMode.FAST);
}
catch(Exception ex)
{
        //error processing
}
```

Next additional parameter is walletFamily. This parameter allows you to specify chain family: ethereum or solana.
Here is quick example:
```C#
try
{
        string url = sdkAuth.startSignInWeb3(walletFamily=WalletGetMode.SOLANA);
        # or
        string url = sdkAuth.startSignInWalletGet(walletFamily=WalletGetMode.ETHEREUM);
}
catch(Exception ex)
{
        //error processing
}
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it:
```C#
try
{
        bool success = await sdkAuth.CompleteSignInAsync("entire redirect URL");
}
catch(Exception ex)
{
        //error processing
}
```
### KYC

User status get:\
Function takes 1 (KycVerificationLevel) argument: verificationLevel.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\

```C#
try
{
        val kycSDK = sdk.getKYC();
        KycUserStatusResponse status = await kycSDK.UserStatusGetAsync(KycVerificationLevel.FULL);
}
catch(Exception ex)
{
        //error processing
}
```

User status top level get:\
Function returns structure with kyc user info. Use this when user pass both kyc level, you will recieve top one.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\
```C#
try
{
        KycUserStatusTopLevelResponse topLevelStatus = await kycSDK.getUserStatusTopLevel();
}
catch(Exception ex)
{
        //error processing
}
```

### MFA

Check whether HyperId Authenticator installed or not:

```C#
try
{
        IHyperIDSDKMFA mfaSDK = sdk.GetMFA();
        bool isAvailable = await mfaSDK.AvailabilityCheckAsync();
}
catch(Exception ex)
{
        //error processing
}
```

Start MFA transaction:
Function takes 2 string arguments: question and code. Code is 2 digit string. Code is used for identification. Both question and code will appear in HyperID Authenticator.
Returns transaction id. You will need it in the next request to check the user response.
```C#
try
{
        int transactionId = await mfaSDK.TransactionStartAsync(question="Your question here",
                                                code="code");
}
catch(Exception ex)
{
        //error processing
}
```

Check stansaction status:
Function takes 1 integer argument: transactionId.
Returns the information about transaction, status and complete result.
```C#
try
{
        MFATransactionStatus ststus = mfaSDK.TransactionStatusCheckAsync(transactionId);
}
```

Cancel transaction:
Function takes 1 integer argument: transactionId.
Does not return anything in case of success.
```C#
try
{
        bool success = mfaSDK.TransactionCancelAsync(transactionId);
}
catch(Exception ex)
{
        //error processing
}
```

## Storage

### There four types of storages: email, user id, identity provider and wallet

### Storage by email
Allows you to setup any data assosiated with email. Email is taken from public user info you can get after authorization.
Function takes 3 arguments: key(str), value(str) and accessType(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by email:
```C#
try
{
        IStorageApiEmail storageByEmail = sdk.GetStorage().StorageByEmail();
        DataSetResult result = await storageByEmail.DataSetAsync(key, value, UserDataAccessScope.PUBLIC);
        # or
        DataSetResult result = await storageByEmail.DataSetAsync(key, value, aUserDataAccessScope.PRIVATE);
}
catch(Exception ex)
{
        //error processing
}
```

User data get by email:
Function takes 1 string argument: key(String).
Returns the data under given key or None if data not found.
```C#
try
{
        DataGetResult result = await storageByEmail.DataGetAsync(key);
}
catch(Exception ex)
{
        //error processing
}
```

Get keys list by email:
Returns a structure with assosiated keys.
```C#
try
{
        KeysGetResult result = await storageByEmail.KeysGetAsync();
}
catch(Exception ex)
{
        //error processing
}
```

Get shared keys list by email:
Returns list with assosiated keys or None in case of keys not found.
```C#
try
{
        KeysSharedGetResult result = await storageByEmail.KeysGetSharedAsync();
}
catch(Exception ex)
{
        //error processing
}
```

Data delete by email:
Function takes 1 string argument: keys(List<string>).
Delete a specified key.
Does not return anything in case of success.
```C#
try
{
        bool success = await storageByEmail.DataDeleteAsync(keys);
}
catch(Exception ex)
{
        //error processing
}
```
### Storage by user id

Allows you to setup any data assosiated with user id. User id is taken from public user info you can get after authorization.
Function takes 3 arguments: key(string), value(string) and accessType(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by user id:
```C#
try
{
        val storageByUserId = sdk.getStorage().StorageByUserId()
        DataSetResult result = await storageByUserId.DataSetAsync(key, value, UserDataAccessScope.PUBLIC);
        # or
        DataSetResult result = await storageByUserId.DataSetAsync(key, value, UserDataAccessScope.PRIVATE);
}
catch(Exception ex)
{
        //error processing
}
```

User data get by user id:
Function takes 1 string argument: key(string).
Returns the data under given key or None if data not found.
```C#
try
{
        DataGetResult result = await storageByUserId.DataGetAsync(key);
}
catch(Exception ex)
{
        //error processing
}
```

Get keys list by user id:
Returns a structure with assosiated keys.
```C#
try
{
        KeysGetResult result = await storageByUserId.KeysGetAsync();
}
catch(Exception ex)
{
        //error processing
}
```

Get shared keys list by user id:
Returns list with assosiated keys or None in case of keys not found.
```C#
try
{
        KeysSharedGetResult result = await storageByUserId.KeysGetSharedAsync("your result listener")
}
catch(Exception ex)
{
        //error processing
}
```

Data delete by user id:
Function takes 1 string argument: keys(List<string>).
Delete a specified key.
Does not return anything in case of success.
```C#
try
{
        bool success = await storageByUserId.DataDeleteAsync(keys);
}
catch(Exception ex)
{
        //error processing
}
```

### Storage by identity provider

Allows you to setup any data assosiated with identity provider.
Function takes 4 arguments: identityProvider(string), key(string), value(string) and accessType(UserDataAccessScope). You can specify two types of accessType: public or private.
Does not return anything in case of success.
User data set by identity provider:
```C#
try
{
        IStorageApiIdp storageByIdentityProvider = sdk.getStorage().StorageByIdp();
        DataSetByIdentityProviderResult result = await storageByIdentityProvider.DataSetAsync("google", key, value, UserDataAccessScope.PUBLIC);
        # or
        DataSetByIdentityProviderResult result = await storageByIdentityProvider.DataSetAsync('google', key, value, UserDataAccessScope.PRIVATE);
}
catch(Exception ex)
{
        //error processing
}
```

User data get by identity provider:
Function takes 2 arguments: identityProvider(string), key(string).
Returns the data under given key or None if data not found.
```C#
try
{
        DataGetByIdpResult result = await storageByIdentityProvider.DataGetAsync("google", key);
}
catch(Exception ex)
{
        //error processing
}
```

Get keys list by identity provider:
Function takes 1 argument: identityProvider(string).
(str).
Returns a structure with assosiated keys.
```C#
try
{
        KeysGetByIdpResult result = storageByIdentityProvider.KeysGetAsync('google');
}
catch(Exception ex)
{
        //error processing
}
```

Get shared keys list by identity provider:
Function takes 1 argument: identityProvider(String).
Returns a structure with assosiated keys or None if keys not found.
```C#
try
{
        KeysSharedGetByIdpResult result = await storageByIdentityProvider.KeysGetSharedAsync('google');
}
catch(Exception ex)
{
        //error processing
}
```

Data delete by identity provider:
Function takes 2 arguments: identityProvider(string), keys(List<string>).
Delete a specified key.
Does not return anything in case of success.
```C#
try
{
        DataDeleteByIdentityProviderRequestResult result = await storageByIdentityProvider.DataDeleteAsync("google", keys);
}
catch(Exception ex)
{
        //error processing
}
```

### Storage by wallet

Allows you to setup any data assosiated with wallet.
Function takes 4 arguments: walletAddress(String), key(String), value(String) and accessType(UserDataAccessScope). You can specify two types of accessType: public or private.
Does not return anything in case of success.
User data set by wallet:
```C#
try
{
        IStorageApiWallet storageByWallet = sdk.getStorage().StorageByWallet();
        DataSetByWalletResult result = await storageByWallet.DataSetAsync(walletAddress='0xAABBCC', key, value, UserDataAccessScope.PUBLIC);
        # or
        DataSetByWalletResult result = await storageByWallet.DataSetAsync(walletAddress='0xAABBCC', key, value, UserDataAccessScope.PRIVATE);
}
catch(Exception ex)
{
        //error processing
}
```

User data get by wallet:
Function takes 2 arguments: walletAddress(string), key(string).
Returns the data under given key or None if data not found.
```C#
try
{
        DataGetByWalletResult result = await storageByWallet.DataGetAsync(walletAddress='0xAABBCC', key);
}
catch(Exception ex)
{
        //error processing
}
```

Get keys list by wallet:
Function takes 1argument: walletAddress(string).
Returns a structure with assosiated keys.
```C#
try
{
        KeysGetByWalletResult result = await storageByWallet.KeysGetAsync(wallet='0xAABBCC');
}
catch(Exception ex)
{
        //error processing
}
```

Get shared keys list by wallet:
Function takes 1 argument: walletAddress(string).
Returns a structure with assosiated keys or None if keys not found.
```C#
try:
{
        KeysSharedGetByWalletResult result = await storageByWallet.KeysGetSharedAsync(walletAddress='0xAABBCC');
}
catch(Exception ex)
{
        //error processing
}
```

Data delete by wallet:
Function takes 2 arguments: walletAddress(String), keys(List<string>).
Delete a specified key.
Does not return anything in case of success.
```C#
try
{
        DataDeleteByWalletRequestResult result = await storageByWallet.DataDeleteAsync(walletAddress='0xAABBCC', keys);
}
catch(Exception ex)
{
        //error processing
}
```
