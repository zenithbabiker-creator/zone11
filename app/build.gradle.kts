dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    implementation("com.google.ar:core:1.45.0")

    // الحزم الفعلية الوحيدة الموجودة والمتحقق منها في مستودع هواوي
    implementation("com.huawei.hmf:tasks:1.5.2.206")[cite: 1]
    implementation("com.huawei.hms:base:6.11.0.300")[cite: 1]
    implementation("com.huawei.hms:arenginesdk:4.0.0.5")
    implementation("com.huawei.hms:hatool:6.11.0.300")
    implementation("com.huawei.agconnect:agconnect-core:1.9.0.300")

    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
