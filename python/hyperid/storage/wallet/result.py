class UserDataKeysByWalletGet():
    def __init__(self, response_json):
        self.keys_private = response_json.get('keys_private')
        self.keys_public = response_json.get('keys_public')