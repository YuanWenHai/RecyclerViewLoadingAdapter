# RecyclerViewLoadingAdapter
一个支持上滑加载更多，Item加载动画的RecyclerAdapter

![image](https://github.com/YuanWenHai/RecyclerViewLoadingAdapter/blob/master/sceenshot/img.gif)

## 基本使用
```java
public class ExampleAdapter extends LoadingAdapter<String> {

    public ExampleAdapter(){
        super(R.layout.item);
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

    //返回false停止加载，在这里我们加载100个item便不再加载
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

}
```
## Item动画
```
  adapter.useItemAnimation(true);//启用Item动画，默认为开启
```
```
  @Override
    protected Animation getItemAnimation() {
        //重写此方法，返回自定义animation
        return super.getItemAnimation();
    }
```
## Loading&LoadingFailed View
```
new LoadingAdapter(R.layout.item);//使用默认加载和失败view
new LoadingAdapter(R.layout.item,R.layout.loading,R.layout.loading_failed);//使用自定义
```



## Dependency
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
  ```
  Add the dependency:
  ```
  dependencies {
	        compile 'com.github.YuanWenHai:RecyclerViewLoadingAdapter:1.1'
	}
  ```

