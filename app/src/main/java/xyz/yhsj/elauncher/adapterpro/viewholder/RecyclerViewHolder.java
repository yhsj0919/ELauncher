package xyz.yhsj.elauncher.adapterpro.viewholder;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import xyz.yhsj.elauncher.adapterpro.adapter.BaseRecyclerViewAdapter;
import xyz.yhsj.elauncher.adapterpro.event.OnItemClickListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemLongClickListener;
import xyz.yhsj.elauncher.adapterpro.event.OnNoDoubleClickListener;
import xyz.yhsj.elauncher.adapterpro.helper.ViewHolderHelper;


/**
 * 描述:适用于RecyclerView的item的ViewHolder
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
    protected Context mContext;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;
    protected ViewHolderHelper mViewHolderHelper;
    protected RecyclerView mRecyclerView;
    protected BaseRecyclerViewAdapter mRecyclerViewAdapter;

    public RecyclerViewHolder(BaseRecyclerViewAdapter recyclerViewAdapter, RecyclerView recyclerView, View itemView, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        super(itemView);
        mRecyclerViewAdapter = recyclerViewAdapter;
        mRecyclerView = recyclerView;
        mContext = mRecyclerView.getContext();
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;
        itemView.setOnClickListener(new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (v.getId() == RecyclerViewHolder.this.itemView.getId() && null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(mRecyclerView, v, getAdapterPositionWrapper());
                }
            }
        });
        itemView.setOnLongClickListener(this);

        mViewHolderHelper = new ViewHolderHelper(mRecyclerView, this);
    }

    public ViewHolderHelper getViewHolderHelper() {
        return mViewHolderHelper;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == this.itemView.getId() && null != mOnItemLongClickListener) {
            return mOnItemLongClickListener.onItemLongClick(mRecyclerView, v, getAdapterPositionWrapper());
        }
        return false;
    }

    public int getAdapterPositionWrapper() {
        if (mRecyclerViewAdapter.getHeadersCount() > 0) {
            return getAdapterPosition() - mRecyclerViewAdapter.getHeadersCount();
        } else {
            return getAdapterPosition();
        }
    }
}