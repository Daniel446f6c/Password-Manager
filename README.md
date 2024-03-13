# <b>Password Manager</b>

## <b>Description</b>

Securely store and manage your passwords local on your device.</br>
Password Manager is a Standalone JavaFX Application build using Maven 3.8.1 .</br>

## <b>Getting Started</b>

### <b>On WINDOWS using Installer (recommended)</b>

1. Download the latest installer "Password Manager-x.x.x.exe"
2. Double-click it and follow the instructions

### <b>On WINDOWS without Installer</b>

1. Download the lastest zip archive "Password Manager-x.x.x-windows.zip"
2. Unzip "Password-Manager-x.x.x-windows.zip"
3. Navigate to the "bin" directory
4. Double-click "run.bat"

### <b>On LINUX without Installer</b>

1. Download the latest zip archive "Password Manager-x.x.x-linux.zip"
2. Unzip "Password-Manager-x.x.x-linux.zip"
3. Navigate to the "bin" directory
4. Open bash and type "./run"

## <b>Build Dependencies</b>

* Java 17

* JavaFX 17

* Maven Plugins:</br>
    * maven-compiler-plugin</br>
    * jpackage-maven-plugin</br>
    * javafx-maven-plugin</br>
    </br>
* WiX 3.0 or greater (requiered by jpackage)

## <b>How to build</b>

<b>IMPORTANT:</b> to build from source you need to install Java 17 and WiX 3 on your machine, otherwise building will fail.

### <b>Windows Installer</b>
````
mvn clean -Pwindows javafx:jlink jpackage:jpackage
````
Output: "Password Manager/target/dist/Password Manager-x.x.x.exe"

### <b>Windows Portable Zip Archive</b>
````
mvn clean -Pwindows javafx:jlink
````
Output: "Password Manager/target/Password Manager-x.x.x-windows.exe"

### <b>Linux Portable Zip Archive</b>
````
mvn clean -Plinux javafx:jlink
````
Output: "Password Manager/target/Password Manager-x.x.x-linux.exe"

## <b>Help</b>

If your encounter any bugs or security issues feel free to contact me.

## <b>Authors</b>

Daniel D
[@Github](https://github.com/Daniel446f6c/)

## <b>Version History</b>

* 1.1.0
    * Added "Open Recent" Control
    * Some Minor Cosmetic Changes
<br><br>

* 1.0.0
    * Initial Release

## <b>License</b>

This project is licensed under the GNU General Public License v3  - see the LICENSE file for details
