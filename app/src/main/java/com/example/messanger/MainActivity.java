package com.example.messanger;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.example.messanger.Login.LoginActivity;


public class MainActivity extends AppCompatActivity {

    private Animation top;
    private ImageView applogo1, applogo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        top = AnimationUtils.loadAnimation(this, R.anim.top);

        applogo1 = findViewById(R.id.ivAppLogo1);
        applogo2 = findViewById(R.id.ivAppLogo2);

        applogo1.setAnimation(top);
        applogo2.setAnimation(top);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                finish();
            }
        }, 3000);




    }




}