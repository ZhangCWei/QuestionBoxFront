package com.example.myapplication.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.widget.ListView;

import com.example.myapplication.entity.User;
import com.example.myapplication.view.fragment.mBottomSheetDialogFragment;
import com.example.myapplication.view.adapter.mListAdapter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Common {
    public static String URL = "http://192.168.3.200:8080";     // 这里不能填 loaclhost 或 127.0.0.1
    public static User user;
    public static  Bitmap bitmap;
    public static List<listviewItem> lvItemList = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static ListView listView;
    public static mListAdapter adapter;
    public static ArrayList<Integer> idList = new ArrayList<>();
    public static ArrayList<String> questionList = new ArrayList<>();
    public static ArrayList<String> answerList = new ArrayList<>();
    public static ArrayList<String> sourcePhoneList = new ArrayList<>();
    public static ArrayList<String> targetPhoneList = new ArrayList<>();
    public static ArrayList<String> questionTimeList = new ArrayList<>();
    public static ArrayList<String> answerTimeList = new ArrayList<>();
    public static ArrayList<String> stateList = new ArrayList<>();
    public static ArrayList<String> askAnswerList = new ArrayList<>();
    public static int nowpos;   // 当前选中的问题在列表中的索引值
    public static int hometabNum = 0;   // 指示首页位置
    public static mBottomSheetDialogFragment BottomSheet = new mBottomSheetDialogFragment();

    public static Bitmap getLargestCircleBitmap(Bitmap bitmap) {
        // 获取最小的边长作为圆的直径
        int diameter = Math.min(bitmap.getWidth(), bitmap.getHeight());

        // 创建空的圆形位图和位图着色器
        Bitmap circleBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // 创建画笔并设置着色器
        Paint paint = new Paint();
        paint.setShader(shader);

        // 创建画布并绘制圆形
        Canvas canvas = new Canvas(circleBitmap);
        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return circleBitmap;
    }

    // MD5 加密
    public static String MD5(String data) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] md5 = md.digest(data.getBytes(StandardCharsets.UTF_8));
            for (byte b : md5) {
                sb.append(Integer.toHexString(b & 0xff));
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

}
