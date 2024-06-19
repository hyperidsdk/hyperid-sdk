namespace HyperId.Private
{
    /// <summary>
    /// https://hyperid.gitbook.io/hyperid-docs/api-documentation/authorization-flows
    /// </summary>
    internal enum FlowMode
    {
        /// <summary>
        /// Sign-in (web2)
        /// </summary>
        WEB2_SIGN_IN = 0,

        /// <summary>
        /// Sign-in with wallet (web3)
        /// </summary>
        WEB3_SIGN_IN = 3,

        /// <summary>
        /// Connect / create wallet after sign-in (web2+web3)
        /// </summary>
        WALLET_GET = 4,

        /// <summary>
        /// Upgrade guest account
        /// </summary>
        UPGRADE_FROM_GUEST = 6,

        /// <summary>
        /// 
        /// </summary>
        IDENTITY_PROVIDER = 9,
    }

}//namespace HyperId.Private