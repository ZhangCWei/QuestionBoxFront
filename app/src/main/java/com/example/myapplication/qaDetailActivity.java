package com.example.myapplication;

import android.graphics.Color;
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
//    不能在这里就写这句，否则会闪退！！！！！！
//    TextView TopBarTitle = (TextView)findViewById(R.id.topbar_title);

    public static int btnFlag = 0;
    private ImageButton backBtn;

    class Threads_Answer extends Thread {
        // 写回答/编辑回答
        private OkHttpClient client = null;
        String id = null;
        String server = null;
        String answer = null;
        String answertime = null;


        @Override
        public void run() {
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
                    .add("answer", answer)
                    .add("answertime", answertime)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL + server)
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("fail to save answer!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){    // 回调的方法执行在子线程。
                        System.out.println("save answer!");
                        Gson gson = new Gson();
                        String qboxitemJson = response.body().string();
                        Questionbox qboxitem = gson.fromJson(qboxitemJson, new TypeToken<Questionbox>(){}.getType());

                        // 切换到主线程来操作UI视图
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 在这里执行需要在主线程中更新的UI操作
                                Common.answerList.set(Common.nowpos, answer);
                                Common.stateList.set(Common.nowpos, "1");
                                Common.answertimeList.set(Common.nowpos, answertime);
                                String atimestr = "回答于 "+answertime;
                                TextView atime = (TextView)findViewById(R.id.atime);
                                atime.setText(atimestr);
                                TextView answerbtn = (TextView) findViewById(R.id.answerbtn);
                                TextView editbtn = (TextView) findViewById(R.id.editbtn);
                                // 保存回答的按钮隐藏
                                answerbtn.setBackgroundColor(Color.parseColor("#ffffff"));
                                answerbtn.setText("");
                                answerbtn.setTranslationZ(0);
                                answerbtn.setElevation(0);
                                answerbtn.setEnabled(false);
                                if(Common.hometabNum == 1){
                                    // 编辑回答的按钮显示
                                    editbtn.setBackgroundResource(R.drawable.answerbtn);
                                    editbtn.setText("编辑回答");
                                    editbtn.setTranslationZ(5);
                                    editbtn.setElevation(5);
                                    editbtn.setEnabled(true);
                                    answerbtn.setEnabled(false);
                                }

                                Common.adapter.notifyDataSetChanged();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qa_detail);
        TextView TopBarTitle = (TextView)findViewById(R.id.topbar_title);
        TopBarTitle.setText("回答详情");
        TextView qtext = (TextView)findViewById(R.id.question);
        qtext.setText(Common.questionList.get(Common.nowpos));
        TextView qtime = (TextView)findViewById(R.id.qtime);
        String qtimestr = "提问于 "+Common.questiontimeList.get(Common.nowpos);
        qtime.setText(qtimestr);
        TextView atime = (TextView)findViewById(R.id.atime);

        EditText editText = (EditText)findViewById(R.id.answer);
        String question = qtext.getText().toString();
        Common.nowpos = Common.questionList.indexOf(question);
        String state = Common.stateList.get(Common.nowpos);
        TextView answerbtn = (TextView) findViewById(R.id.answerbtn);
        TextView editbtn = (TextView) findViewById(R.id.editbtn);

        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(Common.hometabNum == 0 && state.equals("0")){        // 提问我但我未回答的（可写回答）
            editText.setEnabled(true);
            editText.setHint("请输入您的回答...");

            answerbtn.setBackgroundResource(R.drawable.answerbtn);
            answerbtn.setText("完成回答");
            answerbtn.setTranslationZ(5);
            answerbtn.setElevation(5);
            answerbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setEnabled(false);

                    // 获取回答当前时间
                    TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = new Date(System.currentTimeMillis());
                    String answertime = simpleDateFormat.format(date);

                    Threads_Answer Answer = new Threads_Answer();
                    Answer.id = Common.idList.get(Common.nowpos).toString();
                    Answer.server = "/Answer";
                    Answer.answer = editText.getText().toString();
                    Answer.answertime = answertime;
                    Answer.start();
                }
            });
        }
        if(Common.hometabNum == 1 && state.equals("1")){
            editText.setText(Common.answerList.get(Common.nowpos));     // 提问我但我并且我已经回答的（可编辑回答）
            editText.setEnabled(false);
            String atimestr = "回答于 "+Common.answertimeList.get(Common.nowpos);
            atime.setText(atimestr);

            editbtn.setBackgroundResource(R.drawable.answerbtn);
            editbtn.setText("编辑回答");
            editbtn.setTranslationZ(5);
            editbtn.setElevation(5);
            answerbtn.setEnabled(false);
            editbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        editText.setEnabled(true);

                        editbtn.setEnabled(false);
                        editbtn.setBackgroundColor(Color.parseColor("#ffffff"));
                        editbtn.setText("");
                        editbtn.setTranslationZ(0);
                        editbtn.setElevation(0);

                        answerbtn.setEnabled(true);
                        answerbtn.setBackgroundResource(R.drawable.answerbtn);
                        answerbtn.setText("完成编辑");
                        answerbtn.setTranslationZ(5);
                        answerbtn.setElevation(5);
                }
            });

            answerbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setEnabled(false);

                    // 获取回答当前时间
                    TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = new Date(System.currentTimeMillis());
                    String answertime = simpleDateFormat.format(date);

                    Threads_Answer Answer = new Threads_Answer();
                    Answer.id = Common.idList.get(Common.nowpos).toString();
                    Answer.server = "/Answer";
                    Answer.answer = editText.getText().toString();
                    Answer.answertime = answertime;
                    Answer.start();
                    System.out.println("编辑完成");
                }
            });
        }
        if(Common.hometabNum == 2 && state.equals("0")){        // 我提问但未回答的
            editText.setHint("正在等待回答...");
            editText.setEnabled(false);
        }
        if(Common.hometabNum == 3 && state.equals("1")){
            editText.setText(Common.answerList.get(Common.nowpos));     // 我提问且已经回答的（只读）
            editText.setEnabled(false);
            String atimestr = "回答于 "+Common.answertimeList.get(Common.nowpos);
            atime.setText(atimestr);
        }
    }
}
