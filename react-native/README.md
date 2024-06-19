## Authorization in Hyper ID with React Native application using macOS and XCode iOS application

HyperID extends OpenID Connect protocol and you can easily authorize your user in the same way like other identity providers using any third party library what you like.
> Note: In this sample we're using `react-native-app-auth` package but you are free to choose more simple or complicated solutions for this

## Sample

All next acctions you could execute in Terminal what you like

### 1. Clone this repo
```Bash
    git clone <link>
```
### 2. Make sure that you have installed:
`nodeJS`, `npm`, `yarn` and `cocoapods`.

> If you don't, use `homebrew` to do this:
>  
> #### 2.1. Insall `homebrew`
> ```Bash
>    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
> ```
>
> #### 2.2. Instal `NodeJS` with `npm`
>
> ```Bash
>    brew install node
> ```
>
> #### 2.3. Install `yarn`
>
> ```Bash
>    npm install --global yarn
> ```
>
> #### 2.4. Install `cocoapods`
>
> ```Bash
>    sudo gem install cocoapods
> ```

### 3. Open cloned repo

```Bash
    cd /path/to/the/react-native-app-auth
```

### 4. Open example folder

```Bash
    cd ./Example
```

### 5. Resolve Example dependencies with `yarn`

```Bash
    yarn install
```

### 6. Open `ios` folder and resolve `XCode` dependencies with `cocoapods`

```Bash
    pod install
```

### 7. Configure application for your client

* Open `Example/App.js` and edit your `auth` config (row 20). You should replace `clientId`, `clientSecret`, `redirectUrl` placeholders with your data recieved after client registration.
* Search and replace in project redirect url scheme `your.custom.scheme` to yours for correct authorization completion processing usualy it's `build.gradle` and `Info.plist` files.

### 8. Let's open `XCode` solution in `Example/ios` folder

```Bash
    open Example.xcworkspace
```

### 9. Try to build solution

For this you can use `Command+B` shortcut or use app menu `Product -> Build`

> Note: You could have some issues with third party Facebook's `FlipperKit`. You can easily fix that by editing `FlipperTransportTypes.h` header file and add include of C++ STL header right after `#include <string>` at row 10:
>> ```C++
>> #include <functional>
>> ```

### 10. Run

1. Make sure your scheme has `Release` build configuration and run the project using `XCode`. Use `Run` button or use app menu `Product -> Run`.







## Authorization in Hyper ID with React Native application using Windows and Android Studio

HyperID extends OpenID Connect protocol and you can easily authorize your user in the same way like other identity providers using any third party library what you like.
> Note: In this sample we're using `react-native-app-auth` package but you are free to choose more simple or complicated solutions for this

## Sample

All next acctions you could execute in Terminal what you like

### 1. Clone this repo
```Bash
    git clone <link>
```
### 2. Make sure that you have installed:
`nodeJS`, `npm`, `yarn`.

### 3. Open cloned repo

```Bash
    cd /path/to/the/react-native-app-auth
```

### 4. Open example folder

```Bash
    cd ./Example
```

### 5. Resolve Example dependencies with `yarn`

```Bash
    yarn
```

### 7. Configure application for your client

* Open `Example/App.js` and edit your `auth` config (row 20). You should replace `clientId`, `clientSecret`, `redirectUrl` placeholders with your data recieved after client registration.
* Search and replace in project redirect url scheme `your.custom.scheme` to yours for correct authorization completion processing usualy it's `build.gradle` files.

### 8. Let's open `AndroidStudio` solution in `Example/ios` folder

### 9. Try to build solution

### 10. Generate signed app with release configuration


## How to use `HyperID` authorization flows

`HyperID` fully support OpenID Connect but also extends it. Expample application demonstrates it. You can press on picker in center to select one of the next flows

### 1. Regular authorization

You can use `HyperID` as default OpenID Connect identity provider. You can pick with picker `Clear Auth` and use default configuration for `react-native-app-auth`:
```JS
{
      issuer: 'https://login.hypersecureid.com/auth/realms/HyperID',
      clientId: 'your_client_id',
      redirectUrl: 'your.custom.scheme://localhost:4200/auth/callback',
      clientSecret: "your_client_secret",
      clientAuthMethod: "basic",
      additionalParameters: {},
      scopes: ['openid', 'email', 'user-data-set', 'user-data-set'],
   
      serviceConfiguration: {
        authorizationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/auth',
        tokenEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/token',
        revocationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/revoke'
      }
}
```

### 2. Wallet get auth flow

Wallet auth flow presents flow which allows you to login user with HyperID account and his crypto wallet. After login completion user could create or use attached to the account wallet. Information about wallet will be attached to user's `access token` which could be extracted with any `JWT` library.
> You can find `JWT` libraries here https://jwt.io/libraries

Use `wallet_get_mode` parameter to specify wallet add mode.
* `auto wallet get` value `0` gets wallet automaticaly
* `wallet get fast` value `2` gets wallet without signing data
* `wallet get full` value `3` gets wallet and waiting for completing signing data with user's wallet

```JS
{
      issuer: 'https://login.hypersecureid.com/auth/realms/HyperID',
      clientId: 'your_client_id',
      redirectUrl: 'your.custom.scheme://localhost:4200/auth/callback',
      clientSecret: "your_client_secret",
      clientAuthMethod: "basic",
      additionalParameters: {
        flow_mode:        "4",
        wallet_get_mode:  "2"
      },
      scopes: ['openid', 'email', 'user-data-set', 'user-data-set'],
   
      serviceConfiguration: {
        authorizationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/auth',
        tokenEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/token',
        revocationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/revoke'
      }
}
```

