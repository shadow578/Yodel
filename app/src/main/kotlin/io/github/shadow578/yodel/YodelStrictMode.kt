package io.github.shadow578.yodel

import android.os.*
import android.util.Log

/**
 * enable [StrictMode] when running a debug build
 */
fun maybeEnableStrictMode() {
    if (BuildConfig.DEBUG) {
        Log.i("Yodel", "Enable Strict Mode on debug build!")

        // setup the thread policy
        // could probably use .detectAll(), but documentation is not really clear what that includes
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectResourceMismatches()
            .penaltyLog()
            .penaltyFlashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            threadPolicy.detectUnbufferedIo()

        //setup vm policy
        // could probably also use .detectAll(), but documentation is not really clear what that includes
        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedClosableObjects()
            .detectActivityLeaks()
            .detectCleartextNetwork()
            .detectFileUriExposure()
            .penaltyLog()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vmPolicy.detectContentUriWithoutPermission()
                .detectUntaggedSockets()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            vmPolicy.detectNonSdkApiUsage()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            vmPolicy.detectCredentialProtectedWhileLocked()
                .detectImplicitDirectBoot()

        // set policies
        StrictMode.setThreadPolicy(threadPolicy.build())
        StrictMode.setVmPolicy(vmPolicy.build())
    }
}