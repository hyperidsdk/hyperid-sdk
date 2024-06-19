using HyperId.SDK;
using HyperId.SDK.MFA;
using hyperid_sdk_demo.Model;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace hyperid_sdk_demo.Pages
{
    public class MfaModel : PageModel
    {
        private IHyperIDSDKMFA hyperidSDKMFA;

        [BindProperty]
        public MfaSDKState MFASDKState { get; private set; }

        [BindProperty]
        public bool IsMFAAvailable { get; private set; }

        [BindProperty]
        public int TransactionId { get; private set; }

        [BindProperty]
        public MfaTransactionStatus TransactionStatus { get; private set; }



        public MfaModel(IHyperIDSDK hyperIDSDK)
        {
            hyperidSDKMFA = hyperIDSDK.GetMFA();
        }

        public void OnGet()
        {
            MFASDKState = MfaSDKState.CREATED;
            IsMFAAvailable = false;
        }

        public async Task<IActionResult> OnGetAvailabilityCheckAsync()
        {
            try
            {
                IsMFAAvailable = await hyperidSDKMFA.AvailabilityCheckAsync();
                if (IsMFAAvailable)
                {
                    MFASDKState = MfaSDKState.AVAILABILITY_CHECKED;
                }
                else
                {
                    MFASDKState = MfaSDKState.CREATED;
                }
            }
            catch (Exception ex)
            { 

            }

            return Page();
        }
        public async Task<IActionResult> OnGetTransactionCreateAsync()
        {
            try
            {
                TransactionId = await hyperidSDKMFA.TransactionStartAsync("Do you request this???", "02");
                if (TransactionId > 0)
                {
                    MFASDKState = MfaSDKState.TRANSACTION_CREATED;
                }
                else
                {
                    MFASDKState = MfaSDKState.AVAILABILITY_CHECKED;
                }
            }
            catch (Exception ex)
            {
            }

            return Page();
        }
        public async Task<IActionResult> OnGetTransactionStatusCheckAsync(int transactionId)
        {
            TransactionId = transactionId;
            try
            {
                MFATransactionStatus answer = await hyperidSDKMFA.TransactionStatusCheckAsync(TransactionId);
                if (answer.StatusGetResult == MfaTransactionStatusGetResult.SUCCESS)
                {
                    if (answer.TransactionId == TransactionId)
                    {
                        TransactionStatus = answer.Status ?? throw new ArgumentNullException();
                        if (TransactionStatus == MfaTransactionStatus.COMPLETED)
                        {
                            //complete result is valid
                        }
                    }
                    else
                    {
                        //wrong transaction
                    }
                }
                else
                {
                    //transaction not found
                }
            }
            catch (Exception ex)
            {
                //errors
            }

            return Page();
        }
        public async Task<IActionResult> OnGetTransactionCancelAsync(int transactionId)
        {
            TransactionId = transactionId;
            try
            {
                bool isCancelled = await hyperidSDKMFA.TransactionCancelAsync(TransactionId);
                if (isCancelled)
                {
                    MFASDKState = MfaSDKState.AVAILABILITY_CHECKED;
                }
                else
                {
                    MFASDKState = MfaSDKState.TRANSACTION_CREATED;
                }
            }
            catch (Exception ex)
            {
            }

            return Page();
        }        
    }
}
