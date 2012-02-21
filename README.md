# Test coverage
     cd <inside the app directory>
     android update project --path .
     android update test-project -m <full path to app directory (not just .)> -p ../test/
     cd ../test/
     ant coverage

# Code Formatting
Please use the following xml file as formatting in eclipse and make it to be applied whenever a file is saved.
    https://github.com/android/platform_development/raw/master/ide/eclipse/android-formatting.xml
