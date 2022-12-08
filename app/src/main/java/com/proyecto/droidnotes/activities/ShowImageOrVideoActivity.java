package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.proyecto.droidnotes.R;
import com.squareup.picasso.Picasso;

public class ShowImageOrVideoActivity extends AppCompatActivity {


// VARIABLES ///////////////////////////////////////////////////////////////////////////
    ImageView mImageViewBack;
    ImageView mImageViewPicture;
    ImageView mImageViewVideo;
    FrameLayout mFrameLayoutVideo;
    VideoView mVideoView;
    View mView;

    String mExtraUrl;
    String mExtraType;
    // CIERRE ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_or_video);
        setStatusBarColor();

        /// INSTANCIAS ////////////////////////////////////////////////////
        mImageViewBack = findViewById(R.id.imageViewBack);
        mImageViewPicture = findViewById(R.id.imageViewPicture);
        mImageViewVideo = findViewById(R.id.imageViewVideo);
        mFrameLayoutVideo = findViewById(R.id.frameLayoutVideo);
        mVideoView = findViewById(R.id.videoView);
        mView = findViewById(R.id.viewVideo);

        mExtraType = getIntent().getStringExtra("type");
        mExtraUrl = getIntent().getStringExtra("url");
        // CIERRE INSTANCIAS ///////////////////////////////////////////////


        // REPRODUCCION DEL VIDEO O MUESTRA DE LA IMAGEN
        if (mExtraType.equals("imagen")){
            mFrameLayoutVideo.setVisibility(View.GONE);
            mImageViewPicture.setVisibility(View.VISIBLE);
            Picasso.with(ShowImageOrVideoActivity.this).load(mExtraUrl).into(mImageViewPicture);
        }
        else {
            mFrameLayoutVideo.setVisibility(View.VISIBLE);
            mImageViewPicture.setVisibility(View.GONE);
            Uri uri = Uri.parse(mExtraUrl);
            mVideoView.setVideoURI(uri);
        }

        mFrameLayoutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoView.isPlaying()){
                    mView.setVisibility(View.GONE);
                    mImageViewVideo.setVisibility(View.GONE);
                    mVideoView.start();
                }else{
                    mView.setVisibility(View.VISIBLE);
                    mImageViewVideo.setVisibility(View.VISIBLE);
                    mVideoView.pause();
                }
            }
        });


        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }


    // METODO QUE PERMITE HACER OSCURO EL ESTATUS BAR COLOR
    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }

}