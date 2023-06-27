# Downloader_app
Android app that downloads file from specific URL provided by user (the default is provided for linux kernel). The user can download information about file from targeted URL using class ascendant from Async Task whom downloads information about file type alongside file size. There is also possibility to download file .. then application asks for specific permissions and starts downloading file in chunks and writes them in device memory using running foreground service in second thread (alongside there is also reated notification that runs event after the app is closed during run and downloads the file till end), the user can also restore application back to run after tapping in notification (the data restored will be file size from saved state in notification).

Application besides that features:
- Async Task runs for short-term tasks,
- web communication using HTTPS using HttpsUrlConnection class for communication,
- granting permission for application - (internet permission, foreground service permission, notification service permission, write externam storage permission),
- Parcerable interface - for restoring application state from notification,
- broadcast receiver - for updating main thread in application so it will show how many bytes were downloaded in foregrounf thread.   

Development environment:

- Java SE Runtime Environment (build 1.8.0_371-b11)
- Android Studio Electric Eel | 2022.1.1
