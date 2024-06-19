from .enum import AuthorizationMethod

class ClientInfo:
    def __init__(self,
                 client_id : str,
                 redirect_uri : str,
                 auth_method : AuthorizationMethod):
        self.client_id = client_id
        self.redirect_uri = redirect_uri
        self.auth_method = auth_method

    def is_valid(self) -> bool:
        for attr in [self.client_id, self.redirect_uri]:
            if attr is None or attr == '':
                return False
        return True

class ClientInfoBasic(ClientInfo):
    def __init__(self,
                 client_id : str,
                 client_secret : str,
                 redirect_uri : str):
        self.client_secret = client_secret
        super().__init__(client_id=client_id,
                       redirect_uri=redirect_uri,
                       auth_method=AuthorizationMethod.BASIC)
        
    def is_valid(self):
        if self.client_secret is None or self.client_secret == '':
            return False
        return super().is_valid()

class ClientInfoHS256(ClientInfo):
    def __init__(self,
                 client_id : str,
                 client_secret : str,
                 redirect_uri : str):
        self.client_secret = client_secret
        super().__init__(client_id=client_id,
                       redirect_uri=redirect_uri,
                       auth_method=AuthorizationMethod.HS256)
    def is_valid(self):
        if self.client_secret is None or self.client_secret == '':
            return False
        return super().is_valid()
    
class ClientInfoRSA(ClientInfo):
    def __init__(self,
                 client_id : str,
                 private_key : str,
                 redirect_uri : str):
        self.private_key = private_key
        super().__init__(client_id=client_id,
                       redirect_uri=redirect_uri,
                       auth_method=AuthorizationMethod.RSA)
    def is_valid(self):
        if self.private_key is None or self.private_key == '':
            return False
        return super().is_valid()