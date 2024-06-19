import Foundation
import HyperIDBase
import HyperIDAuth

//**************************************************************************************************
//	MARK: HyperIDSDK
//--------------------------------------------------------------------------------------------------
public class HyperIDSDK : HyperIDBaseAPI {
	public typealias AuthRestoreInfoUpdateCallback = (_ authRestoreInfo	: String?) -> ()
	
			var	hyperIDAuthAPI 		: HyperIDAuthAPI!
			var	hyperIDKYCAPI		: HyperIDKYCAPI!
			var	hyperIDMFAAPI		: HyperIDMFAAPI!
			var hyperIDStorageAPI	: HyperIDStorageAPI!
	
	//==================================================================================================
	//	authorization state
	//--------------------------------------------------------------------------------------------------
	public	var isAuthorized			: Bool					{ hyperIDAuthAPI.isAuthorized										}
	public	var authRestoreInfo			: String?				{ hyperIDAuthAPI.isAuthorized ? hyperIDAuthAPI.refreshToken : nil	}
	
	private	var authorizedOperations	: [Operation]	= []
	private	var authorizedOperationsSID	: Int64			= 0
	private	var authTask				: Task<Void, any Error>?
	private	var serialDispatchQueue		: DispatchQueue = DispatchQueue(label: "serialDispatchQueue")
	
	//==================================================================================================
	//	init
	//--------------------------------------------------------------------------------------------------
	public init(clientInfo						: ClientInfo,
				authRestoreInfo					: String?				= nil,
				authRestoreInfoUpdateCallback	: @escaping AuthRestoreInfoUpdateCallback,
				providerInfo					: ProviderInfo			= ProviderInfo.production,
				urlSession						: URLSession! = URLSession.shared) async throws {
		try await super.init(providerInfo:			providerInfo,
							 openIDConfiguration:	nil,
							 urlSession:			urlSession)
		try await hyperIDAuthAPI	= HyperIDAuthAPI(clientInfo:					clientInfo,
													 refreshToken:					authRestoreInfo,
													 refreshTokenUpdateCallback:	authRestoreInfoUpdateCallback,
													 openIDConfiguration:			openIDConfiguration,
													 urlSession:					urlSession)
		try await hyperIDKYCAPI		= HyperIDKYCAPI(openIDConfiguration:			openIDConfiguration,
													urlSession:						urlSession)
		try await hyperIDMFAAPI		= HyperIDMFAAPI(openIDConfiguration:			openIDConfiguration,
													urlSession:						urlSession)
		try await hyperIDStorageAPI	= HyperIDStorageAPI(openIDConfiguration:		openIDConfiguration,
														urlSession:					urlSession)
	}
}

