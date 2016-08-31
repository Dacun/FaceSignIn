package com.example.facesignin;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by dell on 2016/8/8.
 */
public class FaceppAdd {
    String faceid;

    public interface CallBack
    {
        void success(JSONObject result, JSONObject person);
        void error(FaceppParseException exception);
    }
    public   void  detect(final String id,  final Bitmap bm, final CallBack callBack){
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



                    JSONArray faces=jsonObject.getJSONArray("face");
                    JSONObject face=faces.getJSONObject(0);
                    faceid=face.getString("face_id");
                    params.setFaceId(faceid).setPersonName(id);
                    JSONObject person=httpRequests.personAddFace(params);
                    params.setGroupName("签到4.0");
                    JSONObject tarin=httpRequests.trainIdentify(params);





                    Log.e("TAG",jsonObject.toString());
                    if(callBack!=null){
                        callBack.success(jsonObject,person);
                    }

                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.error(e);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }).start();

    }



}
