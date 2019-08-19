# Classic Wow Addon Manager

This is a WIP addon manager for classic wow which manages addons allowing  for easy updates from projects stored on Curseforge, Github and WowInterface. THIS IS ONLY FOR WINDOWS!

## Getting Started

This section will explain how to setup the program.

For a video explaining everything (quite a bit outdated now):

https://www.youtube.com/watch?v=06WbVES4ZuU

### Prerequisites

With the new standalone version, Java comes bundled with it. You therefore don't need to have anything else installed!

### Running the program

To start, simply double click the 'Classic Addon Manager.exe' file included in the .zip file.

```
    Classic Addon Manager.exe
```

Optionally you can also start the jar file manually through the command line, but this is HIGHLY discouraged.

```
    java --illegal-access=deny -jar ClassicAddonManager.jar
```

The first time you run the program it will ask for your the path to your WoW classic 'wow.exe' installation path. 
Simply navigate to the location of your wow.exe file. e.g.
```
    C:\Program Files (x86)\World of Warcraft\_classic_
```

### Updating to newer versions

As of version 0.35 the addon manager features an auto-update feature.
Each time the manager is launched it will look for an update and inform
you if a new one is available. The auto-updater will download and install the update for you.


You can also choose to update manually (less recommended)

To update manually:
 
a.) extract the .zip file of the newest release and overwrite the contents or
  
b.) Download the ClassicAddonManager.jar file and past it into the
system folder, overwriting when prompted.

Option B is a substantially smaller download, and can be recommended over option a for manual updates.

IMPORTANT:

If you choose to update manually, make sure you 'data' folder is present. 
This folder contains all the manager's information about your managed addons.

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
