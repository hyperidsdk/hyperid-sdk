class InvalidJSONData(Exception):
    pass

class Wallet:
    def __init__(self, json_data):
        self.address = json_data.get('wallet_address')
        self.chain_id = json_data.get('wallet_chain_id')
        self.source = json_data.get('wallet_source')
        self.is_verified = json_data.get('is_wallet_verified')
        self.family = json_data.get('wallet_family')

class UserInfo:
    def __init__(self, json_data):
        if not isinstance(json_data, dict):
            raise InvalidJSONData("JSON data should be a dictionary.")
        self.user_id = json_data.get('sub')
        self.is_guest = json_data.get('is_guest')
        self.email = json_data.get('email')
        self.device_id = json_data.get('device_id')
        self.ip = json_data.get('ip')
        self.wallet = Wallet(json_data)    