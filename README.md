# cordova-plugin-acuantsdk
Cordova Acuant SDK Plugin

Cordova plugin is to utilize AcuantSDKs features which supports Andrid and iOS platforms.

# Usage
## To scan the document use the below code
````
cordova.plugins.acuantsdk.scanDocument(sdkCredentials, successcallback, failureCallback);

Define your successcallback and failurecallback functions
function successCallback(base64String) {
    console.log("Success Result from acuant sdk :" + base64String);
}

function failureCallback(message) {
    console.log("Failure Result from acuant sdk :" + message);
}
````
## To capture selfie use the below code
````
cordova.plugins.acuantsdk.takeSelfie(sdkCredentials, successcallback, failureCallback);

Define your successcallback and failurecallback functions
function successCallback(base64String) {
    console.log("Success Result from acuant sdk :" + base64String);
}

function failureCallback(message) {
    console.log("Failure Result from acuant sdk :" + message);
}
````
# Sample Android App
For first time Run 
````
cd 
cordova platform add android
````

## SDK Credentials
Substitute AcuantSDK credentials in credentials.js
