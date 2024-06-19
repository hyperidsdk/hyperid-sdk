using HyperId.SDK;
using HyperId.SDK.Authorization;
using hyperid_sdk_demo.Model;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net;

namespace hyperid_sdk_demo.Pages
{
    public class IndexModel : PageModel
    {
        private const string Message = "eAccessToken.FromJson exception: {ErrorMessage}";

        private readonly ILogger<IndexModel> _logger;

        private IHyperIDSDKAuth authSdk;

        [BindProperty]
        public AuthSDKState AuthSDKState { get; set; }

        [BindProperty]
        public int ConnectionType { get; set; } = 2;

        public IndexModel(ILogger<IndexModel> logger, IHyperIDSDK hyperIDSDK)
        {
            _logger = logger;
            _logger.LogWarning(message: Message);

            authSdk = hyperIDSDK.GetAuth();
        }
        public async Task<IActionResult> OnGetAsync()
        {
            string redirectUri = $"{HttpContext.Request.Scheme}://{HttpContext.Request.Host}{HttpContext.Request.PathBase}{HttpContext.Request.Path}{HttpContext.Request.QueryString}";

            string? code = Microsoft.AspNetCore.WebUtilities.QueryHelpers.ParseQuery(HttpContext.Request.QueryString.ToString()).FirstOrDefault(pair => pair.Key == "code").Value;
            if (!string.IsNullOrWhiteSpace(code))
            {
                try
                {
                    bool isAuthComplete = await authSdk.CompleteSignInAsync(redirectUri);
                    if (isAuthComplete)
                    {
                        AuthSDKState = AuthSDKState.AUTHORIZED;
                    }
                    else
                    {
                        AuthSDKState = AuthSDKState.INITIALISED;
                    }
                }
                catch
                { }
            }
            return Page();
        }
        public async Task<IActionResult> OnPost()
        {
            ClientInfo clientInfo;

            switch (ConnectionType)
            {
                case 0: clientInfo = new ClientInfo("android-sdk-test",
                                                    "https://localhost:44302/Index",
                                                    AuthMethod.CLIENT_SECRET_BASIC,
                                                    "3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L",
                                                    null);
                    break;
                case 1: clientInfo = new ClientInfo("android-sdk-test-hs",
                                                "https://localhost:44302/Index",
                                                AuthMethod.CLIENT_SECRET_HMAC,
                                                "c9prKcovIJdEzofVe2tNgZlwW3rSDEdF",
                                                null);
                    break;
                default: clientInfo = new ClientInfo("android-sdk-test-rsa",
                                                "https://localhost:44302/Index",
                                                AuthMethod.CLIENT_SECRET_RSA,
                                                null,
                                                "Bag Attributes\r\n    localKeyID: C1 D1 AB 7F 1E BC D9 B6 88 28 D2 11 E7 CF 53 3E E5 00 EC A2 \r\n    friendlyName: android-sdk-test-rsa\r\nKey Attributes: <No Attributes>\r\n-----BEGIN PRIVATE KEY-----\r\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDoBnQsaiqCXURs\r\nCY6dd6fjohGVYA5xy7UiIMtqk9UVOFadNVxw5yvBx0D0n4+Zh1VMImQKG7z6PO3/\r\nY9Uc4zlZSIMb1hy+ZIQesmoxgTjQuOnP4NsPcL1QWxQA6hR+1iqVic1ZEtbBhrde\r\nl6tbcEn1T0rNWPjt3PJL2RYW6vGPWxK3RfKjdKpDEOKC5SFO9cSraQ9OqX8IwTfa\r\nv2JfNNW7rx6p4xFwAY7fcDlJ/mwYiQ4qfDydBWkP6iHwR82r+JZSGfUdz9xcSJUd\r\nFle+EKstRfTswCYH39d6FMbllWECTTV2CAcAyRg+MRcBoOSwvL83xYTku2Yq5/fY\r\noDr+B+PVAgMBAAECggEAID0VUz6FHYv7/87sI/EGQNi5/LlWCHW3e0B3Qx27U7F6\r\nR2msqHtWVxxqaBLrjveA4I0+vTDRdyuUHhIvAE6KH1+1594+LC4nNWSw4KQF0up+\r\ngkXJ6kFN7KZbBy1/H4h+bjyxbZjygf1H6TrFsnTNseoMiK++Fr7GY8eMDC8k1Tgc\r\nebEiY9ZwW4IIkeJwYnZh8yOZKc4B06DJrRzd18eEbiXf47DgzJyNnzVtLNqegbTJ\r\nkcVAHrrdDS97otDki2Ab8RX9SAi/E4fAPw3/TVvjV6qah0ateY5kmW5mnMJkE8y6\r\nSkKR/HKZwudJTi/dM8zIevzN6m8q1tdZ+tIcxVf5gQKBgQD/nAh/SMyzIMGOdyaP\r\njTqTs+sjkeOLbqkY2xPWY+XSMxyOW5LL1xi8GUQmf7HLNsyJIHuA+cuiY92jSrIs\r\nDPlvqYin9Hlw7YaRhbsLKQIyKJ3uI+Dt93rPNOTfLLm21fxkXQCNZKP5wexOZD/h\r\nZjWCIdhXD/XsTVK5oaywDHgd5QKBgQDoYTJtiRN3G37WcBSlEsna10llioeprJNV\r\nUQPLuXw20lVU8VlsS6wXi8eHgkdccd19JtL3sC+3tuKx+S2+EHGXamIBJHHwg20P\r\n03TCgfZU7uXtNHS0X+ci7ThzTN/qGPN6XlUd/PNoqY7UCygg7pkcXkELN4f8B7D+\r\n9G6/MYbPMQKBgQCbDcDNzZB23NjtHfQjQm2VKZ/qzNW2QCONc1++Po0sDFs3M++B\r\nfXKAr+b6X52vgwdh63Vf0KepU2Ega/BW7mvlQ3clQxTj3wIxhmjnJTIy0Ra0XclV\r\nMTmrNg/cHZpugbIAA7aRDsq1d+Br0T468bBlxzgf4AuzE1iqSJujk3zNzQKBgCxh\r\n7AS5qosUKEyCiZ7hkMYIWk9XfwOsH1OrLoNpgMzjrUKU+hRR+6NfohNCkaiZYsk1\r\nchO2hdabyn5dbhwf/eICgodfU5exMlJUe7dupQKhwi5k12lf68Bi+GYlJ5sJeu9D\r\nNxSMLF0wDUR4gQiRKZMeeWPQDlvXiDmZq9E+f1XxAoGBAJVr/6PTqYKNj/Wfmuuf\r\nFQge1FFojplu+y4Ur4YUEyTUzLHx0BO+0I9OZCiMrxVr5XN9a+BCeB+7nbZ1p7kJ\r\nnPpQrR3VYr1B9AXUiZmGvzLm0uRvXTooXcveZbVZnwfiLzVrA6eKW7uoz9b9VYIw\r\nU1cA1zoaMZIaEzM1Ipawpi7Z\r\n-----END PRIVATE KEY-----\r\nBag Attributes\r\n    localKeyID: C1 D1 AB 7F 1E BC D9 B6 88 28 D2 11 E7 CF 53 3E E5 00 EC A2 \r\n    friendlyName: android-sdk-test-rsa\r\nsubject=CN = android-sdk-test-rsa\r\n\r\nissuer=CN = android-sdk-test-rsa\r\n\r\n-----BEGIN CERTIFICATE-----\r\nMIICtzCCAZ8CBgGLPQ0faTANBgkqhkiG9w0BAQsFADAfMR0wGwYDVQQDDBRhbmRy\r\nb2lkLXNkay10ZXN0LXJzYTAeFw0yMzEwMTcwOTUwNTJaFw0zMzEwMTcwOTUyMzJa\r\nMB8xHTAbBgNVBAMMFGFuZHJvaWQtc2RrLXRlc3QtcnNhMIIBIjANBgkqhkiG9w0B\r\nAQEFAAOCAQ8AMIIBCgKCAQEA6AZ0LGoqgl1EbAmOnXen46IRlWAOccu1IiDLapPV\r\nFThWnTVccOcrwcdA9J+PmYdVTCJkChu8+jzt/2PVHOM5WUiDG9YcvmSEHrJqMYE4\r\n0Ljpz+DbD3C9UFsUAOoUftYqlYnNWRLWwYa3XperW3BJ9U9KzVj47dzyS9kWFurx\r\nj1sSt0Xyo3SqQxDiguUhTvXEq2kPTql/CME32r9iXzTVu68eqeMRcAGO33A5Sf5s\r\nGIkOKnw8nQVpD+oh8EfNq/iWUhn1Hc/cXEiVHRZXvhCrLUX07MAmB9/XehTG5ZVh\r\nAk01dggHAMkYPjEXAaDksLy/N8WE5LtmKuf32KA6/gfj1QIDAQABMA0GCSqGSIb3\r\nDQEBCwUAA4IBAQCd3WSpPH7C3tvgj2vn33f54wb4btSybf4T1/v+htljsXJJMZeA\r\nJjXg7QmMVXbIjBpdlTke7DL7cVh6H8dsp9C2ia1WZlYVHErlUuLhJMbcmH+yBTNn\r\nOkofV4HxeZcOveXf416Fq/xB2Km+8Z3DUskRT3AKDm7cghGzrz2XIVM+S9uZ9TfP\r\nqTJ3jGSZE8gAJfxAsTY9qfY/fz0gLgEcdKAncTt5i9umjbjGGvvxKW+1jPs0apqC\r\noiSiAB8DR+HjBGnpDpTtAk6rDnHZeIh27r2rGMZNpzHRhzKXX6da5LRJ6xQM+z8Z\r\nU5Rcuob+qCqglJQlCulUbuL2mTH+8OahfFPy\r\n-----END CERTIFICATE-----\r\n");
                    break;
            }
            try
            {
                await authSdk.InitAsync(ProviderInfo.STAGE,
                                        clientInfo,
                                        null);

                Console.WriteLine("AuthSDKState.INITIALISED");

                AuthSDKState = AuthSDKState.INITIALISED;
            }
            catch(Exception ex)
            { }

            return Page();
        }
        public IActionResult OnGetSDKAuthorize(string flow)
        {
            Console.WriteLine($"Flow mode is {flow}");

            try
            {
                string authUrl = "";
                switch(flow)
                {
                    case "web2":
                        {
                            authUrl = authSdk.StartSignInWeb2(null);
                        }
                        break;
                    case "web3":
                        {
                            authUrl = authSdk.StartSignInWeb3(null, null);
                        }
                        break;
                    case "wallet_get":
                        {
                            authUrl = authSdk.StartWalletGet(WalletGetMode.FAST, null);
                        }
                        break;
                    case "guest_upgrade":
                        {
                            authUrl = authSdk.StartSignInGuestUpgrade();
                        }
                        break;
                    case "idp":
                        {
                            authUrl = authSdk.StartSignInIdentityProvider(authSdk.IdentityProviders().First());
                        }
                        break;
                }

                if (!string.IsNullOrEmpty(authUrl))
                {
                    return Redirect(authUrl);
                }
            }
            catch(Exception ex)
            {

            }
            return Page();
        }

        public async Task<IActionResult> OnGetSignOutAsync()
        {
            try
            {
                await authSdk.SignOutAsync();

                AuthSDKState = AuthSDKState.INITIALISED;
            }
            catch (Exception ex)
            {
            }

            return Page();
        }
        public IActionResult OnGetUserInfo()
        {
            try
            {
                UserInfo? userInfo = authSdk.UserInfo();

                AuthSDKState = AuthSDKState.AUTHORIZED;
            }
            catch (Exception ex)
            {
                if (ex is HyperIDSDKException)
                {

                }
            }

            return Page();
        }
        public IActionResult OnGetMFA()
        {
            return RedirectToPage("./Mfa");
        }
        public IActionResult OnGetKYC()
        {
            return RedirectToPage("./Kyc");
        }
        public IActionResult OnGetStorage()
        {
            return RedirectToPage("./Storage");
        }
    }
}