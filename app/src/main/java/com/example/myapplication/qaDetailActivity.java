package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.Common;

import java.io.IOException;
import java.text.SimpleDateFormat;
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


public class qaDetailActivity extends AppCompatActivity {
    // 不能在这里就写这句, 否则会闪退
    // TextView TopBarTitle = (TextView)findViewById(R.id.topbar_title);

    class Threads_Answer extends Thread {
        String id = null;
        String server = null;
        String answer = null;
        String answerTime = null;

        @Override
        public void run() {
            // 写回答/编辑回答
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
                    .add("answer", answer)
                    .add("answertime", answerTime)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL + server)
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("fail to save answer!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        System.out.println("save answer!");
                        assert response.body() != null;
                        // 切换到主线程来操作UI视图
                        runOnUiThread(() -> {
                            // 在这里执行需要在主线程中更新的UI操作
                            Common.answerList.set(Common.nowpos, answer);
                            Common.stateList.set(Common.nowpos, "1");
                            Common.answerTimeList.set(Common.nowpos, answerTime);
                            String ansTimeStr = "回答于 "+ answerTime;
                            TextView ansTime = findViewById(R.id.atime);
                            ansTime.setText(ansTimeStr);
                            TextView answerBtn = findViewById(R.id.answerbtn);
                            TextView editBtn = findViewById(R.id.editbtn);
                            // 保存回答的按钮隐藏
                            answerBtn.setBackgroundColor(Color.parseColor("#ffffff"));
                            answerBtn.setText("");
                            answerBtn.setTranslationZ(0);
                            answerBtn.setElevation(0);
                            answerBtn.setEnabled(false);
                            if(Common.hometabNum == 1){
                                // 编辑回答的按钮显示
                                editBtn.setBackgroundResource(R.drawable.answerbtn);
                                editBtn.setText("编辑回答");
                                editBtn.setTranslationZ(5);
                                editBtn.setElevation(5);
                                editBtn.setEnabled(true);
                                answerBtn.setEnabled(false);
                            }
                            Common.adapter.notifyDataSetChanged();
                        });
                    } else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qa_detail);
        TextView TopBarTitle = findViewById(R.id.topbar_title);
        TopBarTitle.setText("回答详情");
        TextView qText = findViewById(R.id.question);
        qText.setText(Common.questionList.get(Common.nowpos));
        TextView qTime = findViewById(R.id.qtime);
        String qTimeStr = "提问于 "+Common.questionTimeList.get(Common.nowpos);
        qTime.setText(qTimeStr);
        TextView ansTime = findViewById(R.id.atime);

        EditText editText = findViewById(R.id.answer);
        String question = qText.getText().toString();
        Common.nowpos = Common.questionList.indexOf(question);
        String state = Common.stateList.get(Common.nowpos);
        TextView answerBtn = findViewById(R.id.answerbtn);
        TextView editBtn = findViewById(R.id.editbtn);

        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> finish());

        if(Common.hometabNum == 0 && state.equals("0")){        // 提问我但我未回答的（可写回答）
            editText.setEnabled(true);
            editText.setHint("请输入您的回答...");

            answerBtn.setBackgroundResource(R.drawable.answerbtn);
            answerBtn.setText("完成回答");
            answerBtn.setTranslationZ(5);
            answerBtn.setElevation(5);
            answerBtn.setOnClickListener(v -> {
                editText.setEnabled(false);

                // 获取回答当前时间
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());
                String answerTime = simpleDateFormat.format(date);

                Threads_Answer Answer = new Threads_Answer();
                Answer.id = Common.idList.get(Common.nowpos).toString();
                Answer.server = "/Answer";
                Answer.answer = editText.getText().toString();
                Answer.answerTime = answerTime;
                Answer.start();
            });
        }
        if(Common.hometabNum == 1 && state.equals("1")){
            editText.setText(Common.answerList.get(Common.nowpos));     // 提问我但我并且我已经回答的（可编辑回答）
            editText.setEnabled(false);
            String ansTimeStr = "回答于 "+Common.answerTimeList.get(Common.nowpos);
            ansTime.setText(ansTimeStr);

            editBtn.setBackgroundResource(R.drawable.answerbtn);
            editBtn.setText("编辑回答");
            editBtn.setTranslationZ(5);
            editBtn.setElevation(5);
            answerBtn.setEnabled(false);
            editBtn.setOnClickListener(v -> {
                    editText.setEnabled(true);

                    editBtn.setEnabled(false);
                    editBtn.setBackgroundColor(Color.parseColor("#ffffff"));
                    editBtn.setText("");
                    editBtn.setTranslationZ(0);
                    editBtn.setElevation(0);

                    answerBtn.setEnabled(true);
                    answerBtn.setBackgroundResource(R.drawable.answerbtn);
                    answerBtn.setText("完成编辑");
                    answerBtn.setTranslationZ(5);
                    answerBtn.setElevation(5);
            });

            answerBtn.setOnClickListener(v -> {
                editText.setEnabled(false);

                // 获取回答当前时间
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());
                String answerTime = simpleDateFormat.format(date);

                Threads_Answer Answer = new Threads_Answer();
                Answer.id = Common.idList.get(Common.nowpos).toString();
                Answer.server = "/Answer";
                Answer.answer = editText.getText().toString();
                Answer.answerTime = answerTime;
                Answer.start();
                System.out.println("编辑完成");
            });
        }
        if(Common.hometabNum == 2 && state.equals("0")){        // 我提问但未回答的
            editText.setHint("正在等待回答...");
            editText.setEnabled(false);
        }

        if(Common.hometabNum == 3 && state.equals("1")){
            editText.setText(Common.answerList.get(Common.nowpos));     // 我提问且已经回答的（只读）
            editText.setEnabled(false);
            String ansTimeStr = "回答于 "+Common.answerTimeList.get(Common.nowpos);
            ansTime.setText(ansTimeStr);
        }

    }
}
