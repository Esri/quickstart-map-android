quickstart-map-android
==================

A library, sample app and starter template for getting started with Esri's Android Runtime SDK.

See the repo's wiki for additional details: https://github.com/Esri/quickstart-map-android/wiki

![App](https://raw.github.com/Esri/quickstart-map-android/master/quickstart-android.png)

## Features
* Simplifies common coding patterns down to one or a couple of lines of code
* Goes beyond the SDK samples in demonstrating best practices for building mapping applications on Android.
* Includes EsriQuickStart library project, fully functional sample app, and a Hello World template app that you can quickly build upon.
* As of Version 1.1 the library project and associated jar file contain the Javadoc.

## Instructions

1. Make sure you have installed both the [Google Android SDK](http://developer.android.com/sdk/index.html) and the [ArcGIS Runtime SDK for Android](http://resources.arcgis.com/en/help/android-sdk/concepts/0119/01190000002m000000.htm). I have tested this with the ADT Bundle if that is the easiest way for you to go.
2. Download and unzip the .zip file or clone the repo.
3. Create new three new local git projects out of the three included directories: EsriQuickStart, EsriQuickStartSample and EsriQuickStartTemplate.
4. Import each of the projects into Eclipse or ADT Bundle using the following four steps:
	* Step 1: File > Import > Git > Projects from Git > Next
	* Step 2: Select Repository Source > Local > Next > Add > Browse > Finish
	* Step 3: In the Select a Wizard To Use for Importing Projects window, select Import Existing Project > Next. Note: Your git directory will also become the project directory. If you get an error that says project already exists, try editing the .project file and changing the "name" property.
	* Step 4:  Look for any project errors in Eclipse. Most commonly you will need to right click on the project > Android Tools > Fix Project Properties. Then run Project > Clean.
5. For the EsriQuickStart project, right click on the project name in the Package Explorer > Properties > Android > Library > Check the "Is Library" option > OK. Once that is done clean your project.
6. To add the javadoc so that you get access to the code comments via intellisense right click on the EsriQuickStart library project name in the Package Explorer > Properties > Java Build Path > Source > Add Folder > [Select the directory where the javadoc is] > OK. Once that is done clean your project.

[New to Github? Get started here.](http://htmlpreview.github.com/?https://github.com/Esri/esri.github.com/blob/master/help/esri-getting-to-know-github.html)

## Requirements

* [Eclipse](http://www.eclipse.org/downloads/) or [ADT Bundle](http://developer.android.com/sdk/index.html)
* [Google Android SDK](http://developer.android.com/sdk/index.html) - this step is not needed if you download and install the ADT Bundle.
* [ArcGIS Runtime SDK for Android](http://resources.arcgis.com/en/help/android-sdk/concepts/0119/01190000002m000000.htm)
* [Experience with Java would help.](http://developer.android.com/training/index.html)

## Resources

* [ArcGIS Runtime SDK for Android Resource Center](http://resources.arcgis.com/en/communities/runtime-android/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an Issue.

## Contributing

Anyone and everyone is welcome to contribute. 

## Licensing
Copyright 2013 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/quickstart-map-android/blob/master/license.txt) file.

[](Esri Tags: ArcGIS Android Map QuickStart Java)
[](Esri Language: Java)
