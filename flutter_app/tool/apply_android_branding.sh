#!/usr/bin/env bash
set -euo pipefail

manifest="android/app/src/main/AndroidManifest.xml"
sed -i 's/android:label="usman_notepad"/android:label="Sukoon Notes"/g' "$manifest"

mkdir -p \
  android/app/src/main/res/drawable \
  android/app/src/main/res/mipmap-anydpi \
  android/app/src/main/res/mipmap-anydpi-v26 \
  android/app/src/main/res/values

cat > android/app/src/main/res/values/sukoon_brand.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="sukoon_canvas">#F6F0E5</color>
</resources>
XML

cat > android/app/src/main/res/drawable/sukoon_foreground.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M28,38 C39,23 68,22 80,35 C73,30 64,29 56,32 C48,35 46,40 52,44 C58,48 69,47 79,52"
        android:strokeColor="#2F2A26"
        android:strokeWidth="7" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M80,70 C69,85 40,86 28,73 C35,78 44,79 52,76 C60,73 62,68 56,64 C50,60 39,61 29,56"
        android:strokeColor="#2F2A26"
        android:strokeWidth="7" />
    <path
        android:fillColor="#D89B52"
        android:pathData="M73,72 L84,61 L84,78 Z" />
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
        android:fillColor="@android:color/transparent"
        android:pathData="M28,38 C39,23 68,22 80,35 C73,30 64,29 56,32 C48,35 46,40 52,44 C58,48 69,47 79,52"
        android:strokeColor="#2F2A26"
        android:strokeWidth="7" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M80,70 C69,85 40,86 28,73 C35,78 44,79 52,76 C60,73 62,68 56,64 C50,60 39,61 29,56"
        android:strokeColor="#2F2A26"
        android:strokeWidth="7" />
    <path android:fillColor="#D89B52" android:pathData="M73,72 L84,61 L84,78 Z" />
</vector>
XML
cp android/app/src/main/res/mipmap-anydpi/ic_launcher.xml \
  android/app/src/main/res/mipmap-anydpi/ic_launcher_round.xml

cat > android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/sukoon_canvas" />
    <foreground android:drawable="@drawable/sukoon_foreground" />
</adaptive-icon>
XML
cp android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
