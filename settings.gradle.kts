rootProject.name = "unprotect"

include("unprotect-fancymodloader10")
include("unprotect-modlauncher")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net")

        exclusiveContent {
            forRepository {
                maven("https://maven.minecraftforge.net")
            }

            filter {
                includeGroup("cpw.mods")
            }
        }

        exclusiveContent {
            forRepository {
                maven("https://maven.neoforged.net/releases")
            }

            filter {
                includeGroupAndSubgroups("net.neoforged")
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
    }

    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
