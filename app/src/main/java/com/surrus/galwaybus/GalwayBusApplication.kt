package com.surrus.galwaybus

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.logger.LogAdapter
import com.orhanobut.logger.Logger
import net.danlew.android.joda.JodaTimeAndroid
//import com.crashlytics.android.Crashlytics
//import io.fabric.sdk.android.Fabric
//import com.crashlytics.android.core.CrashlyticsCore
//import com.facebook.stetho.Stetho
import com.surrus.galwaybus.di.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

import com.surrus.galwaybus.common.appContext


class GalwayBusApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.i("GalwayBusApplication init")
        appContext = this

        // Stetho
        //Stetho.initializeWithDefaults(this);

        // Enable Firebase analytics in release build only
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Initialize Crashltyics
//        val crashlyticsCore = CrashlyticsCore.Builder()
//                .disabled(BuildConfig.DEBUG)
//                .build()
//        Fabric.with(this, Crashlytics.Builder().core(crashlyticsCore).build())

        // Initialize Logger
        if (!BuildConfig.DEBUG) {
            Logger.init().logAdapter(releaseLogAdapter)
        }

        // Initialize Joda
        JodaTimeAndroid.init(this)

        // Start Koin
        startKoin {
            androidContext(this@GalwayBusApplication)
            modules(appModule)
        }

        Logger.i("GalwayBusApplication init completed")
    }

    internal var releaseLogAdapter: LogAdapter = object : LogAdapter {

        override fun d(tag: String, message: String) {}

        override fun e(tag: String, message: String) {
            Log.e(tag, message)
        }

        override fun w(tag: String, message: String) {
            Log.w(tag, message)
        }

        override fun i(tag: String, message: String) {
            Log.i(tag, message)
        }

        override fun v(tag: String, message: String) {}

        override fun wtf(tag: String, message: String) {
            Log.wtf(tag, message)
        }
    }
}
