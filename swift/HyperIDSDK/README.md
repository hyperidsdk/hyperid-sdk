# HyperID SDK Swift

HyperID SDK for Swift applications

## Overview

The `HyperID SDK` for Swift is your entry-point to secure, passwordless authentication for your mobile app. This guide will cover some important topics for getting started with iOS APIs and to make the most of HyperID's features.

## Getting started

Use GitHub repository link to add our SDK to your projects via Swift Package Manager.

A HyperIDSDK class is the entry-point to the HyperID SDK. It must be instantiated with next parameters

### HyperIDSDK(clientInfo: ClientInfo, authRestoreInfo: String?, providerInfo: ProviderInfo,urlSession: URLSession!)

|Parameter     | Type     | Definition|
---------------|----------|-----------|
|`clientInfo`     |`ClientInfo`|Client credentials retrieved<br>from HyperID Dashboard, which should include<br> `client_id`, `redirect_uri` and specified `authorization method` for your<br>client with `secret` data to identify your client.|
|`authRestoreInfo`|`String?`   |HyperID SDK authorization restore information<br> which allows store authorization state to your external storage like DB, optional.|
|`providerInfo`   |`ProviderInfo`| Configuration object defines HyperID infrastructure to connect (`production` or `sandbox`). Default value - `production`|
|`urlSession`|`URLSession` | Apple Foundation framework's URLSession as the way to send user requests to the HyperID. Optional, default value `URLSession.shared`, but you can provide URLSession configured by your own.|

#### Sample

```Swift
//client info for default auth with pair client_id, client_secret
let clientInfo = ClientInfo(clientId:			"your_client_id",
			    redirectURL:		"custom_protocol://localhost:42",
			    authorizationMethod:	.clientSecret(secret: "your_secret"))
```
```Swift
//client info for extended auth with JWT token signed with your secret using HS256
let clientInfoHS256 = ClientInfo(clientId:		"your_client_id_with_hs256_support",
				 redirectURL:		"custom_protocol://localhost:42",
				 authorizationMethod:	.clientHS256(secret: "your_secret"))
```
```Swift
//client info for extended auth with JWT token signed with your secret using RS256 private key
let keyData : Data = /*put your private key data here*/
let clientInfoHS256 = ClientInfo(clientId:		"your_client_id_with_hs256_support",
				 redirectURL:		"custom_protocol://localhost:42",
				 authorizationMethod:	.clientRS256(privateKey: keyData))
```
```Swift
//default HyperID SDK initialization for production
let hyperIdSDK = try await HyperIDSDK(clientInfo:       clientInfo,
				      authRestoreInfo:  "data stored from previous run")
```
```Swift
//default HyperID SDK initialization for production
let session = URLSession(configuration: URLSessionConfiguration.default)
let hyperIdSDK = try await HyperIDSDK(clientInfo:       clientInfo,
				      authRestoreInfo:  "data stored from previous run",
				      providerInfo:     .sandbox,
				      urlSession:       session)
```
In case of incorrect initialization, the constructor may `throw` the following exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidProviderInfo` | Invalid provider info. No HyperID infrastructure found by this provider info. Please check your input |
|`HyperIDBaseAPIError.serverMaintenance` | HyperID infrastructure on maintenance and not ready to process user requests. Please try later |
|`HyperIDBaseAPIError.networkingError`| Networking error raised by URLSession during initialization.|

## Authorization

### startSignInWeb2(kycVerificationLevel:	KYCVerificationLevel? = nil) -> URL

`HyperID SDK` method starts auth with using Web2 and returns ready-to-sign-in URL to HyperID services.
| Parameter | Type | Definition|
|-|-|-|
|kycVerificationLevel | KYCVerificationLevel? | Optional parameter, disabled by default. You can specify user KYC check<br>during authorization|

#### Sample

```Swift
let url = try hyperIdSDK.startSignInWeb2()
```
```Swift
let url = try hyperIdSDK.startSignInWeb2(kycVerificationLevel: .basic)
```
```Swift
let url = try hyperIdSDK.startSignInWeb2(kycVerificationLevel: .full)
```

In case of invalid usage, the method could `throw` the following exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidKYCVerificationLevel`| Invalid KYCVerificationLevel value. Please select one of the presented in KYCVerificationLevel

### startSignInWeb3(walletFamily: Int64?, kycVerificationLevel:	KYCVerificationLevel?) -> URL

