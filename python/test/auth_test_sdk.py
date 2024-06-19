from flask import Flask, request, render_template, redirect, url_for
from hyperid.auth.enum import InfrastructureType, AuthorizationFlowMode, AuthorizationMethod, WalletGetMode, VerificationLevel
from hyperid.auth.client_info import ClientInfo, ClientInfoBasic, ClientInfoHS256, ClientInfoRSA
from hyperid.error import HyperIdException
from hyperid.sdk.sdk import Sdk

app = Flask(__name__)
app.config['TEMPLATES_AUTO_RELOAD'] = True

client_info = ClientInfoBasic(client_id="you.client.id",
                              client_secret="your.client.secret",
                              redirect_uri="redirect.url")

sdk = Sdk(client_info=client_info, infrastructure_type=InfrastructureType.SANDBOX)

@app.route("/")
def home():
    return render_template('page.html', is_authorized=sdk.is_authorized())

############# AUTH ##################
@app.route("/callback")
def callback():
    try:
        sdk.complete_sign_in(request)
    except Exception as e:
        print("complete_sign_in error: ", e)
    return redirect(url_for('home'))

@app.route("/login", methods=["POST"])
def login():
    url = ""
    mode = AuthorizationFlowMode(int(request.form['login']))
    print("mode: ", mode)
    match mode:
        case AuthorizationFlowMode.SIGN_IN_WEB2: url = sdk.start_sign_in_web2()
        case AuthorizationFlowMode.SIGN_IN_WEB3: url = sdk.start_sign_in_web3()
        case AuthorizationFlowMode.SIGN_IN_WALLET_GET: url = sdk.start_sign_in_wallet_get()
        case AuthorizationFlowMode.SIGN_IN_GUEST_UPGRADE: url = sdk.start_sign_in_guest_upgrade()
        case AuthorizationFlowMode.SIGN_IN_IDENTITY_PROVIDER:
            idp = sdk.get_discover().identity_providers
            if 'github' in idp:
                url = sdk.start_sign_in_by_identity_provider('github')
            else:
                url = sdk.start_sign_in_by_identity_provider('google')
    return redirect(url)

@app.route("/user_info", methods=["POST"])
def user_info():
    user_info = sdk.get_user_info()
    return render_template('page.html', user=user_info, is_authorized=sdk.is_authorized())

############# MFA ##################

@app.route("/mfa/availability-check", methods=["POST"])
def mfa_availability_check():
    try:
        r = sdk.check_availability()
        print("is_mfa_available: ", r)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/mfa/transaction-start", methods=["POST"])
def mfa_transaction_start():
    code = 42
    try:
        transaction_id = sdk.start_transaction(question="Your question here", code=code)
        print("mfa transaction start transaction id: ", transaction_id)
        return render_template('page.html', is_authorized=sdk.is_authorized(), transaction_id=transaction_id)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/mfa/transaction-status-get", methods=["POST"])
def mfa_transaction_status_get():
    try:
        r = sdk.get_transaction_status(transaction_id=1)
        print("mfa transaction start result: ", r.complete_result)
        print("mfa transaction start result: ", r.transaction_status)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())

############# KYC ##################

@app.route("/kyc/user-status-get", methods=["POST"])
def kyc_user_status_get():
    try:
        #optional param, by default is VerificationLevel.KYC_FULL
        vl = VerificationLevel.KYC_BASIC
        r = sdk.get_user_status(vl)
        print("kyc user status get result: ", r.status)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/kyc/user-status-top-levelget", methods=["POST"])
def kyc_user_status_top_level_get():
    try:
        r = sdk.get_user_status_top_level()
        print("kyc user status get result: ", r.status)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())  

############# STORAGE EMAIL ##################

@app.route("/storage/data-set-by-email", methods=["POST"])
def user_data_set_by_email():
    key = request.form['key']
    value = request.form['value']
    try:
        sdk.set_data_by_email(key, value)
    except HyperIdException as e:
        print(e)
    return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-get-by-email", methods=["POST"])
def user_data_get_by_email():
    key = request.form['key']
    try:
        value_data = sdk.get_data_by_email(key)
        print("user_data_by_email_get result: ", value_data)
        return render_template('page.html', user_data_get_by_email_value=value_data, is_authorized=sdk.is_authorized())
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-keys-list-get-by-email", methods=["POST"])
def user_data_keys_by_email_get():
    try:
        res = sdk.get_keys_list_by_email()
        print("user_data_keys_by_email_get public: ", res.keys_public)
        print("user_data_keys_by_email_get private: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized(), user_keys=res.keys_public)    

@app.route("/storage/data-delete-by-email", methods=["POST"])
def user_data_delete_by_email():
    key = request.form['key']
    try:
        sdk.delete_data_key_by_email(key)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())   

