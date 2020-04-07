package xyz.yhsj.elauncher.adapterpro.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.yhsj.elauncher.adapterpro.event.OnItemChildCheckedChangeListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemChildClickListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemChildLongClickListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemChildTouchListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemClickListener;
import xyz.yhsj.elauncher.adapterpro.event.OnItemLongClickListener;
import xyz.yhsj.elauncher.adapterpro.helper.ViewHolderHelper;
import xyz.yhsj.elauncher.adapterpro.viewholder.RecyclerViewHolder;


/**
 * @param <M> 适配的数据类型
 */
public abstract class BaseRecyclerViewAdapter<M> extends RecyclerView.Adapter<RecyclerViewHolder> {
    protected int mDefaultItemLayoutId;
    protected Context mContext;
    protected List<M> mData;
    protected OnItemChildClickListener mOnItemChildClickListener;
    protected OnItemChildLongClickListener mOnItemChildLongClickListener;
    protected OnItemChildCheckedChangeListener mOnItemChildCheckedChangeListener;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;
    protected OnItemChildTouchListener mOnItemChildTouchListener;
    protected BaseHeaderAndFooterAdapter mHeaderAndFooterAdapter;

    protected RecyclerView mRecyclerView;


    /**
     * 扩充数据，用于某些特殊的多数据源场景
     */
    protected HashMap<String, Object> mObj;

    /**
     * 在填充数据列表时，忽略选中状态变化
     */
    private boolean mIsIgnoreCheckedChanged = true;

    public BaseRecyclerViewAdapter(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mContext = mRecyclerView.getContext();
        mData = new ArrayList<>();
        mObj = new HashMap<>();
    }

    public BaseRecyclerViewAdapter(RecyclerView recyclerView, int defaultItemLayoutId) {
        this(recyclerView);
        mDefaultItemLayoutId = defaultItemLayoutId;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        RecyclerViewHolder viewHolder = new RecyclerViewHolder(this, mRecyclerView, LayoutInflater.from(mContext).inflate(viewType, parent, false), mOnItemClickListener, mOnItemLongClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildClickListener(mOnItemChildClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildLongClickListener(mOnItemChildLongClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildCheckedChangeListener(mOnItemChildCheckedChangeListener);
        viewHolder.getViewHolderHelper().setOnItemChildTouchListener(mOnItemChildTouchListener);
        bindItemChildEvent(viewHolder.getViewHolderHelper(), viewType);
        return viewHolder;
    }

    /**
     * 为item的孩子节点设置监听器，并不是每一个数据列表都要为item的子控件添加事件监听器，所以这里采用了空实现，需要设置事件监听器时重写该方法即可
     */
    protected void bindItemChildEvent(ViewHolderHelper helper, int viewType) {
    }

    @Override
    public int getItemViewType(int position) {
        if (mDefaultItemLayoutId == 0) {
            throw new RuntimeException("请在 " + this.getClass().getSimpleName() + " 中重写 getItemViewType 方法返回布局资源 id，或者使用 BaseRecyclerViewAdapter 两个参数的构造方法 BaseRecyclerViewAdapter(RecyclerView recyclerView, int itemLayoutId)");
        }
        return mDefaultItemLayoutId;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder viewHolder, int position) {
        // 在设置值的过程中忽略选中状态变化
        mIsIgnoreCheckedChanged = true;

        bindData(viewHolder.getViewHolderHelper(), position, getItem(position));

        mIsIgnoreCheckedChanged = false;
    }

    /**
     * 填充item数据
     */
    protected abstract void bindData(ViewHolderHelper helper, int position, M model);

    public boolean isIgnoreCheckedChanged() {
        return mIsIgnoreCheckedChanged;
    }

    /**
     * 设置item的点击事件监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 设置item的长按事件监听器
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 设置item中的子控件点击事件监听器
     */
    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    /**
     * 设置item中的子控件长按事件监听器
     */
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener onItemChildLongClickListener) {
        mOnItemChildLongClickListener = onItemChildLongClickListener;
    }

    /**
     * 设置item子控件选中状态变化事件监听器
     */
    public void setOnItemChildCheckedChangeListener(OnItemChildCheckedChangeListener onItemChildCheckedChangeListener) {
        mOnItemChildCheckedChangeListener = onItemChildCheckedChangeListener;
    }

    /**
     * 设置item子控件触摸事件监听器
     */
    public void setOnItemChildTouchListener(OnItemChildTouchListener onItemChildTouchListener) {
        mOnItemChildTouchListener = onItemChildTouchListener;
    }

    /**
     * 获取指定索引位置的数据模型
     */
    public M getItem(int position) {
        return mData.get(position);
    }

    /**
     * 获取数据集合
     */
    public List<M> getData() {
        return mData;
    }

    public final void notifyItemRangeInsertedWrapper(int positionStart, int itemCount) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRangeInserted(positionStart, itemCount);
        } else {
            mHeaderAndFooterAdapter.notifyItemRangeInserted(mHeaderAndFooterAdapter.getHeadersCount() + positionStart, itemCount);
        }
    }

    /**
     * 在集合头部添加新的数据集合（下拉从服务器获取最新的数据集合，例如新浪微博加载最新的几条微博数据）
     */
    public void addNewData(List<M> data) {
        if (data != null) {
            mData.addAll(0, data);
            notifyItemRangeInsertedWrapper(0, data.size());
        }
    }

    /**
     * 在集合尾部添加更多数据集合（上拉从服务器获取更多的数据集合，例如新浪微博列表上拉加载更晚时间发布的微博数据）
     */
    public void addMoreData(List<M> data) {
        if (data != null) {
            mData.addAll(mData.size(), data);
            notifyItemRangeInsertedWrapper(mData.size(), data.size());
        }
    }

