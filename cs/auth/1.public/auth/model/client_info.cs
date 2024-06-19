using System.Diagnostics.CodeAnalysis;

namespace HyperId.SDK.Authorization
{
    /// <summary>
    /// struct eClientInfo
    /// </summary>
    public struct ClientInfo
    {
           public ClientInfo(
            [NotNull] string clientId,
            [NotNull] string redirectUri,
            [NotNull] AuthMethod authorizationMethod,
            [AllowNull] string? clientSecret,
            [AllowNull] string? rsaKeysPem)
        {
            ClientId = clientId;
            AuthMethod = authorizationMethod;
            RedirectUri = redirectUri;
            ClientSecret = clientSecret;
            RSAKeysPem = rsaKeysPem;
        }
        /// <summary>
        /// IsValid
        /// </summary>
        public readonly bool IsValid()
        {
            if(string.IsNullOrEmpty(ClientId) || string.IsNullOrEmpty(RedirectUri))
            {
                return false;
            }

            if(AuthMethod == AuthMethod.CLIENT_SECRET_BASIC
                || AuthMethod == AuthMethod.CLIENT_SECRET_HMAC)
            {
                return !string.IsNullOrEmpty(ClientSecret);
            }
            else
            {
                return !string.IsNullOrEmpty(RSAKeysPem);
            }
        }

        [NotNull]
        public string ClientId { get; private set; }

        [NotNull]
        public AuthMethod AuthMethod { get; private set; }

        [NotNull]
        public string RedirectUri { get; private set; }

        [AllowNull]
        public string? ClientSecret { get; private set; }

        [AllowNull]
        public string? RSAKeysPem { get; private set; }
    }
}//namespace  HyperId.SDK.Authorization