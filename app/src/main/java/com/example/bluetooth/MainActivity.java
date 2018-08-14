package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CONNECT_DEVICE = 1;

    /**
     * 页面的控制类
     **/
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;

    private TextView tv_content;

    private Button button_conn;
    private Button button_print;
    private LinearLayout linearLayout_Button;
    private BluetoothService mService = null;
    private SealView seal_view;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        StringBuffer sb = new StringBuffer();
        sb.append("━━━━神兽出没━━━━━");
        sb.append("\n━━神兽保佑，代码无bug━━");



        tv_content.setText(sb);
    }

    private void initView() {
        tv_content = (TextView) findViewById(R.id.tv_content);
        button_conn = (Button) findViewById(R.id.button_conn);
        button_print = (Button) findViewById(R.id.button_print);
        linearLayout_Button = (LinearLayout) findViewById(R.id.linearLayout_Button);
        imageView = findViewById(R.id.imageView);
        seal_view = findViewById(R.id.seal_view);

        button_conn.setOnClickListener(this);
        button_print.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_conn:
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            case R.id.button_print:
                //打印文字
//                sendMessage(tv_content.getText().toString());

                final Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.ic_111);
                int width = b.getWidth();
                int height = b.getHeight();
                // 设置想要的大小
                int newWidth = 330;
                int newHeight = 330;
                // 计算缩放比例
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                final Bitmap mbitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
                imageView.setImageBitmap(mbitmap);


                //打印图片
                new Thread(new Runnable() {
                    @Override
                    public void run() {

//                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_96);
                        byte[] bytes = PrintUtil.draw2PxPoint(mbitmap );
                        mService.write(bytes);
                    }
                }).start();

                break;
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "连接错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send;
            try {
                send = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
            }
            mService.write(send);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mService == null)
                mService = new BluetoothService(this, mHandler);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mService == null)
                        mService = new BluetoothService(this, mHandler);
                } else {
                    Toast.makeText(this, "用户未启用蓝牙或蓝牙异常", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * Sends a message. 打印图片
     * <p>
     * A string of text to send.
     */
    private void sendMessage(Bitmap bitmap) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }
        // 发送打印图片前导指令
        byte[] start = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B,
                0x40, 0x1B, 0x33, 0x00};
        mService.write(start);

        /** 获取打印图片的数据 **/
        byte[] send = getReadBitMapBytes(bitmap);
        mService.write(send);
        // 发送结束指令
        byte[] end = {0x1d, 0x4c, 0x1f, 0x00};
        mService.write(end);
    }

    /**
     * 解析图片 获取打印数据
     **/
    private byte[] getReadBitMapBytes(Bitmap bitmap) {
        /*** 图片添加水印 **/
//        bitmap = createBitmap(bitmap);
        byte[] bytes = null; // 打印数据
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        System.out.println("width=" + width + ", height=" + height);
        int heightbyte = (height - 1) / 8 + 1;
        int bufsize = width * heightbyte;
        int m1, n1;
        byte[] maparray = new byte[bufsize];

        byte[] rgb = new byte[3];

        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        /** 解析图片 获取位图数据 **/
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int pixel = pixels[width * j + i];
                /** 获取ＲＧＢ值 **/
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                rgb[0] = (byte) r;
                rgb[1] = (byte) g;
                rgb[2] = (byte) b;
                if (r != 255 || g != 255 || b != 255) {// 如果不是空白的话用黑色填充
                    m1 = (j / 8) * width + i;
                    n1 = j - (j / 8) * 8;
                    maparray[m1] |= (byte) (1 << 7 - ((byte) n1));
                }
            }
        }
        byte[] b = new byte[322];
        int line = 0;
        int j = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /** 对位图数据进行处理 **/
        for (int i = 0; i < maparray.length; i++) {
            b[j] = maparray[i];
            j++;
            if (j == 322) {
                /** 322图片的宽 **/
                if (line < ((322 - 1) / 8)) {
                    byte[] lineByte = new byte[329];
                    byte nL = (byte) 322;
                    byte nH = (byte) (322 >> 8);
                    int index = 5;
                    /** 添加打印图片前导字符 **/
                    lineByte[0] = 0x1B;
                    lineByte[1] = 0x2A;
                    lineByte[2] = 1;
                    lineByte[3] = nL;
                    lineByte[4] = nH;
                    /** copy 数组数据 **/
                    System.arraycopy(b, 0, lineByte, index, b.length);

                    lineByte[lineByte.length - 2] = 0x0D;
                    lineByte[lineByte.length - 1] = 0x0A;
                    baos.write(lineByte, 0, lineByte.length);
                    try {
                        baos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    line++;
                }
                j = 0;
            }
        }
        bytes = baos.toByteArray();
        return bytes;
    }

    // 给图片添加水印
    private Bitmap createBitmap(Bitmap src) {
        Time t = new Time();
        t.setToNow();
        int w = src.getWidth();
        int h = src.getHeight();
        String mstrTitle = t.year + " 年 " + (t.month + 1) + " 月 " + t.monthDay
                + " 日";
        Bitmap bmpTemp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpTemp);
        Paint p = new Paint();
        String familyName = "宋体";
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        p.setColor(Color.BLACK);
        p.setTypeface(font);
        p.setTextSize(18);
        canvas.drawBitmap(src, 0, 0, p);
        canvas.drawText(mstrTitle, 20, 310, p);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmpTemp;
    }


}
