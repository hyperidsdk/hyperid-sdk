import SwiftUI
import SwiftData
import HyperIDSDK

//**************************************************************************************************
//	MARK: RootView
//--------------------------------------------------------------------------------------------------
struct RootView: View {
	
	@State			var rootController		: RootController!
	@StateObject	var alertState			: AlertState = AlertState()
	
	//==================================================================================================
	//	body
	//--------------------------------------------------------------------------------------------------
	var body: some View {
		NavigationSplitView {
			VStack {
				if rootController == nil {
					Text("Initialization...")
						.foregroundColor(.gray)
				} else {
					List {
						NavigationLink {
							ClientView(clientController:	rootController.clientWithSecret,
									   alertState:			alertState)
						} label: {
							VStack(alignment: .leading) {
								Text("Run client")
								Text("with ClientSecret identification")
									.foregroundColor(.gray)
									.font(.footnote)
							}
						}
						NavigationLink {
							ClientView(clientController:	rootController.clientWithHS256,
									   alertState:			alertState)
						} label: {
							VStack(alignment: .leading) {
								Text("Run client")
								Text("with JWT assertion token signed with HS256")
									.foregroundColor(.gray)
									.font(.footnote)
							}
						}
						NavigationLink {
							ClientView(clientController:	rootController.clientWithRS256,
									   alertState:			alertState)
						} label: {
							VStack(alignment: .leading) {
								Text("Run client")
								Text("with JWT assertion token signed with RS256")
									.foregroundColor(.gray)
									.font(.footnote)
							}
						}
					}
				}
			}
			.navigationTitle("Clients")
		} detail: {
			Text("Select an item")
		}
		.alert(isPresented: $alertState.isActive) {
			Alert(
				title:			Text(alertState.title!),
				message:		Text(alertState.message!),
				dismissButton:	.default(Text("OK"))
			)
		}
		.onAppear {
			Task {
				rootController = await RootController(alertState: alertState)
			}
		}
	}
}

#Preview {
	RootView()
}