The method starts auth with using Web3 and returns ready-to-sign-in URL to HyperID services.
| Parameter | Type | Definition|
|-|-|-|
|`walletFamily` | `Int64` | Parameter which specify working network for Web3 (`Ethereum` by default)|          
|`kycVerificationLevel` | `KYCVerificationLevel?` | Optional parameter, disabled by default. You can specify user KYC check<br>during authorization|

## Sample

```Swift
let url = try hyperIdSDK.startSignInWeb3()
```
```Swift
let url = try hyperIdSDK.startSignInWeb2(kycVerificationLevel: .basic)
```
```Swift
let url = try hyperIdSDK.startSignInWeb2(walletFamily:         1,
					 kycVerificationLevel: .full)
```

List of WalletFamilies could be discovered in `HyperIDSDK`'s `openIDConfiguration` field. In case of invalid usage, the method could `throw` the following exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidWalletFamily`| Invalid `walletFamily` value. Please select one of the presented in `openIDConfiguration`|
|`HyperIDBaseAPIError.invalidKYCVerificationLevel`| Invalid KYCVerificationLevel value. Please select one of the presented in `KYCVerificationLevel` enum|

### startSignInUsingWallet(walletGetMode: WalletGetMode, walletFamily: WalletFamily?) -> URL

The method starts auth with using your cryptowallet and returns ready-to-sign-in URL to HyperID services.
| Parameter | Type | Definition|
|-|-|-|
|`walletGetMode`| `WalletGetMode`| `.autoWalletGet` allow to join wallet automaticaly <br>`.walletGetFast` value(default) allows to join the wallet to the HyperID witount verifying the user's ownership<br>`.walletGetFull` value allows to join new one with verifying signature or resotre session to existing wallet
|`walletFamily` | `WalletFamily` | Parameter which specify working network for Web3 (`Ethereum` by default)|

#### Sample

```Swift
let url = try hyperIdSDK.startSignInUsingWallet()
```
```Swift
let url = try hyperIdSDK.startSignInUsingWallet(walletGetMode: .walletGetFull)
```
```Swift
let url = try hyperIdSDK.startSignInUsingWallet(walletFamily: 0)
```
```Swift
let url = try hyperIdSDK.startSignInUsingWallet(walletGetMode:	.walletGetFull,
					       	walletFamily:	1)
```
List of available WalletFamilies could be discovered in `HyperIDSDK`'s `openIDConfiguration` field. In case of invalid usage, the method could `throw` the following exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidWalletFamily`| Invalid `walletFamily` value. Please select one of the presented in `openIDConfiguration`|

### startSignInGuestUpgrade()`

The method starts auth flow for guest account upgrade and returns ready-to-sign-in URL to HyperID services.

#### Sample

```Swift
let url = try hyperIdSDK.startSignInGuestUpgrade()
```

### startSignInIdentityProvider(identityProvider: String, kycVerificationLevel: KYCVerificationLevel?) -> URL

The method starts auth with using Web2 and returns ready-to-sign-in URL to HyperID services with using specified identity provider.

| Parameter | Type | Definition|
|-|-|-|
|identityProvider | IdentityProvider | String value which specifies identity providers such as `google` `apple` `.twitter` `discord`, etc. (see details in actual OpenIDConfiguration)|
|kycVerificationLevel | KYCVerificationLevel? | Optional parameter, disabled by default. You can specify user KYC check<br>during authorization|

#### Sample

```Swift
let url = try hyperIdSDK.startSignInIdentityProvider(identityProvider: "google")
```
```Swift
let url = try hyperIdSDK.startSignInIdentityProvider(identityProvider:		"google"
						     kycVerificationLevel:	.full)
```
Full list of available `identity proviiders` could be discovered in `HyperIDSDK`'s openIDConfiguration field. In case of invalid usage, the method could throw the following exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.unknownIdentityProvider`| Identity provider invalid. Please check availability in provider configuration.|

### completeSignIn(redirectURL: URL)

After web part of authorization, HyperID should redirect you to the redirect URL specified in `ClientInfo`. Use it to complete authorization and continue your work with HyperID SDK.
| Parameter | Type | Definition|
|-|-|-|
|`redirectURL` | `URL` | Redirect URL recieved from the `HyperID`|

#### Sample

```Swift
let url = URL(string: "redirect_url")! //replace this with your url

try await hyperIDSDK.completeSignIn(redirectURL: url)
```

