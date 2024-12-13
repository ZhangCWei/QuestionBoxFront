package com.example.myapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.widget.ListView;

import com.example.myapplication.entity.User;
import com.example.myapplication.listviewItem;
import com.example.myapplication.mBottomSheetDialogFragment;
import com.example.myapplication.mListAdapter;

import java.util.ArrayList;
import java.util.List;

public class Common {

    public static User user;
    public static  Bitmap bitmap;
    public static String URL = "http://43.136.61.147:8080";
    public static List<listviewItem> lvItemList = new ArrayList<listviewItem>();
    public static ListView listView;
    public static mListAdapter adapter;
    public static ArrayList<Integer> idList = new ArrayList<>();
    public static ArrayList<String> questionList = new ArrayList<>();
    public static ArrayList<String> answerList = new ArrayList<>();
    public static ArrayList<String> sourcephoneList = new ArrayList<>();
    public static ArrayList<String> targetphoneList = new ArrayList<>();
    public static ArrayList<String> questiontimeList = new ArrayList<>();
    public static ArrayList<String> answertimeList = new ArrayList<>();
    public static ArrayList<String> stateList = new ArrayList<>();
    public static ArrayList<String> AskanswerList = new ArrayList<>();


//    public static ArrayList<Integer> idList;
//    public static ArrayList<String> questionList;
//    public static ArrayList<String> answerList;
//    public static ArrayList<String> sourcephoneList;
//    public static ArrayList<String> targetphoneList;
//    public static ArrayList<String> questiontimeList;
//    public static ArrayList<String> answertimeList ;

    public static int nowpos;   // 当前选中的问题在列表中的索引值
    public static mBottomSheetDialogFragment BottomSheet = new mBottomSheetDialogFragment();
    public static int hometabNum = 0;   // 指示首页现在在4个小tab页面的哪一个
    public static Bitmap getLargestCircleBitmap(Bitmap bitmap) {
        int diameter = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap circleBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);

        Canvas canvas = new Canvas(circleBitmap);
        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return circleBitmap;
    }

}
