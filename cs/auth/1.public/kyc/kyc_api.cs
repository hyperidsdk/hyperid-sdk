using HyperId.SDK.Authorization;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.KYC
{
    public interface IHyperIDSDKKyc
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="kycVerificationLevel"></param>
        /// <returns></returns>
        Task<KycUserStatusResponse> UserStatusGetAsync(KycVerificationLevel kycVerificationLevel,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="kycVerificationLevel"></param>
        /// <returns></returns>
        Task<KycUserStatusTopLevelResponse> UserStatusTopLevelGetAsync(CancellationToken cancellationToken = default);
    }
}//namespace HyperId.SDK.KYC