    public final void notifyDataSetChangedWrapper() {
        if (mHeaderAndFooterAdapter == null) {
            notifyDataSetChanged();
        } else {
            mHeaderAndFooterAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置全新的数据集合，如果传入null，则清空数据列表（第一次从服务器加载数据，或者下拉刷新当前界面数据表）
     */
    public void setData(List<M> data) {
        if (data != null) {
            mData = data;
        } else {
            mData.clear();
        }
        notifyDataSetChangedWrapper();
    }

    /**
     * 清空数据列表
     */
    public void clear() {
        mData.clear();
        notifyDataSetChangedWrapper();
    }

    public final void notifyItemRemovedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRemoved(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemRemoved(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 删除指定索引数据条目
     */
    public void removeItem(int position) {
        mData.remove(position);
        notifyItemRemovedWrapper(position);
    }

    /**
     * 删除指定数据条目。该方法在 ItemTouchHelper.Callback 的 onSwiped 方法中调用
     */
    public void removeItem(RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (mHeaderAndFooterAdapter != null) {
            mData.remove(position - mHeaderAndFooterAdapter.getHeadersCount());
            mHeaderAndFooterAdapter.notifyItemRemoved(position);
        } else {
            removeItem(position);
        }
    }

    /**
     * 删除指定数据条目
     */
    public void removeItem(M model) {
        removeItem(mData.indexOf(model));
    }

    public final void notifyItemInsertedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemInserted(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemInserted(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 在指定位置添加数据条目
     */
    public void addItem(int position, M model) {
        mData.add(position, model);
        notifyItemInsertedWrapper(position);
    }

    /**
     * 在集合头部添加数据条目
     */
    public void addFirstItem(M model) {
        addItem(0, model);
    }

    /**
     * 在集合末尾添加数据条目
     */
    public void addLastItem(M model) {
        addItem(mData.size(), model);
    }

    public final void notifyItemChangedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemChanged(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemChanged(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 替换指定索引的数据条目
     */
    public void setItem(int location, M newModel) {
        mData.set(location, newModel);
        notifyItemChangedWrapper(location);
    }

    /**
     * 替换指定数据条目
     */
    public void setItem(M oldModel, M newModel) {
        setItem(mData.indexOf(oldModel), newModel);
    }

    public final void notifyItemMovedWrapper(int fromPosition, int toPosition) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemMoved(fromPosition, toPosition);
        } else {
            mHeaderAndFooterAdapter.notifyItemMoved(mHeaderAndFooterAdapter.getHeadersCount() + fromPosition, mHeaderAndFooterAdapter.getHeadersCount() + toPosition);
        }
    }

    /**
     * 移动数据条目的位置
     */
    public void moveItem(int fromPosition, int toPosition) {
        notifyItemChangedWrapper(fromPosition);
        notifyItemChangedWrapper(toPosition);

        // 要先执行上面的 notifyItemChanged,然后再执行下面的 moveItem 操作

        mData.add(toPosition, mData.remove(fromPosition));
        notifyItemMovedWrapper(fromPosition, toPosition);
    }

    /**
     * 移动数据条目的位置。该方法在 ItemTouchHelper.Callback 的 onMove 方法中调用
     */
    public void moveItem(RecyclerView.ViewHolder from, RecyclerView.ViewHolder to) {
        int fromPosition = from.getAdapterPosition();
        int toPosition = to.getAdapterPosition();
        if (mHeaderAndFooterAdapter != null) {
            mHeaderAndFooterAdapter.notifyItemChanged(fromPosition);
            mHeaderAndFooterAdapter.notifyItemChanged(toPosition);

            // 要先执行上面的 notifyItemChanged,然后再执行下面的 moveItem 操作

            mData.add(toPosition - mHeaderAndFooterAdapter.getHeadersCount(), mData.remove(fromPosition - mHeaderAndFooterAdapter.getHeadersCount()));
            mHeaderAndFooterAdapter.notifyItemMoved(fromPosition, toPosition);
        } else {
            moveItem(fromPosition, toPosition);
        }
    }

    /**
     * @return 获取第一个数据模型
     */
    public
    @Nullable
    M getFirstItem() {
        return getItemCount() > 0 ? getItem(0) : null;
    }

    /**
     * @return 获取最后一个数据模型
     */
    public
    @Nullable
    M getLastItem() {
        return getItemCount() > 0 ? getItem(getItemCount() - 1) : null;
    }


    public void addHeaderView(View headerView) {
        getHeaderAndFooterAdapter().addHeaderView(headerView);
    }

    public void removeHeaderView(int index) {
        getHeaderAndFooterAdapter().removeHeaderView(index);
    }


    public void addFooterView(View footerView) {
        getHeaderAndFooterAdapter().addFooterView(footerView);
    }

    public void removeFooterView(int index) {
        getHeaderAndFooterAdapter().removeFooterView(index);
    }

    public int getHeadersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getHeadersCount();
    }

    public int getFootersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getFootersCount();
    }

    public BaseHeaderAndFooterAdapter getHeaderAndFooterAdapter() {
        if (mHeaderAndFooterAdapter == null) {
            synchronized (BaseRecyclerViewAdapter.this) {
                if (mHeaderAndFooterAdapter == null) {
                    mHeaderAndFooterAdapter = new BaseHeaderAndFooterAdapter(this);
                }
            }
        }
        return mHeaderAndFooterAdapter;
    }

    /**
     * 是否是头部或尾部
     */
    public boolean isHeaderOrFooter(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition() < getHeadersCount() || viewHolder.getAdapterPosition() >= getHeadersCount() + getItemCount();
    }

    public Object getmObj(String key) {
        return mObj.get(key);
    }

    public void addmObj(String key, Object mObj) {
        this.mObj.put(key, mObj);
    }
}