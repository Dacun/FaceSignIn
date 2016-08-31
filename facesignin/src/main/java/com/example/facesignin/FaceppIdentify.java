package com.example.facesignin;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by dell on 2016/8/8.
 */
public class FaceppIdentify {
    String faceid;

    public interface CallBack
    {
        void success(JSONObject result,JSONObject rec);
        void error(FaceppParseException exception);
    }
    public   void  detect(final Bitmap bm, final CallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {

                //request
                HttpRequests httpRequests=new HttpRequests(Constant.key,Constant.secret,true,true);
                Bitmap bmsmall=Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight());
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bmsmall.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte[] array=stream.toByteArray();
                PostParameters params=new PostParameters();



                params.setImg(array);

                try {
                    JSONObject jsonObject = httpRequests.detectionDetect(params);
                    params.setGroupName("签到4.0");
                    JSONObject rec=httpRequests.recognitionIdentify(params);




                    Log.e("TAG",jsonObject.toString());
                    if(callBack!=null){
                        callBack.success(jsonObject,rec);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.error(e);
                    }
                }

            }
        }).start();

    }
}
