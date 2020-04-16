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
class AppManagerAdapter(recyclerView: RecyclerView) :
    BaseRecyclerViewAdapter<AppInfo>(recyclerView, R.layout.item_app_manager) {

    override fun bindItemChildEvent(helper: ViewHolderHelper, viewType: Int) {
        helper.setItemChildClickListener(R.id.hide)
    }

    override fun bindData(helper: ViewHolderHelper, position: Int, model: AppInfo) {
        helper.setText(R.id.name, model.name + "  " + model.version)
        helper.setText(R.id.version, model.packageName)
        if (model.hide) {
            helper.setImageResource(R.id.hide, R.mipmap.ic_invisible)
        } else {
            helper.setImageResource(R.id.hide, R.mipmap.ic_visible)
        }
        helper.setImageDrawable(R.id.icon, model.icon)
    }
}