### 3. Identity provider flow

In HyperID user can select other identity provider like `Google`, `AppleID`, `Discord`, `Microsoft`, but you can use exact identity provider using `flow_mode` value `9` and `identity_provider` value like `google` or `apple`.
> Detailed list of supported identity providers at https://login.hypersecureid.com/auth/realms/HyperID/.well-known/openid-configuration in `identity_providers` array
```JS
{
      issuer: 'https://login.hypersecureid.com/auth/realms/HyperID',
      clientId: 'your_client_id',
      redirectUrl: 'your.custom.scheme://localhost:4200/auth/callback',
      clientSecret: "your_client_secret",
      clientAuthMethod: "basic",
      additionalParameters: {
        flow_mode:          "9",
        identity_provider:  "google"
      },
      scopes: ['openid', 'email', 'user-data-set', 'user-data-set'],
   
      serviceConfiguration: {
        authorizationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/auth',
        tokenEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/token',
        revocationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/revoke'
      }
}
```

### 4. Transaction flow

HyperID allows authorize user for transaction. After login transaction will be started and returns transaction hash as additional parameter. All you need in field `transaction` give contract/wallet address in `to` field, specify `chain` and `data`. Optionaly you can setup exact client wallet address in `from` field(if you not HyperID will ask user for wallet). If transaction need some paymet you could setup `value` of transaction. To be sure about transaction identification you could setup `nonce` field.
> Optionaly `data` field could be empty in case raw value transfer, but then you should set `value`

```JS
{
      issuer: 'https://login.hypersecureid.com/auth/realms/HyperID',
      clientId: 'your_client_id',
      redirectUrl: 'your.custom.scheme://localhost:4200/auth/callback',
      clientSecret: "your_client_secret",
      clientAuthMethod: "basic",
      additionalParameters: {
        transaction: '{"from":"0x43D192d3eC9CaEFbc92385bED8508d87E566595f","to":"0x0AeB980AB115E45409D9bA33CCffcc75995E3dfA","chain":"11155111","data":"0x70a0823100000000000000000000000043d192d3ec9caefbc92385bed8508d87e566595f"}'
      },
      scopes: ['openid', 'email', 'user-data-set', 'user-data-set'],
   
      serviceConfiguration: {
        authorizationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/auth',
        tokenEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/token',
        revocationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/revoke'
      }
    }
```

After authorization complete you will get `authorizeAdditionalParameters` in `AuthorizeResult` (`authorize`'s result):
* `transaction_hash` - hash of approved by user transaction
* `transaction_result` - error code, 0 if success
* `transaction_result_description` - error code description

### 5. Additional parameters

You could customize selected flow with some additionals parameters in configuration
* `login_types_to_show`: by default all types enabled but you can specify visibility of each panel:
    * `web2` authorize user with email
    * `web3` authorise user with crypto wallet
    * `idp`  authorize user with some of identity providers
* `verification_level`: KYC verification is disabeled by default. Specifying this parameter with value `3` requires confirmed KYC. Parameter with value `4` requires also address confirmation.
* `login_hint`: You could use this parameter when user goes to the relogin with same account and you don`t need to to get the brand new account after flow completion
* `prompt` = `'select_account'`: on Android and iOS without ephemeral sesssion usage you can allow user to select account from already entered accounts. Parameter is optional but strongly recomended.
```JS
additionalParameters:
{
    login_types_to_show:    'web2 web3 idp',
    verification_level:     '4',
    login_hint:             'awersome.user@awersome.email.com',
    prompt:                 'select_account',
}
```

### 6. Guest upgrade

If user are entering with `web3` crypto wallet and wallet is unknown for HyperID user gets guest account with virtual `login`(email). To fix this you can call `guest upgrade` authorization flow:
```JS
{
      issuer: 'https://login.hypersecureid.com/auth/realms/HyperID',
      clientId: 'your_client_id',
      redirectUrl: 'your.custom.scheme://localhost:4200/auth/callback',
      clientSecret: "your_client_secret",
      clientAuthMethod: "basic",
      additionalParameters: {
        flow_mode:        "6",
      },
      scopes: ['openid', 'email', 'user-data-set', 'user-data-set'],
   
      serviceConfiguration: {
        authorizationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/auth',
        tokenEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/token',
        revocationEndpoint: 'https://login.hypersecureid.com/auth/realms/HyperID/protocol/openid-connect/revoke'
      }
    }
```

### Working with user's data storage

HyperID gives you posibility to store user related data to user's storarage as key-value pair as well as get information about users wallet. For each request you should setup `restApiEndpoint` - url which sample gets from OpenID Configuration and `accessToken` for user authorized request.

### 1. User wallets

Request returns list of all wallets attached to the user`s account:
* public wallets accessible to the every client and yours
* private wallets accessible only for your client only

```JS
let response = await walletsGet({ restApiEndpoint: authState.restApiEndpoint },
        { accessToken: authState.accessToken });
```

### 2. Data set

Request which setup for `key` paired `value`.

```JS
let response = await dataSet({ restApiEndpoint : authState.restApiEndpoint }, {
        accessToken: authState.accessToken,
        dataKey: "your data key there",
        dataValue: "your data value",
      });
```

### 3. Data get

Request which returns `value` for given `key`

```JS
let response = await dataGet({ restApiEndpoint : authState.restApiEndpoint }, {
        accessToken: authState.accessToken,
        dataKey: "your data key there",
      });
```