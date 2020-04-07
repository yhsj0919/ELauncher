package xyz.yhsj.elauncher.adapterpro.event;

import android.view.View;
import android.view.ViewGroup;

/**
 * 描述:RecyclerView的item长按事件监听器
 */
public interface OnItemLongClickListener {
    boolean onItemLongClick(ViewGroup parent, View itemView, int position);
}