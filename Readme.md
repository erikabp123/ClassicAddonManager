# Classic Addon Manager _(now also retail)_

NEW UI UPDATE COMING SOON!

This is a WIP addon manager, that despite the name **NOW SUPPORTS RETAIL, PTR** (classic & retail), **AND CLASSIC**. It manages your addons allowing  for easy updates from projects stored on Curseforge, Github, Tukui, and WowInterface. THIS IS ONLY FOR WINDOWS!

![Classic Addon Manager](https://user-images.githubusercontent.com/18148143/96378250-f811b080-118a-11eb-9cfb-481753cf48ee.png)

## Getting Started

This section will explain how to setup the program.

For a video explaining everything (quite a bit outdated now):

https://www.youtube.com/watch?v=06WbVES4ZuU

### Prerequisites

With the new standalone version, Java comes bundled with it. You therefore don't need to have anything else installed!

### Running The Program

To start, simply double click the 'Classic Addon Manager.exe' file included in the .zip file.

```
    Classic Addon Manager.exe
```

The first time you run the program it will attempt to scan for your wow installations. If your games are installed on another drive, or if there is some other problem with scanning. You will need to supply the path manually.


![Setting Up Path](https://user-images.githubusercontent.com/18148143/96378044-d82dbd00-1189-11eb-882b-73026e847d00.png)

## Updating The Manager

As of version 0.35 the addon manager features an auto-update feature.
Each time the manager is launched it will look for an update and inform
you if a new one is available. The auto-updater will download and install the update for you.

![Updating Automatically](https://user-images.githubusercontent.com/18148143/63517508-6909f980-c4ef-11e9-9f97-40cbfb9bb9ad.png)

![Updating Process](https://user-images.githubusercontent.com/18148143/63517512-69a29000-c4ef-11e9-9b68-ef97a0d043a0.png)


You can also choose to update manually (less recommended):  

**Option A:**
 
 Extract the .zip file of the newest release and overwrite the contents or
  
**Option B:**

Download the '*ClassicAddonManager.jar*', '*CHANGELOG.txt*', and '*VERSIONING*' files and past them into the ***system*** folder, overwriting when prompted.


Option B is a substantially smaller download, and can be recommended over *option a* for manual updates.

**IMPORTANT:**

If you choose to update manually, make sure you 'data' folder is present. This folder contains all the manager's information about your managed addons.

## Changing WoW Installation Folder

To change installation folder, simply press the 'File' menu at the top of the screen and select 'setup'. Please make sure to update your installation folder in the addon manager as is necessary. Failing to do so will cause issues.

```
    File -> Setup
```

## Adding A New Addon

**If your addon is from *Curseforge*, *Tukui*, or *WowInterface*:**

Select the tab at the bottom of your screen that says '*Search (Curse/Tukui/WowInterface)*'.
Then select, from the dropdown to the left of the search field, which of the websites to search, and start typing in your search term.
Hitting enter will then search for the specified term and you can then select which addon you wish to download from the list.

**If your addon is from *Github*:**

Select instead the '*Manual (Github)*' tab.
Simply type in the url in the box and press add. 
You will additionally need to specify whether the addon uses Github Releases or if you need to download a specific branch.
For more on this, see the subsection on Github addons.

***NOTE:* An addon is not automatically downloaded when you add it, it will instead be downloaded automatically along with and addons updates the next time you press the button to update your addons.**

![Add-addon](https://user-images.githubusercontent.com/18148143/63619055-f5024b00-c5ed-11e9-9073-c59830562141.png)

### Github Addons

Github addons require you to specify whether the author wants you to download "*releases*" or if you need to just download a specific branch.
Usually the author will specify which of the two they want you to use, but checking the github page by looking at the number of releases can usually tell you which to use if nothing is explicitly stated.
**If the author has 0 releases than you will need to download a branch**, however if the author instead has 56 releases than expect to use releases.

**Releases:**
To specify that the addon uses releases, check the little box saying '*Use releases*'.

**Branches:**
To use a branch, simply leave it unchecked and click add like you would any other addon. 
The manager will then display a pop-up showing the different branches available, and you will then need to pick the appropriate branch.
To know which branch to use, you can usually find this out either through the github readme of the addon, the name of the branch (say there is a branch called '*classic*' then that's pretty obvious).
If you are ever in doubt, using the branch called '*master*' (if there is one) is the safest bet.

![Choosing branch](https://user-images.githubusercontent.com/18148143/63619058-f764a500-c5ed-11e9-977f-ce1429ef0ead.png)


## Updating Your Addons

![Downloading addon updates](https://user-images.githubusercontent.com/18148143/63517510-6909f980-c4ef-11e9-9ec3-c31f54327cb9.png)

To update the addons, simply press the 'update addons' button and the manager will check each managed addon for updates and if there are any available it will download and install them.


## How To Support

If you like the program consider telling your friends about it!


If you feel that still isn't enough...

I have created a patreon where people who wish to make a donation may do so. This is NEVER expected, but I definetly appreciate it. [Patreon](https://www.patreon.com/ClassicAddonManager)

## Built With

* [HTMLUnit](http://htmlunit.sourceforge.net/) - Webscraping
* [GSON](https://github.com/google/gson) - JSON Saving
* [Zip4j](https://github.com/srikanth-lingala/zip4j) - Zip File Handling
* [JavaFX](https://openjfx.io/) - GUI
* [Gradle](https://gradle.org/) - Dependency Management
