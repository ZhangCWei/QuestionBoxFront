package com.example.myapplication;


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

import org.jetbrains.annotations.Nullable;

import java.util.List;

class AskListAdapter extends ArrayAdapter<String> {
    private Context context;
    private int resource;
    private List<String> items;
    List<byte[]> itemsImage;

    public AskListAdapter(Context context, int resource, List<String> items,List<byte[]> itemsImage) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
        this.itemsImage = itemsImage;
    }

    private Bitmap getLargestCircleBitmap(Context context, Bitmap bitmap) {
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

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, null);
        }

        // Find the TextView in your listview_item_ask.xml
        TextView textViewQuestion = view.findViewById(R.id.question);

        // Set the text for the TextView
        textViewQuestion.setText(items.get(position));

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
