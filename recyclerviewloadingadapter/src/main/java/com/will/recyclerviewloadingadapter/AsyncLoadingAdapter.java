package com.will.recyclerviewloadingadapter;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by will on 2017/5/13.
 */

public abstract class AsyncLoadingAdapter<T> extends BaseLoadingAdapter<T> {
    private final OnAsyncTaskListener mAsyncTaskListener = new OnAsyncTaskListener();
    private final WeakHashMap<Call,Boolean> mCalls = new WeakHashMap<>();
    private final Handler mMainThreadHandler = new Handler(Looper.myLooper());
    private Handler mWorkerThreadHandler;
    private HandlerThread mWorkerThread;
    public AsyncLoadingAdapter(@LayoutRes int itemRes){
        super(itemRes);
    }
    public AsyncLoadingAdapter(@LayoutRes int itemRes,@LayoutRes int loadingRes,@LayoutRes int loadingFailedRes){
        super(itemRes,loadingRes,loadingFailedRes);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mWorkerThread = new HandlerThread("asyncLoadingWorkerThread");
        mWorkerThread.start();
        mWorkerThreadHandler = new Handler(mWorkerThread.getLooper());
    }

    @Override
    public void load(boolean interrupt, OnLoadingListener onLoadingListener) {
        //getRecyclerView方法在adapter未attach到RecyclerView之前返回值为null
        if(getRecyclerView() == null){
            return;
        }
        super.load(interrupt, onLoadingListener);
    }

    /**
     * 因为这里使用了HandlerThread来处理网络任务，所以应当在RecyclerView的宿主中主动调用此方法来释放WorkerThread.
     */
    public void recycle(){
        mWorkerThread.quit();
    }
    public abstract Call obtainTargetCall(int page);
    public abstract List<T> getCorrespondingData(Response response);
    @Override
    public void loadData(int page) {
        final Call call = obtainTargetCall(page);
        mCalls.put(call,true);
        mWorkerThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    final Response response = call.execute();
                    mAsyncTaskListener.onResponse(call,response);
                }catch (IOException i){
                    mAsyncTaskListener.onFailure(call,i);
                }
            }
        });
    }

    /**
     * load 方法调用了clear，重写clear加入清除网络任务逻辑
     * @param interrupt
     */
    @Override
    public void clear(boolean interrupt){
        super.clear(interrupt);
        if(interrupt || !isLoading()){
            destroyRunningTasks();
        }
    }
    private void destroyRunningTasks(){
        for(Call call :mCalls.keySet()){
            if(!call.isCanceled() && !call.isExecuted()){
                call.cancel();
            }
        }
    }

    private class OnAsyncTaskListener implements Callback{
        @Override
        public void onFailure(final Call call, IOException e) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCalls.remove(call);

                    update(false);
                }
            });

        }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {
           mMainThreadHandler.post(new Runnable() {
               @Override
               public void run() {
                   mCalls.remove(call);
                   update(getCorrespondingData(response));
               }
           });
        }
    }
}
