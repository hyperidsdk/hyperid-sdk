using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.SDK.Storage
{
    public interface IStorageApiIdp
    {
        Task<DataSetByIdentityProviderResult> DataSetAsync([NotNull] string identityProvider,
            [NotNull] string key,
            [NotNull] string value,
            [NotNull] UserDataAccessScope accessScope,
            CancellationToken cancellationToken = default);
        Task<DataGetByIdpResult> DataGetAsync([NotNull] string identityProvider,
            [NotNull] string key,
            CancellationToken cancellationToken = default);
        Task<KeysGetByIdpResult> KeysGetAsync([NotNull] string identityProvider,
            CancellationToken cancellationToken = default);
        Task<KeysSharedGetByIdpResult> KeysGetSharedAsync([NotNull] string identityProvider,
            CancellationToken cancellationToken = default);
        Task<DataDeleteByIdentityProviderRequestResult> DataDeleteAsync([NotNull] string identityProvider,
            List<string> keys,
            CancellationToken cancellationToken = default);
    }
}