
package xyz.yhsj.elauncher.adapterpro.event;

import android.view.View;
import android.view.ViewGroup;

/**
 * 描述:RecyclerView的item点击事件监听器
 */
public interface OnItemClickListener {
    void onItemClick(ViewGroup parent, View itemView, int position);
}