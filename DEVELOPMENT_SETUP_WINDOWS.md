# Development Environment Setup Guide (Windows & Cloud)

This guide provides step-by-step instructions for setting up the development and build environment for this Android app. It covers two pathways:
1. **Option A: Online / Cloud-Based Setup (Zero or Minimal Local Effort)**
2. **Option B: Full Local Setup on Windows 10/11 (Desktop PC or Laptop)**

---

## Technical Specifications at a Glance
- **Platform:** Android (Min SDK: 24, Target SDK: 36)
- **Language & Framework:** Kotlin 2.2+, Jetpack Compose (Material 3)
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`, Android Gradle Plugin 9.x)
- **Java Compatibility:** JDK 17 or JDK 21 (Java 11+ bytecode target)
- **Hardware Integrations:** Dual-SIM Telephony, SMS/MMS Providers, Room SQLite Database (via KSP)

---

## Option A: Online / Cloud Setup (Zero / Low Effort)

If you prefer to start coding and building immediately without downloading gigabytes of Android SDKs or configuring Windows paths, use cloud-based environments:

### 1. Google AI Studio Build (Current Cloud Environment)
- **Effort Level:** Zero configuration required.
- **Workflow:**
  1. Access the project directly in your browser.
  2. Code edits compile incrementally in the cloud container.
  3. Real-time preview is streamed directly via the in-browser Android streaming emulator.
  4. Generate and download pre-built APKs/AABs directly from the settings/export menu without running any local build tools.

### 2. GitHub Codespaces / Cloud Dev Containers
- **Effort Level:** Very low (runs in browser or connected to VS Code on Windows).
- **Workflow:**
  1. Push or import the project repository to GitHub.
  2. Click **Code > Codespaces > Create codespace on main**.
  3. A pre-configured cloud Linux container launches in your browser with Java and Gradle pre-installed.
  4. Build the debug APK with one command in the terminal:
     ```bash
     gradle assembleDebug
     ```

### 3. Automated Cloud Builds via GitHub Actions (Zero Local SDK)
- **Effort Level:** Set up once in 3 minutes; zero local tools needed on your Windows PC.
- Add a file `.github/workflows/build.yml` in your repo:
  ```yaml
  name: Android Build
  on: [push, pull_request]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - name: Set up JDK 17
          uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
        - name: Build Debug APK
          run: ./gradlew assembleDebug
        - name: Upload APK Artifact
          uses: actions/upload-artifact@v4
          with:
            name: app-debug
            path: app/build/outputs/apk/debug/app-debug.apk
  ```
- Any commit automatically produces a downloadable APK under the GitHub Actions tab.

---

## Option B: Full Local Environment on Windows 10 / 11

Choose this option if you want full offline access, offline emulator testing, or direct USB cable debugging on a physical dual-SIM Android phone.

### System Requirements (Windows)
- **Operating System:** Windows 10 (64-bit) or Windows 11
- **CPU:** 64-bit Intel or AMD processor with hardware virtualization (VT-x / AMD-V)
- **RAM:** Minimum 8 GB (16 GB strongly recommended for smooth emulator performance)
- **Free Disk Space:** At least 15–20 GB SSD space (for Android Studio, Android SDKs, and emulators)

---

### Step 1: Install Android Studio
Android Studio is the official IDE and includes the necessary SDK components, Gradle tools, and emulator engines:
1. Download **Android Studio** (Latest stable release: Ladybug or newer) from [developer.android.com/studio](https://developer.android.com/studio).
2. Run the Windows installer (`.exe`).
3. In the setup wizard, ensure the following components are selected:
   - **Android Studio**
   - **Android SDK**
   - **Android Virtual Device (AVD)**
4. Finish installation and open Android Studio.

---

### Step 2: Configure SDK & Tools in Android Studio
1. In Android Studio, go to **Settings** (`Ctrl + Alt + S`) > **Appearance & Behavior** > **System Settings** > **Android SDK**.
2. Under **SDK Platforms**, check:
   - **Android 14 (API 34)** or **Android 15 / 16 (API 35/36)**
3. Under **SDK Tools**, ensure the following are checked:
   - **Android SDK Build-Tools**
   - **Android SDK Command-line Tools (latest)**
   - **Android Emulator**
   - **Android SDK Platform-Tools**
4. Click **Apply** to let Android Studio download and install them.

---

### Step 3: JDK (Java Development Kit) Configuration
Android Studio bundles its own certified OpenJDK (JBR - JetBrains Runtime):
- In Android Studio: **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**.
- Under **Gradle JDK**, select **Embedded JDK** (or JDK 17/21).

*(Optional for Windows Command Prompt/PowerShell builds outside Android Studio)*:
- If you wish to run `gradlew` commands from PowerShell, download **Eclipse Temurin JDK 17** (`.msi` installer) and set `JAVA_HOME`:
  - Open Windows Search > type **Edit the system environment variables**.
  - Click **Environment Variables**.
  - Under System Variables, click **New**:
    - Variable name: `JAVA_HOME`
    - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17...`
  - Add `%JAVA_HOME%\bin` to your `Path` variable.

