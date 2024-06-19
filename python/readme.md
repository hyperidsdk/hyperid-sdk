# HyperId SDK documentation

This repository contains the HyperID SDK.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. 

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To build the HyperID SDK, run the following at the command-line:
```terminal
$ python -m build
```

To install the HyperID SDK, run the following at the command-line:
```terminal
$ pip install --no-index --no-deps -f ./dist hyperid
```

For running samples:
- [Python 3.10.x](https://www.python.org/)
- [FLask](https://flask.palletsprojects.com/en/3.0.x/)

### How to integrate HyperId into your project:

```python
from hyperid.sdk.sdk import Sdk
from hyperid.auth.enum import InfrastructureType
from hyperid.error import HyperIdException
from hyperid.auth.client_info import ClientInfoBasic

client_info = ClientInfoBasic(client_id="your.client.id",
                              client_secret="your.client.secret",
                              redirect_uri="redirect.url")
sdk = Sdk(client_info=client_info, infrastructure_type=InfrastructureType.PRODUCTION)
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

```python
url = sdk.start_sign_in_web2()
url = sdk.start_sign_in_web3()
url = sdk.start_sign_in_wallet_get()
```
Sign in using identity provider:
 ```python
url = sdk.start_sign_in_by_identity_provider('google')
```

In this case you can obtain full list of identity providers using next method:
 ```python
    idp = sdk.get_discover().identity_providers
    if 'twitter' in idp
        url = sdk.start_sign_in_by_identity_provider('twitter')
    # other code
```
## Additional options for sign in:

HyperID has implemented a KYC procedure that can range from basic to complete, depending on the level of verification required. You could set a verification_level parameter in the next sign in actions. It could be VerificationLevel.KYC_BASIC or VerificationLevel.KYC_FULL

```python
#...
url = start_sign_in_web2(self, verification_level=VerificationLevel.KYC_FULL)
# or
url = start_sign_in_web3(self, verification_level=VerificationLevel.KYC_FULL)
# or
url = start_sign_in_by_identity_provider(self, verification_level=VerificationLevel.KYC_FULL, identity_provider=...)
```

There are two types of wallet get modes: WalletGetMode.WALLET_GET_FAST and WalletGetMode.WALLET_GET_FULL\
Fast mode allows to join a wallet to the current HyperID session without verifying the user's ownership of the crypto wallet's private key.\
Full mode allows to join new one with verifying signature or restore session to existing wallet.

Here is quick example:
```python
url = start_sign_in_wallet_get(wallet_get_mode=WalletGetMode.WALLET_GET_FAST)
```

Next additional parameter is wallet_family. This parameter allows you to specify chain family: evm or solana.
Here is quick example:
```python
url = start_sign_in_web3(wallet_family=WalletGetMode.SOLANA)
# or
url = start_sign_in_wallet_get(wallet_family=WalletGetMode.ETHEREUM)
```

## Completing Authorization

After sign in in HyperID you will receive the callback to your redirect url. Here is the code to handle it:
  ```python
try:
    sdk.complete_sign_in(request)
except HyperIdException as e:
    # error handling
```

## API calls
All API call will generate HyperIdException exception in case of error.

### KYC

User status get:\
Function takes 1 (VerificationLevel) argument: verification_level.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\

```python
try:
    r = sdk.get_user_status(verification_level=VerificationLevel.KYC_FULL)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

User status top level get:
Function returns structure with kyc user info. Use this when user pass both kyc level, you will recieve top one.
Returns a structure with kyc user info or None if user do not have accociated kyc info.\
```python
try:
    r = sdk.get_user_status_top_level()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

### MFA

Check whether HyperId Authenticator installed or not:
Returns bool.
```python
try:
    is_available = sdk.check_availability()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Start MFA transaction:
Function takes 2 string arguments: question and code. Code is 2 digit integer. Code is used for identification. Both question and code will appear in HyperID Authenticator.
Returns transaction id. You will need it in the next request to check the user response.
```python
try:
    transaction_id = sdk.start_transaction(question="Your question here", code=code)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Check transaction status:
Function takes 1 integer argument: transaction_id.
Returns the information about transaction, status and complete result.
```python
try:
    r = sdk.get_transaction_status(transaction_id=your_transaction_id)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Cancel transaction:
Function takes 1 integer argument: transaction_id.
Does not return anything in case of success.
```python
try:
    sdk.cancel_transaction(transaction_id=your_transaction_id)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

## Storage

### There four types of storages: email, user id, identity provider and wallet

### Storage by email
Allows you to setup any data assosiated with email. Email is taken from public user info you can get after authorization.
Function takes 3 arguments: key(str), value(str) and access_scope(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by email:
```python
try:
    sdk.set_data_by_email(key, value, access_scope=UserDataAccessScope.PUBLIC)
    # or
    sdk.set_data_by_email(key, value, access_scope=UserDataAccessScope.PRIVATE)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

User data get by email:
Function takes 1 string argument: key(str).
Returns the data under given key or None if data not found.
```python
try:
    data = sdk.get_data_by_email(key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get keys list by email:
Returns a structure with assosiated keys.
```python
try:
    r = sdk.get_keys_list_by_email()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get shared keys list by email:
Returns list with assosiated keys or None in case of keys not found.
```python
try:
    r = sdk.get_keys_list_shared_by_email()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Data delete by email:
Function takes 1 string argument: key(str).
Delete a specified key.
Does not return anything in case of success.
```python
try:
    r = sdk.delete_data_key_by_email(key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```
### Storage by user id

Allows you to setup any data assosiated with user id. User id is taken from public user info you can get after authorization.
Function takes 3 arguments: key(str), value(str) and access_scope(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by user id:
```python
try:
    sdk.set_data_by_user_id(key, value, access_scope=UserDataAccessScope.PUBLIC)
    # or
    sdk.set_data_by_user_id(key, value, access_scope=UserDataAccessScope.PRIVATE)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

User data get by user id:
Function takes 1 string argument: key(str).
Returns the data under given key or None if data not found.
```python
try:
    data = sdk.get_data_by_user_id(key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get keys list by user id:
Returns a structure with assosiated keys.
```python
try:
    r = sdk.get_keys_list_by_user_id()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get shared keys list by user id:
Returns list with assosiated keys or None in case of keys not found.
```python
try:
    r = sdk.get_keys_list_shared_by_user_id()
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Data delete by user id:
Function takes 1 string argument: key(str).
Delete a specified key.
Does not return anything in case of success.
```python
try:
    r = sdk.delete_data_key_by_user_id(key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

### Storage by identity provider

Allows you to setup any data assosiated with identity provider.
Function takes 4 arguments: identity_provider(str), key(str), value(str) and access_scope(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by identity provider:
```python
try:
    sdk.set_data_by_identity_provider(identity_provider='google', key, value, access_scope=UserDataAccessScope.PUBLIC)
    # or
    sdk.set_data_by_identity_provider(identity_provider='google', key, value, access_scope=UserDataAccessScope.PRIVATE)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

User data get by identity provider:
Function takes 2 arguments: identity_provider(str), key(str).
Returns the data under given key or None if data not found.
```python
try:
    data = sdk.get_data_by_user_id(identity_provider='google', key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get keys list by identity provider:
Function takes 1 argument: identity_provider(str).
(str).
Returns a structure with assosiated keys.
```python
try:
    r = sdk.get_keys_list_by_identity_provider(identity_provider='google')
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get shared keys list by identity provider:
Function takes 1 argument: identity_provider(str).
(str).
Returns a structure with assosiated keys or None if keys not found.
```python
try:
    r = sdk.get_keys_list_shared_by_identity_provider(identity_provider='google')
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Data delete by identity provider:
Function takes 2 arguments: identity_provider(str), key(str).
Delete a specified key.
Does not return anything in case of success.
```python
try:
    r = sdk.delete_data_key_by_identity_provider(identity_provider='google', key)
except HyperIdException as e:
    # error handling
```

### Storage by wallet

Allows you to setup any data assosiated with wallet.
Function takes 4 arguments: wallet(str), key(str), value(str) and access_scope(UserDataAccessScope). You can specify two types of access scope: public or private.
Does not return anything in case of success.
User data set by wallet:
```python
try:
    sdk.set_data_by_wallet(wallet='0xAABBCC', key, value, access_scope=UserDataAccessScope.PUBLIC)
    # or
    sdk.set_data_by_wallet(wallet='0xAABBCC', key, value, access_scope=UserDataAccessScope.PRIVATE)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

User data get by wallet:
Function takes 2 arguments: wallet(str), key(str).
Returns the data under given key or None if data not found.
```python
try:
    data = sdk.get_data_by_user_id(wallet='0xAABBCC', key)
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get keys list by wallet:
Function takes 1argument: wallet(str).
(str).
Returns a structure with assosiated keys.
```python
try:
    r = sdk.get_keys_list_by_wallet(wallet='0xAABBCC')
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Get shared keys list by wallet:
Function takes 1 argument: wallet(str).
(str).
Returns a structure with assosiated keys or None if keys not found.
```python
try:
    r = sdk.get_keys_list_shared_by_wallet(wallet='google')
except HyperIdException as e:
    # error handling
except AuthorizationRequired:
    # sign in required
```

Data delete by wallet:
Function takes 2 arguments: wallet(str), key(str).
Delete a specified key.
Does not return anything in case of success.
```python
try:
    r = sdk.delete_data_key_by_wallet(wallet='0xAABBCC', key)
except HyperIdException as e:
    # error handling
```

## Error handling

### Basically you could handle a basic exception: HyperIdException. All other exceptions are inherited from HyperIdException. For more specific exception handling see error.py .

## Minimun working example:

```python
from flask import Flask
from hyperid.sdk.sdk import Sdk
from hyperid.auth.client_info import ClientInfoBasic

client_info = ClientInfoBasic(client_id="your.client.id",
                              client_secret="your.client.secret",
                              redirect_uri="https://your.host/callback")

app = Flask(__name__)
sdk = Sdk(client_info=client_info, infrastructure_type=InfrastructureType.SANDBOX)

@app.route("/login", methods=["POST"])
def login():
    url = sdk.start_sign_in_web2()
    return redirect(url)

@app.route("/callback")
def callback():
    try:
        sdk.complete_sign_in(request)
    except HyperIdException as e:
        # error handling
    return redirect(url_for('home'))

if __name__ == "__main__":
    app.run(host="your.host", debug=True)
```
