using HyperId.SDK;
using HyperId.SDK.Authorization;
using HyperId.SDK.KYC;
using HyperId.SDK.Storage;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace hyperid_sdk_demo.Pages
{
    public class StorageModel : PageModel
    {
        private IHyperIDSDKStorage storage;

        public StorageModel(IHyperIDSDK sdk)
        {
            storage = sdk.GetStorage();
        }
        public async Task<IActionResult> OnGetKeysGetByEmailAsync()
        {
            try
            {
                KeysGetResult response = await storage.StorageByEmail().KeysGetAsync();
                var r = response.KeysPublic;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetSharedKeysGetByEmailAsync()
        {
            try
            {
                KeysSharedGetResult response = await storage.StorageByEmail().KeysGetSharedAsync();
                var r = response.KeysShared;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataSetByEmailAsync()
        {
            try
            {
                DataSetResult response = await storage.StorageByEmail().DataSetAsync("TEST_KEY", "TEST VALUE", UserDataAccessScope.PUBLIC);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataGetByEmailAsync()
        {
            try
            {
                DataGetResult response = await storage.StorageByEmail().DataGetAsync("TEST_KEY");
                var r = response.Value;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataDeleteByEmailAsync()
        {
            try
            {
                var keys = new List<string>
                {
                    "TEST_KEY"
                };
                bool response = await storage.StorageByEmail().DataDeleteAsync(keys);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetKeysGetByUserIdAsync()
        {
            try
            {
                KeysGetResult response = await storage.StorageByUserId().KeysGetAsync();
                var r = response.KeysPublic;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetSharedKeysGetByUserIdAsync()
        {
            try
            {
                KeysSharedGetResult response = await storage.StorageByUserId().KeysGetSharedAsync();
                var r = response.KeysShared;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataSetByUserIdAsync()
        {
            try
            {
                DataSetResult response = await storage.StorageByUserId().DataSetAsync("TEST_KEY", "TEST VALUE", UserDataAccessScope.PUBLIC);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataGetByUserIdAsync()
        {
            try
            {
                DataGetResult response = await storage.StorageByUserId().DataGetAsync("TEST_KEY");
                var r = response.Value;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataDeleteByUserIdAsync()
        {
            try
            {
                var keys = new List<string>
                {
                    "TEST_KEY"
                };
                bool response = await storage.StorageByUserId().DataDeleteAsync(keys);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetKeysGetByWalletAsync()
        {
            try
            {
                KeysGetByWalletResult response = await storage.StorageByWallet().KeysGetAsync("0xa0d6051556876Ff0eEE43E20885A632F360f2924");
                var r = response.KeysPublic;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetSharedKeysGetByWalletAsync()
        {
            try
            {
                KeysSharedGetByWalletResult response = await storage.StorageByWallet().KeysGetSharedAsync("0xa0d6051556876Ff0eEE43E20885A632F360f2924");
                var r = response.KeysShared;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataSetByWalletAsync()
        {
            try
            {
                DataSetByWalletResult response = await storage.StorageByWallet().DataSetAsync("0xa0d6051556876Ff0eEE43E20885A632F360f2924", "TEST_KEY", "TEST VALUE", UserDataAccessScope.PUBLIC);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataGetByWalletAsync()
        {
            try
            {
                DataGetByWalletResult response = await storage.StorageByWallet().DataGetAsync("0xa0d6051556876Ff0eEE43E20885A632F360f2924", "TEST_KEY");
                var r = response.Value;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataDeleteByWalletAsync()
        {
            try
            {
                var keys = new List<string>
                {
                    "TEST_KEY"
                };
                DataDeleteByWalletRequestResult response = await storage.StorageByWallet().DataDeleteAsync("0xa0d6051556876Ff0eEE43E20885A632F360f2924", keys);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetKeysGetByIdpAsync()
        {
            try
            {
                KeysGetByIdpResult response = await storage.StorageByIdp().KeysGetAsync("google");
                var r = response.KeysPublic;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetSharedKeysGetByIdpAsync()
        {
            try
            {
                KeysSharedGetByIdpResult response = await storage.StorageByIdp().KeysGetSharedAsync("google");
                var r = response.KeysShared;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataSetByIdpAsync()
        {
            try
            {
                DataSetByIdentityProviderResult response = await storage.StorageByIdp().DataSetAsync("google", "TEST_KEY", "TEST VALUE", UserDataAccessScope.PUBLIC);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataGetByIdpAsync()
        {
            try
            {
                DataGetByIdpResult response = await storage.StorageByIdp().DataGetAsync("google", "TEST_KEY");
                var r = response.Value;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
        public async Task<IActionResult> OnGetDataDeleteByIdpAsync()
        {
            try
            {
                var keys = new List<string>
                {
                    "TEST_KEY"
                };
                DataDeleteByIdentityProviderRequestResult response = await storage.StorageByIdp().DataDeleteAsync("google", keys);
                var r = response;
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
    }
}
