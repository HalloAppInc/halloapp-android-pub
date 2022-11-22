# halloapp-android

[Feature tracker](https://docs.google.com/spreadsheets/d/143pxndDicAbJZB9FogsEQCjB4t1DOHdTCQ4YIc7qOdg/edit?usp=sharing)

## Build flavors

This repo has the ability to build two separate apps. All shared items are
located in the main folder, the halloapp folder is for content specific to
HalloApp, and newapp is for content specific to our new app.

### Configuring the build variant in Android Studio

You must make sure you have selected the correct build variant. Go to View ->
Tool Windows -> Build Variants to toggle the Build Variant window. There likely
is also a button on the left edge of Android Studio to toggle whether this
window is visible. In the window you can select halloappDebug for building
HalloApp and newappDebug for building our new app. If you only see options
labeled debug and release, try forcing a Gradle sync by going to File ->
Sync Project with Gradle Files. There also should be a button for this with
an icon of an elephant in the upper right corner near the Device Manager
shortcut.

### Making release builds

For HalloApp the process has not meaningfully changed, except that on the
build variants page you will now need to select halloappRelease.

For our new app, Google will require us to use Android App Bundles and
Google Play App Signing. Start by selecting Android App Bundle instead of
APK on the first page. We will use the same signing key to authenticate
ourselves to Google, but Google will use a different signing key when
distributing our new app to our users. We have no control over this.
Finally, on the build variant page you will select newappRelease.

### Where items belong

There are various considerations for where content should be placed in
the repo. Most features related to the UI of our new app will belong in
the newapp folder. When we are able to reuse components from HalloApp they
can remain in the main folder, but note that if these items need to be
referenced from the AndroidManifest (i.e. Activities, Services, etc)
the manifest entries should be moved from the halloapp manifest to the
main manifest (manifests get merged at build time). We will keep strings
in the main folder so that we can continue using the same string sync
pipeline.
