# ZielClient

Team Quartz client-side Android project. 

Navigation software designed to facilitate a channel which allows assistance to be delivered to an individual.  

![alt text](https://i.imgur.com/EajOPNo.png "Ziel Logo") 

## Getting Started

* Run `git clone https://github.com/COMP30022-18/ZielClient.git` in your terminal.
* Open repository with Android Studio.
* Navigate [here](https://console.firebase.google.com/u/2/project/zielbase/settings/general/android:com.quartz.zielclient), download the `google-services.json` file, and place it under the `app` folder
* Follow the instructions in [this link](https://developer.android.com/studio/run/) to set up your emulator.

Please note that we use Android SDK Version 28, and targeting Java 8. See [this link](https://developer.android.com/studio/write/java8-support) to see how to configure Android Studio to use Java 8.
## Using Application 
To use this application to test its features you should do the following

Ensure that you pull the latest version of this repository.
1. Build and run the application on two devices. 

2. Using two devices one device should sign up as a carer and the other as an assisted

3. The assisted should then request the carer by choosing a location and adding the carer using the phone number.

* If using an emulator you will be required to set location in emulator settings. (Melbourne University has long: 144.9612 and lat: -37.7964)

4. The carer should get a notification and open the channel with the user.

Since account creation is based on phone numbers. Feel free to use these numbers when working with emulator.
* Number: +61111222333  Verification Number: 123456
* Number: +61444555666  Verification Number: 123456





## Current Features
Numbered as per requirements documents.
* Feature 1: A user is able to register an account using their phone number
* Feature 2: Map page- Display Route to destination to the Assisted	
* Feature 3: Link permanent Carers to Assisted
* Feature 4: Request Assistance from Carer
* Feature 5: Location and Route shared from Assisted to Carer
* Feature 6: Audio and Video Call 
* Feature 7: Text Chat including data Sharing	
* Feature 8: Carer’s home page- Carer can view their assisted list
* Feature 9: User status
* Feature 10: User settings
* Feature 12: Application feedback
* Feature 13: Landmark Recognition



## Setting up Google API keys

No longer required to set up seperate API key. The application will make use of the key stored within the google-services.json 
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

To execute tests in Android studio 
Right click /app/src/test/java/com/quartz/zielclient
and select "Run tests in 'ziel client'".
These tests require Gradle build to have been completed. 

## Coding Style


The code style we obey is the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Failure to obey this will result in a pull request being denied until the code is appropriately formatted (this is simple to do with Android Studio - navigate to Preferences/Editor/Code Style/Java, and then make the required changes). To reformat code in Android studio, use the appropriate keybinding (by default, Command+Alt+L on Mac, Ctrl+Alt+L on Windows, and Ctrl+Shift+Alt+L on Linux).

The complete list of coding practices is available [here](https://docs.google.com/document/d/1RXHFtnGiAb5NsvctyE-T2N9ISuCY1cBWbTEWLzMq3gI).



## Built With


[Gradle](https://gradle.org/) - Dependency Management




## Authors

* **Alex Vosnakis** 
* **Armaan Mcleoud**  
* **Wei How Ng** 
* **Jennifer Fong** 
* **Bilal Shehata** 
