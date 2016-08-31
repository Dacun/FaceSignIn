package com.example.facesignin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity implements View.OnClickListener {
    ImageView img;
    Button send;
    Button sure;
    Button repeat;
    EditText name;
    EditText id;
    Bitmap mphotoimg;
    Bitmap br;
    String mphotostr;
    private Paint mpaint;
    String namecontent;
    String idcontent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initviews();
        sure.setOnClickListener(this);
        send.setOnClickListener(this);
        mpaint=new Paint();

    }



    private void initviews() {
        img= (ImageView) findViewById(R.id.img);
        send= (Button) findViewById(R.id.send);
        sure= (Button) findViewById(R.id.sure);
        repeat= (Button) findViewById(R.id.repeat);
        name= (EditText) findViewById(R.id.name);
        id= (EditText) findViewById(R.id.id);
    }


    private static final int MSG_SUC=0x111;
    private static final int MSG_ERR=0x112;
    int age;
    String gender;
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

        }



    };
    private Handler dialodhandler=new Handler(){
         String a="a";
         String b="b";


        @Override
        public void handleMessage(Message msg) {
            Log.e("TAG","showdialag");
            JSONObject tip= (JSONObject) msg.obj;
            try {
                if(tip.getString("success").equals("true")){
                    dialog(a);

                }
            } catch (JSONException e) {
                 dialog(b);
                e.printStackTrace();
            }
            super.handleMessage(msg);
            Log.e("TAG","showdialag");

        }



    };
    private void dialog(String data) {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        if(data.equals("a")){
            builder.setMessage("创建成功");
            builder.setTitle("姓名："+namecontent+" "+"学号："+idcontent);
        }
        else if (data.equals("b")){
            builder.setMessage("创建失败");
        }

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent toindexactivity=new Intent();
                toindexactivity.setClass(MainActivity.this,IndexActivity.class);
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
                //获得 age和gender
                age=face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                gender=face.getJSONObject("attribute").getJSONObject("gender").getString("value");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sure:

                namecontent= name.getText().toString();
                idcontent=id.getText().toString();
                if(namecontent.length()==0||idcontent.length()!=10){

                    AlertDialog.Builder mbuilder=new AlertDialog.Builder(MainActivity.this);
                    mbuilder.setTitle("输入错误");
                    mbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    mbuilder.create().show();
                }
               else{ Log.e("TAG",idcontent+namecontent);
                       paizhao();}
                break;
            case R.id.send:
                FaceppDetect faceppDetect=new FaceppDetect();
                faceppDetect.detect(idcontent,namecontent,mphotoimg, new FaceppDetect.CallBack() {
                    @Override
                    public void success(JSONObject result, JSONObject group) {
                        Message msg=Message.obtain();
                        msg.what=MSG_SUC;
                        msg.obj=result;
                        mhandler.sendMessage(msg);
                        Message groupinfo=Message.obtain();
                        groupinfo.obj=group;
                        dialodhandler.sendMessage(groupinfo);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg=Message.obtain();
                        msg.what=MSG_ERR;
                        msg.obj=exception;
                        mhandler.sendMessage(msg);
                    }
                });
                break;
        }

    }

    private void paizhao() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(intent!=null){
            mphotoimg= (Bitmap) intent.getExtras().get("data");
            mphotoimg= imagezoom(mphotoimg);

            name.setVisibility(View.GONE);
            id.setVisibility(View.GONE);
            sure.setVisibility(View.GONE);
            img.setVisibility(View.VISIBLE);
            img.setImageBitmap(mphotoimg);
            send.setVisibility(View.VISIBLE);
            repeat.setVisibility(View.VISIBLE);

        }


        super.onActivityResult(requestCode, resultCode, intent);
    }


    private Bitmap imagezoom(Bitmap mphotoimg) {
        double maxsize=800.00;
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        mphotoimg.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] b=stream.toByteArray();
        double mid=b.length/1024;
        if (mid>maxsize)
        {
            double i=mid/maxsize;
            mphotoimg = zoomImage(mphotoimg, mphotoimg.getWidth() / Math.sqrt(i),
                    mphotoimg.getHeight() / Math.sqrt(i));
        }
        return  mphotoimg;

    }

    private Bitmap zoomImage(Bitmap mphotoimg, double v, double v1) {
        float width=mphotoimg.getWidth();
        float height=mphotoimg.getHeight();
        Matrix matrix=new Matrix();
        float scalewidth=((float)v)/width;
        float scaleheight=((float)v1)/height;
        matrix.postScale(scalewidth,scaleheight);
        Bitmap bit=Bitmap.createBitmap(mphotoimg,0,0,(int)width,(int)height,matrix,true);
        return bit;
    }


}
