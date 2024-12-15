package com.example.myapplication.view.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AskListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final int resource;
    private final List<String> items;
    List<byte[]> itemsImage;

    public AskListAdapter(Context context, int resource, List<String> items,List<byte[]> itemsImage) {
        // 声明上下文, 资源ID和条目列表
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
        this.itemsImage = itemsImage;
    }

    private Bitmap getLargestCircleBitmap(Context context, Bitmap bitmap) {
        // 获取最大圆形 Bitmap
        int diameter = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap circleBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setShader(shader);

        float radius = diameter / 2f;
        Canvas canvas = new Canvas(circleBitmap);
        canvas.drawCircle(radius, radius, radius, paint);

        return circleBitmap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 若为空, 使用布局填充器加载布局
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, null);
        }

        // 在布局中找到 TextView, 设置文本内容
        TextView textViewQuestion = view.findViewById(R.id.question);
        textViewQuestion.setText(items.get(position));

        // 在布局中找到 ImageView, 设置其图像
        ImageView image = view.findViewById(R.id.avator);
        if(itemsImage != null && itemsImage.size() > position){
            byte[] imageBytes = itemsImage.get(position);
            if (imageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Bitmap circleBitmap = getLargestCircleBitmap(context,bitmap);
                image.setImageBitmap(circleBitmap);
            }
        }
        return view;
    }
}
