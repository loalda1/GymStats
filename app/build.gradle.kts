plugins { alias(libs.plugins.android.application) }
val configFile = file("google-services.json")
if (configFile.exists()) apply(plugin = "com.google.gms.google-services")
val appId = providers.gradleProperty("gymstats.applicationId").getOrElse("org.gymstats.android")
val googleClientId = if (configFile.exists()) {
    val config = groovy.json.JsonSlurper().parse(configFile) as Map<*, *>
    val clients = config["client"] as? List<*> ?: emptyList<Any>()
    val client = clients.filterIsInstance<Map<*, *>>().firstOrNull {
        val info = it["client_info"] as? Map<*, *>
        val android = info?.get("android_client_info") as? Map<*, *>
        android?.get("package_name") == appId
    }
    val oauth = client?.get("oauth_client") as? List<*> ?: emptyList<Any>()
    oauth.filterIsInstance<Map<*, *>>().firstOrNull { (it["client_type"] as? Number)?.toInt() == 3 }?.get("client_id") as? String ?: ""
} else ""
android {
    namespace = "org.gymstats.android"
    compileSdk = 36
    defaultConfig {
        applicationId = appId
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        resValue("string", "google_web_client_id", googleClientId)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        resValues = true
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    testOptions { unitTests.isReturnDefaultValues = true }
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.recyclerview)
    implementation(libs.coroutines.play.services)
    implementation(libs.work.runtime.ktx)
    implementation(libs.credential.manager)
    implementation(libs.credential.google.provider)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
}