During execution this code should call next exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidClientInfo`| Client info installed during initialization invalid. Please reinit SDK and start new authorization.|
|`HyperIDAuthAPIError.authorizationInvalidRedirectURLError`| Recieved URL is invalid to complete authorization. Please restart authorization and try again.|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues

### startSignInWithTransaction(from: String?, to: String, chain: String, data: String, gas: String?, nonce: String?, value: String?) -> URL

The method starts auth with using Web3 and returns ready-to-sign-in URL to HyperID services, starts transaction with parameters and complete the process only transaction completion.

| Parameter | Type | Definition|
|-|-|-|
|`from` | `String?` | User wallet address. Optional, if empty `HyperID` will give you the choice from your attached wallets list |
|`to` | `String` | Contract address |
|`chain` | `String` | Contract chain id |
|`data` | `String` | External formed data |
|`gas` | `String?` | Fee value. Optional |
|`nonce` | `String?` | Your transaction id in this chain. Optional |
|`value` | `String?` | Amount of native tokens to pay. Optional |


```Swift
let url = try hyperIdSDK.startSignInWithTransaction(from:	"0x43D192d3eC9CaEFbc92385bGD3508d87E566595f",
                                                    to:		"0x0AeB980AB115E45409D9bA31CCffcc75995E3dfA",
                                                    chain:	"11155111",
                                                    data:	"0x0",
                                                    nonce:	"0",
                                                    value:	"0x1")
```

|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.transactionDataAndValueIsEmpty`| Transaction data and value are empty. Please set one of this values |

### completeSignInWithTransaction(redirectURL: URL)

After web part of authorization for transaction, HyperID should redirect you to the redirect URL specified in `ClientInfo`. Use it to complete authorization and continue your work with HyperID SDK.
| Parameter | Type | Definition|
|-|-|-|
|`redirectURL` | `URL` | Redirect URL recieved from the `HyperID`|

#### Sample

```Swift
let url = URL(string: "redirect_url")! //replace this with your url

let transactionHash = try await hyperIDSDK.completeSignInWithTransaction(redirectURL: url)
```

During execution this code should call next exceptions:
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.invalidClientInfo`| Client info installed during initialization invalid. Please reinit SDK and start new authorization.|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues
|`HyperIDAuthAPIError.authorizationInvalidRedirectURLError`| Recieved URL is invalid to complete authorization. Please restart authorization and try again.|
|`HyperIDAuthAPIError.transactionInvalidParameters`| Invalid transaction parameters.|
|`HyperIDAuthAPIError.transactionRejectedByUser`| Transaction was terminated by user.|

### isAuthorized

HyperID SDK's variable indicatiing is curent instance of SDK is ready to work with user's HyperID data.

### authRestoreInfo

HyperID SDK's variable which contains user auth restore info if `isAuthorized` is positive. Contains string which allows you to restore user authorization without runing dialouge through HyperID services. Please manage secure storing of this info(db, files in app container or app/group bundle variables) or prepare your code and user to ask authorization every time when HyperID used.

### signOut()

To complete managing authorization you are able to sign out your account.

#### Sample

```Swift
try await hyperIdSDK.signOut()
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### getUserInfo() -> UserInfo

Current authorized user info (see `UserInfo` definition)

##### Sample

```Swift
let userInfo = try async hyperIdSDK.getUserInfo()
```
Possible runtime issues

|Exception| Description|
|---------|------------|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|

## KYC

`HyperID SDK` methods which gives you posibility to check user KYC data

### getUserKYCStatusInfo(kycVerificationLevel:	KYCVerificationLevel)-> UserKYCStatusInfo?

The method returns user KYC data(or `nil` if apsent).
| Parameter | Type | Definition|
|-|-|-|
|kycVerificationLevel | KYCVerificationLevel | Specify what KYC level data you need `.basic` or `.full`(`.basic` as default)|

#### Sample

```Swift
let userKYCData = hyperIdSDK.getUserKYCStatusInfo()
```
```Swift
let userKYCData = hyperIdSDK.getUserKYCStatusInfo(kycVerificationLevel: .full)
```

Possible runtime issues

|Exception| Description|
|---------|------------|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### getUserKYCStatusTopLevelInfo() -> UserKYCStatusTopLevelInfo?

The method returns user top level KYC data(or `nil` if apsent).

#### Sample

```Swift
let userKYCData = hyperIdSDK.getUserKYCStatusTopLevelInfo()
```

Possible runtime issues

|Exception| Description|
|---------|------------|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

## MFA

HyperID allows you to ask user action cofirmation via HyperID Authenticator

### checkMFAAvailability() -> Bool

The method allows checking whether the user is using the HyperID Authenticator.

#### Sample

