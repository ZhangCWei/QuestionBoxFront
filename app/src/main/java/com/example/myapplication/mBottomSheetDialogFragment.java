package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.myapplication.util.Common;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class mBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener{

    class Threads_DeleteItem extends Thread {
        // 删除提问箱列表中的某项
        private OkHttpClient client = null;
        String id = null;
        String server = null;

        @Override
        public void run() {
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
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
                    System.out.println("fail to delete!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){//回调的方法执行在子线程。
                        System.out.println("delete!");
//                        非UI线程（Thread-3）中尝试操作UI视图会报错闪退，只有创建UI视图的原始线程（通常是主线程）才能操作它们。
//                        所以需要确保在主线程中更新UI视图。在Android中，可以使用runOnUiThread方法或Handler来在主线程中执行操作。
//                        尝试在非UI线程中更新ListView的适配器（Common.adapter.notifyDataSetChanged();）会导致异常。
//                        为了解决这个问题，可以在run方法中使用runOnUiThread方法或Handler将notifyDataSetChanged操作包装在主线程中。
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Common.lvItemList.remove(Common.nowpos);
                                Common.adapter.notifyDataSetChanged();
                            }
                        });
//                        Toast.makeText(getContext(), "删除成功！",Toast.LENGTH_SHORT).show();
                        Common.BottomSheet.dismiss();
                    }
                    else {
                        System.out.println("wrong");
                    }
                }
            });
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        Button deletebutton = view.findViewById(R.id.deletebutton);
        deletebutton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("提示");
        alertDialogBuilder.setMessage("是否确认删除该条？");
        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteItem();
                dialog.cancel();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Common.BottomSheet.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    public void DeleteItem(){
        Threads_DeleteItem DeleteItem = new Threads_DeleteItem();
        DeleteItem.id = Common.idList.get(Common.nowpos).toString();
        System.out.println("id=" + DeleteItem.id);
        DeleteItem.server = "/DeleteItem";
        DeleteItem.start();
    }
}
