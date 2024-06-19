import Foundation
import HyperIDSDK

fileprivate let LAST_AUTH_RESTORE_INFO_SECRET_KEY	= "lastAuthRestoreInfoSecret"
fileprivate let LAST_AUTH_RESTORE_INFO_HS256_KEY	= "lastAuthRestoreInfoHS256"
fileprivate let LAST_AUTH_RESTORE_INFO_RS256_KEY	= "lastAuthRestoreInfoRS256"

fileprivate let secret		= "3Sn8mPtwpaitbeTRJ9mcDNoR15kEzF9L"
fileprivate let hsSecret	= "c9prKcovIJdEzofVe2tNgZlwW3rSDEdF"
fileprivate let rsKey		= """
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDoBnQsaiqCXURs
CY6dd6fjohGVYA5xy7UiIMtqk9UVOFadNVxw5yvBx0D0n4+Zh1VMImQKG7z6PO3/
Y9Uc4zlZSIMb1hy+ZIQesmoxgTjQuOnP4NsPcL1QWxQA6hR+1iqVic1ZEtbBhrde
l6tbcEn1T0rNWPjt3PJL2RYW6vGPWxK3RfKjdKpDEOKC5SFO9cSraQ9OqX8IwTfa
v2JfNNW7rx6p4xFwAY7fcDlJ/mwYiQ4qfDydBWkP6iHwR82r+JZSGfUdz9xcSJUd
Fle+EKstRfTswCYH39d6FMbllWECTTV2CAcAyRg+MRcBoOSwvL83xYTku2Yq5/fY
oDr+B+PVAgMBAAECggEAID0VUz6FHYv7/87sI/EGQNi5/LlWCHW3e0B3Qx27U7F6
R2msqHtWVxxqaBLrjveA4I0+vTDRdyuUHhIvAE6KH1+1594+LC4nNWSw4KQF0up+
gkXJ6kFN7KZbBy1/H4h+bjyxbZjygf1H6TrFsnTNseoMiK++Fr7GY8eMDC8k1Tgc
ebEiY9ZwW4IIkeJwYnZh8yOZKc4B06DJrRzd18eEbiXf47DgzJyNnzVtLNqegbTJ
kcVAHrrdDS97otDki2Ab8RX9SAi/E4fAPw3/TVvjV6qah0ateY5kmW5mnMJkE8y6
SkKR/HKZwudJTi/dM8zIevzN6m8q1tdZ+tIcxVf5gQKBgQD/nAh/SMyzIMGOdyaP
jTqTs+sjkeOLbqkY2xPWY+XSMxyOW5LL1xi8GUQmf7HLNsyJIHuA+cuiY92jSrIs
DPlvqYin9Hlw7YaRhbsLKQIyKJ3uI+Dt93rPNOTfLLm21fxkXQCNZKP5wexOZD/h
ZjWCIdhXD/XsTVK5oaywDHgd5QKBgQDoYTJtiRN3G37WcBSlEsna10llioeprJNV
UQPLuXw20lVU8VlsS6wXi8eHgkdccd19JtL3sC+3tuKx+S2+EHGXamIBJHHwg20P
03TCgfZU7uXtNHS0X+ci7ThzTN/qGPN6XlUd/PNoqY7UCygg7pkcXkELN4f8B7D+
9G6/MYbPMQKBgQCbDcDNzZB23NjtHfQjQm2VKZ/qzNW2QCONc1++Po0sDFs3M++B
fXKAr+b6X52vgwdh63Vf0KepU2Ega/BW7mvlQ3clQxTj3wIxhmjnJTIy0Ra0XclV
MTmrNg/cHZpugbIAA7aRDsq1d+Br0T468bBlxzgf4AuzE1iqSJujk3zNzQKBgCxh
7AS5qosUKEyCiZ7hkMYIWk9XfwOsH1OrLoNpgMzjrUKU+hRR+6NfohNCkaiZYsk1
chO2hdabyn5dbhwf/eICgodfU5exMlJUe7dupQKhwi5k12lf68Bi+GYlJ5sJeu9D
NxSMLF0wDUR4gQiRKZMeeWPQDlvXiDmZq9E+f1XxAoGBAJVr/6PTqYKNj/Wfmuuf
FQge1FFojplu+y4Ur4YUEyTUzLHx0BO+0I9OZCiMrxVr5XN9a+BCeB+7nbZ1p7kJ
nPpQrR3VYr1B9AXUiZmGvzLm0uRvXTooXcveZbVZnwfiLzVrA6eKW7uoz9b9VYIw
U1cA1zoaMZIaEzM1Ipawpi7Z
"""

//**************************************************************************************************
//	MARK: RootController
//--------------------------------------------------------------------------------------------------
class RootController {
	private let sdkSecret	: HyperIDSDK
	private let sdkHS		: HyperIDSDK
	private let sdkRS		: HyperIDSDK
	private let session		: URLSession
	
