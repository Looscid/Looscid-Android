# Looscid for Android

Looscid for Android. Open source, no ads. Just Dream.

This is the Android app for [Looscid](https://looscid.github.io/Looscid/). It's written in Kotlin and it's tiny on purpose: the app opens the live Looscid web app full screen. So when Looscid gets new stuff, you get it right away, no app update needed.

The web version lives in [Looscid/Looscid](https://github.com/Looscid/Looscid). This repo is only the Android app.

## What the app does

- Opens Looscid full screen.
- The back button goes back inside Looscid. On the first page, it closes the app.
- Links to other websites open in your browser, not inside the app.
- TalkBack, braille displays and keyboards work the same as in Chrome. The app doesn't catch or change any keys.

## Safe to install

It's a normal Android app that runs in its own sandbox, like any app from a store. It only asks for internet and permission to install its own updates. It can't change system settings, doesn't run in the background and doesn't need root. The worst thing that can happen is the app closing, not your phone. If the page itself crashes, the app reloads it.

Every push is also tested on Android emulators (Android 10 and Android 13): GitHub installs the app, opens it, waits 20 seconds and checks it didn't crash.

## How updates work

There are 2 kinds of updates.

1. **Looscid updates** (new features, fixes, apps inside Looscid). These come from the website, so they show up the next time you open the app. Nothing to install.
2. **App updates** (when this Android app itself changes). Every time you open the app, it checks the newest [release](https://github.com/Looscid/Looscid-Android/releases) on GitHub. If there's a newer version, you get a popup called "Looscid needs an update" with what's new, and 2 buttons: "Update" and "Later".

If you pick Update, the app downloads the new APK and opens Android's installer. The first time, Android asks you to allow installs from Looscid. After that you just tap Install. Android always asks you to tap Install for apps that aren't from a store, so it can't be fully silent.

## Get the app

Download `Looscid.apk` from the [latest release](https://github.com/Looscid/Looscid-Android/releases/latest) and open it on your phone. It also works on GrapheneOS.

## Build it yourself

You need JDK 17 and the Android SDK (Android Studio comes with both).

```
git clone https://github.com/Looscid/Looscid-Android.git
cd Looscid-Android
./gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/app-debug.apk`.

You don't have to build it on your own computer. GitHub Actions builds it on every push. Open the Actions tab, pick a run, and download the `Looscid-debug-apk` file.

## How versions work

- Looscid is in beta right now. The web beta's YEAR.FEATURES.FIXES (BUILD) style, like 2026.138.57 (195), is only for the web beta.
- Until launch, every app stays at 0.x. This app is 0.1.0.
- The number in parentheses is each app's own build count. Android counts only Android builds, so v0.1.0 is 0.1.0 (1).
- When Looscid launches, every app (web, Android and iOS) launches together at 1.0.

Builds so far: web 195, Android 1, iOS 0 (196 total).

## Making a new release

1. In `app/build.gradle.kts`, raise `versionCode` by 1 and set `versionName` (like `0.2.0`).
2. Write what's new in `whats-new/v0.2.0.txt`. Keep it plain text, since it's shown in the update popup.
3. Push a tag called `v0.2.0`. GitHub builds the APK and makes the release, and everyone's app sees it next time they open it.

## Signing

Right now the app is signed with a debug key that's kept in this repo (`keystore/debug.keystore`), so every build can update the one before it. That's fine for testing, but it isn't safe for a real launch, since anyone can sign with it. Release signing with a private key is a later step.

## Later

- F-Droid listing.
- Release signing with a private key.
- iOS version: Looscid-iOS, written in Swift, installed by sideloading.
- Braille display Enter key on native screens:
  - Enter submits one line fields (name, search, email).
  - In the message box, Enter sends and Shift+Enter makes a new line.
  - A Settings checkbox "Enter sends messages". Uncheck it and Enter makes a new line, and you send with the Send button.
  - Enter on a braille display activates whatever has focus, including buttons, menus (it opens them) and checkboxes. On VoiceOver, Enter also runs the selected swipe action (e.g. Like Dream). Native screens use real buttons and accessibility custom actions (iOS accessibilityCustomActions, Android AccessibilityAction), so the screen reader handles Enter on any braille display, including ones paired to phones without built-in braille.

## License

AGPL-3.0. See [LICENSE](LICENSE). Copyright (C) 2025-present Looscid.
