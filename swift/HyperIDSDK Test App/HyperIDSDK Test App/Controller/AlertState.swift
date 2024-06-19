import Foundation

class AlertState : ObservableObject {
	@Published var isActive	: Bool  = false
	@Published var title	: String?
	@Published var message	: String?
}
