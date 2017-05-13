package com.will.recyclerviewloadingadapter;

import java.io.IOException;
import java.util.ArrayList;
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
    public AsyncLoadingAdapter(int itemRes,OkHttpClient client){
        super(itemRes);
        mClient = client;
    }
    public AsyncLoadingAdapter(int itemRes,int loadingRes,int loadingFailedRes,OkHttpClient client){
        super(itemRes,loadingRes,loadingFailedRes);
        mClient = client;
    }

    public abstract String getTargetUrl(int page);
    public abstract ArrayList<T> getCorrespondingData(Response response);

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
        public void onFailure(Call call, IOException e) {
            mCalls.remove(call);
            update(false);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            mCalls.remove(call);
            update(getCorrespondingData(response));
        }
    }
}
