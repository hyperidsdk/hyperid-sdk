using System.Collections.Generic;

namespace HyperId.SDK.Storage
{
    public class KeysGetResult
    {
        public List<string> KeysPublic { get; set; } = new List<string>();
        public List<string> KeysPrivate { get; set; } = new List<string>();
    }

    public class KeysGetByWalletResult
    {
        public required KeysGetByWalletRequestResult Result { get; set; }
        public List<string> KeysPublic { get; set; } = new List<string>();
        public List<string> KeysPrivate { get; set; } = new List<string>();
    }

    public class KeysGetByIdpResult
    {
        public required KeysGetByIdentityProviderRequestResult Result { get; set; }
        public List<string> KeysPublic { get; set; } = new List<string>();
        public List<string> KeysPrivate { get; set; } = new List<string>();
    }




    public class KeysSharedGetResult
    {
        public List<string> KeysShared { get; set; } = new List<string>();
    }

    public class KeysSharedGetByWalletResult
    {
        public required KeysGetByWalletRequestResult Result { get; set; }

        public List<string> KeysShared { get; set; } = new List<string>();
    }

    public class KeysSharedGetByIdpResult
    {
        public required KeysGetByIdentityProviderRequestResult Result { get; set; }

        public List<string> KeysShared { get; set; } = new List<string>();
    }
}