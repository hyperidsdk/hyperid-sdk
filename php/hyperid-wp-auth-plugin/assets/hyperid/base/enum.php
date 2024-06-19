<?php

enum AuthorizationFlowMode : int {
    case SIGN_IN_WEB2               = 0;
    case SIGN_IN_WEB3               = 3;
    case SIGN_IN_WALLET_GET         = 4;
    case SIGN_IN_GUEST_UPGRADE      = 6;
    case SIGN_IN_IDENTITY_PROVIDER  = 9;
}

enum AuthorizationMethod : int {
    case BASIC  = 0;
    case HS256  = 1;
    case RS256  = 2;
}

enum WalletGetMode : int {
    case WALLET_GET_FAST    = 2;
    case WALLET_GET_FULL    = 3;
}

enum WalletFamily : int {
    case ETHEREUM   = 0;
    case SOLANA     = 1;
}

enum VerificationLevel : int {
    case KYC_BASIC  = 3;
    case KYC_FULL   = 4;
}

enum InfrastructureType : string {
    case SANDBOX    = 'https://login-sandbox.hypersecureid.com';
    case PRODUCTION = 'https://login.hypersecureid.com';
}

?>