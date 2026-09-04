#!/usr/bin/env bash
set -euo pipefail

manifest="android/app/src/main/AndroidManifest.xml"
sed -i 's/android:label="usman_notepad"/android:label="UsmanNotepad"/g' "$manifest"

mkdir -p \
  android/app/src/main/res/drawable \
  android/app/src/main/res/mipmap-anydpi \
  android/app/src/main/res/mipmap-anydpi-v26 \
  android/app/src/main/res/values

cat > android/app/src/main/res/values/usman_brand.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="usman_canvas">#F6F0E5</color>
</resources>
XML

cat > android/app/src/main/res/drawable/usman_foreground.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFDF8"
        android:pathData="M28,20 H70 L84,34 V88 H28 Z"
        android:strokeColor="#2F2A26"
        android:strokeWidth="4" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M70,20 V34 H84"
        android:strokeColor="#2F2A26"
        android:strokeWidth="4" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M39,48 H72 M39,59 H68 M39,70 H61"
        android:strokeColor="#6F675F"
        android:strokeWidth="4" />
    <path android:fillColor="#D89B52" android:pathData="M66,75 L80,61 L84,65 L70,79 L63,82 Z" />
</vector>
XML

cat > android/app/src/main/res/mipmap-anydpi/ic_launcher.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#F6F0E5" android:pathData="M0,0 H108 V108 H0 Z" />
    <path
        android:fillColor="#FFFDF8"
        android:pathData="M28,20 H70 L84,34 V88 H28 Z"
        android:strokeColor="#2F2A26"
        android:strokeWidth="4" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M70,20 V34 H84 M39,48 H72 M39,59 H68 M39,70 H61"
        android:strokeColor="#6F675F"
        android:strokeWidth="4" />
    <path android:fillColor="#D89B52" android:pathData="M66,75 L80,61 L84,65 L70,79 L63,82 Z" />
</vector>
XML
cp android/app/src/main/res/mipmap-anydpi/ic_launcher.xml \
  android/app/src/main/res/mipmap-anydpi/ic_launcher_round.xml

cat > android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/usman_canvas" />
    <foreground android:drawable="@drawable/usman_foreground" />
</adaptive-icon>
XML
cp android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
