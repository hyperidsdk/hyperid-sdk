using HyperId.SDK;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace HyperId.Private
{
    internal class RestApiRequest
    {
        private IHyperIDSDKAuthRestApi Api { get; set; }
        public HttpContent Content { get; private set; }
        public string UriPath {  get; private set; }
        private int startCounter = 0;

        public RestApiRequest(IHyperIDSDKAuthRestApi api,
            string uriPath,
            HttpContent content)
        {
            Api = api;
            UriPath = uriPath;
            Content = content;
        }

        public async Task<HttpResponseMessage> StartAsync(CancellationToken cancellationToken)
        {
            if (++startCounter <= 2)
            {
                return await Api.RestApiPostRequestAsync(this, cancellationToken);
            }
            else
            {
                throw new HyperIDSDKExceptionAuthRequired();
            }
        }

        public bool IsRetryPossible()
        {
            return startCounter <= 2;
        }
    }
}
