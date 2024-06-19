using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public interface IStorageApiUser
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="key"></param>
        /// <param name="value"></param>
        /// <param name="accessScope"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<DataSetResult> DataSetAsync([NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="key"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<DataGetResult> DataGetAsync([NotNull] string key,
            CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<KeysGetResult> KeysGetAsync(CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<KeysSharedGetResult> KeysGetSharedAsync(CancellationToken cancellationToken = default);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="keys"></param>
        /// <param name="cancellationToken"></param>
        /// <returns></returns>
        Task<bool> DataDeleteAsync(List<string> keys,
            CancellationToken cancellationToken = default);
    }
}
