package com.example.myapplication.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Interface.FragmentInterface;
import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import com.example.myapplication.util.Common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoFragment extends Fragment {
    private final User host;
    private ImageView headImg;
    private  View tabView;
    private FragmentInterface Listener;

    public InfoFragment(User host) {
        this.host = host;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            Listener = (FragmentInterface) context;
        } else {
            throw new RuntimeException(context + " must implement OnListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 将布局文件转换为视图
        tabView = inflater.inflate(R.layout.tab_info, container, false);

        // 显示用户名称
        TextView txt = tabView.findViewById(R.id.name);
        txt.setText(host.getUsername());

        // 设置EditText文本内容
        EditText Name = tabView.findViewById(R.id.showname);
        EditText Password = tabView.findViewById(R.id.showpassword);
        EditText Phone = tabView.findViewById(R.id.showphone);

        Name.setText(host.getUsername());
        Password.setText(host.getPassword());
        Phone.setText(host.getPhone());

        // 禁用EditText编辑功能
        Name.setEnabled(false);
        Password.setEnabled(false);
        Phone.setEnabled(false);

        headImg = tabView.findViewById(R.id.header);

        // 根据用户信息设置头像
        if(host.getIsChanged() == 1) {
            GetHeader();
        } else {
            // 设置默认头像
            Bitmap icon = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.defaultheader);
            Bitmap circleBitmap = Common.getLargestCircleBitmap(icon);
            headImg.setImageBitmap(circleBitmap);
        }

        // 头像的点击事件监听器
        headImg.setOnClickListener(this::onClickUpload);

        // 退出按钮的点击事件监听器
        Button ExitBtn = tabView.findViewById(R.id.exit);
        ExitBtn.setOnClickListener(this::onExit);

        // 编辑按钮的点击事件监听器
        Button Edit1= tabView.findViewById(R.id.edit1);
        Button Edit2= tabView.findViewById(R.id.edit2);
        Edit1.setOnClickListener(this::onEdit);
        Edit2.setOnClickListener(this::onEdit);

        return tabView;
    }

    public void onClickUpload(View v) {
        // 上传头像
        host.setIsChanged(1);
        Common.user.setIsChanged(1);
        Listener.photo();
    }

    public void onExit(View v) {
        // 退出账号
        Listener.exit();
    }

    private void showAlertDialog(String text, int option) {
        // 创建确认修改的对话框
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("提示");
        alertDialogBuilder.setMessage("是否确认修改？");
        alertDialogBuilder.setPositiveButton("确定", (dialog, which) -> {
            Save(text, option);
            dialog.cancel();
        });
        alertDialogBuilder.setNegativeButton("取消", (dialog, id) -> dialog.cancel());
        alertDialogBuilder.show();
    }

    public void onEdit(View v){
        Button Edit = tabView.findViewById(v.getId());
        EditText Name = tabView.findViewById(R.id.showname);
        EditText Password = tabView.findViewById(R.id.showpassword);
        if(Edit.getText().toString().equals("编辑")) {
            Edit.setText("保存");
            if (v.getId() == R.id.edit1) Name.setEnabled(true);
            else if (v.getId() == R.id.edit2) Password.setEnabled(true);
        } else {
            Edit.setText("编辑");
            if (v.getId() == R.id.edit1) {
                String name = Name.getText().toString();
                showAlertDialog(name, 0);
                Name.setEnabled(false);
            }
            else if (v.getId() == R.id.edit2) {
                String password = Common.MD5(Password.getText().toString());
                showAlertDialog(password, 1);
                Password.setEnabled(false);
            }
        }
    }

    public void GetHeader() {
        headImg.setImageBitmap(Common.bitmap);
    }

    public void Save(String value, int option) {
        // 保存修改的信息
        OkHttpClient client = new OkHttpClient();
        String phone = Common.user.getPhone();
        RequestBody body;
        String url;

        if(option == 0) {
            body = new FormBody.Builder().add("name", value).add("phone", phone).build();
            url = Common.URL + "/changeName";
        } else if (option == 1) {
            body = new FormBody.Builder().add("password", value).add("phone", phone).build();
            url = Common.URL + "/changePassword";
        } else {
            body = new FormBody.Builder().add("phone", value).build();
            url = Common.URL + "/changePhone";
        }

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Logger logger = Logger.getLogger(getClass().getName());
                logger.log(Level.SEVERE, "Request failed", e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    System.out.println("succeed");
                } else {
                    System.out.println("fail");
                }
            }
        });
    }

}