```Swift
let isAvailable = try await hyperIdSDK.checkMFAAvailability()
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### startTransaction(question: String, controlCode: Int) -> Int

The method allows you to initiate MFA question for user with confirmation code to verify code sender and returns transactionId.
| Parameter | Type | Definition|
|-|-|-|
|`question` | `String` | Request text, question what you wanna ask user with confirm or deny response|
|`controlCode`|`Int`| Control code in range 0-99 as the way to verify received request

#### Sample

```Swift
let transactionId = try await hyperIdSDK.startTransaction(question: "Your awesome question", 
							  controlCode: 42)
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDMFAAPIError.controlCodeInvalidValue`| Applied code not in described before range.
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### getMFATransactionStatus(transactionId: Int) -> MFATransactionStatus?

The method returns `MFATransactionStatus` of specified transaction(or `nil` if transaction doesn't exists).
| Parameter | Type | Definition|
|-|-|-|
|`transactionId` | `Int` | Transaction id initiated by your client for this user|
#### Sample
```Swift
let status = try await hyperIdSDK.getTransactionStatus(transactionId: 42)
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### cancelMFATransaction(transactionId: Int) -> MFATransactionStatus?

The metod cancels MFA transaction for user.
| Parameter | Type | Definition|
|-|-|-|
|`transactionId` | `Int` | Transaction id initiated by your client for this user|
#### Sample
```Swift
let status = try await hyperIdSDK.cancelMFATransaction(transactionId: 42)
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDMFAAPIError.MFATransactionNotFound`| HyperID MFA transaction with specifier ID not found|
|`HyperIDMFAAPIError.MFATransactionAlreadyCompleted`| HyperID MFA transaction with specifier ID already completed and cannot be canceled.|
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

## Storage

HyperID allows you to work user's wallets described with specified structure which defines public information about `Wallet`: `address`, `chain`, `family`, `label` given by user

### getUserWallets() -> (walletsPrivate: [Wallet], walletsPublic: [Wallet])

The method returns tuple of 2 named arrays with your client private and public user's wallet. Method have no input parameters

### Sample

```Swift
let result = try await hyperIdSDK.getUserWallets()

// ...
```
 
HyperID allows you to store additional user data in 4 kinds of storage: `.email`, `.userID`, `.wallet`(with specified address) and `.identityProvider`(for each of identity providers) which can be `.private` as well as `.public` and shared between another HyperID clients.

### getUserKeysList(storage: HyperIDStorage) -> (keysPrivate: [String], keysPublic: [String])

The method returns tuple of 2 named arrays with your client private and public keys from user's storage.
| Parameter | Type | Definition|
|-|-|-|
|`storage` | `HyperIDStorage` | Enum which specifies type of user storage

#### Sample

```Swift
let result = try await hyperIdSDK.getUserKeysList(storage: .email)
print("private: \(result.keysPrivate)")
print("public: \(result.keysPublic)")
```
```Swift
let result = try await hyperIdSDK.getUserKeysList(storage: .userId)
print("private: \(result.keysPrivate)")
print("public: \(result.keysPublic)")
```
```Swift
let result = try await hyperIdSDK.getUserKeysList(storage: .wallet(address: "0x42"))
print("private: \(result.keysPrivate)")
print("public: \(result.keysPublic)")
```
```Swift
let result = try await hyperIdSDK.getUserKeysList(storage: .identityProvider("apple"))
print("private: \(result.keysPrivate)")
print("public: \(result.keysPublic)")
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDStorageAPIError.identityProviderNotFound` | Given identity provider is not supported. Check actual list `from openIDConfiguration`
|`HyperIDStorageAPIError.walletNotExists`| Crypto wallet with given address doesn't exists or curent user is not wallet owner |
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### getUserSharedKeysList(storage: HyperIDStorage) -> [String]

The method returns full list of available public keys shared by other clients in next format `"\(clientName)/\(keyName)"`.
| Parameter | Type | Definition|
|-|-|-|
|`storage` | `HyperIDStorage` | Enum which specifies type of user storage

#### Sample

```Swift
let result = try await hyperIdSDK.getUserSharedKeysList(storage: .email)
print("sharedPublic: \(result)")
```
```Swift
let result = try await hyperIdSDK.getUserSharedKeysList(storage: .userId)
print("sharedPublic: \(result)")
```
```Swift
let result = try await hyperIdSDK.getUserSharedKeysList(storage: .wallet(address: "0x42"))
print("sharedPublic: \(result)")
```
```Swift
let result = try await hyperIdSDK.getUserSharedKeysList(storage: .identityProvider("apple"))
print("sharedPublic: \(result)")
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDStorageAPIError.identityProviderNotFound`| Given identity provider is not supported. Check actual list `from openIDConfiguration`
|`HyperIDStorageAPIError.walletNotExists`| Crypto wallet with given address doesn't exists or curent user is not wallet owner |
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### setUserData(_ value: (key: String, value: String), dataScope: UserDataScope, storage: HyperIDStorage)

