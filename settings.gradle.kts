pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // أضف هذا السطر ليتيح لـ Gradle الوصول لسيرفرات هواوي التي اختبرتها للتو:
        maven { url = java.net.URI("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "zone11"
include(":app")
