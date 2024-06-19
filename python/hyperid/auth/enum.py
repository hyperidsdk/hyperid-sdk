from enum import Enum

class AuthorizationFlowMode(Enum):
    SIGN_IN_WEB2                = 0
    SIGN_IN_WEB3                = 3
    SIGN_IN_WALLET_GET          = 4
    SIGN_IN_GUEST_UPGRADE       = 6
    SIGN_IN_IDENTITY_PROVIDER   = 9
    
class AuthorizationMethod(Enum):
    BASIC   = 0
    HS256   = 1
    RS256  = 2

class WalletGetMode(Enum):
    WALLET_GET_FAST     = 2
    WALLET_GET_FULL     = 3

class WalletFamily(Enum):
    ETHEREUM    = 0
    SOLANA      = 1

class VerificationLevel(Enum):
    KYC_BASIC		= 3
    KYC_FULL		= 4
     
class InfrastructureType(Enum):
    SANDBOX     = "https://login-sandbox.hypersecureid.com"
    PRODUCTION  = "https://login.hypersecureid.com"