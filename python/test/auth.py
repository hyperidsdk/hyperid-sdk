from flask import Flask, request, jsonify, render_template, redirect, url_for
from flask_restful import Api, Resource
import json
import sys
from hyperid.auth import auth as hid
from hyperid.mfa import mfa_api as mfa
from hyperid.storage.email import email_api as storage_email_api
from hyperid.storage.user_id import user_id_api as storage_user_id_api
from hyperid.storage.wallet import wallet_api as storage_wallet_api
from hyperid.storage.identity_provider import identity_provider_api as storage_idp_api
from hyperid.kyc import kyc_api as kyc
from hyperid.auth.enum import InfrastructureType, AuthorizationFlowMode, AuthorizationMethod, WalletGetMode, VerificationLevel
from hyperid.auth.user_info import UserInfo
from hyperid.auth.auth_token import AuthToken
from hyperid.error import ServerError
from hyperid.auth.client_info import ClientInfo, ClientInfoBasic, ClientInfoHS256, ClientInfoRSA
from hyperid.error import HyperIdException

app = Flask(__name__)
app.config['TEMPLATES_AUTO_RELOAD'] = True

client_info = ClientInfoBasic(client_id="your_client_id",
                              client_secret="your_client_client_secret",
                              redirect_uri="http://localhost:8085/callback")
auth = hid.Auth(client_info=client_info,
                infrastructure_type=InfrastructureType.PRODUCTION)

rest_api_token_endpoint = auth.get_discover().rest_api_token_endpoint;
mfa_pi = mfa.Mfa(rest_api_token_endpoint)
kc = kyc.Kyc(rest_api_token_endpoint)
storage_e = storage_email_api.Email(rest_api_token_endpoint)
storage_id = storage_user_id_api.UserId(rest_api_token_endpoint)
storage_wallet = storage_wallet_api.Wallet(rest_api_token_endpoint)
storage_idp = storage_idp_api.IdentityProvider(rest_api_token_endpoint)

def is_authorized() -> bool:
    is_authorized  = False
    try:
        is_authorized = auth.get_access_token() != None
    except:
        return is_authorized
    return is_authorized

@app.route("/")
def home():
    return render_template('page.html', is_authorized=is_authorized())

############# AUTH ##################
@app.route("/callback")
def callback():
    error = request.args.get("error")
    if error != None:
        # handle error
        print("error: ", error)
        return redirect(url_for('home'))
    authorization_code = request.args.get("code")
    try:
        auth.exchange_code_to_token(authorization_code)
    except Exception as e:
        print("exchange_code_to_token error: ", e)
    return redirect(url_for('home'))

@app.route("/login", methods=["POST"])
def login():
    url = ""
    mode = AuthorizationFlowMode(int(request.form['login']))
    print("mode: ", mode)
    match mode:
        case AuthorizationFlowMode.SIGN_IN_WEB2: url = auth.start_sign_in_web2()
        case AuthorizationFlowMode.SIGN_IN_WEB3: url = auth.start_sign_in_web3()
        case AuthorizationFlowMode.SIGN_IN_WALLET_GET: url = auth.start_sign_in_wallet_get()
        case AuthorizationFlowMode.SIGN_IN_GUEST_UPGRADE: url = auth.start_sign_in_guest_upgrade()
        case AuthorizationFlowMode.SIGN_IN_IDENTITY_PROVIDER:
            idp = auth.get_discover().identity_providers
            if 'github' in idp:
                url = auth.start_sign_in_by_identity_provider('github')
            else:
                url = auth.start_sign_in_by_identity_provider('google')
    return redirect(url)

@app.route("/user_info", methods=["POST"])
def user_info():
    user_info = auth.get_user_info()
    return render_template('page.html', user=user_info, is_authorized=is_authorized())

############# MFA ##################

@app.route("/mfa/availability-check", methods=["POST"])
def mfa_availability_check():
    r = mfa_pi.check_availability(auth.get_access_token())
    print("is_mfa_available: ", r)
    return render_template('page.html', is_authorized=is_authorized())

@app.route("/mfa/transaction-start", methods=["POST"])
def mfa_transaction_start():
    code = 42
    try:
        transaction_id = mfa_pi.start_transaction(access_token=auth.get_access_token(), question="Your question here", code=code)
        print("mfa transaction start transaction id: ", transaction_id)
        return render_template('page.html', is_authorized=is_authorized(), transaction_id=transaction_id)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=is_authorized())

@app.route("/mfa/transaction-status-get", methods=["POST"])
def mfa_transaction_status_get():
    try:
        r = mfa_pi.get_transaction_status(auth.get_access_token(), transaction_id=1)
        print("mfa transaction start result: ", r.complete_result)
        print("mfa transaction start result: ", r.transaction_status)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=is_authorized())

############# KYC ##################

@app.route("/kyc/user-status-get", methods=["POST"])
def kyc_user_status_get():
    r = kc.get_user_status(auth.get_access_token(), VerificationLevel.KYC_BASIC)
    print("kyc user status get result: ", r.status)
    return render_template('page.html', is_authorized=is_authorized())

@app.route("/kyc/user-status-top-levelget", methods=["POST"])
def kyc_user_status_top_level_get():
    r = kc.get_user_status_top_level(auth.get_access_token())
    print("kyc user status get result: ", r.result.name)
    return render_template('page.html', is_authorized=is_authorized())  

############# STORAGE EMAIL ##################