The method allows setup user key-value pair in selected data scope in selected storage.
| Parameter | Type | Definition|
|-|-|-|
|`value`| `(key: String, value: String)` tuple| Key value pair|
|`dataScope`|`UserDataScope`| Data scope for this data (`.private` or `.public`[default])|
|`storage` | `HyperIDStorage` | Enum which specifies type of user storage|

#### Sample

```Swift
try await hyperIdSDK.setUserData((key: "testKeyPrivateEmail", value: "testValuePrivateEmail"),
				 dataScope:	.private,
				 storage:	.email)
```
```Swift
try await hyperIdSDK.setUserData((key: "testKeyPublicUserID", value: "testValuePublicUserID"),
				 dataScope:	.public,
				 storage:	.userID)
```
```Swift
try await hyperIdSDK.setUserData((key: "testKeyPublicWallet", value: "testValuePublicWallet"),
				 dataScope:	.public,
				 storage:	.wallet(address: "0x42"))
```
```Swift
try await hyperIdSDK.setUserData((key: "testKeyPrivateIdp", value: "testValuePrivateIdp"),
				 dataScope:	.private,
				 storage:	.identityProvider("google"))
```

Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDStorageAPIError.keyInvalid`| Please check your value key. It should be not empty|
|`HyperIDStorageAPIError.keyAccessDenied`| You have no rights to change this key value|
|`HyperIDStorageAPIError.identityProviderNotFound`| Given identity provider is not supported. Check actual list `from openIDConfiguration`
|`HyperIDStorageAPIError.walletNotExists`| Crypto wallet with given address doesn't exists or curent user is not wallet owner |
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### getUserData(_ key: String, storage: HyperIDStorage) -> String?

The method returns value for given key from the storage or nil if value apsent.
| Parameter | Type | Definition|
|-|-|-|
|`key`| `String` | Key |
|`storage` | `HyperIDStorage` | Enum which specifies type of user storage|

#### Sample

```Swift
let value = try await hyperIdSDK.getUserData("testKeyPrivateEmail",
					     storage:	.email)
```
```Swift
let value = try await hyperIdSDK.getUserData("testKeyPublicUserID",
					     storage:	.userID)
```
```Swift
let value = try await hyperIdSDK.getUserData("testKeyPublicWallet",
					     storage:	.wallet(address: "0x42"))
```
```Swift
let value = try await hyperIdSDK.getUserData("testKeyPrivateIdp",
					     storage:	.identityProvider("google"))
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDStorageAPIError.identityProviderNotFound`| Given identity provider is not supported. Check actual list `from openIDConfiguration`
|`HyperIDStorageAPIError.walletNotExists`| Crypto wallet with given address doesn't exists or curent user is not wallet owner |
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|

### deleteUserData(_ key: String, storage: HyperIDStorage)

The method removes value with key from the storage.
| Parameter | Type | Definition|
|-|-|-|
|`key`| `String` | Key |
|`storage` | `HyperIDStorage` | Enum which specifies type of user storage|

#### Sample

```Swift
try await hyperIdSDK.deleteUserData("testKeyPrivateEmail",
				    storage: .email)
```
```Swift
try await hyperIdSDK.deleteUserData("testKeyPublicUserID",
				    storage: .userID)
```
```Swift
try await hyperIdSDK.deleteUserData("testKeyPublicWallet",
				    storage: .wallet(address: "0x42"))
```
```Swift
try await hyperIdSDK.deleteUserData("testKeyPrivateIdp",
				    storage: .identityProvider("google"))
```
Possible runtime issues
|Exception| Description|
|---------|------------|
|`HyperIDStorageAPIError.identityProviderNotFound`| Given identity provider is not supported. Check actual list `from openIDConfiguration`
|`HyperIDStorageAPIError.walletNotExists`| Crypto wallet with given address doesn't exists or curent user is not wallet owner |
|`HyperIDSDKError.authorizationExpired`| HyperID authorization expired. Please authorize user again from the start|
|`HyperIDBaseAPIError.serverMaintenance`|HyperID infrastructure on maintenance and not ready to process user requests. Please try later|
|`HyperIDBaseAPIError.networkingError`| URLSession networking issues|
