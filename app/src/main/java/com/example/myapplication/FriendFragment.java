package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.util.ArrayList;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
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
    private Gson gson = new Gson();
    public ArrayList<ListofTarget> targetList = new ArrayList<>();//存储我关注的人
    public ArrayList<ListofTarget> sourceList = new ArrayList<>();//存储关注我的人

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapterFans;

    public class ListofTarget{
        public String TargetName;
        public String Target;
        public byte[] imageBytes;
        public void additem(String TargetName,String Target,byte[] imageBytes){
            this.TargetName = TargetName;
            this.Target = Target;
            this.imageBytes = imageBytes;
        }
    }

    public void calculateHeight(){
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
        // 获取提问箱列表
        private OkHttpClient client = null;
        @Override
        public void run() {
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("myattention", phone)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/square/myattention")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("fail to get attention!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){//回调的方法执行在子线程。
                        String AttenJson = response.body().string();
                        targetList = gson.fromJson(AttenJson, new TypeToken<ArrayList<ListofTarget>>(){}.getType());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> Atten = new ArrayList<>();
                                ArrayList<byte[]> AttenImage = new ArrayList<>();
                                for(ListofTarget l :targetList){
                                    Atten.add(l.TargetName);
                                    AttenImage.add(l.imageBytes);
                                }
                                adapter = new AskListAdapter(tabView.getContext(), R.layout.listview_item_ask,Atten,AttenImage);
                                listView.setAdapter(adapter);
                                System.out.println(Atten);
                                calculateHeight();
                                swipeRefreshLayout.setRefreshing(false);

                            }
                        });
                    }
                    else {
                        System.out.println("wrong");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    class Threads_GetFans extends Thread {
        // 获取提问箱列表
        private OkHttpClient client = null;
        @Override
        public void run() {
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("myfans", phone)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/square/myfans")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("fail to get fan!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){//回调的方法执行在子线程。
                        String FansJson = response.body().string();
                        sourceList = gson.fromJson(FansJson, new TypeToken<ArrayList<ListofTarget>>(){}.getType());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> Fans = new ArrayList<>();
                                ArrayList<byte[]> FansImage = new ArrayList<>();
                                for(ListofTarget l :sourceList){
                                    Fans.add(l.TargetName);
                                    FansImage.add(l.imageBytes);
                                }
                                adapterFans = new AskListAdapter(tabView.getContext(), R.layout.listview_item_ask,Fans,FansImage);
                                listView_new.setAdapter(adapterFans);
                                System.out.println(Fans);
                                calculateHeight();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                    else {
                        System.out.println("wrong");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    class Threads_delete extends Thread {
        // 获取提问箱列表
        private OkHttpClient client = null;
        private String targetPhone; // 添加目标手机号码字段
        private int position;
        private int state;

        // 构造函数，接收目标手机号码
        public Threads_delete(String targetPhone,int position,int state) {
            this.targetPhone = targetPhone;
            this.position = position;
            this.state = state;//state = 1 删除粉丝；state = 0，删除关注
        }
        @Override
        public void run() {
            client = new OkHttpClient();
            String myPhone = phone;
            if(state == 1){
                //交换目标和源
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
                public void onFailure(Call call, IOException e) {
                    System.out.println("fail to get fan!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){//回调的方法执行在子线程。
                        System.out.println("delete success");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state == 0) {
                                    targetList.remove(position);
                                    ArrayList<String> x = new ArrayList<>();
                                    for (ListofTarget l : targetList) {
                                        x.add(l.TargetName);
                                    }
                                    adapter.clear();
                                    adapter.addAll(x);
                                    listView.setAdapter(adapter);
                                    calculateHeight();
                                }else if(state == 1){
                                    sourceList.remove(position);
                                    ArrayList<String> x = new ArrayList<>();
                                    for (ListofTarget l : sourceList) {
                                        x.add(l.TargetName);
                                    }
                                    adapterFans.clear();
                                    adapterFans.addAll(x);
                                    listView_new.setAdapter(adapterFans);
                                    calculateHeight();
                                }
                            }
                        });
                    }
                    else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }


    private void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(tabView.getContext());
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(tabView.getContext());
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.inputEditText);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        String myName = Common.user.getName();
                        // 处理输入内容
                        String inputText = editText.getText().toString().trim();
                        //Toast.makeText(tabView.getContext(), "正在查找-ID：" + inputText, Toast.LENGTH_SHORT).show();
                        //----
                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = new FormBody.Builder()
                                .add("source",phone)
                                .add("sourceName",myName)
                                .add("target",inputText)
                                .build();
                        Request request = new Request.Builder()
                                .url(Common.URL+"/square/add")
                                .post(body)
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {//回调的方法执行在子线程。
                                    String res = response.body().string();
                                    System.out.println(res);
                                    if(res.equals("repeated")){
                                        Looper.prepare();
                                        Toast.makeText(tabView.getContext(), "请勿重复添加~", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }else if(res.equals("successful")) {
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
                            }});
                        //----
                    }
                })
                .setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
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
        tabView = inflater.inflate(R.layout.tab_friend, container, false);

        phone = Common.user.getPhone();

        listView = tabView.findViewById(R.id.list_atten);
        listView_new = tabView.findViewById(R.id.list_new);
        ImageView tBtn = tabView.findViewById(R.id.toggleButton);
        ImageView nBtn = tabView.findViewById(R.id.newsButton);
        LinearLayout ll_attentionBtn = (LinearLayout)tabView.findViewById(R.id.ll_attention);
        LinearLayout ll_fanBtn = (LinearLayout)tabView.findViewById(R.id.ll_fan);
        Button aBtn = tabView.findViewById(R.id.addButton);
        swipeRefreshLayout = tabView.findViewById(R.id.swipeRefreshLayout);

        Threads_GetAtten GetAtten = new Threads_GetAtten();
        GetAtten.start();

        Threads_GetFans GetFans = new Threads_GetFans();
        GetFans.start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int i,long l){
                Intent intent = new Intent(view.getContext(), AskActivity.class);
                //传递电话号码
                intent.putExtra("target", targetList.get(i).Target);
                intent.putExtra("targetName", targetList.get(i).TargetName);
                System.out.println(targetList.get(i).Target);
                startActivity(intent);
            }
        });

        listView_new.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int i,long l){
                Intent intent = new Intent(view.getContext(), AskActivity.class);
                //传递电话号码
                intent.putExtra("target", sourceList.get(i).Target);
                intent.putExtra("targetName", sourceList.get(i).TargetName);
                System.out.println(sourceList.get(i).Target);
                startActivity(intent);
            }
        });

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

        aBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        listView.setOnScrollListener(scrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Threads_GetAtten GetAtten = new Threads_GetAtten();
                GetAtten.start();

                Threads_GetFans GetFans = new Threads_GetFans();
                GetFans.start();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(tabView.getContext());
                builder.setTitle("提示！");
                builder.setMessage("确定删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String targetPhone = targetList.get(position).Target;
                        //调用删除线程，从数据库删除
                        Threads_delete del = new Threads_delete(targetPhone,position,0);
                        del.start();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                // 返回true避免与点击事件冲突
                return true;
            }
        });

        listView_new.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(tabView.getContext());
                builder.setTitle("提示！");
                builder.setMessage("确定删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String targetPhone = sourceList.get(position).Target;
                        //调用删除线程，从数据库删除
                        Threads_delete del = new Threads_delete(targetPhone,position,1);
                        del.start();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                // 返回true避免与点击事件冲突
                return true;
            }
        });

        return tabView;
    }
}
