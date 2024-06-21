<?php

require_once __DIR__.'/auth/auth.php';
require_once __DIR__.'/modules/mfa.php';
require_once __DIR__.'/modules/kyc.php';
require_once __DIR__.'/modules/storage_email.php';
require_once __DIR__.'/modules/storage_identity_provider.php';
require_once __DIR__.'/modules/storage_user_id.php';
require_once __DIR__.'/modules/storage_wallet.php';

class HyperIDSDK {
    public Auth                 $auth;
    public HyperIDMfa           $mfa;
    public HyperIDKyc           $kyc;
    public HyperIDEmailStorage  $storageEmail;
    public HyperIDUserIdStorage $storageUserId;
    public HyperIDIDPStorage    $storageIdp;
    public HyperIDWalletStorage $storageWallet;

    function __construct(ClientInfo $clientInfo, string $refreshToken = "", InfrastructureType $infrastructureType = InfrastructureType::SANDBOX) {
        $this->auth = new Auth($clientInfo, $refreshToken, $infrastructureType);
        $this->mfa = new HyperIDMfa($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
        $this->kyc = new HyperIDKyc($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
        $this->storageEmail = new HyperIDEmailStorage($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
        $this->storageUserId = new HyperIDUserIdStorage($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
        $this->storageIdp = new HyperIDIDPStorage($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
        $this->storageWallet = new HyperIDWalletStorage($this->auth->getDiscoverConfiguration()->restApiTokenEndpoint);
    }
}

?>