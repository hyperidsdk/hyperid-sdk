import time
import jwt 

class AuthToken:
    def __init__(self, token: str):
        self._token = token
    
    def __str__(self):
        return self._token
    
    @property
    def token(self) -> str:
        return self._token

    @token.setter
    def token(self, value: str):
        self._token = value

    def get_decoded_token(self):
        return jwt.decode(self._token, options={"verify_signature": False})

    def is_expired(self) -> bool:
        decoded_token = jwt.decode(self._token, options={"verify_signature": False})
        exp_timestamp = decoded_token.get('exp')
        if not exp_timestamp:
            return True
        current_timestamp = time.time()
        return current_timestamp > exp_timestamp