package com.htetznaing.msgsender;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout btnChoose;
    AdView banner;
    AdRequest adRequest;
    ImageView iv;
    InterstitialAd iAd;
    public static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChoose = (LinearLayout) findViewById(R.id.btnChoose);
        btnChoose.setOnClickListener(this);
        iv = (ImageView) findViewById(R.id.iv);

        adRequest = new AdRequest.Builder().build();
        banner = (AdView) findViewById(R.id.adView);
        banner.loadAd(adRequest);

        iAd = new InterstitialAd(this);
        iAd.setAdUnitId("ca-app-pub-4173348573252986/8438389384");
        iAd.loadAd(adRequest);
        iAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadAD();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                loadAD();
            }

            @Override
            public void onAdOpened() {
                loadAD();
            }
        });


        try {
            Intent receivedIntent = getIntent();
            String receivedAction = receivedIntent.getAction();
            if (receivedAction.equals(Intent.ACTION_SEND)) {
                Uri gUri = (Uri)getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (gUri !=null){
                    doSend(gUri);
                }else{
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "something was wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    public void doSend(Uri uri){
        final File fl = new File(getRealPathFromURI(MainActivity.this,uri));
        if (fl.exists()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Attention!");
            builder.setMessage("Send this file to Messenger ?");
            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent mmsIntent = new Intent(Intent.ACTION_SEND);
                    mmsIntent.setType("*/*");
                    mmsIntent.setPackage("com.facebook.orca");
                    mmsIntent.putExtra("sms_body", "Please see the attached image");
                    mmsIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fl));
                    showAD();

                    try {
                        startActivity(mmsIntent );
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Messenger not installed! \nPlease install Messenger in your phone :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAD();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            Toast.makeText(this, "Error! file not found :(", Toast.LENGTH_SHORT).show();
        }
    }

    public void doSend2(final Uri uri){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attention!");
        builder.setMessage("Send this file to Messenger ?");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent mmsIntent = new Intent(Intent.ACTION_SEND);
                mmsIntent.setType("*/*");
                mmsIntent.setPackage("com.facebook.orca");
                mmsIntent.putExtra("sms_body", "Please see the attached image");
                mmsIntent.putExtra(Intent.EXTRA_STREAM,uri);
                showAD();

                try {
                    startActivity(mmsIntent );
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Messenger not installed! \nPlease install Messenger in your phone :(", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showAD();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnChoose:
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("*/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("*/*");
                Intent chooserIntent = Intent.createChooser(getIntent, "Select Files");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data!=null){
                Uri wtfUri = data.getData();
                doSend2(wtfUri);
            }
        }
    }

    public void share(View view) {
        Intent iSend = new Intent(Intent.ACTION_SEND);
        iSend.putExtra(Intent.EXTRA_TEXT,"You can send any files to facebook Messenger in Android phones with\n" +
                "this SendAnyFiles2Messenger Application!\n" +
                "Download free at Google Play Store : https://play.google.com/store/apps/details?id="+getPackageName());
        iSend.setType("text/plain");
        startActivity(Intent.createChooser(iSend,"Share Via..."));
    }

    public void loadAD(){
        if (!iAd.isLoaded()){
            iAd.loadAd(adRequest);
        }
    }

    public void showAD(){
        if (iAd.isLoaded()){
            iAd.show();
        }else{
            iAd.loadAd(adRequest);
        }
    }

    public void dev(View view) {
        showAD();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("fb://profile/100011339710114"));
            startActivity(intent);
        }catch (Exception e){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://m.facebook.com/KHtetzNaing"));
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attention!");
        builder.setMessage("Do you want to Exit ?");
        builder.setIcon(R.drawable.ic);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showAD();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showAD();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}