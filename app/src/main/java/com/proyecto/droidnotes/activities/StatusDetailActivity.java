package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.Status;

import java.net.URL;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StatusDetailActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    StoriesProgressView mStoriesProgressView;
    TextView mTextViewComment;
    ImageView mImageViewStatus;
    View mView;

    Status[] mStatus;

    Gson mGson = new Gson();

    int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_detail);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mStoriesProgressView = findViewById(R.id.storiesProgressView);
        mTextViewComment = findViewById(R.id.textViewComment);
        mImageViewStatus = findViewById(R.id.imageViewStatus);
        mView = findViewById(R.id.mainView);

        mStoriesProgressView.setStoriesListener(this);

        String statusJSON = getIntent().getStringExtra("status");
        mStatus = mGson.fromJson(statusJSON, Status[].class);


        mStoriesProgressView.setStoriesCount(mStatus.length);
        mStoriesProgressView.setStoryDuration(4000);
        mStoriesProgressView.startStories(mCounter);

        setStatusInfo();
        setStatusBarColor();
    }

    private void setStatusInfo(){
        try {
            URL url = new URL(mStatus[mCounter].getUrl());
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            mImageViewStatus.setImageBitmap(image);
            mTextViewComment.setText(mStatus[mCounter].getComment());

        }catch (Exception e){
            Toast.makeText(this, "Hubo un error" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }

    @Override
    public void onNext() {
        // LA FUNCION DE CAMBIAR UN ESTADO AL SIGUIENTE
        mCounter = mCounter + 1;
        setStatusInfo();
    }

    @Override
    public void onPrev() {
        if ((mCounter - 1) < 0){
            return;
        }
        // LA FUNCION DE CAMBIAR EL ESTADO AL ANTERIOR
        mCounter = mCounter - 1;
        setStatusInfo();
    }

    @Override
    public void onComplete() {
        // TERMINARON DE MOSTRARSE TODOS LOS ESTADOS
        finish();
    }
}