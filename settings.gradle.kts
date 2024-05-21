pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // TODO SETUP: configure your maven repository here by setting the appropriate values
        maven {
            url = uri("https://nexus-jx.dev.rd.flowx.ai/repository/flowx-maven-releases/")
            credentials {
                username = "your_username"
                password = "your_password"
            }
        }
    }
}

rootProject.name = "AndroidTemplateApp"
include(":app")
 