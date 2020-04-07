package xyz.yhsj.elauncher.setting

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_app_manager.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.adapter.AppManagerAdapter
import xyz.yhsj.elauncher.bean.AppInfo
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.utils.getAllApp
import kotlin.concurrent.thread

class AppManagerActivity : AppCompatActivity() {

    lateinit var appManagerAdapter: AppManagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_manager)
        back.setOnClickListener { finish() }


        appManagerAdapter = AppManagerAdapter(appList)
        appList.layoutManager = LinearLayoutManager(this)
        appList.adapter = appManagerAdapter
        //添加Android自带的分割线
        appList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        appManagerAdapter.setOnItemChildClickListener { _, _, position ->
            //修改列表
            val data = appManagerAdapter.data[position]
            data.hide = !data.hide
            appManagerAdapter.setItem(position, data)
            //修改显示状态
            SpUtil.setValue(this, data.packageName, data.hide)
            //发送广播
            val i = Intent("xyz.yhsj.PACKAGE_HIDE")
            i.data = Uri.parse("package:${data.packageName}")
            sendBroadcast(i)

        }

        appManagerAdapter.setOnItemClickListener { _, _, position ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.parse("package:${appManagerAdapter.data[position].packageName}")
            startActivity(intent)
        }

        refreshApp()
    }


    /**
     * 刷新应用列表
     */
    fun refreshApp() {
        thread {
            val appInfos = getAllApp(this)

            val apps = appInfos.map {
                it.hide = SpUtil.getBoolean(this, it.packageName, false)
                it
            }
            // 如果需要更新UI 回到主线程中进行处理
            runOnUiThread {
                appManagerAdapter.data = apps
            }
        }
    }
}
