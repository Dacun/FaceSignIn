package com.example.facesignin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import android.widget.EditText;
import android.widget.ImageView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private EditText editText;
    String idcontent;
    Bitmap mphotoimg;
    ImageView img;
    Button send;
    private Paint mpaint;
    static String a="a";
    static String b="b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);
        button= (Button) findViewById(R.id.sure);
        editText= (EditText) findViewById(R.id.id);
        img= (ImageView) findViewById(R.id.image);
        send= (Button) findViewById(R.id.send);
        button.setOnClickListener(this);
        send.setOnClickListener(this);
        mpaint=new Paint();
    }
    private Handler mhanler=new Handler(){
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
    private Handler dialoghandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            JSONObject tip= (JSONObject) msg.obj;
            try {
                if(tip.getString("success").equals("true")){
                    dialog(a);

                }
            } catch (JSONException e) {
                    dialog(b);
                    e.printStackTrace();
            }

            Log.e("TAG","showdialag");

            super.handleMessage(msg);
        }
    };

    private void dialog(String data) {
        AlertDialog.Builder builder=new AlertDialog.Builder(AddFaceActivity.this);
        if(data.equals("a")){
            builder.setMessage("添加成功");
            builder.setTitle("学号："+idcontent);
        }
        else if (data.equals("b")){
            builder.setMessage("添加失败");
        }

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent toindexactivity=new Intent();
                toindexactivity.setClass(AddFaceActivity.this,IndexActivity.class);
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

    private static final int MSG_SUC=0x111;
    private static final int MSG_ERR=0x112;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sure:
                idcontent=editText.getText().toString();
                if(idcontent.length()!=10){
                    AlertDialog.Builder mbuilder=new AlertDialog.Builder(AddFaceActivity.this);
                    mbuilder.setTitle("输入错误");
                    mbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    mbuilder.create().show();

                }
                else paizhao();
                break;
            case R.id.send:
                FaceppAdd faceppadd=new FaceppAdd();
                faceppadd.detect(idcontent,mphotoimg, new FaceppAdd.CallBack() {
                    @Override
                    public void success(JSONObject result, JSONObject group) {
                        Message msg=Message.obtain();
                        msg.what=MSG_SUC;
                        msg.obj=result;
                        mhanler.sendMessage(msg);
                        Message personinfo=Message.obtain();
                        personinfo.obj=group;
                        dialoghandler.sendMessage(personinfo);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg=Message.obtain();
                        msg.what=MSG_ERR;
                        msg.obj=exception;
                        mhanler.sendMessage(msg);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null){
            mphotoimg= (Bitmap) data.getExtras().get("data");
            mphotoimg= imagezoom(mphotoimg);


            editText.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
            img.setImageBitmap(mphotoimg);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
