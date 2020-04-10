package xyz.yhsj.elauncher.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import xyz.yhsj.elauncher.MainActivity
import xyz.yhsj.elauncher.R

object Notifications {
    private const val NOTIFICATION_CUSTOM = 9936

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(context: Context, nm: NotificationManager) {
        //创建点击通知时发送的广播
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)


        //创建各个按钮的点击响应广播
        val intentBall = Intent(ActionKey.ACTION_HOVER_BALL)
        val piBall =
            PendingIntent.getBroadcast(context, 0, intentBall, 0)


        //创建自定义小视图
        val customView = RemoteViews(context.packageName, R.layout.layout_notification)

        customView.setOnClickPendingIntent(R.id.ball, piBall);

        //创建自定义大视图
        val customBigView =
            RemoteViews(context.packageName, R.layout.layout_notification)

        customBigView.setOnClickPendingIntent(R.id.ball, piBall);

        //创建通知
        val nb: Notification.Builder =
            Notification.Builder(context, NotificationChannels.DEFAULT)
                .setSmallIcon(R.mipmap.ic_launcher)  //设置通知左侧的小图标
                .setContentTitle("墨水屏")//设置通知标题
                .setContentText("墨水屏自定义功能")  //设置通知内容
                .setOngoing(true)//设置通知不可删除
                .setShowWhen(true)  //设置显示通知时间
                .setContentIntent(pi) //设置点击通知时的响应事件
                .setCustomContentView(customView) //设置自定义小视图
                .setCustomBigContentView(customBigView)//设置自定义大视图
        //发送通知
        nm.notify(NOTIFICATION_CUSTOM, nb.build())
    }


}