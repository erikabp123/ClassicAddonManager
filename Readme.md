# Classic Wow Addon Manager

This is a WIP addon manager for classic wow which manages addons allowing  for easy updates from projects stored on Curseforge, Github and WowInterface. THIS IS ONLY FOR WINDOWS!

![Classic Addon Manager](https://user-images.githubusercontent.com/18148143/63517528-6d361700-c4ef-11e9-838f-cb330e925700.png)

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

![Setting Up Path](https://user-images.githubusercontent.com/18148143/63517516-6a3b2680-c4ef-11e9-92f6-3e6cca0081d3.png)

![Browsing To exe](https://user-images.githubusercontent.com/18148143/63517513-69a29000-c4ef-11e9-8e21-6b627a42ba73.png)

### Updating to newer versions

As of version 0.35 the addon manager features an auto-update feature.
Each time the manager is launched it will look for an update and inform
you if a new one is available. The auto-updater will download and install the update for you.

![Updating Automatically](https://user-images.githubusercontent.com/18148143/63517508-6909f980-c4ef-11e9-9f97-40cbfb9bb9ad.png)

![Updating Process](https://user-images.githubusercontent.com/18148143/63517512-69a29000-c4ef-11e9-9b68-ef97a0d043a0.png)


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


## Adding a new addon

Simply type in the url in the box at the bottom of the screen and press add. If the addon is a Github addon you will additionally need to specify whether the addon uses Github Releases or if you need to download a specific branch. For more on this, see the subsection on Github addons.

NOTE: An addon is not downloaded when you add it, it will automatically be downloaded the next time you press the button to update your addons.

![Add-addon](https://user-images.githubusercontent.com/18148143/63619055-f5024b00-c5ed-11e9-9073-c59830562141.png)

### Github Addons

Github addons require you to specify wheter the author wants you to download his/her "releases" or if you need to just download a specific branch. Usually the author will specify, but checking the github page and looking at the number of releases can usually tell you which to use. If the author has 0 releases than you will need to download a branch, however if the author instead has 56 releases than expect to use releases. To specify that the addon uses releases, check the little box saying 'Use releases', to use a branch, simply leave it unchecked and click add like you would any other addon. The manager will then display a pop-up showing the different branches available, and you will then need to pick the appropriate branch. To know which branch to use, you can usually find this out either through the github readme of the addon, the name of the branch (say there is a branch called 'classic' then that's pretty obvious). If you are ever in doubt, using the branch called 'master' (if there is one) is the safest bet.

![Choosing branch](https://user-images.githubusercontent.com/18148143/63619058-f764a500-c5ed-11e9-977f-ce1429ef0ead.png)


## Updating the addons

![Downloading addon updates](https://user-images.githubusercontent.com/18148143/63517510-6909f980-c4ef-11e9-9ec3-c31f54327cb9.png)

To update the addons, simply press the 'update addons' button and the manager will check each managed addon for updates and if there are any available it will download and install them.

```
    TODO: Add screenshot
```


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