############# STORAGE USER ID ##################

@app.route("/storage/data-set-by-user-id", methods=["POST"])
def user_data_set_by_user_id():
    key = request.form['key']
    value = request.form['value']
    try:
        sdk.set_data_by_user_id(key, value)
        return render_template('page.html', is_authorized=sdk.is_authorized())
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-get-by-user-id", methods=["POST"])
def user_data_get_by_user_id():
    key = request.form['key']
    try:
        value_data = sdk.get_data_by_user_id(key)
        print("user_data_by_email_get result: ", value_data)
        return render_template('page.html', user_data_get_by_email_value=value_data, is_authorized=sdk.is_authorized())
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-keys-list-get-by-user-id", methods=["POST"])
def user_data_keys_by_user_id_get():
    try:
        res = sdk.get_keys_list_by_user_id()
        print("user_data_keys_by_email_get public: ", res.keys_public)
        print("user_data_keys_by_email_get private: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized(), user_keys=res.keys_public)    

@app.route("/storage/data-delete-by-user-id", methods=["POST"])
def user_data_delete_by_user_id():
    key = request.form['key']
    try:
        sdk.delete_data_key_by_user_id(key)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())   

############# STORAGE WALLET ##################

@app.route("/storage/data-set-by-wallet", methods=["POST"])
def user_data_set_by_wallet():
    key = request.form['key']
    value = request.form['value']
    wallet_address = request.form['wallet_address']
    try:
        sdk.set_data_by_wallet(wallet_address, key, value)
        return render_template('page.html', is_authorized=sdk.is_authorized())
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-get-by-wallet", methods=["POST"])
def user_data_get_by_wallet():
    key = request.form['key']
    wallet_address = request.form['wallet_address']
    try:
        value_data = sdk.get_data_by_wallet(wallet_address, key)
        print("user_data_by_wallet_set result: ", value_data)
        return render_template('page.html', is_authorized=sdk.is_authorized(), user_data_get_by_wallet_value=value_data)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=sdk.is_authorized())


@app.route("/storage/data-keys-list-get-by-wallet", methods=["POST"])
def user_data_keys_by_wallet_get():
    wallet_address = request.form['wallet_address']
    try:
        res = sdk.get_keys_list_by_wallet(wallet_address)
        print("user_data_keys_by_wallet_get public: ", res.keys_public)
        print("user_data_keys_by_wallet_get private: ", res.keys_private)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())    

@app.route("/storage/data-delete-by-wallet", methods=["POST"])
def user_data_delete_by_wallet():
    key = request.form['key']
    wallet_address = request.form['wallet_address']
    try:
        sdk.delete_data_key_by_wallet(wallet_address, key)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())  

############# STORAGE IDP ##################

@app.route("/storage/data-set-by-idp", methods=["POST"])
def user_data_set_by_idp():
    idp = request.form['idp']
    key = request.form['key']
    value = request.form['value']
    try:
        sdk.set_data_by_identity_provider(idp, key, value)
        return render_template('page.html', is_authorized=sdk.is_authorized())
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-get-by-idp", methods=["POST"])
def user_data_get_by_idp():
    idp = request.form['idp']
    key = request.form['key']
    try:
        value_data = sdk.get_data_by_identity_provider(idp, key)
        print("user_data_by_idp_get result: ", value_data)
        return render_template('page.html', is_authorized=sdk.is_authorized(), user_data_get_by_idp_value=value_data)
    except Exception as e:
        print("exception: ", e)
        return render_template('page.html', is_authorized=sdk.is_authorized())
    
@app.route("/storage/data-keys-list-get-by-idp", methods=["POST"])
def user_data_keys_by_idp_get():
    idp = request.form['idp']
    try:
        res = sdk.get_keys_list_by_identity_provider(idp)
        print("user_data_keys_by_idp_get result: ", res.keys_public)
        print("user_data_keys_by_idp_get result: ", res.keys_private)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())

@app.route("/storage/data-delete-by-idp", methods=["POST"])
def user_data_delete_by_idp():
    key = request.form['key']
    identity_provider = request.form['identity_provider']
    try:
        sdk.delete_data_key_by_identity_provider(identity_provider, key)
    except:
        return render_template('page.html', is_authorized=sdk.is_authorized())
    return render_template('page.html', is_authorized=sdk.is_authorized())  

@app.route("/logout", methods=["POST"])
def logout():
    try:
        res = sdk.sign_out()
        print("logout result: ", res)
    except Exception as e:
        print("except: ", e)
        return render_template('page.html')
    return redirect(url_for('home'))

print(__name__)
if __name__ == "__main__":
    app.run(host="localhost", port=8085, debug=True)