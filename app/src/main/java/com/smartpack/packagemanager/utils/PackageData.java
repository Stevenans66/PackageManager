/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageData {

    public static boolean mAppType= false, mSystemApp = true;
    public static CharSequence mApplicationName;
    public static Drawable mApplicationIcon;
    public static List<String> mBatchList = new ArrayList<>();
    public static String mApplicationID;
    public static String mDirData;
    public static String mDirNatLib;
    public static String mDirSource;
    public static String mPath;
    public static String mSearchText;

    public static void makePackageFolder(Context context) {
        File file = new File(getPackageDir(context));
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        || (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else if (Utils.getBoolean("system_apps", true, context)
                    && !Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            } else if (!Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else {
                mAppType = false;
            }
            if (mAppType && packageInfo.packageName.contains(".")) {
                if (mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static PackageManager getPackageManager(Context context) {
        return context.getApplicationContext().getPackageManager();
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ApplicationInfo getAppInfo(String packageName, Context context) {
        try {
            return getPackageManager(context).getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppName(String packageName, Context context) {
        return getPackageManager(context).getApplicationLabel(Objects.requireNonNull(getAppInfo(
                packageName, context))) + (isEnabled(packageName, context) ? "" : " (Disabled)");
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        return getPackageManager(context).getApplicationIcon(Objects.requireNonNull(getAppInfo(packageName, context)));
    }

    public static String getSourceDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).sourceDir;
    }

    public static String getParentDir(String packageName, Context context) {
        return Objects.requireNonNull(new File(Objects.requireNonNull(getAppInfo(packageName, context))
                .sourceDir).getParentFile()).toString();
    }

    public static String getNativeLibDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).nativeLibraryDir;
    }

    public static String getDataDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).dataDir;
    }

    public static String getVersionName(String path, Context context) {
        return Objects.requireNonNull(getPackageManager(context).getPackageArchiveInfo(path, 0)).versionName;
    }

    public static String getInstalledDate(String path, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(path, context)).firstInstallTime);
    }

    public static String getUpdatedDate(String path, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(path, context)).lastUpdateTime);
    }

    public static boolean isEnabled(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).enabled;
    }

    public static boolean isSystemApp(String packageName, Context context) {
        return (Objects.requireNonNull(getAppInfo(packageName, context)).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static Drawable getAPKIcon(String apkPath, Context context) {
        PackageInfo pi = PackageData.getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.loadIcon(PackageData.getPackageManager(context));
        } else {
            return null;
        }
    }

    public static String getPackageDir(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            return Objects.requireNonNull(context.getExternalFilesDir("")).toString();
        } else {
            return Environment.getExternalStorageDirectory().toString() + "/Package_Manager";
        }
    }

    public static void clearAppSettings(String packageID) {
        Utils.runCommand("pm clear " + packageID);
    }

    public static String getBatchList() {
        return mBatchList.toString().substring(1, mBatchList.toString().length() - 1);
    }

    public static String showBatchList() {
        String[] array = getBatchList().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            if (s != null && !s.isEmpty())
                sb.append(" - ").append(s.replaceAll(","," ")).append("\n");
        }
        return "\n" + sb.toString();
    }

}