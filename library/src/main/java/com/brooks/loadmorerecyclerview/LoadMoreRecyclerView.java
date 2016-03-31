package com.brooks.loadmorerecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadMoreRecyclerView extends RecyclerView {

    public static final String TAG = LoadMoreRecyclerView.class.getSimpleName();

    //头部视图
    public final static int TYPE_HEADER = 1;

    //底部--往往是loading_more
    public final static int TYPE_FOOTER = 2;

    //代表item展示的模式是list模式
    public final static int TYPE_LIST = 3;


    //是否允许加载更多
    private boolean mIsFooterEnable;

    //true是自动加载模式, false手动加载模式
    private boolean isAutoLoadMore = true;

    //自定义实现了头部和底部加载更多的adapter
    private AutoLoadAdapter mAutoLoadAdapter;

    /**
     * 标记是否正在加载更多，防止再次调用加载更多接口
     */
    private boolean mIsLoadingMore;

    /**
     * 标记加载更多的position
     */
    private int mLastVisibleItemPosition;

    /**
     * 加载更多的监听-业务需要实现加载数据
     */
    private LoadMoreListener mLoadMoreListener;

    //加载更多按钮
    private TextView btnLoadMore;

    //加载进度
    private ProgressBar progressBar;

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化-添加滚动监听
     * 回调加载更多方法，前提是
     * 1、有监听并且支持加载更多：null != 6 7 8 9 10 11 12 1 2 3 4 5 6 7 8 9 10 mLoadMoreListener && mIsFooterEnable
     * 2、目前没有在加载，正在上拉（dy>0），当前最后一条可见的view是否是当前数据列表的最后一条--及加载更多
     */
    private void init() {
        super.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mLoadMoreListener != null && mIsFooterEnable && !mIsLoadingMore && dy >= 0) {
                    int lastVisibleItemPosition = findLastVisibleItemPosition();
                    if (lastVisibleItemPosition + 1 == mAutoLoadAdapter.getItemCount() && isAutoLoadMore) {
                        setLoadingMore(true);
                        mLastVisibleItemPosition = lastVisibleItemPosition;
                        Log.i(TAG, "onScrolled onLoadMore dx:" + dx + " dy:" + dy + " lastVisibleItemPosition " + lastVisibleItemPosition);
                        mLoadMoreListener.onLoadMore();
                    }
                }
            }
        });
    }


    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mAutoLoadAdapter = new AutoLoadAdapter(adapter);
            adapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    mAutoLoadAdapter.notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    mAutoLoadAdapter.notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    mAutoLoadAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    mAutoLoadAdapter.notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    mAutoLoadAdapter.notifyItemRangeRemoved(positionStart, itemCount);
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    mAutoLoadAdapter.notifyItemRangeChanged(fromPosition, toPosition, itemCount);
                }
            });
        }
        super.swapAdapter(mAutoLoadAdapter, true);
    }

    /**
     * 设置加载更多的监听
     *
     * @param listener
     */
    public void setLoadMoreListener(LoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    /**
     * 设置正在加载更多
     *
     * @param loadingMore
     */
    public void setLoadingMore(boolean loadingMore) {
        this.mIsLoadingMore = loadingMore;
    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    private int findLastVisibleItemPosition() {
        return ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
    }

    public void setAutoLoadMore(boolean isAutoLoadMore) {
        this.isAutoLoadMore = isAutoLoadMore;
    }

    /**
     * 设置是否支持自动加载更多
     *
     * @param loadMoreEnable
     */
    public void setLoadMoreEnable(boolean loadMoreEnable) {
        mIsFooterEnable = loadMoreEnable;
    }

    /**
     * 通知更多的数据已经加载
     * <p/>
     * 每次加载完成之后添加了Data数据，用notifyItemRemoved来刷新列表展示，
     * 而不是用notifyDataSetChanged来刷新列表
     *
     * @param hasMore
     */
    public void notifyMoreFinish(boolean hasMore) {
        setLoadMoreEnable(hasMore);
        if (isAutoLoadMore) {
            getAdapter().notifyItemRemoved(mLastVisibleItemPosition);
        } else {
            getAdapter().notifyItemChanged(mLastVisibleItemPosition + 1);
            //getAdapter().notifyDataSetChanged();
            handleCallback();
        }
        mIsLoadingMore = false;
    }


    public interface LoadMoreListener {
        void onLoadMore();
    }

    public interface OnRecyclerViewListener {

        void onItemClick(View v, int position);

        boolean onItemLongClick(View v, int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    /**
     * 自动加载的适配器
     */
    public class AutoLoadAdapter extends Adapter<ViewHolder> {

        /**
         * 数据adapter
         */
        private Adapter mInternalAdapter;

        public AutoLoadAdapter(Adapter adapter) {
            mInternalAdapter = adapter;
        }

        @Override
        public int getItemViewType(int position) {
            int type = -1;
            String s = "";
            int footerPosition = getItemCount() - 1;
            if (hasHeaderView() && position == 0) {
                type = TYPE_HEADER;
                s = "header";
            } else if (footerPosition == position && mIsFooterEnable) {
                type = TYPE_FOOTER;
                s = "footer";
            } else {
                type = TYPE_LIST;
                s = "list";
            }
            Log.i(TAG, "getItemViewType " + s + "  " + position);
            return type;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new HeaderViewHolder(headerView);
            } else if (viewType == TYPE_FOOTER) {
                return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_foot_loadmore, parent, false));
            } else {
                final ViewHolder holder = mInternalAdapter.onCreateViewHolder(parent, viewType);
                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (onRecyclerViewListener != null) {
                            onRecyclerViewListener.onItemClick(v, holder.getAdapterPosition());
                        }
                    }
                });

                holder.itemView.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        if (onRecyclerViewListener != null) {
                            return onRecyclerViewListener.onItemLongClick(v, holder.getAdapterPosition());
                        }
                        return false;
                    }
                });
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == TYPE_LIST) {
                if (hasHeaderView()) {
                    position--;
                }
                Log.i(TAG, "onBindViewHolder " + position);
                mInternalAdapter.onBindViewHolder(holder, position);
            }
        }

        /**
         * 需要计算上加载更多
         */
        @Override
        public int getItemCount() {
            int count = mInternalAdapter.getItemCount();
            if (hasHeaderView()) {
                count++;
            }
            if (mIsFooterEnable) {
                count++;
            }
//            Log.i(TAG, "getItemCount " + count);
            return count;
        }

        public class FooterViewHolder extends ViewHolder {
            public FooterViewHolder(View itemView) {
                super(itemView);
                btnLoadMore = (TextView) itemView.findViewById(R.id.btn_loadmore);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
                btnLoadMore.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!mIsLoadingMore) {
                            setLoadingMore(true);
                            progressBar.setVisibility(VISIBLE);
                            btnLoadMore.setVisibility(GONE);
                            if (mLoadMoreListener != null) {
                                mLoadMoreListener.onLoadMore();
                            }
                        }
                    }
                });
                if (isAutoLoadMore) {
                    progressBar.setVisibility(VISIBLE);
                    btnLoadMore.setVisibility(GONE);
                } else {
                    progressBar.setVisibility(GONE);
                    btnLoadMore.setVisibility(VISIBLE);
                }
            }
        }

        public class HeaderViewHolder extends ViewHolder {

            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
    }

    private View headerView;

    public boolean hasHeaderView() {
        return headerView != null;
    }

    /**
     * 处理回调，使loadmore显示和progressBar隐藏
     */
    private void handleCallback() {
        progressBar.setVisibility(GONE);
        btnLoadMore.setVisibility(VISIBLE);
    }

    private LoadState loadState = LoadState.HasMore;

    public enum LoadState {
        HasMore, NoMore, LoadFaild
    }
}
