package xyz.yhsj.elauncher.adapterpro.event;

import android.view.View;
import android.view.ViewGroup;

/**
 * 描述:AdapterView和RecyclerView的item中子控件长按事件监听器
 */
public interface OnItemChildLongClickListener {
    boolean onItemChildLongClick(ViewGroup parent, View childView, int position);
}