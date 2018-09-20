# ZielClient

Team Quartz client-side Android project. 

Navigation software designed to facilitate a channel which allows assistance to be delivered to an individual.  

## Getting Started

* Run `git clone https://github.com/COMP30022-18/ZielClient.git` in your terminal.
* Open repository with Android Studio.
* Navigate [here](https://console.firebase.google.com/u/2/project/zielbase/settings/general/android:com.quartz.zielclient), download the `google-services.json` file, and place it under the `app` folder
* Follow the instructions in [this link](https://developer.android.com/studio/run/) to set up your emulator.

Please note that we use Android SDK Version 28, and targeting Java 8. See [this link](https://developer.android.com/studio/write/java8-support) to see how to configure Android Studio to use Java 8.

## Setting up Google API keys

* Follow steps at [Get API Key](https://developers.google.com/maps/documentation/android-sdk/signup).
* Once you have an API key created, Enable the following from Library:
  * Maps SDK API
  * Places API 
  * Places SDK for Android 
  * Directions API
  * Street View API
* Create a file called `secrets.xml` in `ZielClient/app/src/main/res/values` with the the following code:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_api_key">YOUR_API_KEY_HERE</string>
</resources>
```
* Replace `YOUR_API_KEY_HERE` with your actual API key.
* You **must** call it `secrets.xml` since its ignored in `.gitignore`.
* Using an untracked local file to store the API key prevents it being exposed in remotely.

## Git workflow

Our workflow to follow is [GitFlow](https://nvie.com/posts/a-successful-git-branching-model/). Each milestone (release candidate) will have its own `develop` branch, from which all work for that release will be branched off.

<p align='center'> <img src=https://c2.staticflickr.com/6/5293/5488984404_4f693eec32.jpg> </p>
<p align='center' fontSize='5px'>  “git-flow” by Bo-Yi Wo at https://www.flickr.com/photos/appleboy/5488984404
 under a Creative Commons Attribution 2.0. Full terms at http://creativecommons.org/licenses/by/2.0.
</p> 


When completing a pull request, should it be successful, the option "Squash commits and merge" **must be chosen**. If this is not done, all commits **will be rolled back on the `develop` branch**.

Additionally, should the pull request be merged by anyone other than the assignee, the pull request **will be rolled back**, and the pull request re-opened with the original assignee.

## Testing

Running tests can be accomplished using the Android Studio test configuration - see [this link](https://developer.android.com/studio/test/) for details.

Unit tests and UI tests should be completed for all major pieces of functionality.

## Coding Style


The code style we obey is the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Failure to obey this will result in a pull request being denied until the code is appropriately formatted (this is simple to do with Android Studio - navigate to Preferences/Editor/Code Style/Java, and then make the required changes). To reformat code in Android studio, use the appropriate keybinding (by default, Command+Alt+L on Mac, Ctrl+Alt+L on Windows, and Ctrl+Shift+Alt+L on Linux).

The complete list of coding practices is available [here](https://docs.google.com/document/d/1RXHFtnGiAb5NsvctyE-T2N9ISuCY1cBWbTEWLzMq3gI).



## Built With

* [Gradle](https://gradle.org/) - Dependency Management



## Authors

* **Alex Vosnakis** 
* **Armaan Mcleoud**  
* **Wei How Ng** 
* **Jennifer Fong** 
* **Bilal Shehata** 
