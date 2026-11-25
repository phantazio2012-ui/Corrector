#!/usr/bin/env python3
"""Branche le module de mise à jour automatique dans le projet Android généré par Capacitor."""
import os, re, shutil, sys

root = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
pkg_dir = os.path.join(root, 'android/app/src/main/java/ci/cgsi/correcteur')
os.makedirs(pkg_dir, exist_ok=True)
shutil.copy(os.path.join(root, 'native/ApkUpdaterPlugin.java'), pkg_dir)

with open(os.path.join(pkg_dir, 'MainActivity.java'), 'w', encoding='utf-8') as f:
    f.write('''package ci.cgsi.correcteur;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ApkUpdaterPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
''')

mf = os.path.join(root, 'android/app/src/main/AndroidManifest.xml')
s = open(mf, encoding='utf-8').read()
perms = [
    '<uses-permission android:name="android.permission.CAMERA" />',
    '<uses-feature android:name="android.hardware.camera" android:required="false" />',
    '<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />',
]
add = ''.join('    ' + p + '\n' for p in perms if p not in s)
s = s.replace('<application', add.lstrip() + '    <application', 1)
open(mf, 'w', encoding='utf-8').write(s)
print(s)
