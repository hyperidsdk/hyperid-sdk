plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.plugin.serialization")
}

android {
	namespace = "ai.hyper_id.sdk_auth"
	compileSdk = 34

	defaultConfig {
		minSdk = 24

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

dependencies()
{
	implementation("com.auth0:java-jwt:4.4.0")

	implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
	implementation("com.squareup.okhttp3:okhttp")
	implementation("com.squareup.okhttp3:logging-interceptor")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
