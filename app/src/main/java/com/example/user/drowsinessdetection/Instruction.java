package com.example.user.drowsinessdetection;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class Instruction extends Activity {

    Button button, button2, button3, button4, button5, button6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        verifyStoragePermissions(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_instruction);

        addListenerOnButton();
        addListenerOnButton2();
        addListenerOnButton3();
        addListenerOnButton4();
        addListenerOnButton5();
        addListenerOnButton6();


    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (permission2 != PackageManager.PERMISSION_GRANTED || permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }

    public void addListenerOnButton() {

        final Context context = this;

        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);

            }

        });

    }

    public void addListenerOnButton2() {

        final Context context = this;

        button2 = (Button) findViewById(R.id.speedD);

        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, SpeedoMain.class);
                startActivity(intent);

            }

        });

    }

    public void addListenerOnButton3() {

        final Context context = this;

        button3 = (Button) findViewById(R.id.alco);

        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, breath.class);
                startActivity(intent);

            }

        });

    }

    public void addListenerOnButton4() {

        final Context context = this;

        button4 = (Button) findViewById(R.id.dl);

        button4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, dl_main.class);
                startActivity(intent);

            }

        });

    }

    public void addListenerOnButton5() {

        final Context context = this;

        button5 = (Button) findViewById(R.id.project);

        button5.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, about_project.class);
                startActivity(intent);

            }

        });
    }

    public void addListenerOnButton6() {

        final Context context = this;

        button6 = (Button) findViewById(R.id.about);

        button6.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, about_us.class);
                startActivity(intent);

            }

        });


    }


}
