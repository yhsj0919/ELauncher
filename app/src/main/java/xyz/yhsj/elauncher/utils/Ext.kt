package xyz.yhsj.elauncher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
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


/**
 * 判断服务是否开启
 * @param context
 * @param ServiceName
 * @return
 */
fun isServiceRunning(context: Context, ServiceName: String?): Boolean {
    if (TextUtils.isEmpty(ServiceName)) {
        return false
    }
    val myManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningService = myManager.getRunningServices(30)

    runningService.forEach {
        if (it.service.className == ServiceName) {
            return true
        }
    }
    return false
}