package com.xinyue.pilotcertificate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by cc_want on 2017/7/6.
 */
public class Synthesizing {

    private Context mContext;
    private Bitmap mBitmap;
    private Paint mPaint;
    private String mOutputDir;

    public Synthesizing(Context context){
        mContext = context;
        init();
    }
    private void init(){
        InputStream is = null;
        try {
            // is = mContext.getResources().getAssets().open("images/PilotCertificate.jpg");
            is = mContext.getResources().getAssets().open("images/back.png");
            mBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Typeface typeFace = Typeface.createFromAsset(mContext.getResources().getAssets(),"fonts/fangzhenglanting-hei.TTF");

        mPaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(45);
        mPaint.setColor(Color.BLACK);
        mPaint.setTypeface(typeFace);
    }
    public void setOutputDir(String path){
        mOutputDir = path;
    }

    public void create(String json) {
        changeProgress(0);
        // 解析json
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<ParseBean>>(){}.getType();
        List<ParseBean> list = gson.fromJson(json, collectionType);
        if(mBitmap == null){
            throw new IllegalArgumentException("Bitmap file not found.");
        }
        if(TextUtils.isEmpty(mOutputDir)){
            throw new IllegalArgumentException("Output dir is null.");
        }
        changeProgress(20);

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        //创建一个bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(bitmap);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(mBitmap, 0, 0, null);
        int initHeightLow = 280;
        int initHeightHigh = 360;
        ParseBean bean;
        for (int i = 0; i < list.size(); i++) {
            bean = list.get(i);
            initHeightLow += 3;
            initHeightHigh += 3;
            //画姓名
            drawText(canvas, bean.getWorkStatus(),260,initHeightLow,0.0f);
            //画国籍
            drawText(canvas, bean.getCreateDate(),260,initHeightHigh,0.0f);
            initHeightHigh +=  180;
            initHeightLow +=  180;
        }
        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();

        changeProgress(80);

        save(bitmap);
    }


    public void create(String name,String nationality,String type){
        changeProgress(0);
        if(mBitmap == null){
            throw new IllegalArgumentException("Bitmap file not found.");
        }
        if(TextUtils.isEmpty(mOutputDir)){
            throw new IllegalArgumentException("Output dir is null.");
        }
        changeProgress(20);

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        //创建一个bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(bitmap);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(mBitmap, 0, 0, null);
        //画姓名
        drawText(canvas,name,260,280,0.0f);
        //画国籍
        drawText(canvas,nationality,260,360,0.0f);

//        //画姓名
//        drawText(canvas,name,260,460,0.0f);
//        //画国籍
//        drawText(canvas,nationality,260,540,0.0f);
//
//        //画姓名
//        drawText(canvas,name,260,650,0.0f);
//        //画国籍
//        drawText(canvas,nationality,260,730,0.0f);
        //画驾驶员类型
       // drawText(canvas,type,205,267,2.2f);
        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();

        changeProgress(80);

        save(bitmap);
    }
    void drawText(Canvas canvas ,String text , float x ,float y ,float angle){
        if(angle != 0){
            canvas.rotate(angle, x, y);
        }
        canvas.drawText(text, x, y, mPaint);
        if(angle != 0){
            canvas.rotate(-angle, x, y);
        }
    }
    private void save(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        bitmap.recycle();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mOutputDir+"/output.jpg");
            fos.write(stream.toByteArray());
            complete();
            changeProgress(100);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Image file not found.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Image file io exception.");
        } finally {
            safeFlush(fos);
            safeClose(fos);
            safeClose(stream);
        }
    }
    private void safeFlush(Flushable flushable) {
        if (flushable == null)
            return;
        try {
            flushable.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void safeClose(Closeable closeable){
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void complete(){
        if(mOnSynthListener != null)
            mOnSynthListener.onComplete();
    }
    private void changeProgress(int progress){
        if(mOnSynthListener != null)
            mOnSynthListener.onProgress(progress);
    }
    private OnSynthListener mOnSynthListener;
    public void setOnSynthListener(OnSynthListener listener){
        mOnSynthListener = listener;
    }
    public interface OnSynthListener{
        void onComplete();
        void onProgress(int progress);
    }


}