	private var authRestoreInfoSecret	: String?
	private var authRestoreInfoHS256	: String?
	private var authRestoreInfoRS256	: String?
	
	let clientWithSecret : ClientController
	let clientWithHS256 : ClientController
	let clientWithRS256 : ClientController
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init?(alertState: AlertState) async {
		let secretClientInfo	= ClientInfo(clientId: 				"android-sdk-test",
											 redirectURL:			"ai.hypersphere.hyperid://localhost:4200/*",
											 authorizationMethod:	.clientSecret(secret:	secret))
		let hsClientInfo		= ClientInfo(clientId: 				"android-sdk-test-hs",
											 redirectURL:			"ai.hypersphere.hyperid://localhost:4200/auth/hyper-id/callback/*",
											 authorizationMethod:	.clientHS256(secret:	hsSecret))
		let rsClientInfo		= ClientInfo(clientId: 				"android-sdk-test-rsa",
											 redirectURL:			"ai.hypersphere.hyperid://localhost:4200/auth/hyper-id/callback/*",
											 authorizationMethod:	.clientRS256(privateKey: Data(base64Encoded: rsKey, options: .ignoreUnknownCharacters)!))
		
		var userDefaults		= UserDefaults.standard
		authRestoreInfoSecret	= userDefaults.string(forKey: LAST_AUTH_RESTORE_INFO_SECRET_KEY)
		authRestoreInfoHS256	= userDefaults.string(forKey: LAST_AUTH_RESTORE_INFO_HS256_KEY)
		authRestoreInfoRS256	= userDefaults.string(forKey: LAST_AUTH_RESTORE_INFO_RS256_KEY)
		
		session 				= URLSession.shared
		do {
			sdkSecret				= try await HyperIDSDK(clientInfo:		secretClientInfo,
														   authRestoreInfo: authRestoreInfoSecret,
														   authRestoreInfoUpdateCallback: { authRestoreInfo in
				//place your save code here
			},
														   providerInfo:	.stage,
														   urlSession:		session)
			sdkHS					= try await HyperIDSDK(clientInfo:		hsClientInfo,
														   authRestoreInfo: authRestoreInfoHS256,
														   authRestoreInfoUpdateCallback: { authRestoreInfo in
				//place your save code here
			},
														   providerInfo:	.stage,
														   urlSession:		session)
			sdkRS					= try await HyperIDSDK(clientInfo:		rsClientInfo,
														   authRestoreInfo: authRestoreInfoRS256,
														   authRestoreInfoUpdateCallback: { authRestoreInfo in
				//place your save code here
			},
														   providerInfo:	.stage,
														   urlSession:		session)
			clientWithSecret	= ClientController(hyperIDSDK: sdkSecret, alertState: alertState)
			clientWithHS256		= ClientController(hyperIDSDK: sdkHS, alertState: alertState)
			clientWithRS256		= ClientController(hyperIDSDK: sdkRS, alertState: alertState)
			DispatchQueue.main.async {
				Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
					self.clientWithSecret.update()
					self.clientWithHS256.update()
					self.clientWithRS256.update()
					
					var userDefaults = UserDefaults.standard
					if self.sdkSecret.authRestoreInfo != self.authRestoreInfoSecret {
						userDefaults.setValue(self.sdkSecret.authRestoreInfo, forKey: LAST_AUTH_RESTORE_INFO_SECRET_KEY)
						self.authRestoreInfoSecret = self.sdkSecret.authRestoreInfo
					}
					if self.sdkHS.authRestoreInfo != self.authRestoreInfoHS256 {
						userDefaults.setValue(self.sdkHS.authRestoreInfo, forKey: LAST_AUTH_RESTORE_INFO_HS256_KEY)
						self.authRestoreInfoSecret = self.sdkHS.authRestoreInfo
					}
					if self.sdkRS.authRestoreInfo != self.authRestoreInfoRS256 {
						userDefaults.setValue(self.sdkRS.authRestoreInfo, forKey: LAST_AUTH_RESTORE_INFO_RS256_KEY)
						self.authRestoreInfoSecret = self.sdkRS.authRestoreInfo
					}
					
				}
			}
		} catch HyperIDBaseAPIError.invalidProviderInfo {
			alertState.title	= "Invalid HyperID SDK configuration"
			alertState.message	= "Please check your HyperIDSDK providerInfo"
			alertState.isActive	= true
			return nil
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
			return nil
		} catch HyperIDBaseAPIError.networkingError(description: let networkingDesc) {
			alertState.title	= "Networking error"
			alertState.message	= "Details: \(networkingDesc)"
			alertState.isActive	= true
			return nil
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "Details: \(error.localizedDescription)"
			alertState.isActive	= true
			return nil
		}
	}
}
