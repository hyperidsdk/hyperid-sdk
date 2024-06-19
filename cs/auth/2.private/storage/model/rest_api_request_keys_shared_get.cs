using System.Collections.Generic;
using System.Net.Http;

namespace HyperId.Private
{
    internal class RestApiRequestKeysSharedGet : RestApiRequest
    {
        private List<string> keysShared = new List<string>();
        public string? NextSearchId { get; set; }

        public RestApiRequestKeysSharedGet(IHyperIDSDKAuthRestApi api,
            string uriPath,
            HttpContent content) : base(api, uriPath, content)
        {
        }
        public void KeysAdd(List<string> keys)
        {
            keysShared.AddRange(keys);
        }
        public List<string> KeysShared()
        {
            return keysShared;
        }
    }
}
