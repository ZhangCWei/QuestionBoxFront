package com.example.myapplication.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.myapplication.R;
import com.example.myapplication.util.Common;
import com.mob.MobSDK;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 确保 Activity 的正常初始化
        super.onCreate(savedInstanceState);

        // 设置当前 Activity 的布局文件
        setContentView(R.layout.activity_register);

        // 设置背景图像资源
        getWindow().setBackgroundDrawableResource(R.drawable.registerbg);

        // 初始化 Mob API
        MobSDK.init(this, "3805a30a09595", "4126fd577130e07a64873af014315bed");
        MobSDK.submitPolicyGrantResult(true);

        // 注册回调 (在不阻塞主线程的情况下执行操作)
        SMSSDK.registerEventHandler(eventHandle);

        // 窗口初始化
        initView();
    }

    //处理获取验证码
    @SuppressLint("HandlerLeak")
    Handler handle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CODE_REPEAT) {
                commit.setEnabled(true);
                get_code.setEnabled(true);
                timer.cancel();         // 取消计时器
                timerTask.cancel();     // 取消计时任务
                TIME = 60;
                get_code.setText("获取验证码");
            } else {
                String showText = "(" + TIME + "s)"; // 更新按钮文本
                get_code.setText(showText);
            }
        }
    };


    @SuppressLint("HandlerLeak")
    Handler submitHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            // 获取消息参数
            @SuppressLint("HandlerLeak")
            int event = msg.arg1;
            int result = msg.arg2;

            // 输出消息
            System.out.println(4);
            System.out.println(msg.arg1);
            System.out.println(msg.arg2);
            System.out.println(msg.obj);

            // 检查结果是否成功
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    Toast.makeText(RegisterActivity.this, "验证成功！", Toast.LENGTH_LONG).show();
                    saveAccount();
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

            // 输出消息
            System.out.println(1);
            System.out.println(msg.arg1);
            System.out.println(msg.arg2);
            System.out.println(msg.obj);
        }
    };

    private void initView() {
        // 获取 View
        inputName = findViewById(R.id.name);
        inputCode = findViewById(R.id.code);
        inputPhone = findViewById(R.id.phone);
        inputPassword = findViewById(R.id.password);
        get_code = findViewById(R.id.get_code);
        commit = findViewById(R.id.commit);

        // 为 get_code 按钮设置事件监听器, 当用户点击按钮时, 执行代码
        get_code.setOnClickListener(view -> {
            OkHttpClient client = new OkHttpClient();
            phone = inputPhone.getText().toString().trim().replaceAll("/s", "");

            if (TextUtils.isEmpty(phone)) {
                // 如果未输入手机号, 提醒用户输入
                Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
            } else {
                // 判断手机号格式是否正确, 不正确则提示
                if (!isPhoneValid(phone)) {
                    Toast.makeText(RegisterActivity.this, "手机号格式错误", Toast.LENGTH_LONG).show();
                    return;
                }
                // 判断手机号是否存在于数据库第二行, 若存在, 弹窗提示无法注册
                RequestBody body = new FormBody.Builder()
                        .add("phonenumber", phone)
                        .build();
                Request request = new Request.Builder()
                        .url(Common.URL + "/register/check")
                        .post(body)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Logger logger = Logger.getLogger(getClass().getName());
                        logger.log(Level.SEVERE, "Request failed", e);
                        // 确保子线程可安全更新UI
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "请求错误，请稍后再试", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {  // 回调方法在子线程执行
                            assert response.body() != null;
                            String res = response.body().string();
                            if (res.equals("notRegistered")) {
                                // 如果手机号未被注册, 执行 alterWarning 函数
                                runOnUiThread(() -> alterWarning());
                            } else {
                                // 否则提示手机号已被注册
                                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "手机号已被注册", Toast.LENGTH_LONG).show());
                            }
                        } else {
                            System.out.println("response failed");
                        }
                    }
                });
            }
        });

        // 为 commit 按钮设置事件监听器, 当用户点击按钮时, 执行代码
        commit.setOnClickListener(view -> {
            // 获取 View 的文本内容
            name = inputName.getText().toString().replaceAll("/s", "");
            code = inputCode.getText().toString().replaceAll("/s", "");
            phone = inputPhone.getText().toString().trim().replaceAll("/s", "");
            password = inputPassword.getText().toString().trim().replaceAll("/s", "");

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(RegisterActivity.this, "验证码为空", Toast.LENGTH_LONG).show();
                return;
            }else if (name.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "请设置昵称", Toast.LENGTH_LONG).show();
                return;
            } else if (phone.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "请设置密码", Toast.LENGTH_LONG).show();
                return;
            }
            // 进行验证
            System.out.println(3);
            SMSSDK.submitVerificationCode(country, phone, code);
        });
    }

    //判断手机号格式是否正确
    private boolean isPhoneValid(String phone) {
        // 手机号正则表达式
        String regex = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";//手机号正则
        return phone.matches(regex);
    }

    private void alterWarning() {
        // 创建对话框对象, 设置标题和消息内容
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("短信验证");
        builder.setMessage("将发送验证短信到" + phone + "进行验证，请您确认");

        // 设置确定按钮及其点击事件
        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            // 关闭对话框, 发送短信验证码
            dialogInterface.dismiss();
            SMSSDK.getVerificationCode(country, phone);

            // 打印提示信息
            System.out.println(2);
            System.out.println(country);
            System.out.println(phone);

            // 显示已发送的提示信息
            Toast.makeText(RegisterActivity.this, "已发送" + i, Toast.LENGTH_LONG).show();

            // 设置按钮可点击性
            get_code.setEnabled(false);
            commit.setEnabled(true);

            // 创建新的 Timer 和 TimerTask
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handle.sendEmptyMessage(TIME--);
                }
            };

            // 安排定时任务，每隔 1 秒执行一次
            timer.schedule(timerTask, 0, 1000);
        });

        // 设置取消按钮及其点击事件
        builder.setNegativeButton("取消", (dialogInterface, i) -> {
            // 关闭对话框, 显示提示信息
            dialogInterface.dismiss();
            Toast.makeText(RegisterActivity.this, "已取消" + i, Toast.LENGTH_LONG).show();
        });

        // 展示对话框
        builder.create().show();
    }

    // 销毁短信注册
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销回调
        if (eventHandle != null) {
            SMSSDK.unregisterEventHandler(eventHandle);
        }
    }
    private void saveAccount(){
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Logger logger = Logger.getLogger(getClass().getName());
                logger.log(Level.SEVERE, "Request failed", e);
                // 确保子线程可安全更新UI
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "请求错误，请稍后再试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String res = response.body().string();
                    if (res.equals("saved")) {
                        // 发送验证码
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        System.out.println("wrong response");
                    }
                } else {
                    System.out.println("response failed");
                }
            }

        });
    }

}
