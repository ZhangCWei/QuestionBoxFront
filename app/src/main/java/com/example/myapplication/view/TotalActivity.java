package com.example.myapplication.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import com.example.myapplication.util.Common;
import com.example.myapplication.view.fragment.HomeFragment;
import com.example.myapplication.view.fragment.InfoFragment;
import com.example.myapplication.view.fragment.FriendFragment;
import com.example.myapplication.Interface.FragmentInterface;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TotalActivity extends FragmentActivity implements View.OnClickListener, FragmentInterface {
    private User host;
    private ViewPager2 viewPager;           // 声明 ViewPager
    private List<Fragment> fragmentList;    // 装载 Fragment 的集合

    // 三个Tab点击对应的布局
    private LinearLayout homeBottomTab;
    private LinearLayout infoBottomTab;
    private LinearLayout friendBottomTab;

    private TextView homeBottomTabText;     // Tab点击对应的Text字
    private ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉 TitleBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 指定布局界面
        setContentView(R.layout.activity_total);

        host = Common.user;
        getHeader();
        initViews();    // 初始化控件
        initEvents();   // 初始化事件
        initData();     // 初始化数据
    }

    // 初始化控件
    private void initViews() {
        viewPager = findViewById(R.id.id_viewpager);
        backButton = findViewById(R.id.backButton);

        homeBottomTab = findViewById(R.id.id_homebottomtab);
        infoBottomTab = findViewById(R.id.id_infobottomtab);
        friendBottomTab = findViewById(R.id.id_friendbottomtab);

        homeBottomTabText = findViewById(R.id.id_homebottomtab_text);
    }

    private void initEvents() {
        // 设置三个Tab点击的点击事件
        homeBottomTab.setOnClickListener(this);
        infoBottomTab.setOnClickListener(this);
        friendBottomTab.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    private void initData() {
        fragmentList = new ArrayList<>();
        // 将三个Fragment加入集合中
        fragmentList.add(new HomeFragment());
        fragmentList.add(new FriendFragment());
        fragmentList.add(new InfoFragment(host));

        // 初始化适配器
        FragmentStateAdapter adapter = new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return fragmentList.size();
            }
        };

        // 设置 ViewPager 的适配器
        viewPager.setAdapter(adapter);

        // 设置ViewPager手指滑动切换页面的监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 设置 pos 对应的集合中的 Fragment
                selectTabBtn(position);
            }
        });
        // 设置默认选中首页
        viewPager.setCurrentItem(0);
        homeBottomTabText.setTextColor(Color.parseColor("#c47731"));
        System.out.println("初始化");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        // 根据点击的Tab进行响应
        switch (v.getId()) {
            case R.id.id_homebottomtab:
                selectTabBtn(0);
                break;
            case R.id.id_friendbottomtab:
                selectTabBtn(1);
                break;
            case R.id.id_infobottomtab:
                selectTabBtn(2);
                break;
            case R.id.backButton:
                finish();   // 返回上一个界面
                break;
        }
    }

    private void selectTabBtn(int i) {
        // 根据点击的Tab按钮设置对应的响应
        TextView TopBarTitle = findViewById(R.id.topbar_title);
        TextView BottomBarText_home = findViewById(R.id.id_homebottomtab_text);
        TextView BottomBarText_info = findViewById(R.id.id_infobottomtab_text);
        TextView BottomBarText_friend = findViewById(R.id.id_friendbottomtab_text);

        switch (i) {
            case 0:
                TopBarTitle.setText("首 页");
                BottomBarText_home.setTextColor(Color.parseColor("#c47731"));
                BottomBarText_info.setTextColor(Color.parseColor("#000000"));
                BottomBarText_friend.setTextColor(Color.parseColor("#000000"));
                break;
            case 1:
                TopBarTitle.setText("交 友");
                BottomBarText_friend.setTextColor(Color.parseColor("#c47731"));
                BottomBarText_home.setTextColor(Color.parseColor("#000000"));
                BottomBarText_info.setTextColor(Color.parseColor("#000000"));
                break;
            case 2:
                TopBarTitle.setText("我 的");
                BottomBarText_info.setTextColor(Color.parseColor("#c47731"));
                BottomBarText_home.setTextColor(Color.parseColor("#000000"));
                BottomBarText_friend.setTextColor(Color.parseColor("#000000"));
                break;
        }
        // 设置当前点击的Tab所对应的页面
        viewPager.setCurrentItem(i);
    }

    public void exit() {
        finish();
    }

    public void photo() {   // 调用系统相册选择图片
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    // 注册用于接收相册选择结果的ActivityResultLauncher
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        handleSelectedImage(uri);
                    }
                }
            }
    );

    // 处理选择的图片
    private void handleSelectedImage(Uri uri) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 获取内容解析器
            ContentResolver cr = this.getContentResolver();

            // 从uri中获取位图, 将其压缩为PNG格式并写入输出流
            Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            // 获取当前ViewPager的页面索引, 根据索引获取对应的Fragment
            int index = viewPager.getCurrentItem();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + index);

            // 获取Fragment中的ImageView
            assert fragment != null;
            ImageView headImg = fragment.requireView().findViewById(R.id.header);

            // 获取处理后的圆形位图, 设置并保存到Common对象中
            Bitmap circleBitmap = Common.getLargestCircleBitmap(bitmap);
            headImg.setImageBitmap(circleBitmap);
            Common.bitmap = circleBitmap;

            // 将位图写入文件
            try (FileOutputStream output = openFileOutput(Common.user.getPhone() + ".png", MODE_PRIVATE)) {
                output.write(outputStream.toByteArray());
            }

            // 获取文件路径, 上传图片文件
            File file = this.getFileStreamPath(Common.user.getPhone() + ".png");
            uploadImage(file);

        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Request failed", e);
        }
    }

    // 上传图片到服务器
    private void uploadImage(File file) {
        String phoneNumber = Common.user.getPhone();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), body)
                .build();
        Request request = new Request.Builder()
                .url(Common.URL + "/upload")
                .post(multipartBody)
                .addHeader("phone", phoneNumber)
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
                    Common.user.setIsChanged(1);
                } else {
                    runOnUiThread(() -> Toast.makeText(TotalActivity.this, "上传失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void getHeader() {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("phone", Common.user.getPhone())
                .build();
        Request request = new Request.Builder()
                .url(Common.URL + "/getheader")
                .post(body)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Logger logger = Logger.getLogger(getClass().getName());
                logger.log(Level.SEVERE, "Request failed", e);
                runOnUiThread(() -> Toast.makeText(TotalActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    byte[] data = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Common.bitmap = Common.getLargestCircleBitmap(bitmap);
                } else {
                    runOnUiThread(() -> Toast.makeText(TotalActivity.this, "响应失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}