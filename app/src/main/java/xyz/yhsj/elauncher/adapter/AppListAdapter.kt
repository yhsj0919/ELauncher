package xyz.yhsj.elauncher.adapter

import androidx.recyclerview.widget.RecyclerView
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.adapterpro.adapter.BaseRecyclerViewAdapter
import xyz.yhsj.elauncher.adapterpro.helper.ViewHolderHelper
import xyz.yhsj.elauncher.bean.AppInfo

/**
 * 列表适配器
 */
class AppListAdapter(recyclerView: RecyclerView) :
    BaseRecyclerViewAdapter<AppInfo>(recyclerView, R.layout.item_app_list) {
    override fun bindData(helper: ViewHolderHelper, position: Int, model: AppInfo) {
        helper.setText(R.id.name, model.name)

        if (model.isApp) {
            helper.setImageDrawable(R.id.icon, model.icon)
        } else {

            when (model.packageName) {
                "setting" -> helper.setImageResource(R.id.icon, R.mipmap.ic_setting)
                "clear" -> helper.setImageResource(R.id.icon, R.mipmap.ic_clear)
                else -> helper.setImageResource(R.id.icon, R.mipmap.ic_launcher)
            }
        }

    }
}