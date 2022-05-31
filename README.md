# StudentPal 	

Social Platform with an integrating scheduling system designed to enhance student time-management and organizational skills.

This project is developed for Android devices enabling users to:
* Create and customize their profiles
* Search and send friend requests to other users
* Search and send friend requests to other users
* Schedule and invite other user to events (social or study)
* Post images
* Interact with other students through the chat interface
* Provide geospatial navigation to scheduled events

## Deployment Instructions
1.	Install [Android Studio](https://developer.android.com/studio) by visiting the link.

2.	Once successfully installed, launch Android studio and select the option to “Open an existing Android Studio Project”

2.	Once successfully installed, launch Android studio and select the option to “Open an existing Android Studio Project” 

3.	If the project files are not downloaded, they can be located by accessing [StudentPal’s GitHub repository](https://github.com/Ikram10/StudentPal_Repo)

4.	Locate and select the project file to open (This should be configured with Gradle, and should display an Android icon)

5.	Once the project has launched, a Firebase Cloud Messaging Server key and Google Cloud Platform API key will need to be created. See links provided to generate the keys.
* [FCM Server Key](https://firebase.google.com/docs/cloud-messaging/server)
* [GCP Key](https://cloud.google.com/docs/authentication/api-keys)

6.	Copy and paste both generated keys into the correct constant values in the project's
      [Constant File](https://github.com/Ikram10/StudentPal_Repo/blob/master/app/src/main/java/com/example/studentpal/common/Constants.kt) by replacing the “BuildConfig” values.

5.	Once the project has launched, a Firebase Cloud Messaging Server key and Google Cloud Platform API key will need to be created. See links provided to generate the keys. 
* [FCM Server Key](https://firebase.google.com/docs/cloud-messaging/server)
* [GCP Key](https://cloud.google.com/docs/authentication/api-keys) 

6.	Copy and paste both generated keys into the correct constant values in the project's 
[Constant File](https://github.com/Ikram10/StudentPal_Repo/blob/master/app/src/main/java/com/example/studentpal/common/Constants.kt) by replacing the “BuildConfig” values.

7.	Once the API keys have been added, press the Run button, ensuring the app module is selected

8.	Selection of a physical or virtual device is required to run the app. See [Android Studio's device manager guide](https://developer.android.com/studio/run/managing-avds) to correctly set up a device.

8.	Selection of a physical or virtual device is required to run the app. See [Android Studio's device manager guide](https://developer.android.com/studio/run/managing-avds) to correctly set up a device.

