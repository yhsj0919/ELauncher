package xyz.yhsj.elauncher.utils

import android.content.Context
import android.content.Intent
import xyz.yhsj.elauncher.bean.AppInfo
import java.lang.Exception


fun getAllApp(context: Context): ArrayList<AppInfo> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = pm.queryIntentActivities(mainIntent, 0)

    val apps = ArrayList<AppInfo>()

    activities.forEach {
        val packName: String = it.activityInfo.packageName

        val appInfo = AppInfo()
        appInfo.icon = it.activityInfo.applicationInfo.loadIcon(pm)
        appInfo.name = it.activityInfo.applicationInfo.loadLabel(pm).toString()
        appInfo.packageName = packName
        appInfo.packageName = packName

        try {
            val info = pm.getPackageInfo(packName, 0);
            appInfo.version = info.versionName
            appInfo.updateTime = info.lastUpdateTime
        } catch (e: Exception) {
            appInfo.version = "1.0"
        }

        apps.add(appInfo)
    }

    return apps
}