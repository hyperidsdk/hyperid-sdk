using Microsoft.EntityFrameworkCore.Metadata;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace HyperId.SDK.Authorization
{
    public struct WalletInfo
    {
        public WalletInfo(string walletAddress,
            string? walletChainId,
            int walletSourceId,
            bool isWalletVerified,
            int walletFamilyId,
            string? walletTags)
        {
            WalletAddress = walletAddress;
            WalletChainId = walletChainId;
            WalletSourceId = walletSourceId;
            IsWalletVerified = isWalletVerified;
            WalletFamilyId = walletFamilyId;
            WalletTags = walletTags;
        }

        public string WalletAddress { get; }
        public string? WalletChainId { get; }
        public int WalletSourceId { get; }
        public bool IsWalletVerified { get; }
        public int WalletFamilyId { get; }
        public string? WalletTags { get; }
    }

    /// <summary>
    /// 
    /// </summary>
    public struct UserInfo
    {
        public UserInfo(string userId,
            bool isGuest,
            string? email,
            bool isEmailVerified,
            string? deviceId,
            string? ip,
            WalletInfo? walletInfo)
        {
            UserId = userId;
            IsGuest = isGuest;
            Email = email;
            IsEmailVerified = isEmailVerified;
            DeviceId = deviceId;
            Ip = ip;
            WalletInfo = walletInfo;
        }

        public string UserId { get; }
        public bool IsGuest { get; }
        public string? Email { get; }
        public bool IsEmailVerified { get; }
        public string? DeviceId { get; }
        public string? Ip { get; }
        public WalletInfo? WalletInfo { get; }
    }
}//HyperId.SDK.Authorization