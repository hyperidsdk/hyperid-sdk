using System;
using Microsoft.IdentityModel.Tokens;

namespace HyperId.Private
{
    /// <summary>
    /// class eTime
    /// </summary>
    internal static class eTime
    {
        private const int oneHour = 60;

        /// <summary>
        /// Utc
        /// </summary>
        public static long Utc(DateTime dateTime)
        {
            long dateTimeUtc = EpochTime.GetIntDate(dateTime);
            return dateTimeUtc;
        }
        /// <summary>
        /// Now
        /// </summary>
        public static string Now()
        {
            DateTime dateTime = DateTime.UtcNow.ToUniversalTime();
            long dateTimeUtc = Utc(dateTime);
            return dateTimeUtc.ToString();
        }
        /// <summary>
        /// NowPlusHour
        /// </summary>
        public static string NowPlusHour()
        {
            DateTime dateTime = DateTime.UtcNow.AddMinutes(oneHour);
            long dateTimeUtc = EpochTime.GetIntDate(dateTime);
            return dateTimeUtc.ToString();
        }
    }
}// namespace HyperId.Private