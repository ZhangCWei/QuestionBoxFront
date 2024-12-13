package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.entity.Questionbox;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AskActivity extends AppCompatActivity {
    private EditText askText;//提问处
    private ListView answeredList;//已回答过的问题列表
    private Button commitBtn;
    private String question;
    private ImageButton backBtn;
    private String targetName;
    private String target;
    private String phone;
    private AskListAdapter adapter;
    private ArrayList<Questionbox> answerList;
    private ArrayList<Integer> answerIdList = new ArrayList<>();;


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
        // 提交提问
        private OkHttpClient client = null;
        @Override
        public void run() {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date(System.currentTimeMillis());
            String questionTime = simpleDateFormat.format(date);
            System.out.println(questionTime);
            question = askText.getText().toString();
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("source", phone)
                    .add("target", target)
                    .add("question", question)
                    .add("questiontime",questionTime)
                    .add("targetName",targetName)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/AskQuestion")
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
                        Looper.prepare();
                        Toast.makeText(AskActivity.this, "问题提交成功！", Toast.LENGTH_SHORT).show();
                        askText.setText("");
                        Looper.loop();
                    } else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    class Threads_Ans extends Thread {
        // 获取提问箱列表
        private OkHttpClient client = null;
        @Override
        public void run() {
            client = new OkHttpClient();
            Gson gson = new Gson();
            RequestBody body = new FormBody.Builder()
                    .add("phone", target)
                    .add("state", "1")
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/gettarget")
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
                        String AnswerJson = response.body().string();
                        AskActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                answerList = gson.fromJson(AnswerJson, new TypeToken<ArrayList<Questionbox>>() {}.getType());
                                Common.AskanswerList.clear();
                                answerIdList.clear();
                                if (adapter != null) adapter.clear();
                                for (Questionbox qb : answerList) {
                                    Common.AskanswerList.add(qb.getQuestion());
                                    answerIdList.add(qb.getId());
                                }
                                if (!Common.AskanswerList.isEmpty()) {
                                    adapter = new AskListAdapter(AskActivity.this, R.layout.listview_item_answer, Common.AskanswerList,null);
                                    answeredList.setAdapter(adapter);
                                    calculateHeight();
                                }
                            }
                        });
                    }else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        phone = Common.user.getPhone();

        askText = findViewById(R.id.askPlace);
        answeredList = findViewById(R.id.answered);
        commitBtn = findViewById(R.id.commit);
        backBtn = findViewById(R.id.backButton);

        target = getIntent().getStringExtra("target");//手机号
        targetName = getIntent().getStringExtra("targetName");//昵称

        TextView TopBarTitle = (TextView)findViewById(R.id.topbar_title);
        TopBarTitle.setText(targetName + " 的 回 答");


        Threads_Ans ans = new Threads_Ans();
        ans.start();

        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(askText.length() != 0){
                    Threads_Ask ask = new Threads_Ask();
                    ask.start();
                }else{
                    Toast.makeText(AskActivity.this, "请输入你的提问", Toast.LENGTH_SHORT).show();
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        answeredList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int i,long l){
                int selectedId = answerIdList.get(i);
                String idstr = String.valueOf(selectedId);
                Intent intent = new Intent(AskActivity.this, AnswerDetail.class);
                adapter.notifyDataSetChanged();
                //传递电话号码
                intent.putExtra("id", idstr);
                System.out.println(selectedId);
                startActivity(intent);
            }
        });
    }
}
