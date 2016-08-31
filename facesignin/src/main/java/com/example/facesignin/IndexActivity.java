package com.example.facesignin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener {
    private Button creat;
    private Button addface;
    private Button identify;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        creat= (Button) findViewById(R.id.creat);
        addface= (Button) findViewById(R.id.addface);
        identify= (Button) findViewById(R.id.identify);
        creat.setOnClickListener(this);
        addface.setOnClickListener(this);
        identify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        scaleview(v);
        switch (v.getId()){
            case R.id.creat:
                Intent intenttocreat=new Intent();
                intenttocreat.setClass(IndexActivity.this,MainActivity.class);
                startActivity(intenttocreat);
                break;
            case R.id.addface:
                Intent intenttoaddface=new Intent();
                intenttoaddface.setClass(IndexActivity.this,AddFaceActivity.class);
                startActivity(intenttoaddface);
                Log.e("TAG","adc");
                break;
            case R.id.identify:
                Intent intenttoidentify=new Intent();
                intenttoidentify.setClass(IndexActivity.this,IdentifyActivity.class);
                startActivity(intenttoidentify);

        }


    }

    private void scaleview(View v) {
        AnimationSet animationset =new AnimationSet(true);
        ScaleAnimation scaleanimation=new ScaleAnimation(1,1.2f,1,1.2f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scaleanimation.setDuration(150);
        animationset.addAnimation(scaleanimation);
        v.startAnimation(animationset);
    }
}
