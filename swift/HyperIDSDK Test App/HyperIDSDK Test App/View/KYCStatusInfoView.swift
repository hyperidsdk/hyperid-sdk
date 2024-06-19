import SwiftUI
import HyperIDSDK

//**************************************************************************************************
//	MARK: KYCStatusInfoView
//--------------------------------------------------------------------------------------------------
struct KYCStatusInfoView: View {
	var kycStatusInfo : UserKYCStatusInfo
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			switch kycStatusInfo.verificationLevel {
			case .unsupported(let code):
				DisplayItemView(title: "verificationLevel", value: "unsupported(\(code)")
			case .basic:
				DisplayItemView(title: "verificationLevel", value: "basic")
			case .full:
				DisplayItemView(title: "verificationLevel", value: "full")
			}
			switch kycStatusInfo.userStatus {
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
			if let kycId = kycStatusInfo.kycId {
				DisplayItemView(title: "kycId", value: kycId)
			}
			if let firstName = kycStatusInfo.firstName {
				DisplayItemView(title: "firstName", value: firstName)
			}
			if let lastName = kycStatusInfo.lastName {
				DisplayItemView(title: "lastName", value: lastName)
			}
			if let birthday = kycStatusInfo.birthday {
				DisplayItemView(title: "birthday", value: birthday)
			}
			if let countryA2 = kycStatusInfo.countryA2 {
				DisplayItemView(title: "countryA2", value: countryA2)
			}
			if let countryA3 = kycStatusInfo.countryA3 {
				DisplayItemView(title: "countryA3", value: countryA3)
			}
			if let providedCountryA2 = kycStatusInfo.providedCountryA2 {
				DisplayItemView(title: "providedCountryA2", value: providedCountryA2)
			}
			if let providedCountryA3 = kycStatusInfo.providedCountryA3 {
				DisplayItemView(title: "providedCountryA3", value: providedCountryA3)
			}
			if let providedCountryA3 = kycStatusInfo.providedCountryA3 {
				DisplayItemView(title: "providedCountryA3", value: providedCountryA3)
			}
			if let addressCountryA2 = kycStatusInfo.addressCountryA2 {
				DisplayItemView(title: "addressCountryA2", value: addressCountryA2)
			}
			if let addressCountryA3 = kycStatusInfo.addressCountryA3 {
				DisplayItemView(title: "addressCountryA3", value: addressCountryA3)
			}
			if let phoneNumberCountryA2 = kycStatusInfo.phoneNumberCountryA2 {
				DisplayItemView(title: "phoneNumberCountryA2", value: phoneNumberCountryA2)
			}
			if let phoneNumberCountryA3 = kycStatusInfo.phoneNumberCountryA3 {
				DisplayItemView(title: "phoneNumberCountryA3", value: phoneNumberCountryA3)
			}
			if let phoneNumberCountryCode = kycStatusInfo.phoneNumberCountryCode {
				DisplayItemView(title: "phoneNumberCountryCode", value: phoneNumberCountryCode)
			}
			if let ipCountriesA2 = kycStatusInfo.ipCountriesA2 {
				DisplayItemView(title: "ipCountriesA2", value: ipCountriesA2.joined(separator: "\n"))
			}
			if let ipCountriesA3 = kycStatusInfo.ipCountriesA3 {
				DisplayItemView(title: "ipCountriesA3", value: ipCountriesA3.joined(separator: "\n"))
			}
			if let moderationComment = kycStatusInfo.moderationComment {
				DisplayItemView(title: "moderationComment", value: moderationComment)
			}
			if let rejectReasons = kycStatusInfo.rejectReasons {
				DisplayItemView(title: "rejectReasons", value: rejectReasons.joined(separator: "\n"))
			}
			if let supportLink = kycStatusInfo.supportLink {
				DisplayItemView(title: "supportLink", value: supportLink.absoluteString)
			}
			DisplayItemView(title: "createDT",			value: DateFormatter().string(from: kycStatusInfo.createDt))
			DisplayItemView(title: "reviewCreateDt",	value: DateFormatter().string(from: kycStatusInfo.reviewCreateDt))
			DisplayItemView(title: "reviewCompleteDt",	value: DateFormatter().string(from: kycStatusInfo.reviewCompleteDt))
			DisplayItemView(title: "expirationDt",		value: DateFormatter().string(from: kycStatusInfo.expirationDt))
		}
	}
}
