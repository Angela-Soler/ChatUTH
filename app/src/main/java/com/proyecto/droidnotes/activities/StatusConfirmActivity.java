package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.adapters.OptionsPagerAdapter;
import com.proyecto.droidnotes.adapters.StatusPagerAdapter;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.Status;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ChatsProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.NotificationProvider;
import com.proyecto.droidnotes.utils.ExtensionFile;
import com.proyecto.droidnotes.utils.ShadowTransformer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusConfirmActivity extends AppCompatActivity {

    // VARIABLES GLOBALES =========================================================================
    ViewPager mViewPager;
    // CIERRE =====================================
    ArrayList<String> data;
    ImageProvider mImageProvider;
    AuthProvider mAuthProvider;

    ArrayList<Status> mStatus = new ArrayList<>();
    // ============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_confirm);
        setStatusBarColor();

        // INSTANCIAS ==============================================================================
        mViewPager = findViewById(R.id.viewPager);
        mAuthProvider = new AuthProvider();

        data = getIntent().getStringArrayListExtra("data");
        mImageProvider = new ImageProvider();
        // ========================================================================================

        // INCLUYE EN messages TODOS LOS MENSAJES QUE SE ALMACENARIAN EN LA BDD
        if (data != null){
            for (int i = 0; i < data.size(); i++){
                Status s = new Status();
                long now = new Date().getTime();
                // VENCIMIENTO DE TIEMPO 3 MINUTOS
                long limit = now + (60 * 1000 * 60 * 24);
                s.setIdUser(mAuthProvider.getId());
                s.setComment("");
                s.setTimestamp(now);
                s.setTimestampLimit(limit);
                s.setUrl(data.get(i));
                mStatus.add(s);
            }
        }

        StatusPagerAdapter pagerAdapter = new StatusPagerAdapter(
                getApplicationContext(),
                getSupportFragmentManager(),
                dpToPixels(2, this),
                data
        );
        ShadowTransformer transformer = new ShadowTransformer(mViewPager, pagerAdapter);
        transformer.enableScaling(true);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setPageTransformer(false, transformer);

    }

    // METODO PARA ENVIAR TODA LA INFORMACION A LA BDD
    public void send(){
        mImageProvider.uploadMultipleStatus(StatusConfirmActivity.this, mStatus);
        finish();
    }


    public void setComment(int position, String comment){
      mStatus.get(position).setComment(comment);
        //messages.get(position).setMessage(message);
    }


    public static float dpToPixels(int dp, Context context){
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }
}