adb root & adb remount
adb shell rm /system/priv-app/remote.apk
adb push remote.apk /system/priv-app
adb reboot