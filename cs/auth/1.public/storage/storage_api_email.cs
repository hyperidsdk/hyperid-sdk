using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public interface IStorageApiEmail
    {
        Task<DataSetResult> DataSetAsync([NotNull] string key,
            [NotNull] string value,
            [NotNull]UserDataAccessScope accessScope,
            CancellationToken cancellationToken = default);
        Task<DataGetResult> DataGetAsync([NotNull] string key,
            CancellationToken cancellationToken = default);
        Task<KeysGetResult> KeysGetAsync(CancellationToken cancellationToken = default);
        Task<KeysSharedGetResult> KeysGetSharedAsync(CancellationToken cancellationToken = default);
        Task<bool> DataDeleteAsync(List<string> keys, CancellationToken cancellationToken = default);
    }
}
