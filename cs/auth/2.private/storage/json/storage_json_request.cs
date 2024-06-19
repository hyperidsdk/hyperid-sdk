using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace HyperId.Private
{
    #region Data set Jsons

    /// <summary>
    /// 
    /// </summary>
    internal class UserDataSetJson
    {
        public UserDataSetJson(string key,
            string value,
            int accessScope)
        {
            Key = key;
            Value = value;
            AccessScope = accessScope;
        }

        [JsonPropertyName("value_key")]
        public string Key { get; set; }

        [JsonPropertyName("value_data")]
        public string Value { get; set; }

        [JsonPropertyName("access_scope")]
        public int AccessScope { get; set; }
    }
    /// <summary>
    /// 
    /// </summary>
    internal class UserDataSetWalletJson
    {
        [JsonPropertyName("wallet_address")]
        public string WalletAddress { get; set; }

        [JsonPropertyName("value_key")]
        public string Key { get; set; }

        [JsonPropertyName("value_data")]
        public string Value { get; set; }

        [JsonPropertyName("access_scope")]
        public int AccessScope { get; set; }
    }
    /// <summary>
    /// 
    /// </summary>
    internal class UserDataSetIdpJson
    {
        [JsonPropertyName("identity_provider")]
        public string IdentityProvider { get; set; }

        [JsonPropertyName("value_key")]
        public string Key { get; set; }

        [JsonPropertyName("value_data")]
        public string? Value { get; set; }

        [JsonPropertyName("access_scope")]
        public int AccessScope { get; set; }
    }
    #endregion

    #region Data get Jsons

    internal class KeysJson
    {
        public KeysJson(List<string> keys)
        {
            Keys = keys;
        }

        [JsonPropertyName("value_keys")]
        public List<string> Keys { get; set; }
    }

    internal class KeysWithWalletJson
    {
        public KeysWithWalletJson(string walletAddress,
            List<string> keys)
        {
            WalletAddress = walletAddress;
            Keys = keys;
        }

        [JsonPropertyName("wallet_address")]
        public string WalletAddress { get; set; }

        [JsonPropertyName("value_keys")]
        public List<string> Keys { get; set; }
    }

    internal class KeysWithIdpJson
    {
        public KeysWithIdpJson(string identityProvider,
            List<string> keys)
        {
            IdentityProvider = identityProvider;
            Keys = keys;
        }

        [JsonPropertyName("identity_provider")]
        public string IdentityProvider { get; set; }

        [JsonPropertyName("value_keys")]
        public List<string> Keys { get; set; }
    }

    #endregion

    #region keys get Jsons

    internal class KeysGetJson
    {
        public KeysGetJson(int requestId)
        {
            RequestId = requestId;
        }

        [JsonPropertyName("request_id")]
        public int RequestId { get; set; }
    }
    internal class KeysGetByWalletJson
    {
        public KeysGetByWalletJson(string walletAddress)
        {
            WalletAddress = walletAddress;
        }

        [JsonPropertyName("wallet_address")]
        public string WalletAddress { get; set; }
    }

    internal class KeysGetByIdpJson
    {
        public KeysGetByIdpJson(string identityProvider)
        {
            IdentityProvider = identityProvider;
        }

        [JsonPropertyName("identity_provider")]
        public string IdentityProvider { get; set; }
    }

    internal class KeysSharedGetJson
    {
        public KeysSharedGetJson(int pageSize)
        {
            PageSize = pageSize;
        }
        [JsonPropertyName("search_id")]
        public string? SearchId { get; set; }

        [JsonPropertyName("page_size")]
        public int PageSize { get; set; }
    }
    internal class KeysSharedGetByWalletJson
    {
        public KeysSharedGetByWalletJson(string walletAddress,
            int pageSize)
        {
            WalletAddress = walletAddress;
            PageSize = pageSize;
        }
        [JsonPropertyName("wallet_address")]
        public string WalletAddress { get; set; }

        [JsonPropertyName("search_id")]
        public string? SearchId { get; set; }

        [JsonPropertyName("page_size")]
        public int PageSize { get; set; }
    }
    internal class KeysSharedGetByIdpJson
    {
        public KeysSharedGetByIdpJson(string identityProvider,
            int pageSize)
        {
            IdentityProvider = identityProvider;
            PageSize = pageSize;
        }

        [JsonPropertyName("identity_provider")]
        public string IdentityProvider { get; set; }

        [JsonPropertyName("search_id")]
        public string? SearchId { get; set; }

        [JsonPropertyName("page_size")]
        public int PageSize { get; set; }
    }

    #endregion
}//namespace HyperId.Private
