using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public interface IHyperIDSDKStorage
    {
        IStorageApiEmail StorageByEmail();
        IStorageApiUser StorageByUserId();
        IStorageApiWallet StorageByWallet();
        IStorageApiIdp StorageByIdp();
    }

}//namespace HyperId.SDK.Storage
