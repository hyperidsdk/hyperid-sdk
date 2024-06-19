using System.Text.Json.Serialization;

namespace HyperId.Private
{
    internal class JsonResponceAuthComplete
    {
        [JsonPropertyName("access_token")]
        public required string AccessToken { get; set; }

        [JsonPropertyName("expires_in")]
        public required long ExpiresIn { get; set; }

        [JsonPropertyName("refresh_token")]
        public required string RefreshToken { get; set; }

        [JsonPropertyName("refresh_expires_in")]
        public required long RefreshExpiresIn { get; set; }
    }
}//namespace HyperId.Private