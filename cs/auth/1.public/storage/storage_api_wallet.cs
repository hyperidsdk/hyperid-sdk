using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public interface IStorageApiWallet
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletAddress"></param>
        /// <param name="key"></param>
        /// <param name="value"></param>
        /// <param name="accessScope"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<DataSetByWalletResult> DataSetAsync([NotNull] string walletAddress,
            [NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletAddress"></param>
        /// <param name="key"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<DataGetByWalletResult> DataGetAsync([NotNull] string walletAddress, 
            [NotNull] string key,
            CancellationToken cancellationToken = default);
    
        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletAddress"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<KeysGetByWalletResult> KeysGetAsync([NotNull] string walletAddress,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletAddress"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<KeysSharedGetByWalletResult> KeysGetSharedAsync([NotNull] string walletAddress,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="walletAddress"></param>
        /// <param name="keys"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<DataDeleteByWalletRequestResult> DataDeleteAsync([NotNull] string walletAddress,
            List<string> keys,
            CancellationToken cancellationToken = default);
    }
}
