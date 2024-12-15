package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.entity.Questionbox;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment implements View.OnClickListener {
    View tabView;
    private LinearLayout QmeUnansweredTab;
    private LinearLayout QmeAnsweredTab;
    private LinearLayout meQUnansweredTab;
    private LinearLayout meQAnsweredTab;

    private TextView QmeUnansweredTabText;
    private TextView QmeAnsweredTabText;
    private TextView meQUnansweredTabText;
    private TextView meQAnsweredTabText;

    private Gson gson = new Gson();
//    private ListView listView;

    List<Questionbox> QBox;

//    List<listviewItem> lvItemList;

    class Threads_GetBox extends Thread {
        // 获取提问箱列表
        private OkHttpClient client = null;
        String phone = null;
        String state = null;
        String server = null;
        @Override
        public void run() {
            client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("phone", phone)
                    .add("state", state)
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
                    System.out.println("fail to get box!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){//回调的方法执行在子线程。
                        String QBoxJson = response.body().string();
                        QBox = gson.fromJson(QBoxJson, new TypeToken<ArrayList<Questionbox>>(){}.getType());
//                        System.out.println("QBox"+QBox);
//                        Common.idList = new ArrayList<>();
//                        Common.questionList = new ArrayList<>();
//                        Common.answerList = new ArrayList<>();
//                        Common.sourcephoneList = new ArrayList<>();
//                        Common.targetphoneList = new ArrayList<>();
//                        Common.questiontimeList = new ArrayList<>();
//                        Common.answertimeList = new ArrayList<>();
                        Common.idList.clear();
                        Common.questionList.clear();
                        Common.answerList.clear();
                        Common.sourcePhoneList.clear();
                        Common.targetPhoneList.clear();
                        Common.questionTimeList.clear();
                        Common.answerTimeList.clear();
                        Common.stateList.clear();
                        for(Questionbox qb : QBox){
                            Common.idList.add(qb.getId());
                        }
                        for(Questionbox qb : QBox){
                            Common.questionList.add(qb.getQuestion());
                        }
                        for(Questionbox qb : QBox){
                            Common.answerList.add(qb.getAnswer());
                        }
                        for(Questionbox qb : QBox){
                            Common.sourcePhoneList.add(qb.getSourcePhone());
                        }
                        for(Questionbox qb : QBox){
                            Common.targetPhoneList.add(qb.getTargetPhone());
                        }
                        for(Questionbox qb : QBox){
                            Common.questionTimeList.add(qb.getQuestionTime());
                        }
                        for(Questionbox qb : QBox){
                            Common.answerTimeList.add(qb.getAnswerTime());
                        }
                        for(Questionbox qb : QBox){
                            Common.stateList.add(qb.getState());
                        }
//                        System.out.println("stateList="+Common.stateList);
//                        System.out.println("congratulation!");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Common.listView = (ListView) tabView.findViewById(R.id.listview_1);
//                                Common.lvItemList = new ArrayList<listviewItem>();
                                Common.lvItemList.clear();
                                InitlvItem();
//                                System.out.println("ql"+Common.questionList);
//                                System.out.println("tl"+Common.timeList);
//                                System.out.println("ll"+lvItemList);
//                                ArrayAdapter<String> adapter = new ArrayAdapter<>(tabView.getContext(), android.R.layout.simple_list_item_1, questionList);
                                Common.adapter = new mListAdapter(getActivity(), R.layout.listview_item, Common.lvItemList);
                                Common.listView.setAdapter(Common.adapter);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tabView = inflater.inflate(R.layout.tab_home, container, false);
        InitData();
        InitEvents();
        return tabView;
    }

    @Override
    public void onResume() {
        System.out.println("onResume:"+Common.hometabNum);
        super.onResume();
        // 从上个界面返回后更新数据
        if(Common.hometabNum == 0){
            selectTabBtn(0);
        }
        if(Common.hometabNum == 1){
            selectTabBtn(1);
        }
        if(Common.hometabNum == 2){
            selectTabBtn(2);
        }
        if(Common.hometabNum == 3){
            selectTabBtn(3);
        }
    }

    public void InitEvents(){
        QmeUnansweredTab.setOnClickListener(this);
        QmeAnsweredTab.setOnClickListener(this);
        meQUnansweredTab.setOnClickListener(this);
        meQAnsweredTab.setOnClickListener(this);
    }

    public void InitData(){
        QmeUnansweredTab = (LinearLayout) tabView.findViewById(R.id.id_QmeUnanswered);
        QmeAnsweredTab = (LinearLayout) tabView.findViewById(R.id.id_QmeAnswered);
        meQUnansweredTab = (LinearLayout) tabView.findViewById(R.id.id_meQUnanswered);
        meQAnsweredTab = (LinearLayout) tabView.findViewById(R.id.id_meQAnswered);

        QmeUnansweredTabText = (TextView) tabView.findViewById(R.id.id_QmeUnanswered_text);
        QmeAnsweredTabText = (TextView) tabView.findViewById(R.id.id_QmeAnswered_text);
        meQUnansweredTabText = (TextView) tabView.findViewById(R.id.id_meQUnanswered_text);
        meQAnsweredTabText = (TextView) tabView.findViewById(R.id.id_meQAnswered_text);

        QmeUnansweredTabText.setTextColor(Color.parseColor("#000000"));
        QmeAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
        meQUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
        meQAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
//        Threads_GetBox GetBox_0 = new Threads_GetBox();
//        System.out.println("错在这？");
//        GetBox_0.phone = Common.user.getPhone();
//        GetBox_0.state = "0";
//        GetBox_0.server = "/gettarget";
//        GetBox_0.start();
    }
    private void InitlvItem() {
        int length = Common.questionList.size();
//        System.out.println("qlsize="+length);
        int i = 0;
        while(i < length){
            listviewItem lvitem = new listviewItem(Common.questionList.get(i), Common.questionTimeList.get(i));
            Common.lvItemList.add(lvitem);
            i++;
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.id_QmeUnanswered) {
            Common.hometabNum = 0;
            selectTabBtn(0);
        }
        if(v.getId() == R.id.id_QmeAnswered) {
            Common.hometabNum = 1;
            selectTabBtn(1);
        }
        if(v.getId() == R.id.id_meQUnanswered) {
            Common.hometabNum = 2;
            selectTabBtn(2);
        }
        if(v.getId() == R.id.id_meQAnswered) {
            Common.hometabNum = 3;
            selectTabBtn(3);
        }
        System.out.println("click:"+Common.hometabNum);
    }
    public void selectTabBtn(int i) {

        // 根据点击的Tab按钮设置对应的响应
        switch (i) {
            case 0:
                QmeUnansweredTabText.setTextColor(Color.parseColor("#000000"));
                QmeAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                Threads_GetBox GetBox_1 = new Threads_GetBox();
                GetBox_1.phone = Common.user.getPhone();
                GetBox_1.state = "0";
                GetBox_1.server = "/gettarget";
                GetBox_1.start();
                break;

            case 1:
                QmeAnsweredTabText.setTextColor(Color.parseColor("#000000"));
                QmeUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                Threads_GetBox GetBox_2 = new Threads_GetBox();
                GetBox_2.phone = Common.user.getPhone();
                GetBox_2.state = "1";
                GetBox_2.server = "/gettarget";
                GetBox_2.start();
                break;

            case 2:
                meQUnansweredTabText.setTextColor(Color.parseColor("#000000"));
                QmeUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                QmeAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                Threads_GetBox GetBox_3 = new Threads_GetBox();
                GetBox_3.phone = Common.user.getPhone();
                GetBox_3.state = "0";
                GetBox_3.server = "/getsource";
                GetBox_3.start();
                break;

            case 3:
                meQAnsweredTabText.setTextColor(Color.parseColor("#000000"));
                QmeUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                QmeAnsweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                meQUnansweredTabText.setTextColor(Color.parseColor("#9c9c9c"));
                Threads_GetBox GetBox_4 = new Threads_GetBox();
                GetBox_4.phone = Common.user.getPhone();
                GetBox_4.state = "1";
                GetBox_4.server = "/getsource";
                GetBox_4.start();
                break;
        }
    }
}