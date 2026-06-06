import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.panda.study1.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            // Đây sẽ là tên app hiển thị cho người dùng (Ví dụ: XAPK Installer)
            packageName = "XAPK Installer"
            packageVersion = "1.0.0"

            // Chỗ này mới là nơi khai báo package name thực sự cho HĐH quản lý
            macOS {
                bundleID = "com.panda.study1"
            }
            windows {
                menuGroup = "Panda Tools" // Tạo thư mục trong Start Menu của Win
            }
        }
    }
}