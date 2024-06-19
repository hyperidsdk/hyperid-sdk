using HyperId.Private;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Authorization
{
    /// <summary>
    /// interface ICore
    /// </summary>
    public interface IHyperIDSDKAuth
    {
        /// <summary>
        /// Prepare URL to authorization start 
        /// </summary>
        /// <returns>Url to authorization start in browser.</returns>
        /// <exception cref="HyperIDAuthException"></exception>
        string StartSignInWeb2([AllowNull] KycVerificationLevel? kycVerificationLevel = default);
        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletFamily"></param>
        /// <param name="kycVerificationLevel"></param>
        /// <returns>Url to authorization start in browser.</returns>
        string StartSignInWeb3([AllowNull] WalletFamily? walletFamily = default,
            [AllowNull] KycVerificationLevel? kycVerificationLevel = default);
        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletGetMode"></param>
        /// <param name="walletFamily"></param>
        /// <returns>Url to authorization start in browser.</returns>
        string StartWalletGet([AllowNull] WalletGetMode walletGetMode = WalletGetMode.FAST,
            [AllowNull] WalletFamily? walletFamily = default);
        /// <summary>
        /// 
        /// </summary>
        /// <returns>Url to authorization start in browser.</returns>
        string StartSignInGuestUpgrade();
        /// <summary>
        /// 
        /// </summary>
        /// <param name="identyProvider"></param>
        /// <returns></returns>
        /// <exception cref="ArgumentNullException">if <paramref name="identyProvider"/> is null</exception>
        string StartSignInIdentityProvider([NotNull]string identyProvider);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="redirectUri">Entire link obtained as result of authorization on HyperId portal</param>
        /// <param name="cancellationToken"></param>
        /// <returns>true is request is success. Failse otherwice</returns>
        /// <exception cref="HyperIDAuthException"></exception>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="ArgumentNullException">if <paramref name="redirectUri"/> is null</exception>
        Task<bool> CompleteSignInAsync([NotNull]string redirectUri,
            CancellationToken cancellationToken = default);
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        Task SignOutAsync(CancellationToken cancellationToken = default);


        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        /// <exception cref="HyperIDAuthException"></exception>
        UserInfo? UserInfo();

        /// <summary>
        /// 
        /// </summary>
        /// <returns>list of identity providers</returns>
        /// <exception cref="HyperIDAuthException">Raised if SDK was not initialised</exception>
        List<string>    IdentityProviders();

        string? AccessToken();
        string? RefreshToken();
    }
}//namespace HyperId.SDK.Authorization