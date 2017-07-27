[Android_FindKids](#android_findkids)  
　1. [Requirement](#requirement)  
　2. [Usage](#usage)  
　3. [Disclaimer](#disclaimer)  
  
# Android_FindKids  

The Andoird FindKids APP which based on amap APIs.  
The FindKids can help to locate the place where the kid is.  
When user enable Data usage/WIFI or GPS, it will work to get the GPS info.  
And save them into /sdcard/Findkids/info.txt & SMS to the phone number you set & FTP server you set.  

## Requirement
You should get the key from [AMAP](https://lbs.amap.com/) with the package name.  
You should add the phone number in Receiver.java, the number which will receiver the GPS info.
You can also add the FTP info like the hostname/username/password(optional).

## Usage
Since the app have no UI, so you should enable the permissions in the app@settings.  
Enable the Data usage/WIFI or enable the GPS function.  
The app will locate the current place and send info via SMS or to the FTP server.  

## Disclaimer  
The app only for learning usage.  
Please do not use it for illegal purposes. The author is not responsible for the consequences.