package com.example.myapplication.view;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.example.myapplication.R;
import com.example.myapplication.util.Common;
import com.mob.MobSDK;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;


import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private boolean approved =false;
    private boolean registered = true;
    private TimerTask timerTask;
    private Timer timer;
    private EditText inputName;
    private EditText inputPhone;
    private EditText inputCode;
    private EditText inputPassword;
    private Button get_code;
    private Button commit;
    public String country = "86";
    private String name;
    private String phone;
    private String password;
    private String code;
    private int TIME = 60;
    private static final int CODE_REPEAT = 1;
    private static Connection con = null;
    private static PreparedStatement stmt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setBackgroundDrawableResource(R.drawable.registerbg);
        //mob
        MobSDK.init(this, "3805a30a09595", "4126fd577130e07a64873af014315bed");
        MobSDK.submitPolicyGrantResult(true);

        //注册回调
        SMSSDK.registerEventHandler(eventHandle);
        //窗口初始化
        initView();
    }

    //处理获取验证码
    @SuppressLint("HandlerLeak")
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CODE_REPEAT) {
                commit.setEnabled(true);
                get_code.setEnabled(true);
                timer.cancel();
                timerTask.cancel();
                TIME = 60;
                get_code.setText("获取验证码");
            } else {
                String showText = "(" + TIME + "s)";
                get_code.setText(showText);
            }
        }
    };
    @SuppressLint("HandlerLeak")
    Handler submitHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            @SuppressLint("HandlerLeak") int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            System.out.println(4);
            System.out.println(msg.arg1);
            System.out.println(msg.arg2);
            System.out.println(msg.obj);
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    Toast.makeText(RegisterActivity.this, "验证成功！", Toast.LENGTH_LONG).show();
                    saveaccount();
                } else {
                    Toast.makeText(RegisterActivity.this, "验证错误！", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    EventHandler eventHandle = new EventHandler() {
        @Override
        public void afterEvent(int event, int result, Object data) {
            Message msg = new Message();
            msg.arg1 = event;
            msg.arg2 = result;
            msg.obj = data;
            submitHandle.sendMessage(msg);
            System.out.println(1);
            System.out.println(msg.arg1);
            System.out.println(msg.arg2);
            System.out.println(msg.obj);
        }
    };

    private void initView() {

        inputName = findViewById(R.id.name);
        inputCode = findViewById(R.id.code);
        inputPhone = findViewById(R.id.phone);
        inputPassword = findViewById(R.id.password);
        get_code = findViewById(R.id.get_code);
        commit = findViewById(R.id.commit);

        get_code.setOnClickListener(view -> {
            OkHttpClient client = new OkHttpClient();
            phone = inputPhone.getText().toString().trim().replaceAll("/s", "");
            if (!TextUtils.isEmpty(phone)) {
                //判断手机号格式是否正确，不正确则提示
                if (!isPhoneValid(phone)) {
                    Toast.makeText(RegisterActivity.this, "手机号格式错误", Toast.LENGTH_LONG).show();
                    return;
                }
                //判断手机号是否存在于数据库第二行，若存在，弹窗提示无法注册
                RequestBody body = new FormBody.Builder()
                        .add("phonenumber",phone)
                        .build();
                Request request = new Request.Builder()
                        .url(Common.URL+"/register/check")
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
                            if (res.equals("notRegistered")) {
                                //发送验证码
                                registered = false;
                            } else {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "手机号已被注册", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                        } else
                            System.out.println("response failed");
                    }});
                    if(!registered){
                        alterWarning();
                    }
            } else {
                Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
            }
        });

        commit.setOnClickListener(view -> {
            name = inputName.getText().toString().replaceAll("/s", "");
            code = inputCode.getText().toString().replaceAll("/s", "");
            phone = inputPhone.getText().toString().trim().replaceAll("/s", "");
            password = inputPassword.getText().toString().trim().replaceAll("/s", "");

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(RegisterActivity.this, "验证码为空", Toast.LENGTH_LONG).show();
                return;
            }else if (name.length() == 0) {
                Toast.makeText(RegisterActivity.this, "请设置昵称", Toast.LENGTH_LONG).show();
                return;
            } else if (phone.length() == 0) {
                Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
                return;
            } else if (password.length() == 0) {
                Toast.makeText(RegisterActivity.this, "请设置密码", Toast.LENGTH_LONG).show();
                return;
            }
            //验证
            System.out.println(3);
            SMSSDK.submitVerificationCode(country, phone, code);
        });
    }

    //判断手机号格式是否正确
    private boolean isPhoneValid(String phone) {
        // 此处根据需求自行修改手机号正则表达式
        String regex = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";//手机号正则
        return phone.matches(regex);
    }


    private void alterWarning(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("短信验证");
        builder.setMessage("将发送验证短信到"+phone+"进行验证，请您确认");

        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            dialogInterface.dismiss();//关闭dialog
            System.out.println(2);
            SMSSDK.getVerificationCode(country,phone);//发送短信验证码
            System.out.println(country);
            System.out.println(phone);
            Toast.makeText(RegisterActivity.this,"已发送"+i,Toast.LENGTH_LONG).show();
            get_code.setEnabled(false);//获取验证码按钮设置不可点击
            commit.setEnabled(true);//提交按钮可点击
            timer=new Timer();
            timerTask=new TimerTask() {
                @Override
                public void run() {
                    handle.sendEmptyMessage(TIME--);
                }
            };
            timer.schedule(timerTask,0,1000);
        });

        builder.setNegativeButton("取消", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Toast.makeText(RegisterActivity.this,"已取消"+i,Toast.LENGTH_LONG).show();
        });
        builder.create().show();//创建并展示
    }

    //销毁短信注册
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销回调
        SMSSDK.unregisterEventHandler(eventHandle);
    }
    private void saveaccount(){
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("name",name)
                .add("password", password)
                .add("phonenumber",phone)
                .build();
        Request request = new Request.Builder()
                .url(Common.URL+"/register/confirm")
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
                    if (res.equals("saved")) {
                        //发送验证码
                        Intent intent = null;
                        intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else
                        System.out.println("wrong response");
                } else
                    System.out.println("response failed");
            }});
    }
}
