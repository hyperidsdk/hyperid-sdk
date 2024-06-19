using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public class DataGetResult
    {
        public DataGetResult(DataGetRequestResult result,
            string? key,
            string? value)
        {
            Result = result;
            Key = key;
            Value = value;
        }

        public DataGetRequestResult Result { get; set; }
        public string? Key { get; set; }
        public string? Value { get; set; }
    }

    public class DataGetByWalletResult
    {
        public DataGetByWalletResult(DataGetByWalletRequestResult result,
            string? key,
            string? value)
        {
            Result = result;
            Key = key;
            Value = value;
        }

        public DataGetByWalletRequestResult Result { get; set; }
        public string? Key { get; set; }
        public string? Value { get; set; }
    }

    public class DataGetByIdpResult
    {
        public DataGetByIdpResult(DataGetByIdentityProviderRequestResult result,
            string? key,
            string? value)
        {
            Result = result;
            Key = key;
            Value = value;
        }

        public DataGetByIdentityProviderRequestResult Result { get; set; }
        public string? Key { get; set; }
        public string? Value { get; set; }
    }
}
