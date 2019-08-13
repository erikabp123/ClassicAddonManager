# Classic Wow Addon Manager

This is a WIP addon manager for classic wow which manages addons allowing  for easy updates from projects stored on Curseforge and Github.

Currently it only support Curseforge but in the next update Github support will be added!

## Getting Started

This section will explain how to setup the program.

### Prerequisites

With the new standalone version, Java comes bundled with it. You therefore don't need to have anything else installed!

### Running the program

To start, simply double click the 'Classic Addon Manager.exe' file included in the .zip file.

```
    Classic Addon Manager.exe
```

Optionally you can also start the jar file manually through the command line, but this is not recommended.

```
    java --illegal-access=deny -jar ClassicAddonManager.jar
```

The first time you run the program it will ask for your wow classic installation path. 
Simply paste that in the location of your wow.exe file. e.g.
```
    C:\Program Files (x86)\World of Warcraft\_classic_
```

### Updating to newer versions

To update to a newer version either
 
a.) extract the .zip file and overwrite the contents or
  
b.) Download the ClassicAddonManager-x.x.jar file, renamed it to simply ClassicAddonManager.jar and past it into the
system folder, overwrite when prompted.

Option B is a substantially smaller download, and can be recommended over option a.

### Changing WoW installation folder

To change installation folder, simply press the 'File' menu at the top of the screen and select 'setup'.

Please make sure to update your installation folder in the addon as is necessary. Failing to do so will cause issues.


## How it works (technical)

```
    TODO
```

## Built With

* [HTMLUnit](http://htmlunit.sourceforge.net/) - Webscraping
* [GSON](https://github.com/google/gson) - JSON Saving
* [Zip4j](https://github.com/srikanth-lingala/zip4j) - Zip File Handling
* [JavaFX](https://openjfx.io/) - GUI
* [Gradle](https://gradle.org/) - Dependency Management
