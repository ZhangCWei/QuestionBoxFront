package com.example.myapplication.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.view.adapter.AskListAdapter;
import com.example.myapplication.R;
import com.example.myapplication.entity.QuestionBox;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AskActivity extends AppCompatActivity {
    private EditText askText;       // 提问处
    private ListView answeredList;  // 已回答过的问题列表
    private String targetName;
    private String target;
    private String phone;
    private AskListAdapter adapter;
    private ArrayList<QuestionBox> answerList;
    private final ArrayList<Integer> answerIdList = new ArrayList<>();;

    public void calculateHeight() {
        if (adapter != null) {
            int totalHeight = 0;
            int itemCount = adapter.getCount();
            for (int i = 0; i < itemCount; i++) {
                View listItem = adapter.getView(i, null, answeredList);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = answeredList.getLayoutParams();
            params.height = totalHeight + (answeredList.getDividerHeight() * (itemCount - 1));
            answeredList.setLayoutParams(params);
        }
    }

    class Threads_Ask extends Thread {
        @Override
        public void run() {
            // 设置默认时区GMT+8
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));

            // 格式化日期时间
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // 获取当前系统时间, 格式化为字符串
            Date date = new Date(System.currentTimeMillis());
            String questionTime = simpleDateFormat.format(date);
            System.out.println(questionTime);

            // 获取输入的提问字符串
            String question = askText.getText().toString();

            // 提交提问
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("source", phone)
                    .add("target", target)
                    .add("question", question)
                    .add("questiontime", questionTime)
                    .add("targetName", targetName)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL + "/askQuestion")
                    .post(body)
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
                        runOnUiThread(() -> {
                            Toast.makeText(AskActivity.this, "问题提交成功！", Toast.LENGTH_SHORT).show();
                            askText.setText("");
                        });
                    } else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    class Threads_Ans extends Thread {
        @Override
        public void run() {
            // 获取提问箱列表
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Common.URL + "/getTarget")).newBuilder()
                    .addQueryParameter("phone", target)
                    .addQueryParameter("state", "1")
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
                    System.out.println("fail to get attention!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        assert response.body() != null;
                        String AnswerJson = response.body().string();
                        AskActivity.this.runOnUiThread(() -> {
                            // 使用 Gson 将 JSON 字符串转换为 ArrayList<Questionbox> 对象
                            answerList = gson.fromJson(AnswerJson, new TypeToken<ArrayList<QuestionBox>>() {}.getType());
                            // 清空回答列表
                            Common.askAnswerList.clear();
                            answerIdList.clear();
                            // 清除适配器数据
                            if (adapter != null) adapter.clear();
                            // 重新添加回答列表
                            for (QuestionBox qb : answerList) {
                                Common.askAnswerList.add(qb.getQuestion());
                                answerIdList.add(qb.getId());
                            }
                            // 若回答列表不为空, 设置适配器并计算高度
                            if (!Common.askAnswerList.isEmpty()) {
                                adapter = new AskListAdapter(AskActivity.this, R.layout.listview_item_answer, Common.askAnswerList,null);
                                answeredList.setAdapter(adapter);
                                calculateHeight();
                            }
                        });
                    }else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        phone = Common.user.getPhone();

        askText = findViewById(R.id.askPlace);
        answeredList = findViewById(R.id.answered);
        Button commitBtn = findViewById(R.id.commit);
        ImageButton backBtn = findViewById(R.id.backButton);
        TextView TopBarTitle = findViewById(R.id.topbar_title);

        target = getIntent().getStringExtra("target");
        targetName = getIntent().getStringExtra("targetName");
        TopBarTitle.setText(targetName + " 的 回 答");

        // 创建并启动获取回答的线程
        Threads_Ans ans = new Threads_Ans();
        ans.start();

        // 设置提交按钮的点击事件监听器
        commitBtn.setOnClickListener(v -> {
            // 如果提问文本框不为空, 启动提问线程
            if(askText.length() != 0){
                Threads_Ask ask = new Threads_Ask();
                ask.start();
            } else {
                Toast.makeText(AskActivity.this, "请输入你的提问", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置返回按钮的点击事件监听器
        backBtn.setOnClickListener(v -> finish());

        // 设置已回答问题列表项的点击事件监听器
        answeredList.setOnItemClickListener((parent, view, i, l) -> {
            // 获取选中问题的 ID
            int selectedId = answerIdList.get(i);
            String idStr = String.valueOf(selectedId);
            // 创建跳转到回答详情的 Intent
            Intent intent = new Intent(AskActivity.this, AnsDetailActivity.class);
            adapter.notifyDataSetChanged();
            // 传递选中问题的 ID
            intent.putExtra("id", idStr);
            System.out.println(selectedId);
            // 启动回答详情活动
            startActivity(intent);
        });
    }
}
