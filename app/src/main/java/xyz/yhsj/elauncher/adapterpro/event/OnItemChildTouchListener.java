package xyz.yhsj.elauncher.adapterpro.event;

import android.view.MotionEvent;
import android.view.View;

import xyz.yhsj.elauncher.adapterpro.viewholder.RecyclerViewHolder;


/**

 * 描述:RecyclerView的item中子控件触摸事件监听器
 */
public interface OnItemChildTouchListener {
    boolean onItemChildTouch(RecyclerViewHolder holder, View childView, MotionEvent event);
}