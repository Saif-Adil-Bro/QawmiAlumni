pluginManagement {
    repositories {
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
        google()
        mavenCentral()
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

rootProject.name = "QawmiAlumni"
include(":app")
