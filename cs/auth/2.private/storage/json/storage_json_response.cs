using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace HyperId.Private
{
    internal class SimpleResponceJson
    {
        [JsonPropertyName("result")]
        public required int Result { get; set; }
    }

    internal class UserDataGetEntryJson
    {
        [JsonPropertyName("value_key")]
        public required string Key { get; set; }

        [JsonPropertyName("value_data")]
        public string? Value { get; set; }
    }

    internal class UserDataGetResultJson
    {
        [JsonPropertyName("result")]
        public required int Result { get; set; }

        [JsonPropertyName("values")]
        public List<UserDataGetEntryJson>? Values { get; set; }
    }

    internal class KeysGetResponseJson
    {
        [JsonPropertyName("result")]
        public required int Result { get; set; }

        [JsonPropertyName("keys_private")]
        public List<string>? KeysPrivate { get; set; }

        [JsonPropertyName("keys_public")]
        public List<string>? KeysPublic { get; set; }
    }

    internal class KeysSharedGetResponseJson
    {
        [JsonPropertyName("result")]
        public required int Result { get; set; }

        [JsonPropertyName("keys_shared")]
        public List<string>? KeysShared { get; set; }

        [JsonPropertyName("next_search_id")]
        public string? NextSearchId { get; set; }
    }

}//namespace HyperId.Private
