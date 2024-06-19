
namespace HyperId.SDK.Authorization
{
    public enum ProviderInfo
    {
        STAGE,
        SANDBOX,
        PRODUCTION
    }

    /// <summary>
    /// enum AuthMethod
    /// </summary>
    public enum AuthMethod
    {
        CLIENT_SECRET_BASIC,
        CLIENT_SECRET_HMAC,
        CLIENT_SECRET_RSA,
    }

    /// <summary>
    /// enum WalletGetMode
    /// </summary>
    public enum WalletGetMode
    {
        FAST = 2,
        FULL = 3
    }

    /// <summary>
    /// 
    /// </summary>
    public enum WalletFamily
    {
        ETHEREUM = 0,
        SOLANA = 1,
    }

}//HyperId.SDK.Authorization