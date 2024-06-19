using HyperId.Private;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.Private
{
    internal interface IHyperIDSDKAuthRestApi
    {
        Task<HttpResponseMessage> RestApiGetRequestAsync(CancellationToken cancellationToken);

        Task<HttpResponseMessage> RestApiPostRequestAsync(RestApiRequest request,
            CancellationToken cancellationToken);
    }
}
