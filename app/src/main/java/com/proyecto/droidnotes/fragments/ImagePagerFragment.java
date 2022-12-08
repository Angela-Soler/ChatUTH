package com.proyecto.droidnotes.fragments;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ConfirmImageSendActivity;
import com.proyecto.droidnotes.adapters.CardAdapter;
import com.proyecto.droidnotes.utils.ExtensionFile;

import java.io.File;

public class ImagePagerFragment extends Fragment {

    // VARIABLES GLOBALES ==========================================================================
    View mView;
    CardView mCardViewOptions;
    ImageView mImageViewPicture;
    ImageView mImageViewBack;
    ImageView mImageViewSend;
    LinearLayout mLinearLayoutImagePager;
    VideoView mVideo;
    View mViewVideo;
    ImageView mImageViewVideo;

    FrameLayout mFrameLayoutVideo;

    EditText mEditTextComment;

    // =============================================================================================
    public static Fragment newInstance(int position, String imagePath, int size) {
        ImagePagerFragment fragment = new ImagePagerFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("size", size);
        args.putString("image", imagePath);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //CONFIGURACION
        mView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        mCardViewOptions = mView.findViewById(R.id.cardViewOptions);
        mCardViewOptions.setMaxCardElevation(mCardViewOptions.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);
        mImageViewPicture = mView.findViewById(R.id.imageViewPicture);
        mImageViewBack = mView.findViewById(R.id.imageViewBack);
        mLinearLayoutImagePager = mView.findViewById(R.id.linearLayoutViewPager);
        mEditTextComment = mView.findViewById(R.id.editTextComment);
        mImageViewSend = mView.findViewById(R.id.imageViewSend);
        mFrameLayoutVideo = mView.findViewById(R.id.frameLayoutVideo);
        mVideo = mView.findViewById(R.id.videoView);
        mImageViewVideo = mView.findViewById(R.id.imageViewVideo);
        mViewVideo = mView.findViewById(R.id.viewVideo);




        //OBTENER LA INFORMACION DEL METODO NEWINSTANCE
        String imagePath = getArguments().getString("image");
        int size = getArguments().getInt("size");
        int position = getArguments().getInt("position");

        if (ExtensionFile.isImageFile(imagePath)){
            mFrameLayoutVideo.setVisibility(View.GONE);
            mImageViewPicture.setVisibility(View.VISIBLE);

            if (imagePath != null){
                File file = new File(imagePath);
                // TRANFORMAR LA RUTA PARA MOSTRAR LA IMAGEN EN EL FRAGMENT
                mImageViewPicture.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }
        }
        else if (ExtensionFile.isVideoFile(imagePath)){
            mFrameLayoutVideo.setVisibility(View.VISIBLE);
            mImageViewPicture.setVisibility(View.GONE);

            Uri uri = Uri.parse(imagePath);
            mVideo.setVideoURI(uri);
        }


        if (size == 1){
            mLinearLayoutImagePager.setPadding(0,0,0,0);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mImageViewBack.getLayoutParams();
            params.leftMargin = 10;
            params.topMargin = 35;
        }



        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        mEditTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((ConfirmImageSendActivity) getActivity()).setMessage(position, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ConfirmImageSendActivity) getActivity()).send();
            }
        });

        mFrameLayoutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideo.isPlaying()){
                    mViewVideo.setVisibility(View.GONE);
                    mImageViewVideo.setVisibility(View.GONE);
                    mVideo.start();
                }
                else{
                    mViewVideo.setVisibility(View.VISIBLE);
                    mImageViewVideo.setVisibility(View.VISIBLE);
                    mVideo.pause();
                }
            }
        });

        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideo.isPlaying()){
            mVideo.pause();
        }
    }

    public CardView getCardView(){
        return mCardViewOptions;
    }
}