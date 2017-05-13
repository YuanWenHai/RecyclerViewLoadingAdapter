package com.will.example;

import android.util.Log;

import com.will.recyclerviewloadingadapter.AsyncLoadingAdapter;
import com.will.recyclerviewloadingadapter.BaseRecyclerViewHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Will on 2017/5/13.
 */

public class AsyncAdapter extends AsyncLoadingAdapter<String> {
    private  static final String HOST = "http://c.m.163.com/nc/article/local/5aSq5Y6f/";
    private static final String SUFFIX = "-5.html";
    public AsyncAdapter(){
        super(R.layout.item,new OkHttpClient());
    }

    @Override
    public String getTargetUrl(int page) {
        return HOST+page*5+SUFFIX;
    }

    @Override
    public List<String> getCorrespondingData(Response response){
        List<String> list = new ArrayList<>();
        try{
            String content = response.body().string();
            Log.e("content",content);
            JSONObject obj = new JSONObject(content);
            if(obj.has("太原")){
                JSONArray array = obj.getJSONArray("太原");
                for(int a=0;a<array.length();a++){
                    list.add(array.getJSONObject(a).toString());
                }
            }

        }
        catch (Exception e){
            list.add("解析错误");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean hasMoreData() {
        return getPageIndex() < 10;
    }

    @Override
    public void convert(BaseRecyclerViewHolder holder, String item) {
        holder.setText(R.id.item_text,item);
    }
}
