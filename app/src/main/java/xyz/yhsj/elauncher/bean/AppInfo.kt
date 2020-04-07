package xyz.yhsj.elauncher.bean

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(
    /**
     * 应用名称
     */
    var name: String = "",
    /**
     *图标
     */
    var icon: Drawable? = null,

    //图标文件，用于替换图标
    var iconFile: Drawable? = null,

    /**
     * 包名
     */
    var packageName: String = "",
    /**
     * 版本
     */
    var version: String = "",
    /**
     * 更新时间
     */
    var updateTime: Long = 0,
    /**
     * 是否是应用
     */
    var isApp: Boolean = true,

    /**
     * 隐藏
     */
    var hide: Boolean = false

)