package com.will.recyclerviewloadingadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by will on 2017/5/13.
 */

public abstract class AsyncLoadingAdapter<T> extends BaseLoadingAdapter<T> {
    private final OkHttpClient mClient;
    private final OnAsyncTaskListener mAsyncTaskListener = new OnAsyncTaskListener();
    private final WeakHashMap<Call,Boolean> mCalls = new WeakHashMap<>();
    private final Handler mHandler = new Handler(Looper.myLooper());
    public AsyncLoadingAdapter(@LayoutRes int itemRes, OkHttpClient client){
        super(itemRes);
        mClient = client;
    }
    public AsyncLoadingAdapter(@LayoutRes int itemRes,@LayoutRes int loadingRes,@LayoutRes int loadingFailedRes,OkHttpClient client){
        super(itemRes,loadingRes,loadingFailedRes);
        mClient = client;
    }

    public abstract String getTargetUrl(int page);
    public abstract List<T> getCorrespondingData(Response response);

    @Override
    public void loadData(int page) {
        Request request = new Request.Builder().url(getTargetUrl(page)).build();
        Call call = mClient.newCall(request);
        mCalls.put(call,true);
        call.enqueue(mAsyncTaskListener);
    }

    @Override
    public void load(boolean interrupt, OnLoadingListener onLoadingListener) {
        if(interrupt){
            destroyRunningTasks();
        }
        super.load(interrupt, onLoadingListener);
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
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCalls.remove(call);
                    update(false);
                }
            });

        }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                   mCalls.remove(call);
                   update(getCorrespondingData(response));
               }
           });
        }
    }
}
