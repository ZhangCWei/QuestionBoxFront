package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.example.myapplication.entity.User;
import com.example.myapplication.util.Common;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoFragment extends Fragment {
        private User host;
        private ImageView headimg;
        private  View tabView;
        private FragmentInterface Listener;

        public InfoFragment(User host) {
                this.host = host;
        }

        @Nullable
        @Override
        public void onAttach(Context context) {
                super.onAttach(context);
                if (context instanceof FragmentInterface) {
                        Listener = (FragmentInterface) context;
                } else {
                        throw new RuntimeException(context.toString()
                                + " must implement OnListener");
                }
        }

        Handler handler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        Bundle bundle = (Bundle)msg.obj;//在主线程接收
                        byte[] c=  bundle.getByteArray("bytes");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(c, 0, c.length);
                        Bitmap circleBitmap = Common.getLargestCircleBitmap(bitmap);
                        headimg.setImageBitmap(circleBitmap);
                }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                tabView = inflater.inflate(R.layout.tab_info, container, false);
                TextView txt = tabView.findViewById(R.id.name);
                txt.setText(host.getName());
                EditText Name = tabView.findViewById(R.id.showname);
                EditText Password = tabView.findViewById(R.id.showpassword);
                EditText Phone = tabView.findViewById(R.id.showphone);
                Name.setText(host.getName());
                Password.setText(host.getRealpassword());
                Phone.setText(host.getPhone());
                Name.setEnabled(false);
                Password.setEnabled(false);
                Phone.setEnabled(false);
                headimg = tabView.findViewById(R.id.header);
                if(host.getIschanged()==1) {
                        Getheader();
                }
                else
                {
                        Bitmap icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_1);
                        Bitmap circleBitmap = Common.getLargestCircleBitmap(icon);
                        headimg.setImageBitmap(circleBitmap);
                }
                headimg.setOnClickListener(this::onClickUpload);
                Button ExitBtn = tabView.findViewById(R.id.exit);
                ExitBtn.setOnClickListener(this::onExit);
                Button Edit1= tabView.findViewById(R.id.edit1);
                Button Edit2= tabView.findViewById(R.id.edit2);
                Edit1.setOnClickListener(this::onEdit);
                Edit2.setOnClickListener(this::onEdit);
                return tabView;
        }

        public void onClickUpload(View v) {
                host.setIschanged(1);
                Common.user.setIschanged(1);
                Listener.photo();

        }
        public void onExit(View v) {
                Listener.exit();
        }
        public void onEdit(View v){
               Button Edit = tabView.findViewById(v.getId());
               if(Edit.getText().toString().equals("编辑")) {
                       if (v.getId() == R.id.edit1) {
                               EditText Name = tabView.findViewById(R.id.showname);
                               Name.setEnabled(true);
                               Edit.setText("保存");
                       } else if (v.getId() == R.id.edit2) {
                               EditText Password = tabView.findViewById(R.id.showpassword);
                               Password.setEnabled(true);
                               Edit.setText("保存");
                       }
               }
               else{
                       if (v.getId() == R.id.edit1) {
                               EditText Name = tabView.findViewById(R.id.showname);
                               AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                               alertDialogBuilder.setTitle("提示");
                               alertDialogBuilder.setMessage("是否确认修改？");
                               alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                               Save(Name.getText().toString(), 0);
                                               dialog.cancel();
                                       }
                               });
                               alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int id) {
                                               dialog.cancel();
                                       }
                               });
                               alertDialogBuilder.show();
                               Name.setEnabled(false);
                               Edit.setText("编辑");
                       } else if (v.getId() == R.id.edit2) {
                               EditText Password = tabView.findViewById(R.id.showpassword);
                               AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                               alertDialogBuilder.setTitle("提示");
                               alertDialogBuilder.setMessage("是否确认修改？");
                               alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                               Save(Password.getText().toString(), 1);
                                               dialog.cancel();
                                       }
                               });
                               alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int id) {
                                               dialog.cancel();
                                       }
                               });
                               alertDialogBuilder.show();
                               Password.setEnabled(false);
                               Edit.setText("编辑");
                       }

               }

        }

        public void Getheader() {
               headimg.setImageBitmap(Common.bitmap);
        }
        public void Save(String value, int option)
        {
                OkHttpClient client = new OkHttpClient();
                RequestBody body;
                Request request;
                if(option==0)
                {
                        body = new FormBody.Builder()
                                .add("name",value)
                                .add("phone", Common.user.getPhone())
                                .build();
                        request = new Request.Builder()
                                .url(Common.URL+"/changeName")
                                .post(body)
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
                }
                else if(option==1)
                {
                        body = new FormBody.Builder()
                                .add("password",value)
                                .add("phone", Common.user.getPhone())
                                .build();
                        request = new Request.Builder()
                                .url(Common.URL+"/changePassword")
                                .post(body)
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
                }
                else {
                        body = new FormBody.Builder()
                                .add("phone",value)
                                .build();
                        request = new Request.Builder()
                                .url(Common.URL+"/changePhone")
                                .post(body)
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
                }

                client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                                if(response.isSuccessful()){    // 回调的方法执行在子线程。
                                        System.out.println("succeed");
                                }
                                else {
                                        System.out.println("fail");
                                }
                        }
                });

        }

}