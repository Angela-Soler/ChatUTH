package com.proyecto.droidnotes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ProfileActivity;
import com.proyecto.droidnotes.activities.StatusConfirmActivity;
import com.proyecto.droidnotes.adapters.StatusAdapter;
import com.proyecto.droidnotes.models.Status;
import com.proyecto.droidnotes.providers.StatusProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class StatusFragment extends Fragment {

    ////////////////////////////// VARIABLES /////////////////////////////////////////////
    View mView;
    LinearLayout mLinearLayoutAddStatus;

    Options mOptions;
    ArrayList<String> mReturnValues = new ArrayList<>();

    RecyclerView mRecyclerView;
    StatusAdapter mAdapter;
    StatusProvider mStatusProvider;

    ArrayList<Status> mNoRepeatStatusList;
    Gson mGson = new Gson();

    ListenerRegistration mListener;
    //////////////////////////// CIERRE //////////////////////////////////////////////////

    public StatusFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_status, container, false);
        ///////////////////////// INSTANCIAS //////////////////////////////////////////////
        mLinearLayoutAddStatus = mView.findViewById(R.id.linearLayoutAddStatus);
        mRecyclerView = mView.findViewById(R.id.recyclerViewStatus);
        mStatusProvider = new StatusProvider();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        ///////////////////////// CIERRE INSTANCIAS ////////////////////////////////////////
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mOptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(mReturnValues)                            //Pre selected Image Urls
                .setExcludeVideos(true)
                .setSpanCount(4)                                               //Span count for gallery min 1 & max 5
                .setMode(Options.Mode.All)                                     //Option to select only pictures or videos or both
                .setVideoDurationLimitinSeconds(0)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/pix/images");

        mLinearLayoutAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPix();
            }
        });

        getStatus();
        setInterval();

        return mView;
    }

    private void setInterval() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // RECORRIENDO CADA ESTADO DEL USUARIO EN UN INTERVALO DE UN MINUTOS Y PREGUNTAR SI EL TIEMPO LIMITE
                // ES MENOR A NUESTRA HORA ACTUAL Y SI ES MENOR ENTONCES SE ACTUALIZAN LOS DATOS
                if (mNoRepeatStatusList != null){
                    for (int i=0; i < mNoRepeatStatusList.size(); i++){
                        if (mNoRepeatStatusList.get(i).getJson() != null){
                            Status[] statusGSON = mGson.fromJson(mNoRepeatStatusList.get(i).getJson(), Status[].class);

                            for (int j = 0; j < statusGSON.length; j++){
                                long now = new Date().getTime();
                                if (now > statusGSON[j].getTimestampLimit()){
                                    if (mListener != null){
                                        mListener.remove();
                                    }
                                    getStatus();
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 60000);
    }

    // METODO QUE NOS DEVUELVE LOS ESTADOS DEPENDIENDO EL TIEMPO LIMITE
    private void getStatus() {
        // EVENTO PARA OBTENER LA INFORMACION EN TIEMPO REAL
     mListener = mStatusProvider.getStatusByTimestampLimit().addSnapshotListener(new EventListener<QuerySnapshot>() {
         @Override
         public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
              if (querySnapshot != null){
                  ArrayList<Status> statusList = new ArrayList<>();
                  mNoRepeatStatusList = new ArrayList<>();

                  //////////// AÑADIMOS TODOS LOS ELEMENTOS ENCONTRADOS EN LA CONSULTA A LA LISTA statusList //////////
                  for (DocumentSnapshot d: querySnapshot.getDocuments()){
                      Status s = d.toObject(Status.class);
                      statusList.add(s);
                  }

                  // AÑADIR A LA LISTA NOREPEATLIST ELEMENTOS NO REPETIDOS
                  for (Status status: statusList){
                      boolean isFound = false;

                      for (Status s: mNoRepeatStatusList){
                          if (s.getIdUser().equals(status.getIdUser())){
                              isFound = true;
                              break;
                          }
                      }
                      if (!isFound){
                          mNoRepeatStatusList.add(status);
                      }
                  }

                  //////////   ENCAPSULAREMOS TODOS LOS ESTADO QUE PUBLICAMOS EN LA BDD ////////////
                  // AÑADIMOS A LA LISTA DE NO REPETIDOS TODOS LOS ESTADOS DE UN USUARIO (ID)
                  for (Status noRepeat: mNoRepeatStatusList){
                      ArrayList<Status> sList = new ArrayList<>();
                      for (Status s: statusList){
                          if (s.getIdUser().equals(noRepeat.getIdUser())){
                              sList.add(s);
                          }
                      }
                      String statusJSON = mGson.toJson(sList);
                      Log.d("STATUS", "JSON:" + statusJSON);
                      noRepeat.setJson(statusJSON);

                  }
                  //////////   CIERRE DEL ENCAPSULAMIENTO  ////////////////////////////////////////
                  mAdapter = new StatusAdapter(getActivity(), mNoRepeatStatusList);
                  mRecyclerView.setAdapter(mAdapter);
              }
         }
     });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }
    }

    // INICIALIZA NUESTRA LIBRERIA PARA SELECCIONAR LA IMAGEN
    public void startPix()
    {
        Pix.start(StatusFragment.this, mOptions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Intent intent = new Intent(getContext(), StatusConfirmActivity.class);
            intent.putExtra("data", mReturnValues);
            startActivity(intent);
        }
    }

    // Metodo para los permisos a uso de la camara y accesar a la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(StatusFragment.this, mOptions);
            } else {
                Toast.makeText(getContext(), "Por favor concede los permisos para accesar a la camara!!", Toast.LENGTH_LONG).show();
            }
        }
    }


}