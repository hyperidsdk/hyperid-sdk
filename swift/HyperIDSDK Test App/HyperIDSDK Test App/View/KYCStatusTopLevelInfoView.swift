import SwiftUI
import HyperIDSDK

//**************************************************************************************************
//	MARK: KYCStatusTopLevelInfoView
//--------------------------------------------------------------------------------------------------
struct KYCStatusTopLevelInfoView: View {
	var kycStatusTopLevelInfo : UserKYCStatusTopLevelInfo
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			switch kycStatusTopLevelInfo.verificationLevel {
			case .unsupported(let code):
				DisplayItemView(title: "verificationLevel", value: "unsupported(\(code)")
			case .basic:
				DisplayItemView(title: "verificationLevel", value: "basic")
			case .full:
				DisplayItemView(title: "verificationLevel", value: "full")
			}
			switch kycStatusTopLevelInfo.userStatus {
			case .unsupported(let code):
				DisplayItemView(title: "userStatus", value: "unsupported(\(code)")
			case .none:
				DisplayItemView(title: "userStatus", value: "none")
			case .pending:
				DisplayItemView(title: "userStatus", value: "pending")
			case .completeSuccess:
				DisplayItemView(title: "userStatus", value: "completeSuccess")
			case .completeFailRetryable:
				DisplayItemView(title: "userStatus", value: "completeFailRetryable")
			case .completeFailFinal:
				DisplayItemView(title: "userStatus", value: "completeFailFinal")
			case .deleted:
				DisplayItemView(title: "userStatus", value: "delete")
			}
			DisplayItemView(title: "createDT",			value: DateFormatter().string(from: kycStatusTopLevelInfo.createDt))
			DisplayItemView(title: "reviewCreateDt",	value: DateFormatter().string(from: kycStatusTopLevelInfo.reviewCreateDt))
			DisplayItemView(title: "reviewCompleteDt",	value: DateFormatter().string(from: kycStatusTopLevelInfo.reviewCompleteDt))
		}
	}
}
