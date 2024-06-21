class UserDataKeysByWalletGet():
    def __init__(self, response_json):
        self.keys_private = response_json.get('keys_private')
        self.keys_public = response_json.get('keys_public')

class WalletData():
    def __init__(self,
                 address : str,
                 chain : str,
                 isPublic : bool = True):
        self.address = address
        self.chain = chain
        self.isPublic = isPublic