package com.will.recyclerviewloadingadapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/7/17.
 * <p>一个简单的RecyclerViewAdapter</p>
 * <p>到末尾自动加载更多，加载更多失败后，即{@link #update(boolean)}返回false后,将判定为加载失败
 * 此时将在末尾展示loadingFailedView</p>
 * <p>loadingFailedView的点击默认实现为重新加载，可以通过{@link #setOnReloadListener(OnReloadClickListener)}替换</p>
 * <p>{@link #useItemAnimation(boolean)}可以使用/停用ItemAnimation.</p>
 */
public abstract class BaseLoadingAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_LOADING_FAILED = 2;
    private ArrayList<T> mData;
    private int pageIndex = 1;
    private boolean isLoading;
    private OnItemClickListener itemClickListener;
    private OnReloadClickListener reloadClickListener;
    private OnLoadingListener onLoadingListener;
    private int loadingViewRes;
    private int itemLayoutRes;
    private int loadingFailedViewRes;
    private boolean loadingSuccessful = true;
    private boolean useItemAnimation = true;
    private RecyclerView mRecyclerView;

    private int lastAnimatedItemIndex = -1;
    public BaseLoadingAdapter(@LayoutRes int itemLayoutRes,@LayoutRes int loadingViewRes,@LayoutRes int loadingFailedViewRes){
        mData = new ArrayList<>();
        this.itemLayoutRes = itemLayoutRes;
        this.loadingViewRes = loadingViewRes;
        this.loadingFailedViewRes = loadingFailedViewRes;
    }
    public BaseLoadingAdapter(@LayoutRes int itemLayoutRes){
        this(itemLayoutRes,R.layout.loading_view,R.layout.loading_failed_view);
    }

    /**
     * 返回是否有更多数据，这个返回值将影响到本次加载完后下次到末尾时是否刷新
     * @return 返回值
     */
    public abstract boolean hasMoreData();

    /**
     * <p>加载内容，当下拉至末尾时会调用此方法</p>
     * 因为是加载内容，故多为异步加载，在异步任务完成后，务必调用{@link #update(boolean)} 提交更新，无论成功与否.
     * <p>@建议：如果任务失败/完成过快会造成loadingView显示时间过短，影响视觉效果，可以用postDelayed控制update的延时来控制</p>
     * @param page 加载页数，初始值为1
     *
     */
    public abstract void loadData(int page);


    public abstract void convert(BaseRecyclerViewHolder holder, T item );

    public void load(boolean interrupt,OnLoadingListener onLoadingListener){
        this.onLoadingListener = onLoadingListener;
        if(interrupt || !isLoading){
            clear(interrupt);
            loadData(pageIndex);
        }else{
            onLoadingListener.onResult(false);
        }
    }
    public void clear(boolean interrupt){
        if(interrupt || !isLoading){
            isLoading = false;
            mData.clear();
            pageIndex = 1;
            lastAnimatedItemIndex = -1;
            notifyDataSetChanged();
        }
    }


    @Override
    public int getItemViewType(int position){
        if(hasMoreData() && position == mData.size()){
            return loadingSuccessful ? TYPE_LOADING : TYPE_LOADING_FAILED;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount(){
        return hasMoreData()&&pageIndex!=1 ? mData.size()+1 : mData.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        if(holder instanceof BaseRecyclerViewHolder){
            if( useItemAnimation && position > lastAnimatedItemIndex ){
                animate(holder.itemView);
                lastAnimatedItemIndex = position;
            }
            convert((BaseRecyclerViewHolder) holder, mData.get(position));
        }else if(holder instanceof BaseLoadingAdapter.LoadingViewHolder) {
            if(!isLoading){
                isLoading = true;
                loadData(pageIndex);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type){
        View v;
        if(type == TYPE_ITEM){
            v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes,parent,false);
            final BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        itemClickListener.onItemClicked(mData.get(holder.getAdapterPosition()),holder);
                    }
                }
            });
            return holder;
        }else if(type == TYPE_LOADING) {
            v = LayoutInflater.from(parent.getContext()).inflate(loadingViewRes,parent,false);
            return new LoadingViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(loadingFailedViewRes,parent,false);
            return new LoadingFailedViewHolder(v);
        }

    }
    public class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(View v){
            super(v);
        }
    }
    public class LoadingFailedViewHolder extends RecyclerView.ViewHolder{
        public LoadingFailedViewHolder(View v){
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reloadClickListener == null){
                        loadingSuccessful = true;
                        notifyDataSetChanged();
                    }else{
                        reloadClickListener.onReload();
                    }
                }
            });
        }
    }

    /**
     *  异步任务完成后将数据加载进data，并刷新显示
     * @param newData 新数据
     * @return 添加后data的size
     */
    public int update(List<T> newData){
        mData.addAll(newData);
        update(true);
        return mData.size();
    }

    /**
     *异步任务完成后将数据加载进data，并刷新显示
     * @param which 成功/失败,若为失败，则不递增load次数
     */
    public void update(boolean which){
        if(pageIndex == 1 && onLoadingListener != null ){
            onLoadingListener.onResult(which);
        }
        isLoading = false;
        loadingSuccessful = which;
        if(which){
            pageIndex++;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                if(!loadingSuccessful && pageIndex != 1){
                    mRecyclerView.smoothScrollToPosition(getItemCount()-1);
                }
            }
        });
    }
    public RecyclerView getRecyclerView(){
        return mRecyclerView;
    }
    public List<T> getData(){
        return mData;
    }

    public int getPageIndex(){
        return pageIndex;
    }
    public void setPageIndex(int i ){
        pageIndex = i;
    }


    /**
     * 是否正在加载，配合swipeRefreshLayout时调用此方法，避免加载冲突
     * @return isLoading
     */
    public boolean isLoading(){
        return isLoading;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }

    /**
     * 点击OnFailureView时的回调
     * @param listener listener
     */
    public void setOnReloadListener(OnReloadClickListener listener){
        this.reloadClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClicked(Object item, BaseRecyclerViewHolder holder);
    }

    public interface OnReloadClickListener{
        void onReload();
    }
    public interface OnLoadingListener{
        void onResult(boolean which);
    }

    /**
     * 为item添加动画。此处使用了延时队列，主要是为了保证最先出现的几个ItemAnimation的次序性
     * <p>但同时的，因为有延时存在，会出现ItemView已经展示完毕动画才开始的情况，故在将任务加入队列时隐藏该view,在队列任务执行时
     * 再将ItemView设为可见.</p>
     * <p>不过我总觉得这可能会出现一些问题...</p>
     * @param view 将要执行动画的View
     */
    private void animate(final View view){
        view.setVisibility(View.INVISIBLE);
        getRecyclerView().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.startAnimation(getItemAnimation());
                view.setVisibility(View.VISIBLE);
            }
        },50);
    }

    /**
     * 重写本方法可使用自定义itemAnimation
     * @return animation
     */
    protected Animation getItemAnimation() {
        Animation animation = new AlphaAnimation(0f,1f);
        animation.setDuration(500);
        return animation;
    }

    /**
     * 是否使用ItemAnimation
     */
    public void useItemAnimation(boolean which){
        useItemAnimation = which;
    }
}
