package xyz.yhsj.elauncher.utils

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class AccessibilityOperator private constructor() {
    private var mAccessibilityEvent: AccessibilityEvent? = null
    private var accessibilityService: AccessibilityService? = null

    val rootNodeInfo: AccessibilityNodeInfo?
        get() {
            var nodeInfo: AccessibilityNodeInfo? = null
            accessibilityService?.let {
                nodeInfo = accessibilityService!!.rootInActiveWindow
            }

            if (nodeInfo == null && mAccessibilityEvent != null) {
                nodeInfo = mAccessibilityEvent!!.source
            }

            return nodeInfo
        }

    fun init(service: AccessibilityService) {
        accessibilityService = service
    }

    fun updateEvent(event: AccessibilityEvent) {
        mAccessibilityEvent = event
    }

    /**
     * 根据Text搜索所有符合条件的节点, 模糊搜索方式
     */
    fun findNodesByText(text: String): List<AccessibilityNodeInfo>? {
        val nodeInfo = rootNodeInfo
        return nodeInfo?.findAccessibilityNodeInfosByText(text)
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     *
     * @param viewId
     */
    fun findNodesById(viewId: String): List<AccessibilityNodeInfo>? {
        val nodeInfo = rootNodeInfo
        return nodeInfo?.findAccessibilityNodeInfosByViewId(viewId)
    }

    fun clickByText(text: String): Boolean {
        return performClick(findNodesByText(text))
    }

    fun clickParentByText(text: String, depth: Int): Boolean {
        return this.performClick(this.findParentNodesByText(text, depth))
    }

    fun clickParentById(viewId: String, depth: Int): Boolean {
        return this.performClick(this.findParentNodesById(viewId, depth))
    }

    fun findParentNodesByText(text: String, depth: Int): List<AccessibilityNodeInfo> {
        val rootNodeInfo = this.rootNodeInfo
        val resultNodeList = mutableListOf<AccessibilityNodeInfo>()
        if (rootNodeInfo != null) {
            val nodeList = findAccessibilityNodeInfosByText(rootNodeInfo, text)
            val iterator = nodeList.iterator()

            while (iterator.hasNext()) {
                val accessibilityNodeInfo = iterator.next() as AccessibilityNodeInfo
                resultNodeList.add(getParentNode(accessibilityNodeInfo, depth))
            }
        }

        return resultNodeList
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun findParentNodesById(viewId: String, depth: Int): List<AccessibilityNodeInfo> {
        val rootNodeInfo = this.rootNodeInfo
        val resultNodeList = mutableListOf<AccessibilityNodeInfo>()
        if (rootNodeInfo != null) {
            val nodeList = rootNodeInfo.findAccessibilityNodeInfosByViewId(viewId)
            val iterator = nodeList.iterator()

            while (iterator.hasNext()) {
                val accessibilityNodeInfo = iterator.next() as AccessibilityNodeInfo
                resultNodeList.add(this.getParentNode(accessibilityNodeInfo, depth))
            }
        }

        return resultNodeList
    }

    private fun findAccessibilityNodeInfosByText(
        node: AccessibilityNodeInfo?,
        text: String?
    ): List<AccessibilityNodeInfo> {
        val resultNodeList = mutableListOf<AccessibilityNodeInfo>()
        if (node != null && text != null) {
            val nodeList = node.findAccessibilityNodeInfosByText(text)
            if (nodeList != null && !nodeList.isEmpty()) {
                val iterator = nodeList.iterator()
                while (iterator.hasNext()) {
                    val nodeInList = iterator.next() as AccessibilityNodeInfo
                    if (TextUtils.equals(nodeInList.text, text)) {
                        resultNodeList.add(nodeInList)
                    }
                }
            }

            return resultNodeList
        } else {
            return resultNodeList
        }
    }

    private fun getParentNode(nodeInfo: AccessibilityNodeInfo, depth: Int): AccessibilityNodeInfo {
        var resultNodeInfo = nodeInfo

        for (i in 0 until depth) {
            val parentNode = resultNodeInfo.parent
            resultNodeInfo = parentNode
        }

        return resultNodeInfo
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     *
     * @param viewId
     * @return 是否点击成功
     */
    fun clickById(viewId: String): Boolean {
        return performClick(findNodesById(viewId))
    }

    private fun performClick(nodeInfoList: List<AccessibilityNodeInfo>?): Boolean {
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            var node: AccessibilityNodeInfo
            for (i in nodeInfoList.indices) {
                node = nodeInfoList[i]
                // 进行模拟点击
                if (node.isEnabled) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }

    fun clickBackKey(): Boolean {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    fun performGlobalAction(action: Int): Boolean {
        return accessibilityService!!.performGlobalAction(action)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getNodeInfo(nodeInfo: AccessibilityNodeInfo?): String {
        var result = ""
        if (nodeInfo != null) {
            result =
                nodeInfo.className.toString() + ";text:" + nodeInfo.text + ";id:" + nodeInfo.viewIdResourceName + ";"
        }
        return result
    }

    fun clickTextParent(text: String): Boolean {
        val nodeInfo = rootNodeInfo
        return nodeInfo?.let { clickTextParent(it, text) } ?: false
    }

    private fun clickTextParent(rootInfo: AccessibilityNodeInfo?, text: String): Boolean {
        if (rootInfo != null && !TextUtils.isEmpty(rootInfo.className)) {
            if ("android.widget.TextView" == rootInfo.className.toString()) {
                if (!TextUtils.isEmpty(rootInfo.text) && rootInfo.text.toString()
                        .startsWith(text)
                ) {
                    val result = performClick(rootInfo.parent)
                    Log.v(TAG, rootInfo.parent.className.toString() + ":result=" + result)

                    return result
                }
            }
            for (i in 0 until rootInfo.childCount) {
                val result = clickTextParent(rootInfo.getChild(i), text)
                if (result) {
                    return result
                }
            }
            return false
        }
        return false
    }

    private fun performClick(targetInfo: AccessibilityNodeInfo): Boolean {
        return targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    companion object {
        private val TAG = "AccessibilityOperator"
        val instance = AccessibilityOperator()
    }
}