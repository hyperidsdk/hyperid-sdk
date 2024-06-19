using HyperId.SDK.Storage;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HyperId.Private
{
    internal class EnumHelper
    {
        public static int UserDataAccessScopeToValue(UserDataAccessScope accessScope)
        {
            switch (accessScope)
            {
                case UserDataAccessScope.PRIVATE:   return 0;
                case UserDataAccessScope.PUBLIC:    return 1;
            }
            return 0;
        }
    }
}
