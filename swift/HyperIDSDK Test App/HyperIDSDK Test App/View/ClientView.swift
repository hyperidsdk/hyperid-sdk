import SwiftUI
import HyperIDSDK

//**************************************************************************************************
//	MARK: ClientView
//--------------------------------------------------------------------------------------------------
struct ClientView: View {
	@ObservedObject var clientController				: ClientController
	var alertState										: AlertState
	
	@State private var isUserInfoPresented				: Bool = false
	@State private var isKYCStatusInfoPresented			: Bool = false
	@State private var isKYCStatusTopLevelInfoPresented	: Bool = false
	@State private var isUserWalletsViewPresented		: Bool = false
	@State private var isStorageKeysViewPresesnted		: Bool = false
	
	@State private var mfaQuestion						: String = ""
	
	@State private var selectedStorageIndex : Int = 0
	let storages: [String] = [
		"Email",
		"UserID",
		"Wallet",
		"IDP Apple",
	]
	@State private var walletAddress					: String = ""
	@State private var storageKey						: String = ""
	@State private var storageValue						: String = ""
	
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		List {
			if clientController.isAuthorized {
				sectionKYC
				sectionMFA
				sectionStorage
				sectionAccount
			} else {
				sectionSignIn
			}
		}
		.sheet(isPresented: $isUserInfoPresented) {
			NavigationView {
				UserInfoView(userInfo: clientController.userInfo!)
					.navigationTitle("User Info")
			}
		}
		.sheet(isPresented: $isKYCStatusInfoPresented) {
			NavigationView {
				VStack {
					if let info = clientController.kycStatusInfo
					{
						KYCStatusInfoView(kycStatusInfo: info)
					} else {
						Text("Info not found")
					}
				}
					.navigationTitle("KYC Status Info")
			}
		}
		.sheet(isPresented: $isKYCStatusTopLevelInfoPresented) {
			NavigationView {
				VStack {
					if let info = clientController.kycStatusTopLevelInfo
					{
						KYCStatusTopLevelInfoView(kycStatusTopLevelInfo: info)
					} else {
						Text("Info not found")
					}
				}
					.navigationTitle("KYC Status Top Level Info")
			}
		}
		.sheet(isPresented: $isStorageKeysViewPresesnted) {
			NavigationView {
				VStack {
					StorageKeysView(privateKeys:	clientController.clientPrivateKeys,
									publicKeys:		clientController.clientPublicKeys,
									sharedKeys:		clientController.clientSharedKeys)
				}
					.navigationTitle("Storage keys")
			}
		}
		.sheet(isPresented: $isUserWalletsViewPresented, content: {
			NavigationView {
				VStack {
					UserWalletsView(walletsPublic:	clientController.walletsPublic,
									walletsPrivate:	clientController.walletsPrivate)
				}
					.navigationTitle("Storage keys")
			}
		})
	}
	//==================================================================================================
	//	sectionSignIn
	//--------------------------------------------------------------------------------------------------
	var sectionSignIn : some View {
		Section {
			Button(action: {
				clientController.signInWeb2()
			}, label: {
				Text("SignIn Web2")
			})
			Button(action: {
				clientController.signInWeb3()
			}, label: {
				Text("SignIn Web3")
			})
			Button(action: {
				clientController.signInWithWallet()
			}, label: {
				Text("SignIn Wallet")
			})
			Button(action: {
				clientController.signInGuestUpdgrade()
			}, label: {
				Text("SignIn GuestUpgrade")
			})
			Button(action: {
				clientController.signInWithGoogle()
			}, label: {
				Text("SignIn with Google")
			})
		}
	}
	//==================================================================================================
	//	sectionAccount
	//--------------------------------------------------------------------------------------------------
	var sectionAccount : some View {
		Section(header: Text("Account")){
			Button(action: {
				Task {
					await clientController.getUserInfo()
					isUserInfoPresented = true
				}
			}, label: {
				Text("User info")
			})
			Button(action: {
				clientController.signout()
			}, label: {
				Text("Logout")
					.foregroundColor(.red)
			})
		}
	}
	//==================================================================================================
	//	sectionKYC
	//--------------------------------------------------------------------------------------------------
	var sectionKYC : some View {
		Section(header: Text("KYC")) {
			Button {
				Task {
					await clientController.getUserKYCStatusInfo()
					isKYCStatusInfoPresented = true
				}
			} label: {
				Text("KYC Status Info")
			}
			Button {
				Task {
					await clientController.getUserKYCStatusTopLevelInfo()
					isKYCStatusTopLevelInfoPresented = true
				}
			} label: {
				Text("KYC Status Top Level Info")
			}
		}
	}
	//==================================================================================================
	//	sectionMFA
	//--------------------------------------------------------------------------------------------------
	var sectionMFA : some View {
		Section(header: Text("MFA")) {
			TextField("Question", text: $mfaQuestion)
			Button(action: {
				Task {
					await clientController.startMFATransaction(question: mfaQuestion)
				}
			}, label: {
				Text("Ask")
			})
			if let mfaTransactionId = clientController.mfaTransactionId,
			   let confirmCode = clientController.controlCode {
				VStack {
					DisplayItemView(title: "Transaction ID", value: "\(mfaTransactionId)")
					DisplayItemView(title: "Confirm code", value: "\(confirmCode)")
				}
				if let mfaTransactionStatus = clientController.mfaTransactionStatus {
					switch mfaTransactionStatus {
					case .unsupported(code: let code):
						DisplayItemView(title: "Status", value: "Unsupported(\(code)")
					case .pending:
						DisplayItemView(title: "Status", value: "Pending")
					case .completed(approved: .approved):
						DisplayItemView(title: "Status", value: "Completed Approved")
					case .completed(approved: .denied):
						DisplayItemView(title: "Status", value: "Completed Denied")
					case .completed(approved: .unsupported(code: let code)):
						DisplayItemView(title: "Status", value: "Completed Unsupported(\(code)")
					case .expired:
						DisplayItemView(title: "Status", value: "Expired")
					case .canceled:
						DisplayItemView(title: "Status", value: "Canceled")
					}
				}
				Button(action: {
					Task {
						await clientController.getMFATransactionStatus()
					}
				}, label:{
					Text("Load status")
				})
				Button(action: {
					Task {
						await clientController.cancelMFATransaction()
					}
				}, label:{
					Text("Cancel transaction")
						.foregroundColor(.red)
				})
			}
		}
	}
	//==================================================================================================
	//	selectedStorage
	//--------------------------------------------------------------------------------------------------
	private var selectedStorage : HyperIDStorage {
		switch selectedStorageIndex {
		case 0:
			return .email
		case 1:
			return .userID
		case 2:
			return .wallet(address: walletAddress)
		case 3:
			return .identityProvider("apple")
		default:
			fatalError()
		}
	}
	//==================================================================================================
	//	selectedStorage
	//--------------------------------------------------------------------------------------------------
	var sectionStorage : some View {
		Section(header: Text("Storage")) {
			Button(action: {
				Task {
					await clientController.getUserWallets()
					isUserWalletsViewPresented = true
				}
			}, label: {
				Text("Load user wallets")
			})
			Picker("Select storage", selection: $selectedStorageIndex) {
				ForEach(0..<4) {
					Text(storages[$0]).tag($0)
				}
			}
				.pickerStyle(SegmentedPickerStyle())
			if selectedStorageIndex == 2 {
				TextField("Wallet address", text: $walletAddress)
			}
			Button(action:{
				Task {
					await clientController.loadStorageClientKeys(storage: selectedStorage)
					isStorageKeysViewPresesnted = true
				}
			}, label: {
				Text("Load client keys")
			})
			Button(action:{
				Task {
					await clientController.loadStorageSharedKeys(storage: selectedStorage)
					isStorageKeysViewPresesnted = true
				}
			}, label: {
				Text("Load shared keys")
			})
			TextField("Key", text: $storageKey)
			TextField("Value", text: $storageValue)
			Button(action:{
				Task {
					await clientController.setStorageData(key:			storageKey,
														  value:		storageValue,
														  isPrivate:	true,
														  storage:		selectedStorage)
				}
			}, label: {
				Text("Set private")
			})
			Button(action:{
				Task {
					await clientController.setStorageData(key:			storageKey,
														  value:		storageValue,
														  isPrivate:	false,
														  storage:		selectedStorage)
				}
			}, label: {
				Text("Set public")
			})
			Button(action:{
				Task {
					await clientController.getStorageValue(key: storageKey, storage:selectedStorage)
					storageValue = clientController.lastLoadedValue ?? ""
				}
			}, label: {
				Text("Get")
			})
			Button(action:{
				Task {
					await clientController.removeStorageValue(key: storageKey, storage: selectedStorage)
				}
			}, label: {
				Text("Delete")
			})
		}
	}
}
