# <b>Password Manager</b>

## <b>Description</b>

Securely store and manage your passwords local on your device.</br>
Password Manager is a Standalone JavaFX Application build using Maven 3.9.9 .</br>

## <b>Getting Started</b>

### <b>On WINDOWS using Installer (recommended)</b>

1. Download the latest installer "Password Manager-x.x.x.exe"
2. Double-click it and follow the instructions

### <b>On UBUNTU/DEBIAN LINUX using Installer (recommended)</b>

1. Download the latest installer "password-manager_x.x.x-1_amd64.deb"
2. Double-click it and follow the instructions

### <b>On WINDOWS without Installer</b>

1. Download the lastest zip archive "Password Manager-x.x.x-windows.zip"
2. Unzip "Password-Manager-x.x.x-windows.zip"
3. Navigate to the "bin" directory
4. Double-click "run.bat"

### <b>On UBUNTU/DEBIAN LINUX without Installer</b>

1. Download the latest zip archive "Password Manager-x.x.x-linux.zip"
2. Unzip "Password-Manager-x.x.x-linux.zip"
3. Navigate to the "bin" directory
4. Open bash and type "./run"

## <b>Build Dependencies</b>

* Java 17
    * JavaFX 17

* Maven 3.9.7 or greater
    * Maven Plugins:</br>
        * maven-compiler-plugin</br>
        * jpackage-maven-plugin</br>
        * javafx-maven-plugin</br>

* WiX 3.0 or greater (requiered by jpackage)

* fakeroot (requiered by jpackage on ubuntu/debian linux)

## <b>How to build</b>

<b>IMPORTANT:</b> to build from source you need to install the required build dependencies on your machine.

### <b>Windows Installer</b>
````
mvn clean -Pwindows javafx:jlink jpackage:jpackage
````
Output: "Password Manager/target/dist/Password Manager-x.x.x.exe"

### <b>Ubuntu/Debian Linux Installer</b>
````
mvn clean -Plinux javafx:jlink jpackage:jpackage
````
Output: "Password Manager/target/dist/password-manager_x.x.x-1_amd64.deb"

### <b>Windows Portable Zip Archive</b>
````
mvn clean -Pwindows javafx:jlink
````
Output: "Password Manager/target/Password Manager-x.x.x-windows.zip"

### <b>Ubuntu/Debian Linux Portable Zip Archive</b>
````
mvn clean -Plinux javafx:jlink
````
Output: "Password Manager/target/Password Manager-x.x.x-linux.zip"

## <b>Help</b>

If your encounter any bugs or security issues feel free to contact me.

## <b>Authors</b>

Daniel D
[@Github](https://github.com/Daniel446f6c/)

## <b>Version History</b>

* 1.2.0
    * Added Ubuntu/Debian Linux Support
    * Added Scrollbar & Table Styling
    * Set Dark Theme as Default
<br><br>

* 1.1.0
    * Added "Open Recent" Control
    * Some Minor Cosmetic Changes
<br><br>

* 1.0.0
    * Initial Release

## <b>License</b>

This project is licensed under the GNU General Public License v3  - see the LICENSE file for details
