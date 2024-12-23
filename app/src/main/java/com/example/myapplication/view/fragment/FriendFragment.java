package com.example.myapplication.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.util.Common;
import com.example.myapplication.view.AskActivity;
import com.example.myapplication.view.adapter.AskListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FriendFragment extends Fragment {
    private ListView listView;
    private ListView listView_new;
    private View tabView;
    private String phone;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Gson gson = new Gson();
    public ArrayList<ListOfTarget> targetList = new ArrayList<>();  // 存储我关注的人
    public ArrayList<ListOfTarget> sourceList = new ArrayList<>();  // 存储关注我的人
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapterFans;

    public static class ListOfTarget {
        public String TargetName;
        public String Target;
        public String imageBytes;

        public byte[] getImageBytes() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getDecoder().decode(imageBytes);
            }
            return new byte[0];
        }
    }

    public void calculateHeight(){
        // 若 adapter 不为空, 计算列表视图的总高度
        if (adapter != null) {
            int totalHeight = 0;
            int itemCount = adapter.getCount();
            for (int i = 0; i < itemCount; i++) {
                View listItem = adapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (itemCount - 1));
            listView.setLayoutParams(params);
        }
        // 若 adapterFans 不为空, 计算列表视图的总高度
        if (adapterFans != null) {
            int totalHeight = 0;
            int itemCount = adapterFans.getCount();
            for (int i = 0; i < itemCount; i++) {
                View listItem = adapterFans.getView(i, null, listView_new);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView_new.getLayoutParams();
            params.height = totalHeight + (listView_new.getDividerHeight() * (itemCount - 1));
            listView_new.setLayoutParams(params);
        }
    }

    class Threads_GetAtten extends Thread {
        @Override
        public void run() {
            // 获取提问箱列表
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Common.URL + "/square/myAttention")).newBuilder()
                    .addQueryParameter("myattention", phone)
                    .build();

            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("fail to get attention!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        assert response.body() != null;
                        String AttenJson = response.body().string();
                        targetList = gson.fromJson(AttenJson, new TypeToken<ArrayList<ListOfTarget>>(){}.getType());
                        requireActivity().runOnUiThread(() -> {
                            // 分别存储目标名称和目标图片
                            ArrayList<String> Atten = new ArrayList<>();
                            ArrayList<byte[]> AttenImage = new ArrayList<>();

                            // 遍历 targetList, 填充列表
                            if(targetList != null) {
                                for (ListOfTarget l : targetList) {
                                    Atten.add(l.TargetName);
                                    AttenImage.add(l.getImageBytes());
                                }
                            }

                            // 创建适配器并设置给 listView
                            adapter = new AskListAdapter(tabView.getContext(), R.layout.listview_item_ask, Atten, AttenImage);
                            listView.setAdapter(adapter);
                            System.out.println(Atten);

                            // 计算高度并停止刷新动画
                            calculateHeight();
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    } else {
                        System.out.println("wrong");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    class Threads_GetFans extends Thread {
        @Override
        public void run() {
            // 获取提问箱列表
            OkHttpClient client = new OkHttpClient();
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Common.URL + "/square/myFans")).newBuilder()
                    .addQueryParameter("myfans", phone)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("fail to get fan!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        assert response.body() != null;
                        String FansJson = response.body().string();
                        sourceList = gson.fromJson(FansJson, new TypeToken<ArrayList<ListOfTarget>>(){}.getType());
                        requireActivity().runOnUiThread(() -> {
                            ArrayList<String> Fans = new ArrayList<>();
                            ArrayList<byte[]> FansImage = new ArrayList<>();
                            if (sourceList != null) {
                                for (ListOfTarget l : sourceList) {
                                    Fans.add(l.TargetName);
                                    FansImage.add(l.getImageBytes());
                                }
                            }
                            adapterFans = new AskListAdapter(tabView.getContext(), R.layout.listview_item_ask,Fans,FansImage);
                            listView_new.setAdapter(adapterFans);
                            System.out.println(Fans);
                            calculateHeight();
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    } else {
                        System.out.println("wrong");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    class Threads_delete extends Thread {
        private String targetPhone; // 添加目标手机号码字段
        private final int position;
        private final int state;

        // 构造函数, 接收目标手机号码
        public Threads_delete(String targetPhone,int position,int state) {
            this.targetPhone = targetPhone;
            this.position = position;
            // state = 1 删除粉丝
            // state = 0 删除关注
            this.state = state;
        }
        @Override
        public void run() {
            // 获取提问箱列表
            OkHttpClient client = new OkHttpClient();
            String myPhone = phone;
            if(state == 1){
                // 交换目标和源
                String item = myPhone;
                myPhone = targetPhone;
                targetPhone = item;
            }

            RequestBody body = new FormBody.Builder()
                    .add("target", targetPhone)
                    .add("source", myPhone)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/square/delete")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("fail to get fan!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        System.out.println("delete success");
                        requireActivity().runOnUiThread(() -> {
                            if (state == 0) {
                                targetList.remove(position);
                                ArrayList<String> x = new ArrayList<>();
                                for (ListOfTarget l : targetList) {
                                    x.add(l.TargetName);
                                }
                                adapter.clear();
                                adapter.addAll(x);
                                listView.setAdapter(adapter);
                                calculateHeight();
                            }else if(state == 1){
                                sourceList.remove(position);
                                ArrayList<String> x = new ArrayList<>();
                                for (ListOfTarget l : sourceList) {
                                    x.add(l.TargetName);
                                }
                                adapterFans.clear();
                                adapterFans.addAll(x);
                                listView_new.setAdapter(adapterFans);
                                calculateHeight();
                            }
                        });
                    } else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    private void showInputDialog() {
        // 显示输入对话框, 添加好友
        LayoutInflater layoutInflater = LayoutInflater.from(tabView.getContext());
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(tabView.getContext());
        alertDialogBuilder.setView(promptView);

        final EditText editText = promptView.findViewById(R.id.inputEditText);

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("确定", (dialog, id) -> {
            String myName = Common.user.getUsername();
            // 处理输入内容
            String inputText = editText.getText().toString().trim();
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("source", phone)
                    .add("sourceName", myName)
                    .add("target", inputText)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL + "/square/add")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Request failed", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String res = response.body().string();
                        System.out.println(res);
                        if (res.equals("repeated")) {
                            Looper.prepare();
                            Toast.makeText(tabView.getContext(), "请勿重复添加~", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else if (res.equals("successful")) {
                            Looper.prepare();
                            Toast.makeText(tabView.getContext(), "添加成功-ID!", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        Looper.prepare();
                        Toast.makeText(tabView.getContext(), "该用户不存在，请检查好友ID", Toast.LENGTH_SHORT).show();
                        System.out.println("response failed");
                        Looper.loop();
                    }
                }
            });
        });
        alertDialogBuilder.setNegativeButton("取消", (dialog, id) -> dialog.cancel());

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private final AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        // 监听滚动事件
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用布局填充器将布局文件转换为视图
        tabView = inflater.inflate(R.layout.tab_friend, container, false);

        // 获取用户号码
        phone = Common.user.getPhone();

        // 获取布局中的视图组件
        listView = tabView.findViewById(R.id.list_atten);
        listView_new = tabView.findViewById(R.id.list_new);
        ImageView tBtn = tabView.findViewById(R.id.toggleButton);
        ImageView nBtn = tabView.findViewById(R.id.newsButton);
        LinearLayout ll_attentionBtn = tabView.findViewById(R.id.ll_attention);
        LinearLayout ll_fanBtn = tabView.findViewById(R.id.ll_fan);
        Button aBtn = tabView.findViewById(R.id.addButton);
        swipeRefreshLayout = tabView.findViewById(R.id.swipeRefreshLayout);

        // 启动获取关注列表和粉丝列表的线程
        Threads_GetAtten GetAtten = new Threads_GetAtten();
        GetAtten.start();

        Threads_GetFans GetFans = new Threads_GetFans();
        GetFans.start();

        // 设置 listView 点击事件监听器, 跳转到 AskActivity 并传递目标信息
        listView.setOnItemClickListener((parent, view, i, l) -> {
            Intent intent = new Intent(view.getContext(), AskActivity.class);
            intent.putExtra("target", targetList.get(i).Target);
            intent.putExtra("targetName", targetList.get(i).TargetName);
            System.out.println(targetList.get(i).Target);
            startActivity(intent);
        });

        // 设置 listView_new 点击事件监听器, 跳转到 AskActivity 并传递目标信息
        listView_new.setOnItemClickListener((parent, view, i, l) -> {
            Intent intent = new Intent(view.getContext(), AskActivity.class);
            intent.putExtra("target", sourceList.get(i).Target);
            intent.putExtra("targetName", sourceList.get(i).TargetName);
            System.out.println(sourceList.get(i).Target);
            startActivity(intent);
        });

        // 设置关注按钮的点击事件监听器, 切换 listView 的可见性并更改按钮图标
        ll_attentionBtn.setOnClickListener(new View.OnClickListener() {
            boolean isArrowDown = true;
            @Override
            public void onClick(View v) {
                int visibility = listView.getVisibility();
                listView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
                if (isArrowDown) {
                    tBtn.setImageResource(R.drawable.arrowright);
                } else {
                    tBtn.setImageResource(R.drawable.arrowdown);
                }
                isArrowDown = !isArrowDown;
            }
        });

        // 设置粉丝按钮的点击事件监听器，切换 listView_new 的可见性并更改按钮图标
        ll_fanBtn.setOnClickListener(new View.OnClickListener() {
            boolean isArrowDown = true;
            @Override
            public void onClick(View v) {
                int visibility = listView_new.getVisibility();
                listView_new.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
                if (isArrowDown) {
                    nBtn.setImageResource(R.drawable.arrowright);
                } else {
                    nBtn.setImageResource(R.drawable.arrowdown);
                }
                isArrowDown = !isArrowDown;
            }
        });

        // 设置添加按钮的点击事件监听器, 显示输入对话框
        aBtn.setOnClickListener(v -> showInputDialog());

        // 设置 listView 的滚动监听器
        listView.setOnScrollListener(scrollListener);

        // 设置 SwipeRefreshLayout 的刷新监听器，重新启动获取关注和粉丝列表的线程
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Threads_GetAtten GetAtten1 = new Threads_GetAtten();
            GetAtten1.start();

            Threads_GetFans GetFans1 = new Threads_GetFans();
            GetFans1.start();
        });

        // 设置 listView 的长按事件监听器, 显示删除确认对话框
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(tabView.getContext());
            builder.setTitle("提示！");
            builder.setMessage("确定删除？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                String targetPhone = targetList.get(position).Target;
                // 启动删除线程, 从数据库中删除
                Threads_delete del = new Threads_delete(targetPhone, position, 0);
                del.start();
            });
            builder.setNegativeButton("取消", null);
            builder.create().show();
            // 返回 true 避免与点击事件冲突
            return true;
        });

        // 设置 listView_new 的长按事件监听器, 显示删除确认对话框
        listView_new.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(tabView.getContext());
            builder.setTitle("提示！");
            builder.setMessage("确定删除？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                String targetPhone = sourceList.get(position).Target;
                // 启动删除线程, 从数据库中删除
                Threads_delete del = new Threads_delete(targetPhone, position, 1);
                del.start();
            });
            builder.setNegativeButton("取消", null);
            builder.create().show();
            // 返回 true 避免与点击事件冲突
            return true;
        });

        // 返回根视图
        return tabView;
    }
}
