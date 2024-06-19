using HyperId.SDK;
using HyperId.SDK.Authorization;
using HyperId.SDK.KYC;
using HyperId.SDK.MFA;
using HyperId.SDK.Storage;
using System.Diagnostics.CodeAnalysis;
using System.Threading.Tasks;
using System.Threading;

namespace HyperId.Private
{
    internal class HyperIDSDKImpl : IHyperIDSDK
    {
        private AuthSDKImpl         auth        = new AuthSDKImpl();
        private IHyperIDSDKMFA?     mfa;
        private IHyperIDSDKKyc?     kyc;
        private IHyperIDSDKStorage? storage;

        async Task IHyperIDSDK.InitAsync([NotNull] ProviderInfo providerInfo,
            [NotNull] ClientInfo clientInfo,
            [AllowNull] string? authRestoreInfo,
            CancellationToken cancellationToken)
        {
            await auth.InitAsync(providerInfo,
                clientInfo,
                authRestoreInfo,
                cancellationToken);
        }

        void IHyperIDSDK.Done()
        {
            auth.Done();
        }

        IHyperIDSDKAuth IHyperIDSDK.GetAuth()
        {
            return auth;
        }

        IHyperIDSDKMFA IHyperIDSDK.GetMFA()
        {
            if (mfa == null)
            {
                mfa = new MFASDKImpl((IHyperIDSDKAuthRestApi)auth);
            }
            return mfa;
        }

        IHyperIDSDKKyc IHyperIDSDK.GetKYC()
        {
            if(kyc == null)
            {
                kyc = new KycSDKImpl((IHyperIDSDKAuthRestApi)auth);
            }
            return kyc;
        }

        IHyperIDSDKStorage IHyperIDSDK.GetStorage()
        {
            if(storage == null)
            {
                storage = new StorageSDKImpl((IHyperIDSDKAuthRestApi)auth);
            }
            return storage;
        }

        public string? GetAuthRestoreInfo()
        {
            return auth.RefreshToken();
        }
    }
}//namespace HyperId.Private
