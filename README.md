# Test coverage
     cd <inside the app directory>
     android update project --path .
     android update test-project -m <full path to app directory (not just .)> -p ../test/
     cd ../test/
     ant coverage

# Code Formatting
Please use the following xml file as formatting in eclipse and make it to be applied whenever a file is saved.
    https://github.com/android/platform_development/raw/master/ide/eclipse/android-formatting.xml


# Facebook SDK
Install Facebook SDK by cloning the GitHub repository: git clone git://github.com/facebook/facebook-android-sdk.git


# Face Account Credentials for OpenPhoto
The file `FakeAccountOpenPhotoApi.java` is a fake implementation for the interface `IAccountOpenOpenphotoApi.java`.
This fake implementation will return credentials to the site http://apigee.openphoto.me


#Environment
To make the environment working we need to **Add Support Library** in the project `ActionBarSherlock`.
These are the steps on Eclipse:

1. Click with right button in the project `ActionBarSherlock`
2. Select _Android Tools_
3. Select _Add Support Library_
4. Accept and Finish