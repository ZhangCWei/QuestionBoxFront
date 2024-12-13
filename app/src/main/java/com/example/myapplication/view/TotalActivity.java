
package com.example.myapplication.view;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.FriendFragment;
import com.example.myapplication.HomeFragment;
import com.example.myapplication.InfoFragment;
import com.example.myapplication.Interface.FragmentInterface;
import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import com.example.myapplication.mFragmentPagerAdapter;
import com.example.myapplication.util.Common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    // 声明ViewPager
    private ViewPager ViewPager;
    private Toolbar toolbar;
    // 适配器
    private FragmentPagerAdapter Adapter;
    // 装载Fragment的集合
    private List<Fragment> FragmentList;

    // 三个Tab点击对应的布局
    private LinearLayout HomeBottomTab;
    private LinearLayout InfoBottomTab;
    private LinearLayout FriendBottomTab;

    // 三个Tab点击对应的Text字
    private TextView HomeBottomTabText;
    private TextView InfoBottomTabText;
    private TextView FriendBottomTabText;
    private ImageButton backButton;

    // 三个页面的Fragment
    private Fragment HomeFragment;
    private  Fragment InfoFragment;
    private  Fragment FriendFragment;

    // 获取FragmentManager对象
    FragmentManager mFragmentManager = getSupportFragmentManager();
    // 获取FragmentTransaction对象
    FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉TitleBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 指定布局界面
        setContentView(R.layout.activity_total);
        host = Common.user;
        Getheader();
        initViews();    // 初始化控件
        initEvents();   // 初始化事件
        initDatas();    // 初始化数据
    }

    // 初始化控件
    private void initViews() {
        ViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        backButton = findViewById(R.id.backButton);

        HomeBottomTab = (LinearLayout) findViewById(R.id.id_homebottomtab);
        InfoBottomTab = (LinearLayout) findViewById(R.id.id_infobottomtab);
        FriendBottomTab = (LinearLayout) findViewById(R.id.id_friendbottomtab);

        HomeBottomTabText = (TextView) findViewById(R.id.id_homebottomtab_text);
        InfoBottomTabText = (TextView)findViewById(R.id.id_infobottomtab_text);
        FriendBottomTabText = (TextView)findViewById(R.id.id_friendbottomtab_text);
    }
    private void initEvents() {
        // 设置三个Tab点击的点击事件
        HomeBottomTab.setOnClickListener(this);
        InfoBottomTab.setOnClickListener(this);
        FriendBottomTab.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }
    private void initDatas() {
        FragmentList = new ArrayList<>();
        // 将三个Fragment加入集合中
        FragmentList.add(new HomeFragment());
        FragmentList.add(new FriendFragment());
        FragmentList.add(new InfoFragment(host));

        // 初始化适配器
        Adapter = new mFragmentPagerAdapter(mFragmentManager, FragmentList);

        // 设置ViewPager的适配器
        ViewPager.setAdapter(Adapter);

        // 设置ViewPager手指滑动切换页面的监听
        ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            // 页面滚动事件
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            // 页面选中事件
            @Override
            public void onPageSelected(int position) {
                // 设置position对应的集合中的Fragment
                ViewPager.setCurrentItem(position);
//                System.out.println("打印："+position);
                selectTabBtn(position);
            }

            @Override
            // 页面滚动状态改变事件
            public void onPageScrollStateChanged(int state) {

            }
        });
        ViewPager.setCurrentItem(0);
        TextView BottomBarText_home = (TextView)findViewById(R.id.id_homebottomtab_text);
        BottomBarText_home.setTextColor(Color.parseColor("#c47731"));
        System.out.println("初始化了");
    }

    @Override
    public void onClick(View v) {
        // 根据点击的Tab进行响应
        if(v.getId() == R.id.id_homebottomtab) {
            selectTabBtn(0);
        }
        if(v.getId() == R.id.id_friendbottomtab) {
            selectTabBtn(1);
        }
        if(v.getId() == R.id.id_infobottomtab) {
            selectTabBtn(2);
        }
        if (v.getId() == R.id.backButton) {
            finish(); // 返回上一个界面
        }

    }

    private void selectTabBtn(int i) {

        // 根据点击的Tab按钮设置对应的响应
        TextView TopBarTitle = (TextView)findViewById(R.id.topbar_title);
        TextView BottomBarText_home = (TextView)findViewById(R.id.id_homebottomtab_text);
        TextView BottomBarText_info = (TextView)findViewById(R.id.id_infobottomtab_text);
        TextView BottomBarText_friend = (TextView)findViewById(R.id.id_friendbottomtab_text);
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
        ViewPager.setCurrentItem(i);
    }
    public void photo() {//调用系统相册选择图片
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1000);//打开相册
    }
    public void exit() {
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//相册的调用回调
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {//判断是否是我们通过photo()发起的
            if (resultCode == TotalActivity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));//获取位图
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    int index = ViewPager.getCurrentItem();// 当前可见的fragment
                    Fragment fragment = (Fragment) ViewPager.getAdapter().instantiateItem(ViewPager,index);// fragment中的某一控件
                    ImageView headimg = fragment.getView().findViewById(R.id.header);
                    Bitmap circleBitmap = Common.getLargestCircleBitmap(bitmap);
                    headimg.setImageBitmap(circleBitmap);
                    Common.bitmap = circleBitmap;
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                FileOutputStream output;
                try
                {
                    output = openFileOutput(Common.user.getPhone()+".png", MODE_PRIVATE);
                    output.write(outputStream.toByteArray());
                    output.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                File file = this.getFileStreamPath(Common.user.getPhone()+".png");
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                MultipartBody multipartBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), body)
                        .build();
                String phonenumber = Common.user.getPhone();
                Request request = new Request.Builder()
                        .url(Common.URL+"/upload")
                        .post(multipartBody)
                        .addHeader("phone",phonenumber)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {//回调的方法执行在子线程。
                            System.out.println("succeed");
                            Common.user.setIschanged(1);
                        } else {
                            System.out.println("fail");
                        }
                    }
                });
            }
        }
    }
    public void Getheader() {
        OkHttpClient client = new OkHttpClient();
        Bitmap bitmap;
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
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {//回调的方法执行在子线程。
                    byte[] data = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap circleBitmap = Common.getLargestCircleBitmap(bitmap);
                    Common.bitmap = circleBitmap;

                } else {
                    System.out.println("fail");
                }
            }
        });
    }

}