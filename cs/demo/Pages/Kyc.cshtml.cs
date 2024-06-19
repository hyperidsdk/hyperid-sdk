using HyperId.SDK;
using Microsoft.AspNetCore.Mvc.RazorPages;
using HyperId.SDK.KYC;
using Microsoft.AspNetCore.Mvc;
using HyperId.SDK.Authorization;

namespace hyperid_sdk_demo.Pages
{
    public class KycModel : PageModel
    {
        private IHyperIDSDKKyc hyperidSDKKyc;

        public KycModel(IHyperIDSDK hyperIDSDK)
        {
            hyperidSDKKyc = hyperIDSDK.GetKYC();
        }

        public async Task<IActionResult> OnGetStausGetAsync()
        {
            try
            {
                KycUserStatusResponse response = await hyperidSDKKyc.UserStatusGetAsync(KycVerificationLevel.FULL);
            }
            catch (Exception ex)
            {

            }

            return Page();
        }

        public async Task<IActionResult> OnGetTopLevelStatusGetAsync()
        {
            try
            {
                KycUserStatusTopLevelResponse response = await hyperidSDKKyc.UserStatusTopLevelGetAsync();
            }
            catch (Exception ex)
            {

            }

            return Page();
        }
    }
}
