#!/bin/bash

# Simple APK build script
export ANDROID_HOME=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0

echo "Creating simple APK build..."

# Create build directories
mkdir -p simple_apk/src/com/example/screenshotocr
mkdir -p simple_apk/res/layout
mkdir -p simple_apk/res/values
mkdir -p simple_apk/res/drawable

# Create simple manifest
cat > simple_apk/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.screenshotocr"
    android:versionCode="1"
    android:versionName="1.0">

    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    <application
        android:allowBackup="true"
        android:icon="@android:drawable/ic_menu_gallery"
        android:label="Screenshot OCR"
        android:theme="@android:style/Theme.Material.Light">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# Create simple layout
cat > simple_apk/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Screenshot OCR"
        android:textSize="24sp"
        android:gravity="center"
        android:layout_marginBottom="20dp" />

    <Button
        android:id="@+id/btnScan"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Scan Screenshots"
        android:layout_marginBottom="10dp" />

    <Button
        android:id="@+id/btnSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Search Text" />

</LinearLayout>
EOF

# Create strings
cat > simple_apk/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Screenshot OCR</string>
</resources>
EOF

# Create simple MainActivity in Java (easier to compile)
cat > simple_apk/src/com/example/screenshotocr/MainActivity.java << 'EOF'
package com.example.screenshotocr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btnScan = findViewById(R.id.btnScan);
        Button btnSearch = findViewById(R.id.btnSearch);
        
        btnScan.setOnClickListener(v -> 
            Toast.makeText(this, "Scan feature coming soon!", Toast.LENGTH_SHORT).show()
        );
        
        btnSearch.setOnClickListener(v -> 
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}
EOF

echo "Building APK..."

# Generate R.java
aapt package -f -m -J simple_apk/src -M simple_apk/AndroidManifest.xml -S simple_apk/res -I $ANDROID_HOME/platforms/android-34/android.jar

# Compile Java source with Java 8 target
javac -source 8 -target 8 -d simple_apk/classes -cp $ANDROID_HOME/platforms/android-34/android.jar simple_apk/src/com/example/screenshotocr/*.java

# Create classes.dex
d8 --output simple_apk/ simple_apk/classes/com/example/screenshotocr/*.class

# Package resources
aapt package -f -M simple_apk/AndroidManifest.xml -S simple_apk/res -I $ANDROID_HOME/platforms/android-34/android.jar -F simple_apk/resources.apk

# Create APK structure
mkdir -p simple_apk/apk
cd simple_apk
unzip -o resources.apk -d apk/
cp classes.dex apk/
cd apk
zip -r ../../app-simple.apk .
cd ../..

# Sign APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore ~/.android/debug.keystore -storepass android -keypass android app-simple.apk androiddebugkey

# Align APK
zipalign -v 4 app-simple.apk app-simple-aligned.apk

echo "Simple APK created: app-simple-aligned.apk"
ls -la app-simple*.apk