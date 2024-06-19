class HyperIdException(Exception):
    def __init__(self, message):
        self.message = message
        super().__init__(self.message)

class AuthorizationRequired(HyperIdException):
    def __init__(self, message="Authorization required. Please sign in."):
        self.message = message
        super().__init__(self.message)

class AccessTokenExpired(HyperIdException):
    def __init__(self, message="Access token is expired. Please sign in first."):
        self.message = message
        super().__init__(self.message)

class RefreshTokenExpired(HyperIdException):
    def __init__(self, message="Refresh token is expired. Re-authorization required"):
        self.message = message
        super().__init__(self.message)

class ServerError(HyperIdException):
    def __init__(self,  message="Server Under maintenance. Please try again later."):
        self.message = message
        super().__init__(message)

class UnknownError(HyperIdException):
    def __init__(self,  message="Unknow error"):
        self.message = message
        super().__init__(message)

class HyperIdMFAApiError(HyperIdException):
    def __init__(self,  message):
        self.message = message
        super().__init__(message)
        
class HyperIdAuthenticatorNotInstalled(HyperIdMFAApiError):
    def __init__(self):
        super().__init__("HyperId Authenticator not install, please install it first.")

class TransactionNotFound(HyperIdMFAApiError):
    def __init__(self):
        super().__init__("Transaction not found.")

class TransactionAlreadyCompleted(HyperIdMFAApiError):
    def __init__(self):
        super().__init__("Transaction already completed.")

class HyperIdStorageApiError(HyperIdException):
    def __init__(self,  message):
        self.message = message
        super().__init__(message)

class IdentityProviderNotFound(HyperIdStorageApiError):
    def __init__(self):
        super().__init__("Identity provider not found.")

class DataKeyAccessDenied(HyperIdStorageApiError):
    def __init__(self):
        super().__init__("Key access violation: Your permissions are not sufficient.")

class DataKeyInvalid(HyperIdStorageApiError):
    def __init__(self):
        super().__init__("Provided key is invalid")

class WalletNotFound(HyperIdStorageApiError):
    def __init__(self):
        super().__init__("Specified wallet not found.")

class WrongCredentialsError(HyperIdException):
    def __init__(self,  message="Wrong credentials"):
        self.message = message
        super().__init__(message)