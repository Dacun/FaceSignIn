package com.example.facesignin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IdentifyActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView img;
    private Button test;
    private Bitmap mphotoimg;
    private Paint mpaint;
    String a="a";
    String b="b";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);
        img= (ImageView) findViewById(R.id.image);
        test= (Button) findViewById(R.id.test);
        paizhao();
        test.setOnClickListener(this);
        mpaint=new Paint();
    }

    private void paizhao() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);
    }
    private static final int MSG_SUC=0x111;
    private static final int MSG_ERR=0x112;
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SUC:

                    JSONObject rs=(JSONObject)msg.obj;
                    PrepareRsBitmap(rs);

                    img.setImageBitmap(mphotoimg);
                    Log.e("TAG","drawfinish");


                    break;
                case  MSG_ERR:
                    Log.e("TAG","error");
                  /*  String err= (String) msg.obj;
                    if (TextUtils.isEmpty(err)){
                        mtip.setText("Error");
                    }else{
                        mtip.setText("ErrorMessage");
                    }*/
                    break;
            }
            super.handleMessage(msg);

            super.handleMessage(msg);
        }
    };
    String tag;
    private Handler dialoghandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            JSONObject js= (JSONObject) msg.obj;
            try {
                JSONArray faces=js.getJSONArray("face");
                JSONObject face=faces.getJSONObject(0);
                JSONArray candidates=face.getJSONArray("candidate");
                JSONObject candidate=candidates.getJSONObject(0);
                int confidence=candidate.getInt("confidence");

                tag=candidate.getString("tag");
                if(confidence>15){
                    dialog(a,tag,confidence);
                }
                else
                    dialog(b,null,0);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };

    private void dialog(String data,String tag,int confidence) {
        AlertDialog.Builder builder=new AlertDialog.Builder(IdentifyActivity.this);
        if(data.equals("a")){
            builder.setMessage("签到成功");
            builder.setTitle("姓名："+tag+" "+"confidence:"+confidence);
        }
        else if (data.equals("b")){
            builder.setMessage("刷脸失败");
        }

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent toindexactivity=new Intent();
                toindexactivity.setClass(IdentifyActivity.this,IndexActivity.class);
                startActivity(toindexactivity);
            }
        });
        builder.create().show();
    }

    private void PrepareRsBitmap(JSONObject rs) {
        Bitmap bitmap=Bitmap.createBitmap(mphotoimg.getWidth(),mphotoimg.getHeight(),mphotoimg.getConfig());
        Canvas canvas=new Canvas(bitmap);
        canvas.drawBitmap(mphotoimg,0,0,null);
        try {
            JSONArray faces=rs.getJSONArray("face");

            int facecount=faces.length();


            for(int i=0;i<facecount;i++){
                //单独face对象
                JSONObject face=faces.getJSONObject(i);
                JSONObject posobj=face.getJSONObject("position");
                float x= (float) posobj.getJSONObject("center").getDouble("x");
                float y= (float) posobj.getJSONObject("center").getDouble("y");
                float w= (float) posobj.getDouble("width");
                float h= (float) posobj.getDouble("height");
                x=x/100*bitmap.getWidth();
                y=y/100*bitmap.getHeight();
                w=w/100*bitmap.getWidth();
                h=h/100*bitmap.getHeight();

                mpaint.setColor(0xffffffff);
                mpaint.setStrokeWidth(3);
                //画box
                canvas.drawLine(x-w/2,y-h/2,x-w/2,y+h/2,mpaint);
                canvas.drawLine(x-w/2,y-h/2,x+w/2,y-h/2,mpaint);
                canvas.drawLine(x+w/2,y-h/2,x+w/2,y+h/2,mpaint);
                canvas.drawLine(x+w/2,y+h/2,x-w/2,y+h/2,mpaint);
                mphotoimg=bitmap;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mphotoimg= (Bitmap) data.getExtras().get("data");
        img.setVisibility(View.VISIBLE);
        img.setImageBitmap(mphotoimg);
        test.setVisibility(View.VISIBLE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        FaceppIdentify faceppidentify=new FaceppIdentify();
        faceppidentify.detect(mphotoimg, new FaceppIdentify.CallBack() {
            @Override
            public void success(JSONObject result, JSONObject rec) {
                Message msg=Message.obtain();
                msg.what=MSG_SUC;
                msg.obj=result;
                mhandler.sendMessage(msg);
                Message identifyinfo=Message.obtain();
                identifyinfo.obj=rec;
                dialoghandler.sendMessage(identifyinfo);

            }

            @Override
            public void error(FaceppParseException exception) {
                Message msg=Message.obtain();
                msg.what=MSG_ERR;
                msg.obj=exception;
                mhandler.sendMessage(msg);
            }
        });

    }
}
