package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.Chat;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ChatsProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfirmMultiUserChatActivity extends AppCompatActivity {

    Chat mExtraChat;

    // VARIABLES GLOBALES
    TextInputEditText mTextInputGroupName;
    Button mButtonConfirm;
    CircleImageView mCircleImagePhoto;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ImageProvider mImageProvider;
    ChatsProvider mChatsProvider;

    Options mOptions;
    // Arreglo que almacene las url de las imagenes que seleccionemos
    ArrayList<String> mReturnValues = new ArrayList<>();

    File mImageFile;
    String mGroupName = "";

    ProgressDialog mDialog;

    Boolean group = false;
    Boolean verIntegrantes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_multi_user_chat);

        mTextInputGroupName = findViewById(R.id.phone);
        mButtonConfirm = findViewById(R.id.btnConfirm);
        mCircleImagePhoto = findViewById(R.id.circleImagePhoto);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider();
        mChatsProvider = new ChatsProvider();

        //PROGRESS DIALOG
        mDialog = new ProgressDialog(ConfirmMultiUserChatActivity.this);
        mDialog.setTitle("Espere un momento");
        mDialog.setMessage("Guardando informacion");

        group = getIntent().getBooleanExtra("group",false);
        verIntegrantes = getIntent().getBooleanExtra("verIntegrantes",false);

        Log.i("LOG","group"+group.toString());

        String chat = getIntent().getStringExtra("chat" );
        String idChat = getIntent().getStringExtra("idChat" );
        Gson gson = new Gson();
        mExtraChat = gson.fromJson(chat, Chat.class);


        if (group == true){
            mExtraChat.setId(idChat);
            updateChat(idChat);
            finish();
        }

        if (verIntegrantes == true){
            mExtraChat.setId(idChat);
            updateChat(idChat);
            finish();
        }

        mOptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(1)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(mReturnValues)                            //Pre selected Image Urls
                .setExcludeVideos(true)
                .setSpanCount(4)                                               //Span count for gallery min 1 & max 5
                .setMode(Options.Mode.All)                                     //Option to select only pictures or videos or both
                .setVideoDurationLimitinSeconds(0)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/pix/images");                                       //Custom Path For media Storage



        // Añadimos el evento onvlick para la imagen
        mCircleImagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPix();
            }
        });

        // Evento onclick para el boton CONFIRMAR
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupName = mTextInputGroupName.getText().toString();
                if (!mGroupName.equals("") && mImageFile != null) {
                    saveImage();
                } else {
                    Toast.makeText(ConfirmMultiUserChatActivity.this, "Debe de seleccionar la imagen y nombre de grupo", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    //INICIALIZA NUESTRA LIBRERIA PARA SELECCIONAR LA IMAGEN
    private void startPix()
    {
        Pix.start(ConfirmMultiUserChatActivity.this, mOptions);
    }


    private void goToHomeActivity()
    {
        mDialog.dismiss();
        Toast.makeText(ConfirmMultiUserChatActivity.this, "Se almaceno correctamente", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(ConfirmMultiUserChatActivity.this, HomeActivity.class);
        //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA A HOME-ACTIVITY
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    // METODO PARA SUBIR LA IMAGEN Y OBTENER LA URL
    private void saveImage() {

        mDialog.show();
        mImageProvider.save(ConfirmMultiUserChatActivity.this, mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getDownloadUri().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            mExtraChat.setGroupName(mGroupName);
                            mExtraChat.setGroupImage(url);
                            createChat();
                        }
                    });
                } else {
                    //DETENER EL DIALOG
                    mDialog.dismiss();
                    Toast.makeText(ConfirmMultiUserChatActivity.this, "No se pudo almacenar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createChat() {
        mChatsProvider.create(mExtraChat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                goToHomeActivity();
            }
        });
    }

    private void updateChat(String idChat) {
        mChatsProvider.update(mExtraChat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Usuarios agregados correctamente",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConfirmMultiUserChatActivity.this, HomeActivity.class);
                //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
    // MOSTRARA LAS IMAGENES DE LA GALERIA
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mImageFile = new File(mReturnValues.get(0));
            mCircleImagePhoto.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
        }
    }

    // Metodo para los permisos a uso de la camara y accesar a la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(ConfirmMultiUserChatActivity.this, mOptions);
            } else {
                Toast.makeText(ConfirmMultiUserChatActivity.this, "Por favor concede los permisos para accesar a la camara!!", Toast.LENGTH_LONG).show();
            }
        }
    }


}