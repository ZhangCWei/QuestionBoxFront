package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.entity.Questionbox;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnswerDetail extends AppCompatActivity {
    private String id;
    private TextView qText;
    private EditText aText;
    private TextView qTime;
    private TextView aTime;
    private ImageButton backBtn;

    class Threads_detail extends Thread {
        // 获取提问箱列表
        private OkHttpClient client = null;
        @Override
        public void run() {
            System.out.println(id);
            client = new OkHttpClient();
            Gson gson = new Gson();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/getdetail")
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

                    if(response.isSuccessful()) {//回调的方法执行在子线程。
                        String AnswerJson = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Questionbox aBoxItem = gson.fromJson(AnswerJson, new TypeToken<Questionbox>() {}.getType());
                                qText.setText(aBoxItem.getQuestion());
                                aText.setText(aBoxItem.getAnswer());
                                aText.setEnabled(false);
                                qTime.setText("提问于 "+aBoxItem.getQuestionTime());
                                aTime.setText("回答于 "+aBoxItem.getAnswerTime());
                                TextView TopBarTitle = (TextView) findViewById(R.id.topbar_title);
                                TopBarTitle.setText("详  情");
                            }
                        });
                    }else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qa_detail);
        qText = findViewById(R.id.question);
        aText = findViewById(R.id.answer);
        qTime = findViewById(R.id.qtime);
        aTime = findViewById(R.id.atime);
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            if (id != null) {
                Threads_detail det = new Threads_detail();
                det.start();
            } else {
                System.out.println("wrong");
            }
        }
        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
