// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

//**************************************************************************************************
//	MARK: Package
//--------------------------------------------------------------------------------------------------
let package = Package(
	name: "HyperIDSDK",
	platforms: [
		.macOS(.v12), .iOS(.v15), .tvOS(.v15)
	],
	products: [
		// Products define the executables and libraries a package produces, making them visible to other packages.
		.library(
			name: "HyperIDSDK",
			targets: ["HyperIDSDK"]),
	],
	dependencies: [
		//TODO: after publishing replace previous dependency with url to the package
		.package(name: "HyperIDAuth", path: "../HyperIDAuth"),
	],
	targets: [
		// Targets are the basic building blocks of a package, defining a module or a test suite.
		// Targets can depend on other targets in this package and products from dependencies.
		.target(
			name: "HyperIDSDK", dependencies: [
				"HyperIDAuth",
			]),
		.testTarget(
			name: "HyperIDSDKTests",
			dependencies: ["HyperIDSDK"]),
	]
)