//**************************************************************************************************
//	MARK: HyperIDSDK - auth
//--------------------------------------------------------------------------------------------------
public extension HyperIDSDK {
	//==================================================================================================
	//	startSignInWeb2
	//--------------------------------------------------------------------------------------------------
	func startSignInWeb2(kycVerificationLevel:	KYCVerificationLevel? = nil) throws -> URL {
		try hyperIDAuthAPI.startSignInWeb2(kycVerificationLevel: kycVerificationLevel)
	}
	//==================================================================================================
	//	startSignInWeb3
	//--------------------------------------------------------------------------------------------------
	func startSignInWeb3(walletFamily			: Int64? = 0,
						 kycVerificationLevel	: KYCVerificationLevel? = nil) throws -> URL {
		try hyperIDAuthAPI.startSignInWeb3(walletFamily:			walletFamily,
										   kycVerificationLevel:	kycVerificationLevel)
	}
	//==================================================================================================
	//	startSignInUsingWallet
	//--------------------------------------------------------------------------------------------------
	func startSignInUsingWallet(walletGetMode	: WalletGetMode = .walletGetFast,
								walletFamily	: Int64? = 0) throws -> URL {
		try hyperIDAuthAPI.startSignInUsingWallet(walletGetMode:	walletGetMode,
												  walletFamily:		walletFamily)
	}
	//==================================================================================================
	//	startSignInGuestUpgrade
	//--------------------------------------------------------------------------------------------------
	func startSignInGuestUpgrade() throws -> URL {
		try hyperIDAuthAPI.startSignInGuestUpgrade()
	}
	//==================================================================================================
	//	startSignInIdentityProvider
	//--------------------------------------------------------------------------------------------------
	func startSignInIdentityProvider(identityProvider		: String,
									 kycVerificationLevel	: KYCVerificationLevel? = nil) throws -> URL {
		try hyperIDAuthAPI.startSignInIdentityProvider(identityProvider:		identityProvider,
													   kycVerificationLevel:	kycVerificationLevel)
	}
	//==================================================================================================
	//	startSignInWithTransaction
	//--------------------------------------------------------------------------------------------------
	func startSignInWithTransaction(from	: String?	= nil,
									to		: String,
									chain	: String,
									data	: String?,
									gas		: String?	= nil,
									nonce	: String?	= nil,
									value	: String?	= nil) throws -> URL {
		try hyperIDAuthAPI.startSignInWithTransaction(from:		from,
													  to:		to,
													  chain:	chain,
													  data:		data,
													  gas:		gas,
													  nonce:	nonce,
													  value:	value)
	}
	//==================================================================================================
	//	completeSignIn
	//--------------------------------------------------------------------------------------------------
	func completeSignIn(redirectURL : URL) async throws {
		do {
			try await hyperIDAuthAPI.exchangeToTokens(redirectURL: redirectURL)
		} catch HyperIDAuthAPIError.tokenExchangeInvalidGrant(description: _) {
			throw HyperIDAuthAPIError.authorizationInvalidRedirectURLError(description: "URL invalid to complete authorization. Please restart authorization")
		}
	}
	//==================================================================================================
	//	completeSignInWithTransaction
	//--------------------------------------------------------------------------------------------------
	func completeSignInWithTransaction(redirectURL : URL) async throws -> TransactionHash {
		try await hyperIDAuthAPI.exchangeToTokensWithTransaction(redirectURL: redirectURL)
	}
	//==================================================================================================
	//	signOut
	//--------------------------------------------------------------------------------------------------
	func signOut() async throws {
		try await hyperIDAuthAPI.logout()
	}
	//==================================================================================================
	//	getUserInfo
	//--------------------------------------------------------------------------------------------------
	func getUserInfo() async throws -> UserInfo {
		return try await processAuthorizedAction { _ in try await self.hyperIDAuthAPI.getUserInfo() }
	}
}

//**************************************************************************************************
//	MARK: HyperIDSDK - KYC
//--------------------------------------------------------------------------------------------------
public extension HyperIDSDK {
	//==================================================================================================
	//	getUserKYCStatusInfo
	//--------------------------------------------------------------------------------------------------
	func getUserKYCStatusInfo(kycVerificationLevel:	KYCVerificationLevel = .basic) async throws -> UserKYCStatusInfo? {
		try await processAuthorizedAction {
			try await self.hyperIDKYCAPI.getUserKYCStatusInfo(kycVerificationLevel: .basic, accessToken: $0)
		}
	}
	//==================================================================================================
	//	getUserKYCStatusTopLevelInfo
	//--------------------------------------------------------------------------------------------------
	func getUserKYCStatusTopLevelInfo() async throws -> UserKYCStatusTopLevelInfo? {
		try await processAuthorizedAction {
			try await self.hyperIDKYCAPI.getUserKYCStatusTopLevelInfo(accessToken: $0)
		}
	}
}

//**************************************************************************************************
//	MARK: HyperIDSDK - MFA
//--------------------------------------------------------------------------------------------------
public extension HyperIDSDK {
	//==================================================================================================
	//	checkMFAAvailability
	//--------------------------------------------------------------------------------------------------
	func checkMFAAvailability() async throws -> Bool {
		try await processAuthorizedAction {
			try await self.hyperIDMFAAPI.checkAvailability(accessToken: $0)
		}
	}
	//==================================================================================================
	//	startMFATransaction
	//--------------------------------------------------------------------------------------------------
	func startMFATransaction(question		: String,
							 controlCode	: Int) async throws -> Int {
		try await processAuthorizedAction {
			try await self.hyperIDMFAAPI.startTransaction(question:		question,
														  controlCode:	controlCode,
														  accessToken:	$0)
		}
	}
	//==================================================================================================
	//	getMFATransactionStatus
	//--------------------------------------------------------------------------------------------------
	func getMFATransactionStatus(transactionId : Int) async throws -> MFATransactionStatus? {
		try await processAuthorizedAction {
			try await self.hyperIDMFAAPI.getTransactionStatus(transactionId:	transactionId,
															  accessToken:		$0)
		}
	}
	//==================================================================================================
	//	cancelMFATransaction
	//--------------------------------------------------------------------------------------------------
	func cancelMFATransaction(transactionId : Int) async throws {
		try await processAuthorizedAction {
			try await self.hyperIDMFAAPI.cancelTransaction(transactionId:	transactionId,
														   accessToken:		$0)
		}
	}
}

