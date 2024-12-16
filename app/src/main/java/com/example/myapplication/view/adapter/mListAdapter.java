package com.example.myapplication.view.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.R;
import com.example.myapplication.util.Common;
import com.example.myapplication.util.listviewItem;
import com.example.myapplication.view.QaDetailActivity;

import java.util.List;

public class mListAdapter extends ArrayAdapter<listviewItem> {
    private final int resourceId;
    private final FragmentActivity mContext;

    public mListAdapter(FragmentActivity context, int textViewResourceId, List<listviewItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        @SuppressLint("ViewHolder")
        View mview = LayoutInflater.from(getContext()).inflate(resourceId, null);

        listviewItem lvItem = getItem(position);
        TextView question = mview.findViewById(R.id.question);
        TextView questionTime = mview.findViewById(R.id.questiontime);
        assert lvItem != null;
        question.setText(lvItem.getQuestion());
        questionTime.setText(lvItem.getQuestionTime());

        LinearLayout ll_question = mview.findViewById(R.id.ll_question);
        ll_question.setOnClickListener(new View.OnClickListener() {
            Intent intent;
            @Override
            public void onClick(View view) {
                // Toast.makeText(getContext(),"你点击了第"+position+"项",Toast.LENGTH_SHORT).show();
                // TextView qtext = ll_question.findViewById(R.id.question);
                TextView qText = view.findViewById(R.id.question);
                String question = qText.getText().toString();
                Common.nowpos = Common.questionList.indexOf(question);
                System.out.println(question);
                Common.nowpos = Common.questionList.indexOf(question);
                intent = new Intent(mContext, QaDetailActivity.class);
                mContext.startActivity(intent);
            }
        });

        TextView deleteBtn = mview.findViewById(R.id.deletebtn);
        deleteBtn.setOnClickListener(view -> {
            TextView text = mview.findViewById(R.id.question);
            String question1 = text.getText().toString();
            Common.nowpos = Common.questionList.indexOf(question1);
            // System.out.println("pos"+Common.nowpos);
            // System.out.println("id"+Common.idList.get(Common.nowpos));
            // 在适配器里要使用getSupportFragmentManager()，要通过FragmentActivity类型来获取，要将原本Context改为FragmentActivity
            Common.BottomSheet.show(mContext.getSupportFragmentManager(), "Dialog");
        });

        return mview;
    }

}
