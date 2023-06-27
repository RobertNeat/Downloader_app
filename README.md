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

# How to compile and run
To run application:

1. Download zip package
2. Extract package and open using Android Studio
3. If there is error with versions (pre Electric Eel) you should change version of the IDE in one of the gradle files and rebuild
4. Build application and run (either on VM Android or physical device, the development device is Samsung A53)

# Screenshots:
1. Downloading image from URL:

<table>
    <tr>
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/1b_file2_url.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/2b_file2_first_launch.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/3b_file2_info_download.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/4b_file2_write_storage_permit.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/5b_file2_write_oermit_accepted.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/6b_file2_notification_permit.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/7b_file2_downloaded.png" width="200"/>
        </td>  
        <td>    
            <img src="https://github.com/RobertNeat/Downloader_app/blob/main/res_images/image_download/8b_file2_in_storage.png" width="200"/>
        </td>   
    </tr>
</table>

2. Downloading larger file from specific URL (the notification creation, return to app through notification):


