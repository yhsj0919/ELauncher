package xyz.yhsj.elauncher.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences 工具类
 *
 */
object SpUtil {

    private var sp: SharedPreferences? = null

    /** 保存在手机里面的文件名  */
    private val SharePreferncesName = "SP_SETTING"

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * 键值对的key
     * @param value
     * 键值对的值
     * @return 是否保存成功
     */
    fun setValue(context: Context, key: String, value: Any): Boolean {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }

        val edit = sp!!.edit()
        if (value is String) {
            return edit.putString(key, value).commit()
        } else if (value is Boolean) {
            return edit.putBoolean(key, value).commit()
        } else if (value is Float) {
            return edit.putFloat(key, value).commit()
        } else if (value is Int) {
            return edit.putInt(key, value).commit()
        } else if (value is Long) {
            return edit.putLong(key, value).commit()
        } else if (value is Set<*>) {
            IllegalArgumentException("Value can not be Set object!")
            return false
        }
        return false
    }

    /**
     * 得到Boolean类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    fun getBoolean(
        context: Context, key: String,
        defaultValue: Boolean
    ): Boolean {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }
        return sp!!.getBoolean(key, defaultValue)
    }

    /**
     * 得到String类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    fun getString(
        context: Context, key: String,
        defaultValue: String
    ): String? {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }
        return sp!!.getString(key, defaultValue)
    }

    /**
     * 得到Float类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    fun getFloat(context: Context, key: String, defaultValue: Float): Float {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }
        return sp!!.getFloat(key, defaultValue)
    }

    /**
     * 得到Int类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }
        return sp!!.getInt(key, defaultValue)
    }

    /**
     * 得到Long类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    fun getLong(context: Context, key: String, defaultValue: Long): Long {
        if (sp == null) {
            sp = context.getSharedPreferences(
                SharePreferncesName,
                Context.MODE_PRIVATE
            )
        }
        return sp!!.getLong(key, defaultValue)
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    fun remove(context: Context, key: String): Boolean {
        val sp = context.getSharedPreferences(
            SharePreferncesName, Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.remove(key)
        return editor.commit()
    }

    /**
     * 清除所有数据
     *
     * @param context
     * @return 是否成功
     */
    fun clear(context: Context): Boolean {
        val sp = context.getSharedPreferences(
            SharePreferncesName, Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.clear()
        return editor.commit()
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return 是否存在
     */
    fun contains(context: Context, key: String): Boolean {
        val sp = context.getSharedPreferences(
            SharePreferncesName, Context.MODE_PRIVATE
        )

        return sp.contains(key)
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return Map<String></String>, ?>
     */
    fun getAll(context: Context): Map<String, *> {
        val sp = context.getSharedPreferences(
            SharePreferncesName, Context.MODE_PRIVATE
        )
        return sp.all
    }

}
