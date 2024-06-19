import SwiftUI
import HyperIDSDK

//**************************************************************************************************
//	MARK: UserInfoView
//--------------------------------------------------------------------------------------------------
struct UserInfoView: View {
	var userInfo : UserInfo
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			if let userId = userInfo.userId {
				DisplayItemView(title: "userId:", value: userId)
			}
			DisplayItemView(title: "isGuest:", value: userInfo.isGuest ? "true" : "false")
			if let email = userInfo.email {
				DisplayItemView(title: "email", value: email)
			}
			if let deviceId = userInfo.deviceId {
				DisplayItemView(title: "deviceId", value: deviceId)
			}
			if let ip = userInfo.ip {
				DisplayItemView(title: "ip", value: ip)
			}
			if let walletAddress = userInfo.wallet?.address {
				DisplayItemView(title: "wallet.address", value: walletAddress)
			}
			if let walletChainId = userInfo.wallet?.chainId {
				DisplayItemView(title: "wallet.chainId", value: walletChainId)
			}
			if let walletSource = userInfo.wallet?.source {
				DisplayItemView(title: "wallet.source", value: walletSource)
			}
			if let walletIsVerified = userInfo.wallet?.isVerified {
				DisplayItemView(title: "wallet.isVerified", value: walletIsVerified ? "true" : "false")
			}
			if let walletFamily = userInfo.wallet?.family {
				DisplayItemView(title: "wallet.family", value: walletFamily)
			}
		}
	}
}
