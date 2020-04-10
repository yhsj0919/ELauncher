package xyz.yhsj.elauncher.service

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import xyz.yhsj.elauncher.utils.AccessibilityOperator

class MyAccessibilityService : AccessibilityService() {
    private val TAG = MyAccessibilityService::class.java.simpleName
    private var mContext: Context? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        mContext = applicationContext
        AccessibilityOperator.instance.init(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        Log.e(TAG, "onAccessibilityEvent")
        AccessibilityOperator.instance.updateEvent(event)
        val packageName = AccessibilityOperator.instance.rootNodeInfo?.packageName?.toString()
//        pasteToEditTextContent(packageName)
        var accessibilityService = AccessibilityOperator.instance
        //按下返回键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
        //向下拉出状态栏
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        //向下拉出状态栏并显示出所有的快捷操作按钮
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        //按下HOME键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME)
        //显示最近任务
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_RECENTS)
        //长按电源键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        //分屏
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        //锁屏(安卓9.0适用)
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        //截屏(安卓9.0适用)
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        //打开快速设置
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    /**
     * 修改EditText输入框内容。
     * 下面样例修改了QQ搜索输入框内容。
     */
    private fun changeEditTextContent(packageName: String?) {
        getNodeToOperate(packageName)?.let {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                "被无障碍服务修改啦"
            )
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }


    /**
     * 读取剪贴板内容，粘贴到EditText输入框。
     * 下面样例修改了QQ搜索输入框内容。
     */
    private fun pasteToEditTextContent(packageName: String?) {
        getNodeToOperate(packageName)?.let {
            it.performAction(AccessibilityNodeInfo.FOCUS_INPUT)
            it.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            it.recycle()
        }
    }

    private fun getNodeToOperate(packageName: String?): AccessibilityNodeInfo? {
        if (packageName != null && packageName == "com.tencent.mobileqq") {
            val nodes =
                AccessibilityOperator.instance.findNodesById("com.tencent.mobileqq:id/et_search_keyword")
            if (nodes != null && nodes.isNotEmpty()) {
                return nodes[0]
            }
        }
        return null
    }

    override fun onInterrupt() {
    }

}