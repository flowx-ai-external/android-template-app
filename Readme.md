## Project Setup Guide

This document outlines the necessary steps to configure the project to connect to your FLOWX.AI environment.

### Prerequisites

Ensure you have the following:
- Maven repository credentials (username and password)
- Environment-specific URLs (Base URL, Image Base URL, Engine Path, Authentication URL)
- Keycloak Client ID
- The UUID of the theme to be applied (optional)
- Project and Process identifiers for starting or continuing a process

### Configuration Steps

To configure the application, search the codebase for comments containing `// TODO SETUP`. These comments will guide you to the specific files and lines requiring modification.
The key configuration areas are detailed below:

1. **Maven Repository Access**

   To enable Gradle to download the FLOWX.AI SDK, you must configure your Maven repository credentials.

   - **File**: `settings.gradle.kts` (in the root project directory)
   - **Action**: Locate the `maven` block for the FLOWX.AI repository and insert your provided `username` and `password`.

    ```kotlin title="settings.gradle.kts"
    // TODO SETUP: configure your maven repository here by setting the appropriate values
    maven {
        url = uri("https://nexus-jx.dev.rd.flowx.ai/repository/flowx-maven-releases/")
        credentials {
            username = "your_username"
            password = "your_password"
        }
    }
    ```

2. **FLOWX.AI SDK Version**

    Specify the version of the FLOWX.AI Android SDK.

    - **File**: `app/build.gradle.kts`
    - **Action**: Find the line starting with `implementation("ai.flowx.android:android-sdk:` and update the version number if necessary.

    ```kotlin title="app/build.gradle.kts"
    // TODO SETUP: configure your integration by setting the appropriate version of the SDK
    implementation("ai.flowx.android:sdk:9.0.0")
    ```

3. **Environment configuration**

   Define the connection parameters for your specific FLOWX.AI environment.

   - **File**: `app/src/main/kotlin/ai/flowx/external/android/template/app/Env.kt`
   - **Action**: Update the constant values within the `Env` object as per the comments. Pay close attention to URL formatting requirements (e.g., presence or absence of trailing slashes or suffix path segments).

    ```kotlin title="app/src/main/kotlin/ai/flowx/external/android/template/app/Env.kt"
    // TODO SETUP: configure your environment here by setting the appropriate values
    object Env {
        // Platform data:
        const val baseUrl = "your_base_url" // must NOT end with a slash ('/')
        const val imageBaseUrl = "your_image_base_url" // must NOT end with a slash ('/')
        const val enginePath = "your_engine_path" // must NOT end with a slash ('/')
    
        // Keycloak data:
        const val authBaseUrl = "your_authentication_url" // must end with a slash ('/'); should contain the `/protocol/openid-connect/` suffix as path's last segments
        const val clientId = "your_client_id"
    }
    ```

4. **Theme configuration**

   Specify the workspace and the theme to be applied during process rendering.

   - **File**: `app/src/main/kotlin/ai/flowx/external/android/template/app/screens/StartProcessScreen.kt`
   - **Action**: Locate the `Flowx.getInstance().setupTheme` method call.

       - To use a remote theme, set the `themeUuid` parameter to the corresponding UUID string
       - If `themeUuid` is left empty, the SDK will attempt to load a theme from the local `assets` folder specified by `fallbackThemeJsonFileAssetsPath`. Ensure this file (e.g., `theme/some_theme.json`) exists in `app/src/main/assets/` if you intend to use a fallback.

    ```kotlin title="app/src/main/kotlin/ai/flowx/external/android/template/app/screens/StartProcessScreen.kt"
    // TODO SETUP: configure your theme here by adding the `workspaceUuid`,  the `themeUuid` and/or `fallbackThemeJsonFileAssetsPath`
    Flowx.getInstance().setupTheme(
        workspaceUuid = "your_workspace_id",
        themeUuid = "your_theme_id", // when empty, no theme will be downloaded and will fallback to the theme specified in the `fallbackThemeJsonFileAssetsPath` parameter, if any
        fallbackThemeJsonFileAssetsPath = "theme/some_theme.json", // when null, no fallback will be used
    ) {
        // ...
    }
    ```

5. **Process definition**

   Configure the default workspace, project and process identifiers for starting or continuing a process.

   - **File**: `app/src/main/kotlin/ai/flowx/external/android/template/app/MainViewModel.kt`
   - **Action**: Modify the constants within the companion object.
   
       - To start a new process: Provide valid values for `START_PROCESS_PROJECT_ID` and `START_PROCESS_NAME`
       - To continue an existing process: Provide the UUID of an active process instance for `CONTINUE_PROCESS_UUID`

    ```kotlin title="app/src/main/kotlin/ai/flowx/external/android/template/app/MainViewModel.kt"
    // TODO SETUP: configure your process here by setting the appropriate values
    companion object {
        const val START_PROCESS_WORKSPACE_ID = "your_workspace_id"
        const val START_PROCESS_PROJECT_ID = "your_project_id"
        const val START_PROCESS_NAME = "your_process_name"
        const val CONTINUE_PROCESS_UUID = "your_process_uuid"
    }
    ```
