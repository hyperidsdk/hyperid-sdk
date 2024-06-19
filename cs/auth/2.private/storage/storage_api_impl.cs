using HyperId.SDK.Storage;

namespace HyperId.Private
{
    internal class StorageSDKImpl : IHyperIDSDKStorage
    {
        private IHyperIDSDKAuthRestApi RestApi {  get; set; }
        private IStorageApiEmail? StorageApiEmail {  get; set; }
        private IStorageApiUser? StorageApiUser {  get; set; }
        private IStorageApiWallet? StorageApiWallet {  get; set; }
        private IStorageApiIdp? StorageApiIdp {  get; set; }

        public StorageSDKImpl(IHyperIDSDKAuthRestApi restApi)
        {
            RestApi = restApi;
        }

        public IStorageApiEmail StorageByEmail()
        {
            if(StorageApiEmail == null)
            {
                StorageApiEmail = new StorageApiEmailImpl(RestApi);
            }
            return StorageApiEmail;
        }

        public IStorageApiIdp StorageByIdp()
        {
            if (StorageApiIdp == null)
            {
                StorageApiIdp = new StorageApiIdpImpl(RestApi);
            }
            return StorageApiIdp;
        }

        public IStorageApiUser StorageByUserId()
        {
            if (StorageApiUser == null)
            {
                StorageApiUser = new StorageApiUserImpl(RestApi);
            }
            return StorageApiUser;
        }

        public IStorageApiWallet StorageByWallet()
        {
            if (StorageApiWallet == null)
            {
                StorageApiWallet = new StorageApiWalletImpl(RestApi);
            }
            return StorageApiWallet;
        }
    }

}//namespace HyperId.Private
