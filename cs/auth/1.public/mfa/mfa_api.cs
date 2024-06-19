using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.MFA
{
    public interface IHyperIDSDKMFA
    {
        /// <summary>
        /// 
        /// </summary>
        /// <returns>true - if MFA available. False - otherwise</returns>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        Task<bool> AvailabilityCheckAsync(CancellationToken cancellationToken = default);

        /// <summary>
        ///
        /// </summary>
        /// <param name="request">specific user request data. Will be shown insde transaction on HyperID Authenticator</param>
        /// <param name="code">control code to show user in HyperIdAuthenticator app. Must contain only 2 symbols</param>
        /// <returns>TransactionId if request was successfull. -1 otherwise</returns>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        Task<int> TransactionStartAsync([NotNull] string question,
			[NotNull] string code,
            CancellationToken cancellationToken = default);


        /// <summary>
        /// Check transaction status to ensure user action in HyperIdAuthenticator app
        /// </summary>
        /// <param name="transactionId">transaction ID obtained in RequestStart result</param>
        /// <returns>Pair with transactionId and transactionStatus</returns>
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        Task<MFATransactionStatus> TransactionStatusCheckAsync(int transactionId,
            CancellationToken cancellationToken = default);


        /// <summary>
        /// Cancel transaction for HyperIdAuthenticator app
        /// </summary>
        /// <param name="transactionId"></param>
        /// 
        /// <returns>True if trasaction cancel was succesfull. Otherwise - False</returns>
        /// 
        /// <exception cref="TaskCanceledException"></exception>
        /// <exception cref="HyperIDAuthException"></exception>
        Task<bool> TransactionCancelAsync(int transactionId,
            CancellationToken cancellationToken = default);
    }

}//namespace HyperId.SDK.MFA