//**************************************************************************************************
//	MARK: HyperIDSDK - Storage
//--------------------------------------------------------------------------------------------------
public extension HyperIDSDK {
	//==================================================================================================
	//	getUserWallets
	//--------------------------------------------------------------------------------------------------
	func getUserWallets() async throws -> (walletsPrivate: [Wallet], walletsPublic: [Wallet])
	{
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.getUserWallets(accessToken: $0)
		}
	}
	//==================================================================================================
	//	getUserKeysList
	//--------------------------------------------------------------------------------------------------
	func getUserKeysList(storage: HyperIDStorage) async throws -> (keysPrivate: [String], keysPublic: [String]) {
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.getUserKeysList(storage: storage, accessToken: $0)
		}
	}
	//==================================================================================================
	//	getUserSharedKeysList
	//--------------------------------------------------------------------------------------------------
	func getUserSharedKeysList(storage: HyperIDStorage) async throws -> [String] {
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.getUserSharedKeysList(storage: storage, accessToken: $0)
		}
	}
	//==================================================================================================
	//	setUserData
	//--------------------------------------------------------------------------------------------------
	func setUserData(_ value		: (key: String, value: String),
					 dataScope		: UserDataAccessScope = .public,
					 storage		: HyperIDStorage) async throws {
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.setUserData(value, dataScope: dataScope, storage: storage, accessToken: $0)
		}
	}
	//==================================================================================================
	//	getUserData
	//--------------------------------------------------------------------------------------------------
	func getUserData(_ key			: String,
					 storage		: HyperIDStorage) async throws -> String? {
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.getUserData(key, storage: storage, accessToken: $0)
		}
	}
	//==================================================================================================
	//	deleteUserData
	//--------------------------------------------------------------------------------------------------
	func deleteUserData(_ key		: String,
						storage		: HyperIDStorage) async throws {
		try await processAuthorizedAction {
			try await self.hyperIDStorageAPI.deleteUserData(key, storage: storage, accessToken: $0)
		}
	}
}