@app.route("/storage/data-set-by-email", methods=["POST"])
def user_data_set_by_email():
    key = request.form['key']
    value = request.form['value']
    try:
        storage_e.set_data(auth.get_access_token(), key, value)
        return render_template('page.html', is_authorized=is_authorized())
    except:
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-get-by-email", methods=["POST"])
def user_data_get_by_email():
    key = request.form['key']
    try:
        value_data = storage_e.get_data(auth.get_access_token(), key)
        print("user_data_by_email_get result: ", value_data)
        return render_template('page.html', user_data_get_by_email_value=value_data, is_authorized=is_authorized())
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-keys-list-get-by-email", methods=["POST"])
def user_data_keys_by_email_get():
    try:
        res = storage_e.get_keys_list(auth.get_access_token())
        print("user_data_keys_by_email_get public: ", res.keys_public)
        print("user_data_keys_by_email_get private: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized(), user_keys=res.keys_public)    

@app.route("/storage/data-delete-by-email", methods=["POST"])
def user_data_delete_by_email():
    key = request.form['key']
    try:
        storage_e.delete_data_key(auth.get_access_token(), key)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())   

############# STORAGE USER ID ##################

@app.route("/storage/data-set-by-user-id", methods=["POST"])
def user_data_set_by_user_id():
    key = request.form['key']
    value = request.form['value']
    try:
        storage_id.set_data(auth.get_access_token(), key, value)
        return render_template('page.html', is_authorized=is_authorized())
    except:
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-get-by-user-id", methods=["POST"])
def user_data_get_by_user_id():
    key = request.form['key']
    try:
        value_data = storage_id.get_data(auth.get_access_token(), key)
        print("user_data_by_email_get result: ", value_data)
        return render_template('page.html', user_data_get_by_email_value=value_data, is_authorized=is_authorized())
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-keys-list-get-by-user-id", methods=["POST"])
def user_data_keys_by_user_id_get():
    try:
        res = storage_id.get_keys_list(auth.get_access_token())
        print("user_data_keys_by_email_get public: ", res.keys_public)
        print("user_data_keys_by_email_get private: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized(), user_keys=res.keys_public)    

@app.route("/storage/data-delete-by-user-id", methods=["POST"])
def user_data_delete_by_user_id():
    key = request.form['key']
    try:
        storage_id.delete_data_key(auth.get_access_token(), key)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())   

############# STORAGE WALLET ##################

@app.route("/storage/data-set-by-wallet", methods=["POST"])
def user_data_set_by_wallet():
    key = request.form['key']
    value = request.form['value']
    wallet_address = request.form['wallet_address']
    try:
        storage_wallet.set_data(auth.get_access_token(), wallet_address, key, value)
        return render_template('page.html', is_authorized=is_authorized())
    except:
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-get-by-wallet", methods=["POST"])
def user_data_get_by_wallet():
    key = request.form['key']
    wallet_address = request.form['wallet_address']
    try:
        value_data = storage_wallet.get_data(auth.get_access_token(), wallet_address, key)
        print("user_data_by_wallet_set result: ", value_data)
        return render_template('page.html', is_authorized=is_authorized(), user_data_get_by_wallet_value=value_data)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=is_authorized())


@app.route("/storage/data-keys-list-get-by-wallet", methods=["POST"])
def user_data_keys_by_wallet_get():
    wallet_address = request.form['wallet_address']
    try:
        res = storage_wallet.get_keys_list(auth.get_access_token(), wallet_address)
        print("user_data_keys_by_wallet_get public: ", res.keys_public)
        print("user_data_keys_by_wallet_get private: ", res.keys_private)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())    

@app.route("/storage/data-delete-by-wallet", methods=["POST"])
def user_data_delete_by_wallet():
    key = request.form['key']
    wallet_address = request.form['wallet_address']
    try:
        storage_wallet.delete_data_key(auth.get_access_token(), wallet_address, key)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())  

############# STORAGE IDP ##################

@app.route("/storage/data-set-by-idp", methods=["POST"])
def user_data_set_by_idp():
    idp = request.form['idp']
    key = request.form['key']
    value = request.form['value']
    try:
        storage_idp.set_data(auth.get_access_token(), idp, key, value)
        return render_template('page.html', is_authorized=is_authorized())
    except:
        return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-get-by-idp", methods=["POST"])
def user_data_get_by_idp():
    idp = request.form['idp']
    key = request.form['key']
    try:
        value_data = storage_idp.get_data(auth.get_access_token(), idp, key)
        print("user_data_by_idp_get result: ", value_data)
        return render_template('page.html', is_authorized=is_authorized(), user_data_get_by_idp_value=value_data)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=is_authorized())
    
@app.route("/storage/data-keys-list-get-by-idp", methods=["POST"])
def user_data_keys_by_idp_get():
    idp = request.form['idp']
    try:
        res = storage_idp.get_keys_list(auth.get_access_token(), idp)
        print("user_data_keys_by_idp_get result: ", res.keys_public)
        print("user_data_keys_by_idp_get result: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())

@app.route("/storage/data-delete-by-idp", methods=["POST"])
def user_data_delete_by_idp():
    key = request.form['key']
    identity_provider = request.form['identity_provider']
    try:
        storage_idp.data_key_delete(auth.get_access_token(), identity_provider, key)
    except:
        return render_template('page.html', is_authorized=is_authorized())
    return render_template('page.html', is_authorized=is_authorized())  

@app.route("/logout", methods=["POST"])
def logout():
    try:
        res = auth.logout()
        print("logout result: ", res)
    except Exception as e:
        print("except: ", e)
        return render_template('page.html')
    return redirect(url_for('home'))

print(__name__)
if __name__ == "__main__":
    app.run(host="localhost", port=8085, debug=True)