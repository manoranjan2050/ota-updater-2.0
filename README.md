![OTAUpdater Header](http://sensation-devs.org/banner/bannerotaupdate.png)



OTA Update Center
==========

OTA Update Center is an update application for ROMs and Kernels supported by OTA Update Center.

ROM/Kernel devs
==========

Do you want to use this software??

Go to: [https://otaupdatecenter.pro](https://otaupdatecenter.pro) to register an account, and add/update your ROM/Kernel.
      
Go to the [Download](https://github.com/OTAUpdateCenter/ota-updater-2.0/downloads) section, download the latest apk and include it with your ROM/Kernel.

Add the `/system/rom.ota.prop` and/or `/system/kernel.ota.prop` containing:

    {"otaid":"<otaid>","otaver":"<otaver>","otatime":"<otatime>"}
    
Replace:

  `<otaid>` with the OTA ID you used when adding your ROM/Kernel on our website
  
  `<otaver>` with the OTA Version (user-friendly)
  
  `<otatime>` with the OTA date/time (yyyymmdd-hhmm format) 

If your device has some quirky sdcard (not /sdcard) naming in the OS or recovery, add these lines to your build.prop:

    otaupdater.sdcard.os=<sdcard name (e.g. sdcard2 for /sdcard2) in the main system here>
    otaupdater.sdcard.recovery=<sdcard name (e.g. sdcard2 for /sdcard2) in recovery here>

Known Bugs
==========

Samsung Galaxy series, HTC Rezound devices do not work properly yet.


How to Build
==========
    
    git clone git@github.com:OTAUpdateCenter/ota-updater-2.0.git
      
Add to Eclipse, make your changes and export as an Android application! :D



