package com.will.example;

import android.view.animation.Animation;
import android.widget.Toast;

import com.will.recyclerviewloadingadapter.BaseLoadingAdapter;
import com.will.recyclerviewloadingadapter.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by will on 2016/10/28.
 */

public class ExampleAdapter extends BaseLoadingAdapter<String> {

    public ExampleAdapter(){
        super(R.layout.item);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(Object item, BaseRecyclerViewHolder holder) {
                Toast.makeText(getRecyclerView().getContext(),(String)item,Toast.LENGTH_SHORT).show();
            }
        });
    }
    //数据与相应的view绑定
    @Override
    public void convert(BaseRecyclerViewHolder holder, String item) {
        holder.setText(R.id.item_text,item);
    }

    //无论加载结果如何，都应调用update方法
    @Override
    public void loadData(final int page) {
        getRecyclerView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(new Random().nextInt(99)%2 > 0){
                    update(getTestData(page));
                }else{
                    update(false);
                }
            }
        },1000);
    }

    //是否继续调用loadData方法加载
    @Override
    public boolean hasMoreData() {
        return getPageIndex()*20 <= 100;
    }
    private List<String> getTestData(int page){
        List<String > list = new ArrayList<>();
        for(int i = (page - 1)*20;i<page*20;i++){
            list.add(String.valueOf(i));
        }
        return list;
    }

    @Override
    protected Animation getItemAnimation() {
        //自定义animation
        return super.getItemAnimation();
    }
}
