import Foundation
import Combine
import HyperIDSDK
import AuthenticationServices

//**************************************************************************************************
//	MARK: ClientController
//--------------------------------------------------------------------------------------------------
class ClientController : NSObject, ObservableObject {
	private var hyperIDSDK					: HyperIDSDK
	private var authSession					: ASWebAuthenticationSession?
	private var alertState					: AlertState!
	
	@Published var isAuthorized				: Bool		= false
	@Published var userInfo					: UserInfo?
	
	@Published var kycStatusInfo			: UserKYCStatusInfo?
	@Published var kycStatusTopLevelInfo	: UserKYCStatusTopLevelInfo?
	
	@Published var mfaTransactionId			: Int?
	@Published var controlCode				: Int?
	@Published var mfaTransactionStatus		: MFATransactionStatus?
	
	@Published var walletsPublic			: [Wallet]?
	@Published var walletsPrivate			: [Wallet]?
	
	@Published var clientPrivateKeys		: [String]?
	@Published var clientPublicKeys			: [String]?
	@Published var clientSharedKeys			: [String]?
	@Published var lastLoadedValue			: String?
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	init(hyperIDSDK: HyperIDSDK, alertState: AlertState!) {
		self.hyperIDSDK	= hyperIDSDK
		self.alertState	= alertState
	}
	//==================================================================================================
	//	update
	//--------------------------------------------------------------------------------------------------
	func update() {
		self.isAuthorized = hyperIDSDK.isAuthorized
	}
	//==================================================================================================
	//	signInWeb2
	//--------------------------------------------------------------------------------------------------
	func signInWeb2() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInWeb2())
		} catch HyperIDBaseAPIError.invalidKYCVerificationLevel {
			alertState.title	= "Fatal error"
			alertState.message	= "Invalid KYC Verifcation Level"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signInWeb3
	//--------------------------------------------------------------------------------------------------
	func signInWeb3() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInWeb3())
		} catch HyperIDBaseAPIError.invalidKYCVerificationLevel {
			alertState.title	= "Fatal error"
			alertState.message	= "Invalid KYC Verifcation Level"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signInWithWallet
	//--------------------------------------------------------------------------------------------------
	func signInWithWallet() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInUsingWallet(walletGetMode: .walletGetFull))
		} catch HyperIDBaseAPIError.invalidKYCVerificationLevel {
			alertState.title	= "Fatal error"
			alertState.message	= "Invalid KYC Verifcation Level"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signInGuestUpdgrade
	//--------------------------------------------------------------------------------------------------
	func signInGuestUpdgrade() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInGuestUpgrade())
		} catch HyperIDBaseAPIError.invalidKYCVerificationLevel {
			alertState.title	= "Fatal error"
			alertState.message	= "Invalid KYC Verifcation Level"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signInWithGoogle
	//--------------------------------------------------------------------------------------------------
	func signInWithGoogle() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInIdentityProvider(identityProvider: "google"))
		} catch HyperIDBaseAPIError.invalidKYCVerificationLevel {
			alertState.title	= "Fatal error"
			alertState.message	= "Invalid KYC Verifcation Level"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signInWithTransaction
	//--------------------------------------------------------------------------------------------------
	func signInWithTransaction() {
		do {
			runAuthSession(authURL: try hyperIDSDK.startSignInWithTransaction(from:		"0x43D192d3eC9CaEFbc92385bED8508d87E566595f",
																			  to:		"0x0AeB980AB115E45409D9bA33CCffcc75995E3dfA",
																			  chain:	"11155111",
																			  data:		"0x0",
																			  nonce:	"0",
																			  value:	"0x1"))
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	getUserInfo
	//--------------------------------------------------------------------------------------------------
	@MainActor func getUserInfo() async {
		do {
			userInfo = try await hyperIDSDK.getUserInfo()
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	signout
	//--------------------------------------------------------------------------------------------------
	func signout() {
		Task {
			do {
				try await hyperIDSDK.signOut()
			} catch HyperIDBaseAPIError.serverMaintenance {
				alertState.title	= "HyperID server maintenance"
				alertState.message	= "Please try again later"
				alertState.isActive	= true
			} catch HyperIDBaseAPIError.networkingError(description: let desc) {
				alertState.title	= "Networking error"
				alertState.message	= "Details: \(desc)"
				alertState.isActive	= true
			}
		}
	}
	//==================================================================================================
	//	runAuthSession
	//--------------------------------------------------------------------------------------------------
	private func runAuthSession(authURL: URL) {
		authSession = ASWebAuthenticationSession(url:				authURL,
												 callbackURLScheme:	"ai.hypersphere.hyperid",
												 completionHandler:	{ redirectURL, error in
			if let redirectURL = redirectURL {
				Task {
					try await self.hyperIDSDK.completeSignIn(redirectURL: redirectURL)
				}
			} else if let error = error {
				self.alertState.title		= "Auth error"
				self.alertState.message		= "\(error)"
				self.alertState.isActive	= true
			}
		})
		authSession?.presentationContextProvider = self
		authSession?.prefersEphemeralWebBrowserSession = true
		authSession?.start()
	}
	//==================================================================================================
	//	runAuthSession
	//--------------------------------------------------------------------------------------------------
	private func runAuthSessionWithTransaction(authURL: URL) {
		authSession = ASWebAuthenticationSession(url:				authURL,
												 callbackURLScheme:	"ai.hypersphere.hyperid",
												 completionHandler:	{ redirectURL, error in
			if let redirectURL = redirectURL {
				Task {
					try await self.hyperIDSDK.completeSignInWithTransaction(redirectURL: redirectURL)
				}
			} else if let error = error {
				self.alertState.title		= "Auth error"
				self.alertState.message		= "\(error)"
				self.alertState.isActive	= true
			}
		})
		authSession?.presentationContextProvider = self
		authSession?.prefersEphemeralWebBrowserSession = true
		authSession?.start()
	}
	//==================================================================================================
	//	getUserKYCStatusInfo
	//--------------------------------------------------------------------------------------------------
	@MainActor func getUserKYCStatusInfo() async {
		do {
			kycStatusInfo = try await hyperIDSDK.getUserKYCStatusInfo(kycVerificationLevel: .basic)
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	getUserKYCStatusTopLevelInfo
	//--------------------------------------------------------------------------------------------------
	@MainActor func getUserKYCStatusTopLevelInfo() async {
		do {
			kycStatusTopLevelInfo = try await hyperIDSDK.getUserKYCStatusTopLevelInfo()
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	startMFATransaction
	//--------------------------------------------------------------------------------------------------
	@MainActor func startMFATransaction(question: String) async {
		do {
			controlCode				= Int.random(in: 0..<100)
			mfaTransactionId		= try await hyperIDSDK.startMFATransaction(question: question, controlCode: controlCode!)
			mfaTransactionStatus	= nil
			return
		} catch HyperIDMFAAPIError.controlCodeInvalidValue {
			alertState.title	= "Control code invalid"
			alertState.message	= "Please check your code"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
		controlCode			= nil
		mfaTransactionId	= nil
	}
	//==================================================================================================
	//	getMFATransactionStatus
	//--------------------------------------------------------------------------------------------------
	@MainActor func getMFATransactionStatus() async {
		do {
			mfaTransactionStatus = try await hyperIDSDK.getMFATransactionStatus(transactionId: mfaTransactionId!)
			if mfaTransactionStatus == nil {
				alertState.title	= "Transaction not found"
				alertState.message	= "Transaction with id \(mfaTransactionId ?? -1) not found"
				alertState.isActive	= true
			}
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	cancelMFATransaction
	//--------------------------------------------------------------------------------------------------
	@MainActor func cancelMFATransaction() async {
		do {
			try await hyperIDSDK.cancelMFATransaction(transactionId: mfaTransactionId!)
			mfaTransactionId		= nil
			controlCode				= nil
			mfaTransactionStatus	= nil
		} catch HyperIDMFAAPIError.MFATransactionNotFound {
			alertState.title		= "MFA Transaction not found"
			alertState.message		= "Transaction with id \(mfaTransactionId ?? -1) not found"
			alertState.isActive		= true
			mfaTransactionId		= nil
			controlCode				= nil
			mfaTransactionStatus	= nil
		} catch HyperIDMFAAPIError.MFATransactionAlreadyCompleted {
			alertState.title	= "MFA Transaction already completed"
			alertState.message	= "Transaction with id \(mfaTransactionId ?? -1) already completed"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	getUserWallets
	//--------------------------------------------------------------------------------------------------
	@MainActor func getUserWallets() async {
		do {
			let wallets = try await hyperIDSDK.getUserWallets()
			walletsPublic = wallets.walletsPublic
			walletsPrivate = wallets.walletsPrivate
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	loadStorageClientKeys
	//--------------------------------------------------------------------------------------------------
	@MainActor func loadStorageClientKeys(storage: HyperIDStorage) async {
		clientPublicKeys	= nil
		clientPrivateKeys	= nil
		clientSharedKeys	= nil
		do {
			let (keysPrivate: keysPrivate, keysPublic: keysPublic) = try await hyperIDSDK.getUserKeysList(storage: storage)
			clientPrivateKeys	= keysPrivate
			clientPublicKeys	= keysPublic
		} catch HyperIDStorageAPIError.identityProviderNotFound {
			alertState.title	= "Unknown identity provider"
			alertState.message	= "Please check provider availability in provider configuration"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.walletNotExists {
			alertState.title	= "Wallet not found"
			alertState.message	= "Wallet not found or not attached to the HyperID account"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	loadStorageSharedKeys
	//--------------------------------------------------------------------------------------------------
	@MainActor func loadStorageSharedKeys(storage: HyperIDStorage) async {
		clientPublicKeys	= nil
		clientPrivateKeys	= nil
		clientSharedKeys	= nil
		do {
			clientSharedKeys = try await hyperIDSDK.getUserSharedKeysList(storage: storage)
		} catch HyperIDStorageAPIError.identityProviderNotFound {
			alertState.title	= "Unknown identity provider"
			alertState.message	= "Please check provider availability in provider configuration"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.walletNotExists {
			alertState.title	= "Wallet not found"
			alertState.message	= "Wallet not found or not attached to the HyperID account"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	setStorageData
	//--------------------------------------------------------------------------------------------------
	@MainActor func setStorageData(key			: String,
								   value		: String,
								   isPrivate	: Bool,
								   storage		: HyperIDStorage) async {
		do {
			try await hyperIDSDK.setUserData((key: key, value: value),
											 dataScope: isPrivate ? .private : .public,
											 storage: storage)
		} catch HyperIDStorageAPIError.keyInvalid {
			alertState.title	= "Key invalid"
			alertState.message	= "Key \"\(key)\" invalid"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.keyAccessDenied {
			alertState.title	= "Key access denied"
			alertState.message	= "You have no access to this key"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.identityProviderNotFound {
			alertState.title	= "Unknown identity provider"
			alertState.message	= "Please check provider availability in provider configuration"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.walletNotExists {
			alertState.title	= "Wallet not found"
			alertState.message	= "Wallet not found or not attached to the HyperID account"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	getStorageValue
	//--------------------------------------------------------------------------------------------------
	@MainActor func getStorageValue(key		: String,
									storage	: HyperIDStorage) async {
		do {
			lastLoadedValue = try await hyperIDSDK.getUserData(key, storage: storage)
		} catch HyperIDStorageAPIError.identityProviderNotFound {
			alertState.title	= "Unknown identity provider"
			alertState.message	= "Please check provider availability in provider configuration"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.walletNotExists {
			alertState.title	= "Wallet not found"
			alertState.message	= "Wallet not found or not attached to the HyperID account"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
	//==================================================================================================
	//	removeStorageValue
	//--------------------------------------------------------------------------------------------------
	@MainActor func removeStorageValue(key		: String,
									   storage	: HyperIDStorage) async {
		do {
			try await hyperIDSDK.deleteUserData(key, storage: storage)
		} catch HyperIDStorageAPIError.identityProviderNotFound {
			alertState.title	= "Unknown identity provider"
			alertState.message	= "Please check provider availability in provider configuration"
			alertState.isActive	= true
		} catch HyperIDStorageAPIError.walletNotExists {
			alertState.title	= "Wallet not found"
			alertState.message	= "Wallet not found or not attached to the HyperID account"
			alertState.isActive	= true
		} catch HyperIDSDKError.authorizationExpired {
			alertState.title	= "Authorization expired"
			alertState.message	= "Please sign in again"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.serverMaintenance {
			alertState.title	= "HyperID server maintenance"
			alertState.message	= "Please try again later"
			alertState.isActive	= true
		} catch HyperIDBaseAPIError.networkingError(description: let desc) {
			alertState.title	= "Networking error"
			alertState.message	= "\(desc)"
			alertState.isActive	= true
		} catch {
			alertState.title	= "Unknown error"
			alertState.message	= "\(error.localizedDescription)"
			alertState.isActive	= true
		}
	}
}

extension ClientController : ASWebAuthenticationPresentationContextProviding {
	func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor { UIApplication.shared.windows.first! }
}
