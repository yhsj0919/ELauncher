package xyz.yhsj.elauncher.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_auto_run.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.adapter.AutoRunListAdapter
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.utils.getAllApp
import kotlin.concurrent.thread

class AutoRunActivity : AppCompatActivity() {

    lateinit var autoRunListAdapter: AutoRunListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_run)
        back.setOnClickListener { finish() }

        tv_package.setOnClickListener { refreshApp() }

        cb_open.isChecked = SpUtil.getBoolean(this, "AutoRun", false)

        cb_open.setOnCheckedChangeListener { _, b ->
            SpUtil.setValue(this, "AutoRun", b)
        }

        tv_package.text = SpUtil.getString(this, "AutoRun_Name", "点击选取")

        autoRunListAdapter = AutoRunListAdapter(appList)
        appList.layoutManager = LinearLayoutManager(this)
        appList.adapter = autoRunListAdapter
        //添加Android自带的分割线
        appList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        autoRunListAdapter.setOnItemClickListener { _, _, position ->
            val item = autoRunListAdapter.data[position]
            tv_package.text = item.name

            SpUtil.setValue(this, "AutoRun_Name", item.name)
            SpUtil.setValue(this, "AutoRun_Package", item.packageName)

            autoRunListAdapter.data = arrayListOf()
        }


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
            }.filter { it.packageName != this.packageName }
            // 如果需要更新UI 回到主线程中进行处理
            runOnUiThread {
                autoRunListAdapter.data = apps
            }
        }
    }
}
