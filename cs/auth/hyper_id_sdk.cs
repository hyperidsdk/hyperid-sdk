using HyperId.Private;
using HyperId.SDK.Authorization;
using HyperId.SDK.KYC;
using HyperId.SDK.MFA;
using HyperId.SDK.Storage;
using System.Diagnostics.CodeAnalysis;
using System.Threading.Tasks;
using System.Threading;

namespace HyperId.SDK
{
    public class HyperIDSDKFactory
    {
        public static IHyperIDSDK Instance() { return new HyperIDSDKImpl(); }
    }

    public interface IHyperIDSDK
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="providerInfo">Required. provider connection params</param>
        /// <param name="clientInfo">Required. HyperId client params</param>
        /// <param name="authRestoreInfo">Optional. AuthRestoreInfo to restore SDK state</param>
        /// <returns></returns>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        /// <exception cref="ArgumentNullException">Raised if <paramref name="providerInfo"/> or <paramref name="clientInfo"/> are null</exception>
        Task InitAsync(
            [NotNull] ProviderInfo providerInfo,
            [NotNull] ClientInfo clientInfo,
            [AllowNull] string? authRestoreInfo,
            CancellationToken cancellationToken = default);

        void Done();

        /// <summary>
        /// Auth Init and authrize is required to correct work with MFA/KYC/Storage modules. 
        /// </summary>
        /// <returns>interface to work with authorization SDK</returns>
        public IHyperIDSDKAuth GetAuth();
        public IHyperIDSDKMFA GetMFA();
        public IHyperIDSDKKyc GetKYC();
        public IHyperIDSDKStorage GetStorage();

        public string? GetAuthRestoreInfo();
    }

}//namespace HyperId.SDK
