using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public class WalletData
    {
        public string Address { get; set; } = "";
        public string Chain { get; set; } = "";
        public bool IsPublic{ get; set; } = true;

        public WalletData(string address,
                          string chain,
                          bool isPublic = true)
        {
            Address = address;
            Chain = chain;
            IsPublic = isPublic;
        }

    }
    public class WalletsGetResult
    {
        public WalletsGetResult(UserWalletsGetResult result,
            List<WalletData> wallets)
        {
            Result = result;
            Wallets = wallets;
        }

        public UserWalletsGetResult Result { get; set; }
        public List<WalletData> Wallets { get; set; }
    }
}
