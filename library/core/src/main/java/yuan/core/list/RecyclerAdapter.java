package yuan.core.list;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.List;

import yuan.core.R;

/**
 * 描述：新版RecyclerView适配器，简化使用
 * BaseViewHolder 采用 BRVAH 扩展实用行
 *
 * @author yuanye
 * @date 2019/7/18 10:55
 */
public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    //加载中动画控件id
    private static final int PROGRESS_ID = R.id.image_loading;
    private static final int PROGRESS_ANIM_ID = R.anim.anim_circle_rotate;
    private static final int PROGRESS_TAG_ID = R.id.adapter_default_tag;

    public static final int HEADER_VIEW = 0x1001;
    public static final int FOOTER_VIEW = 0x1002;

    public static final int LOADING_VIEW = R.layout.loading_view_layout;
    public static final int ERROR_VIEW = R.layout.error_view_layout;
    public static final int EMPTY_VIEW = R.layout.empty_view_layout;
    public static final int DATA_VIEW = 0x1006;

    //header footer
    private View mHeaderLayout;
    private View mFooterLayout;
    /*以上内容待完成*/
    /**
     * context
     */
    protected Context mContext;

    /**
     * recyclerView 多个RecyclerView可能使用同一个Adapter
     */
    protected RecyclerView mRecyclerView;
    /**
     * item click listener
     */
    private OnItemClickListener mItemClickListener;

    /**
     * item long click listener
     */
    private OnItemLongClickListener mItemLongClickListener;
    /**
     * 状态布局点击事件
     */
    private OnStateViewClickListener mStateViewClickListener;
    /**
     * 多类型设置器
     */
    private MultipleType mMultipleType;
    /**
     * 数据源
     */
    protected List<T> mData;
    /**
     * 是否启用自定义View：emptyView,errorView,loadingView
     */
    protected boolean isFullScreen;
    /**
     * 全屏View
     */
    private View fullScreenView;
    /**
     * 全屏View类型
     */
    private int fullScreenType = DATA_VIEW;
    /**
     * 空布局
     */
    private View mEmptyLayout;
    /**
     * 失败布局
     */
    private View mErrorLayout;
    /**
     * 加载中布局
     */
    private View mLoadingLayout;

    /**
     * 自动切换加载中/空布局等状态
     */
    private boolean autoSwitch = true;

    /**
     * 无参构造方法
     * 必须通过{@link #setData(List)}设置数据源
     * 必须通过{@link #setMultipleType(MultipleType)}}设置布局
     */
    public RecyclerAdapter(List<T> data) {
        this(data, android.R.layout.simple_list_item_1);
    }

    /**
     * 绑定绑定数据/布局文件
     *
     * @param layoutResId
     */
    public RecyclerAdapter(List<T> data, @LayoutRes final int layoutResId) {
        init(layoutResId);
        this.mData = data;
    }

    /**
     * 初始化
     */
    private void init(@LayoutRes final int layoutResId) {
        /*默认类型设置器*/
        setMultipleType(new MultipleType() {
            @Override
            public int getItemLayoutId(int position) {
                return layoutResId;
            }
        });

        /*数据发生改变时监听*/
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                //显示空布局
                if (mData.size() == 0 && isFullScreen && autoSwitch) {
                    showEmpty(false);
                }
                //显示数据
                if (mData.size() >= 0 && autoSwitch) {
                    showContent(false);
                }
            }
        });
    }

    /**
     * 设置是否可以自动数据切换空布局
     *
     * @param autoSwitch
     */
    public void setAutoSwitch(boolean autoSwitch) {
        this.autoSwitch = autoSwitch;
    }

    /**
     * 是否启用 emptyView,errorView,loadingView
     *
     * @param isFullScreen
     */
    public void setEnableFullView(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
    }

    /**
     * 设置绑定空布局
     *
     * @param mEmptyLayout
     */
    public void setEmptyLayout(View mEmptyLayout) {
        this.mEmptyLayout = mEmptyLayout;
    }

    /**
     * 设置加载失败布局
     *
     * @param mErrorLayout
     */
    public void setErrorLayout(View mErrorLayout) {
        this.mErrorLayout = mErrorLayout;
    }

    /**
     * 设置加载中布局
     *
     * @param mLoadingLayout
     */
    public void setLoadingLayout(View mLoadingLayout) {
        this.mLoadingLayout = mLoadingLayout;
    }


    /**
     * 显示空布局并自动刷新
     */
    public void showEmpty() {
        showEmpty(true);
    }

    /**
     * 显示空布局
     *
     * @param refresh 是否刷新
     */
    public void showEmpty(boolean refresh) {
        if (mEmptyLayout == null) {
            Log.e(TAG, "请先指定Empty布局");
            return;
        }
        //避免重复设置相同布局
        if (fullScreenType == EMPTY_VIEW) return;
        this.isFullScreen = true;
        this.fullScreenType = EMPTY_VIEW;
        this.fullScreenView = mEmptyLayout;
        if (refresh) notifyDataSetChanged();
    }

    /**
     * 显示加载失败布局，默认刷新
     */
    public void showError() {
        showError(true);
    }

    /**
     * 显示加载失败布局
     *
     * @param refresh 是否刷新
     */
    public void showError(boolean refresh) {
        if (mErrorLayout == null) {
            Log.e(TAG, "请先指定Empty布局");
            return;
        }
        //避免重复设置相同布局
        if (fullScreenType == ERROR_VIEW) return;

        this.isFullScreen = true;
        this.fullScreenType = ERROR_VIEW;
        this.fullScreenView = mErrorLayout;
        if (refresh) notifyDataSetChanged();
    }

    /**
     * 显示加载中布局
     */
    public void showLoading() {
        showLoading(true);
    }

    /**
     * 显示加载中布局
     *
     * @param refresh 设置加载中后是否刷新
     */
    public void showLoading(boolean refresh) {
        if (mLoadingLayout == null) {
            Log.e(TAG, "请先指定Empty布局");
            return;
        }
        //避免重复设置相同布局
        if (fullScreenType == LOADING_VIEW) return;
        this.fullScreenType = LOADING_VIEW;
        this.isFullScreen = true;
        this.fullScreenView = mLoadingLayout;
        //默认加载中布局时，开启动画
        if (mLoadingLayout.getTag(PROGRESS_TAG_ID) != null
                && mLoadingLayout.getTag(PROGRESS_TAG_ID) == TAG) {
            ImageView image = mLoadingLayout.findViewById(PROGRESS_ID);
            Animation rotateAnimation = AnimationUtils.loadAnimation(mLoadingLayout.getContext(), PROGRESS_ANIM_ID);
            LinearInterpolator interpolator = new LinearInterpolator();
            rotateAnimation.setInterpolator(interpolator);
            image.startAnimation(rotateAnimation);
        }
        if (refresh) notifyDataSetChanged();
    }

    /**
     * 显示数据
     */
    public void showContent() {
        showContent(true);
    }

    /**
     * 显示数据
     *
     * @param refresh 是否刷新
     */
    public void showContent(boolean refresh) {
        if (fullScreenType == DATA_VIEW) return;
        this.isFullScreen = false;
        this.fullScreenType = DATA_VIEW;
        if (refresh) notifyDataSetChanged();
    }


    /**
     * 设置数据源
     *
     * @param mData
     */
    public void setData(List<T> mData) {
        this.mData = mData;
    }

    /**
     * 注册多类型布局，必须在Adapter初始化之前注册才能生效
     *
     * @param multipleType
     */
    public void setMultipleType(MultipleType multipleType) {
        this.mMultipleType = multipleType;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        mRecyclerView = recyclerView;
        /* 设置默认 emptyView、errorView、loadingView */
        if (mEmptyLayout == null) {
            View emptyView = LayoutInflater.from(mContext).inflate(EMPTY_VIEW, recyclerView, false);
            //标记为默认设置布局
            emptyView.setTag(PROGRESS_TAG_ID, TAG);
            setEmptyLayout(emptyView);
        }
        if (mErrorLayout == null) {
            View errorView = LayoutInflater.from(mContext).inflate(ERROR_VIEW, recyclerView, false);
            errorView.setTag(PROGRESS_TAG_ID, TAG);
            setErrorLayout(errorView);
        }
        if (mLoadingLayout == null) {
            View loadingView = LayoutInflater.from(mContext).inflate(LOADING_VIEW, recyclerView, false);
            loadingView.setTag(PROGRESS_TAG_ID, TAG);
            setLoadingLayout(loadingView);
        }

        /*自动显示加载中动画*/
        if (autoSwitch) {
            showLoading(false);
        }

        /* 当 emptyView、errorView、loadingView布局时设置显示一行  */
        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            final GridLayoutManager.SpanSizeLookup oldSpanSizeLookup = gridManager.getSpanSizeLookup();
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isFullScreen) {
                        return gridManager.getSpanCount();
                    }
                    return oldSpanSizeLookup.getSpanSize(position);
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public final int getItemViewType(int position) {
        //针对全屏自定义View,控制类型
        if (isFullScreen) {
            return fullScreenType;
        }
        //缓存布局类型，同时也是缓存的布局类型编号，递增，从0开始,type是无序的
        Integer itemLayoutId = mMultipleType.getItemLayoutId(position);
        return itemLayoutId;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (isFullScreen) {
            /* 全屏布局  emptyView、loadingView、errorView*/
            itemView = fullScreenView;
        } else {
            /* 加载item布局 */
            itemView = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        }
        final BaseViewHolder viewHolder = new BaseViewHolder(itemView);


        /* 统一处理Item点击事件 */
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ItemClick 处理
                if (mItemClickListener != null && !isFullScreen)
                    mItemClickListener.onItemClick(viewHolder, v, viewHolder.getAdapterPosition());

                if (mStateViewClickListener != null) {
                    mStateViewClickListener.onStateViewClick(viewHolder, v, fullScreenType);
                }
            }
        });

        /*统一处理长按事件*/
        if (!isFullScreen) itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(viewHolder, v, viewHolder.getAdapterPosition());
                }
                return false;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (isFullScreen) {
            onBindFullViewHolder(holder, position);
            return;
        }
        T item = null;//绑定数据，可能为空
        if (mData != null && mData.size() > position) item = mData.get(position);
        onBindHolder(holder, item, position);
    }

    @Override
    public int getItemCount() {
        //启用全屏自定义View，返回数据集合为1
        if (isFullScreen) return 1;
        return mData != null ? mData.size() : 0;
    }

    /**
     * 设置全屏
     *
     * @param holder
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    /**
     * 绑定数据
     *
     * @param holder
     * @param position
     */
    public abstract void onBindHolder(BaseViewHolder holder, T item, int position);

    /**
     * 绑定全屏数据项
     *
     * @param holder
     */
    public void onBindFullViewHolder(BaseViewHolder holder, int position) {
    }

    /**
     * 提供外部设置点击事件
     *
     * @param listener 事件监听
     */
    public void setOnItemClick(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    /**
     * 提供外部设置长按点击事件
     *
     * @param listener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    /**
     * 设置状态布局点击事件
     *
     * @param listener
     */
    public void setStateViewClickListener(OnStateViewClickListener listener) {
        this.mStateViewClickListener = listener;
    }

    /**
     * 状态View点击事件
     * 不同布局对应对应不同的state值
     * <p>
     * 空布局
     * 加载中
     * 加载失败
     */
    public interface OnStateViewClickListener {
        void onStateViewClick(BaseViewHolder holder, View view, int state);
    }

    /**
     * Item点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(BaseViewHolder holder, View view, int position);
    }

    /**
     * Item长按事件
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(BaseViewHolder adapter, View view, int position);
    }

    /**
     * 根据position注册多类型
     */
    public interface MultipleType {
        @LayoutRes
        int getItemLayoutId(int position);
    }
}