//**************************************************************************************************
//	MARK: HyperIDSDK - private
//--------------------------------------------------------------------------------------------------
private extension HyperIDSDK {
	//==================================================================================================
	//	processAuthorizedAction
	//--------------------------------------------------------------------------------------------------
	func processAuthorizedAction<T>(action : @escaping (_ accessToken: String) async throws -> T) async throws -> T {
		try await withCheckedThrowingContinuation { continuation in
			Task
			{
				try await queueAuthorizedAction(action: action) {
					continuation.resume(returning: $0)
				} onFail: {
					continuation.resume(throwing: $0)
				}

			}
		}
	}
	//==================================================================================================
	//	processAuthorizedAction
	//--------------------------------------------------------------------------------------------------
	func queueAuthorizedAction<T>(action		: @escaping (_ accessToken	: String) async throws -> T,
								  onComplete	: @escaping (_ result		: T) -> (),
								  onFail		: @escaping (_ error		: any Error) -> ()) async throws {
		let id = authorizedOperationsSID
		authorizedOperationsSID += 1
		let operation = AuthorizedOperation(id_:		id,
											operation:	action,
											onComplete: onComplete,
											onFail: 	onFail)
		serialDispatchQueue.sync {
			authorizedOperations.append(operation)
			tryProcessAuthorizedActions()
		}
	}
	//==================================================================================================
	//	tryProcessAuthorizedActions
	//--------------------------------------------------------------------------------------------------
	private func tryProcessAuthorizedActions()
	{
		if authTask != nil { return }

		authTask = Task {
			await withThrowingTaskGroup(of: Void.self) { taskGroup in
				var operationsSnapshoot : [Operation] = []
				serialDispatchQueue.sync {
					operationsSnapshoot.append(contentsOf: authorizedOperations.prefix(URLSession.shared.configuration.httpMaximumConnectionsPerHost))
				}
				if isAuthorized {
					do {
						if let accessToken = hyperIDAuthAPI.accessToken {
							try Task.checkCancellation()
							try await self.runOperations(operationsSnapshoot, taskGroup: &taskGroup, accessToken: accessToken)
						} else {
							try Task.checkCancellation()
							taskGroup.addTask {
								try Task.checkCancellation()
								try await self.hyperIDAuthAPI.refreshTokens()
							}
							try await taskGroup.next()
							guard let accessToken = hyperIDAuthAPI.accessToken else {
								hyperIDAuthAPI.invalidateTokens()
								operationsSnapshoot.forEach { $0.fail(error: HyperIDSDKError.authorizationExpired) }
								completeAuthTask()
								return
							}
							try Task.checkCancellation()
							try await self.runOperations(operationsSnapshoot, taskGroup: &taskGroup, accessToken: accessToken)
						}
					} catch HyperIDBaseAPIError.invalidAccessToken {
						do {
							try Task.checkCancellation()
							taskGroup.addTask { try await self.hyperIDAuthAPI.refreshTokens() }
							try await taskGroup.next()
							guard let accessToken = hyperIDAuthAPI.accessToken else {
								hyperIDAuthAPI.invalidateTokens()
								operationsSnapshoot.forEach { $0.fail(error: HyperIDSDKError.authorizationExpired) }
								completeAuthTask()
								return
							}
							try Task.checkCancellation()
							try await self.runOperations(operationsSnapshoot, taskGroup: &taskGroup, accessToken: accessToken)
						} catch let error as CancellationError {
							return
						} catch HyperIDBaseAPIError.invalidAccessToken,
								HyperIDAuthAPIError.tokenExchangeInvalidGrant(description: _) {
							hyperIDAuthAPI.invalidateTokens()
							operationsSnapshoot.forEach { $0.fail(error: HyperIDSDKError.authorizationExpired) }
						} catch {
							operationsSnapshoot.forEach { $0.fail(error: error) }
						}
					} catch HyperIDAuthAPIError.tokenExchangeInvalidGrant(description: _) {
						hyperIDAuthAPI.invalidateTokens()
						operationsSnapshoot.forEach { $0.fail(error: HyperIDSDKError.authorizationExpired) }
					} catch let error as CancellationError {
						return
					} catch {
						operationsSnapshoot.forEach { $0.fail(error: error) }
					}
				} else {
					hyperIDAuthAPI.invalidateTokens()
					operationsSnapshoot.forEach { $0.fail(error: HyperIDSDKError.authorizationExpired) }
				}
				completeAuthTask()
			}
		}
	}
	//==================================================================================================
	//	runOperations
	//--------------------------------------------------------------------------------------------------
	private func runOperations(_ operations : [Operation], taskGroup: inout ThrowingTaskGroup<Void, any Error>, accessToken: String) async throws {
		for operation in operations {
			try Task.checkCancellation()
			taskGroup.addTask {
				try Task.checkCancellation()
				try await operation.process(accessToken: accessToken)
			}
		}
		try await taskGroup.waitForAll()
	}
	//==================================================================================================
	//	completeAuthTask
	//--------------------------------------------------------------------------------------------------
	private func completeAuthTask() {
		authTask = nil
		self.serialDispatchQueue.sync {
			authorizedOperations.removeAll { $0.isCompleted }
			if(!authorizedOperations.isEmpty)
			{
				tryProcessAuthorizedActions()
			}
		}
	}
}

//**************************************************************************************************
//	HyperIDBase.ProviderInfo
//--------------------------------------------------------------------------------------------------
extension HyperIDBase.ProviderInfo {
	public static var stage			: Self { Self.init(scheme: "https", host: "login-stage.hypersecureid.com",	port: 443) }
}

//**************************************************************************************************
//	MARK: HyperIDSDK.Using
//--------------------------------------------------------------------------------------------------
public typealias ProviderInfo			= HyperIDBase.ProviderInfo
public typealias OpenIDConfiguration	= HyperIDBase.OpenIDConfiguration
public typealias ClientInfo				= HyperIDAuth.ClientInfo
public typealias KYCVerificationLevel	= HyperIDAuth.KYCVerificationLevel
public typealias WalletGetMode			= HyperIDAuth.WalletGetMode
public typealias UserInfo				= HyperIDAuth.UserInfo
public typealias TransactionHash		= HyperIDAuth.TransactionHash

public typealias HyperIDErrorProtocol	= HyperIDBase.HyperIDErrorProtocol
public typealias HyperIDBaseAPIError	= HyperIDBase.HyperIDBaseAPIError
public typealias HyperIDAuthAPIError	= HyperIDAuth.HyperIDAuthAPIError
