using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace HyperId.Private
{
    /// <summary>
    /// class Discovery
    /// </summary>
    internal class Discovery
    {
        [JsonPropertyName("issuer")]
        public required string Issuer { get; set; }

        [JsonPropertyName("authorization_endpoint")]
        public required string AuthEndpoint { get; set; }

        [JsonPropertyName("token_endpoint")]
        public required string TokenEndpoint { get; set; }

        [JsonPropertyName("revocation_endpoint")]
        public required string RevokeEndpoint { get; set; }

        [JsonPropertyName("end_session_endpoint")]
        public required string SessionEndEndpoint { get; set; }

        [JsonPropertyName("rest_api_token_endpoint")]
        public required string RestApiTokenEndpoint { get; set; }

        [JsonPropertyName("rest_api_public_endpoint")]
        public required string RestApiPublicEndpoint { get; set; }

        [JsonPropertyName("client_scopes_default")]
        public required List<string> ScopesDefault { get; set; }

        [JsonPropertyName("client_scopes_optional")]
        public required List<string> ScopesOptional { get; set; }

        [JsonPropertyName("wallet_family")]
        public required Dictionary<string, int> WalletFamilies { get; set; }

        [JsonPropertyName("wallet_source")]
        public required Dictionary<string, int> WalletSources { get; set; }

        [JsonPropertyName("identity_providers")]
        public required List<string> IdentityProviders { get; set; }

        public List<string> Scopes()
        {
            List<string> scopes = new List<string>();
            scopes.AddRange(ScopesDefault);
            scopes.AddRange(ScopesOptional);
            return scopes;
        }
    }
}//namespace HyperId.Private