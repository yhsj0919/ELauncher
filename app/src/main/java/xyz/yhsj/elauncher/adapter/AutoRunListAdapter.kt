package xyz.yhsj.elauncher.adapter

import androidx.recyclerview.widget.RecyclerView
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.adapterpro.adapter.BaseRecyclerViewAdapter
import xyz.yhsj.elauncher.adapterpro.helper.ViewHolderHelper
import xyz.yhsj.elauncher.bean.AppInfo
import xyz.yhsj.elauncher.utils.SpUtil

/**
 * 列表适配器
 */
class AutoRunListAdapter(recyclerView: RecyclerView) :
    BaseRecyclerViewAdapter<AppInfo>(recyclerView, R.layout.item_auto_run_list) {


    override fun bindData(helper: ViewHolderHelper, position: Int, model: AppInfo) {
        helper.setText(R.id.name, model.name)
        helper.setText(R.id.version, model.version)
        helper.setImageDrawable(R.id.icon, model.icon)
    }
}