# Setup 

Dieses Repository nutzt Github Actions als CI-Tool.
Eine Action wird ausgelöst, wenn ein Push in dem Master-Branch erfolgt.
Diese Action startet Unitests, statische Codeanalyse, und erstellt im Fall erfolgreicher Tests eine APK, die den aktuellen Stand der App darstellt und heruntergleaden werden kann.
Bei Bedarf kann über die Action auch ein release erstellt werden.

Zur vollständigen Nutzung der App ist ein physisches Androidgerät mit Android 7.0 Nougat und funktionierendem Mikrofon nötig.
Die App benötigt folgende Permissions:

- android.permission.RECORD_AUDIO

- android.permission.WAKE_LOCK

- android.permission.WRITE_EXTERNAL_STORAGE

- android.permission.READ_EXTERNAL_STORAGE

# Abhängigkeiten

- Kotlin 1.3.61

- Material Design Components

- Room für Datenbankzugriff

- JUnit für Unittests

- jraska/livedata-testing für Unittests der Datenbank mit LiveData Rückgabewerten

- Ktlint für statische Codeanalyse
