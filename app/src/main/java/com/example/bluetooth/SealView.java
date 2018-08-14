package com.example.bluetooth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by xiangzhihong on 2016/4/1 on 18:07.
 */
public class SealView extends View {

    private int circleStrokeWidth, centre, circleRadius;
    private float textSize, textPadding;
    private int scaleTextColor, circleColor;
    //    private String str = "华为科技技术有效公司";
    private String circleText = null;
    private int radius;

    public SealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(attrs, 0);
    }

    private void initViews(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Seal, defStyle, 0);
        circleText = typedArray.getString(R.styleable.Seal_scale_text);
        textSize = typedArray.getDimension(R.styleable.Seal_scale_text_size, 20);
        scaleTextColor = typedArray.getColor(R.styleable.Seal_scale_text_color, getResources().getColor(R.color.c9));
        textPadding = typedArray.getFloat(R.styleable.Seal_scale_text_padding, 50);
        circleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.Seal_circle_stroke_width, 3);
        circleColor = typedArray.getColor(R.styleable.Seal_circle_color, getResources().getColor(R.color.c9));
        circleRadius = typedArray.getDimensionPixelSize(R.styleable.Seal_circle_radius, 7);
        typedArray.recycle();
    }


    @Override
    protected void onDraw(Canvas rootCanvas) {
        super.onDraw(rootCanvas);
        Bitmap image = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();

        drawRing(canvas, paint);
        drawStar(canvas);
        drawText(canvas);
        rootCanvas.drawBitmap(image, 0, 0, null);
    }

    private void drawRing(Canvas canvas, Paint paint) {
        centre = canvas.getWidth() / 2; // 获取圆心的x坐标
        radius = (int) (centre - circleStrokeWidth / 2); // 圆环的半径
        paint.setColor(Color.BLACK); // 设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setStrokeWidth(circleStrokeWidth); // 设置圆环的宽度
        paint.setAntiAlias(true); // 消除锯齿
        canvas.drawCircle(centre, centre, radius, paint); // 画出圆环

    }

    //绘制五角星
    private void drawStar(Canvas canvas) {
        float start_radius = (float) ((radius / 2) * 1.1);
        int x = centre, y = centre;
        float x1, y1, x2, y2, x3, y3, x4, y4, x5, y5;
        float r72 = (float) Math.toRadians(72);
        float r36 = (float) Math.toRadians(36);
        //顶点
        x1 = x;
        y1 = y - start_radius;
        //左1
        x2 = (float) (x - start_radius * Math.sin(r72));
        y2 = (float) (y - start_radius * Math.cos(r72));
        //右1
        x3 = (float) (x + start_radius * Math.sin(r72));
        y3 = (float) (y - start_radius * Math.cos(r72));
        //左2
        x4 = (float) (x - start_radius * Math.sin(r36));
        y4 = (float) (y + start_radius * Math.cos(r36));
        //右2
        x5 = (float) (x + start_radius * Math.sin(r36));
        y5 = (float) (y + start_radius * Math.cos(r36));

        //连接各个节点，绘制五角星
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x5, y5);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.close();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        canvas.drawPath(path, paint);
    }

    private void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        //圆弧文字所在矩形范围
        RectF oval = new RectF(0, 0, 2 * radius, (float) (2 * radius));
        //第一个文字偏移角度，其中padding/2为文字间距
        float firstrad = 90 + textPadding * (circleText.length()) / 4 - textPadding / 8;

        double canshu = 3;
        if (circleText.length() > 20) {
            canshu = 3.6;
        } else if (circleText.length() > 15) {
            canshu = 3;
        }else if (circleText.length() > 10){
            canshu = 2.5;
        }else{
            canshu = 2;
        }

        for (int i = 0; i < circleText.length(); i++) {
            Path path = new Path();
            //根据角度生成弧线路径
            path.addArc(oval, -(firstrad - (float) (textPadding * i / canshu)), textPadding);
            canvas.drawTextOnPath(String.valueOf(circleText.charAt(i)), path, -(float) (radius / 4), (float) (radius / 4), paint);
        }
    }
}
