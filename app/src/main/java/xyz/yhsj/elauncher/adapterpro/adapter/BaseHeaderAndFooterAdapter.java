package xyz.yhsj.elauncher.adapterpro.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 */
public class BaseHeaderAndFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int BASE_ITEM_TYPE_HEADER = 1024;
    private static final int BASE_ITEM_TYPE_FOOTER = 2048;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();

    private RecyclerView.Adapter mInnerAdapter;

    BaseHeaderAndFooterAdapter(RecyclerView.Adapter innerAdapter) {
        mInnerAdapter = innerAdapter;
    }

    public RecyclerView.Adapter getInnerAdapter() {
        return mInnerAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            return new RecyclerView.ViewHolder(mHeaderViews.get(viewType)) {
            };

        } else if (mFootViews.get(viewType) != null) {
            return new RecyclerView.ViewHolder(mFootViews.get(viewType)) {
            };
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterView(position)) {
            return mFootViews.keyAt(position - getHeadersCount() - getRealItemCount());
        }
        return mInnerAdapter.getItemViewType(position - getHeadersCount());
    }

    private int getRealItemCount() {
        return mInnerAdapter.getItemCount();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderView(position)) {
            return;
        }
        if (isFooterView(position)) {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount());
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (mHeaderViews.get(viewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    } else if (mFootViews.get(viewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position - getHeadersCount());
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        mInnerAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderView(position) || isFooterView(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    private boolean isHeaderView(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterView(int position) {
        return position >= getHeadersCount() + getRealItemCount();
    }

    public void addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public void removeHeaderView(int index) {
        mHeaderViews.delete(index + BASE_ITEM_TYPE_HEADER);
        notifyDataSetChanged();
    }

    public void addFooterView(View view) {
        mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, view);
    }

    public void removeFooterView(int index) {
        mFootViews.delete(index + BASE_ITEM_TYPE_FOOTER);
        notifyDataSetChanged();
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFootViews.size();
    }
}