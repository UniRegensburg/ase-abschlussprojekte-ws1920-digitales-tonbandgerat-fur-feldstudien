# Setup 

This project/repository uses Github Actions as a CI tool.
The default action gets triggered by a push into the master branch.
This action calls the defined unit tests, static code analysis, and, given the successful passing of these tests, generates an APK, which represents the current implementation of the app, for download.
Given the need, the action can also create a release of the software.

The app can be used in its entirety with a physical Android device (Android 7.0 Nougat +) with a working microphone (internal or external).
The app uses the following permissions:

- android.permission.RECORD_AUDIO

- android.permission.WAKE_LOCK

- android.permission.WRITE_EXTERNAL_STORAGE

- android.permission.READ_EXTERNAL_STORAGE

# Dependencies

- Kotlin 1.3.61

- Material Design Components

- Room for database access

- JUnit for unit tests

- jraska/livedata-testing for unit tests of the database with LiveData return values

- Ktlint for static code analysis

- arthenica/mobile-ffmpeg-full for FFMPEG-functionality to convert and edit audio files

- adrielcafe/AndroidAudioConverter for conversion of audio files

- apptik.widget/multislider for the display of seekbars with multiple thumbs
