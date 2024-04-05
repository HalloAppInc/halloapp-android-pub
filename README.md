# halloapp-android

Android version of HalloApp and Katchup. Make sure to insert respective `google-services.json` files in the project you are compiling (e.g. `app/src/halloapp/google-services.json` and `app/src/katchup/google-services.json`). HalloApp also uses [Sentry](sentry.io) which can either be removed or connected to your own project (make sure to put `sentry.properties` in the root of this project).

## Build flavors

This repo has the ability to build two separate apps. All shared items are
located in the main folder, the halloapp folder is for content specific to
HalloApp, and katchup is for content specific to Katchup.

### Configuring the build variant in Android Studio

You must make sure you have selected the correct build variant. Go to View ->
Tool Windows -> Build Variants to toggle the Build Variant window. There likely
is also a button on the left edge of Android Studio to toggle whether this
window is visible. In the window you can select halloappDebug for building
HalloApp and katchupDebug for building Katchup by clicking on the right column
to open up a dropdown menu and selecting the right option. If you only see options
labeled debug and release, try forcing a Gradle sync by going to File ->
Sync Project with Gradle Files. There also should be a button for this with
an icon of an elephant in the upper right corner near the Device Manager
shortcut.

### Making release builds

For HalloApp the process has not meaningfully changed, except that on the
build variants page you will now need to select halloappRelease.

For our new app, Google requires us to use Android App Bundles and
Google Play App Signing and does not allow us to use our HalloApp
signing key as the upload key. Start by selecting Android App Bundle
instead of APK on the first page. Then you will need to select the
katchup.jks key store. Finally, on the build variant page you will
select katchupRelease.

### Where items belong

There are various considerations for where content should be placed in
the repo. Most features related to the UI of our new app will belong in
the katchup folder. When we are able to reuse components from HalloApp they
can remain in the main folder, but note that if these items need to be
referenced from the AndroidManifest (i.e. Activities, Services, etc)
the manifest entries should be moved from the halloapp manifest to the
main manifest (manifests get merged at build time). We will keep strings
in the main folder so that we can continue using the same string sync
pipeline.
