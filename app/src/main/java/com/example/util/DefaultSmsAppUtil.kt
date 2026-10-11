package com.example.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.result.ActivityResultLauncher

object DefaultSmsAppUtil {

    fun isDefaultSmsApp(context: Context): Boolean {
        try {
            val defaultPkg = Telephony.Sms.getDefaultSmsPackage(context)
            if (defaultPkg == context.packageName) return true
        } catch (e: Exception) {
            // ignore
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = context.getSystemService(RoleManager::class.java)
                roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun openDefaultAppsSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val appSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(appSettings)
            } catch (e2: Exception) {
                try {
                    val fallback = Intent(android.provider.Settings.ACTION_SETTINGS)
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                } catch (e3: Exception) {
                    // ignore
                }
            }
        }
    }

    fun requestDefaultSmsApp(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                launcher.launch(intent)
                return
            }
        }

        @Suppress("DEPRECATION")
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
        }
        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            // fallback to general app settings if intent cannot be resolved
            val settingsIntent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            try {
                activity.startActivity(settingsIntent)
            } catch (e2: Exception) {
                val appSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(appSettings)
            }
        }
    }
}