---

### Step 4: Clone / Extract the Project on Windows
> **Windows Path Length Tip:** Windows historically imposes a 260-character path limit (`MAX_PATH`). Extract or clone the project into a short path (e.g., `C:\Projects\SMSQuery` or `D:\dev\SMSQuery`), avoiding deeply nested folders like `C:\Users\...\AppData\...`.

1. Open Android Studio.
2. Select **Open** and navigate to the project directory containing `settings.gradle.kts`.
3. Android Studio will automatically start Gradle sync and download necessary dependencies (Compose, Room, KSP).
4. Once indexing completes, verify the green hammer icon (**Make Project** / `Ctrl + F9`) builds successfully.

---

### Step 5: Running & Testing the App on Windows

#### Option 1: On an Android Emulator (Virtual Device)
1. In Android Studio, open **Device Manager** (toolbar or right sidebar).
2. Click **Create Device** > select a phone profile (e.g., Pixel 8).
3. Select a system image (e.g., VanillaIceCream API 35 or UpsideDownCake API 34).
4. *Windows Virtualization Check:*
   - If prompted, ensure **Windows Hypervisor Platform** is enabled in Windows:
     - Search **Turn Windows features on or off** > check **Windows Hypervisor Platform** and **Virtual Machine Platform** > restart PC.
5. Launch the virtual device and click the green **Run** button (`Shift + F10`) in Android Studio.

#### Option 2: On a Physical Dual-SIM Android Phone (Recommended for Real SMS & SIM Testing)
Since this app features dual-SIM detection and system SMS handler capabilities, testing on a physical dual-SIM phone is ideal:
1. On your Android phone, enable **Developer Options**:
   - Go to **Settings > About Phone** > tap **Build Number** 7 times.
2. Under **Settings > System > Developer Options**, enable:
   - **USB Debugging**
   - (On Xiaomi/MIUI/HyperOS devices, also enable **USB Debugging (Security settings)** to allow permission grants).
3. Connect the phone to your Windows PC using a USB cable.
4. When prompted on the phone screen, select **Always allow from this computer**.
5. Select your physical phone in Android Studio's device dropdown and click **Run**.

---

## Comparison Summary

| Metric | Option A: Cloud / Online | Option B: Local Windows Setup |
| :--- | :--- | :--- |
| **Setup Time** | Under 2 minutes | 20–45 minutes |
| **Download Size** | 0 GB on your PC | ~10–15 GB (IDE, SDKs, Emulator) |
| **PC Performance Required** | Any basic laptop or PC | 8–16 GB RAM, Quad-core CPU, SSD |
| **Physical Dual-SIM Testing** | Not supported (virtual only) | **Full support** via USB debugging |
| **Offline Capability** | Requires internet connection | Works completely offline |
| **Best For** | Quick prototyping, investor demos, APK downloads | Daily feature development, hardware debugging |
