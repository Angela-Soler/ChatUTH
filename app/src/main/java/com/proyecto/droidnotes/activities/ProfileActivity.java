package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.UploadTask;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.fragments.BottonSheetInfo;
import com.proyecto.droidnotes.fragments.BottonSheetSelectImage;
import com.proyecto.droidnotes.fragments.BottonSheetUsername;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.proyecto.droidnotes.utils.MyToolbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // VARIABLES GLOBALES ==========================================================================
    FloatingActionButton mFabSelectImage;
    BottonSheetSelectImage mBottonSheetSelectImage;
    BottonSheetUsername mBottonSheetUsername;
    BottonSheetInfo mBottonSheetInfo;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ImageProvider mImageProvider;
    TextView mTextViewUsername, mTextViewPhone, mTextViewInfo, mTextViewEmail, mTextViewCareer, mTextViewAccount;
    CircleImageView mCircleImageProfile;
    ImageView mImageViewEditUsername;
    ImageView mImageViewEditInfo;

    User mUser;
    Options mOptions;
    // Arreglo que almacene las url de las imagenes que seleccionemos
    ArrayList<String> mReturnValues = new ArrayList<>();
    File mImageFile;

    ListenerRegistration mListener;
    // =============================================================================================

    // EVENTO ONCREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MyToolbar.show(this, "Perfil", true);

        // INSTANCIA DE VARIABLES =================================================================
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewPhone = findViewById(R.id.textViewPhone);
        mTextViewInfo = findViewById(R.id.textViewInfo);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewCareer = findViewById(R.id.textViewCareer);
        mTextViewAccount = findViewById(R.id.viewAcccount);

        mCircleImageProfile = findViewById(R.id.circleImageProfile);
        mImageViewEditUsername = findViewById(R.id.imageViewEditUsername);
        mImageViewEditInfo = findViewById(R.id.imageViewEditInfo);


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

        mFabSelectImage = findViewById(R.id.fabSelectImage);
        // =========================================================================================


        mFabSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottonSheetSelectImage();
            }
        });

        mImageViewEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottonSheetUsername();
            }
        });


        //EVENTO ONCLICK PARA LA INFORMACION
        mImageViewEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottonSheetEditInfo();
            }
        });

        getUserInfo();
    }


    // ELIMINAMOS EL ESCUCHADOR DE EVENTOS EN TIEMPO REAL QUE SE MANEJA EN EL ADDSNAPSHOTS
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }

    }

    // METODO PARA RETORNAR EL DOCUMENTO QUE QUEREMOS TRAER DE FIREBASE
    // FUNCIONALIDADES EN TIEMPO REAL EN EL PERFIL
    private void getUserInfo() {
       mListener = mUsersProvider.getUserInfo(mAuthProvider.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            // ACTUALIZAR EN TIEMPO REAL EL NOMBRE DEL USUARIO EN PROFILE_ACTIVITY
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null)
                {
                    if (documentSnapshot.exists()) {
                        // OBTENER TODA LA INFORMACION A TRAVES DEL PACKAGE MODELO
                        mUser = documentSnapshot.toObject(User.class);
                        mTextViewUsername.setText(mUser.getUsername());
                        mTextViewPhone.setText(mUser.getPhone());
                        mTextViewInfo.setText(mUser.getInfo());
                        mTextViewCareer.setText(mUser.getCareer());
                        mTextViewAccount.setText(mUser.getAccount());
                        mTextViewEmail.setText(mUser.getEmail());

                        if (mUser.getImage() != null) {
                            if (!mUser.getImage().equals("")) {
                                // Mostrar nuestra imagen en un objeto
                                Picasso.with(ProfileActivity.this).load(mUser.getImage()).into(mCircleImageProfile);
                            }
                            else{
                             setImageDefault();
                            }
                        }
                        else {
                            setImageDefault();
                        }
                    }
                }
            }
        });
    }


    // CONFIGURACION PARA MOSTRAR EL BOTON SHEET SEA PARA ELIMINAR FOTO O SELECCIONAR
    private void openBottonSheetSelectImage() {
        if (mUser != null) {
            mBottonSheetSelectImage = BottonSheetSelectImage.newIntence(mUser.getImage());
            mBottonSheetSelectImage.show(getSupportFragmentManager(), mBottonSheetSelectImage.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBottonSheetEditInfo() {
        if (mUser != null) {
            mBottonSheetInfo = BottonSheetInfo.newIntence(mUser.getInfo());
            mBottonSheetInfo.show(getSupportFragmentManager(), mBottonSheetInfo.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar :(", Toast.LENGTH_SHORT).show();
        }
    }


    private void openBottonSheetUsername() {
        if (mUser != null) {
            mBottonSheetUsername = BottonSheetUsername.newIntence(mUser.getUsername());
            mBottonSheetUsername.show(getSupportFragmentManager(), mBottonSheetUsername.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar :(", Toast.LENGTH_SHORT).show();
        }
    }



    // IMAGE =======================================================================================
    // METODO, UNA VEZ QUE SE ELIMINE LA IMAGEN QUE SE COLOQUE LA IMAGEN POR DEFECTO
    public void setImageDefault ()
    {
        mCircleImageProfile.setImageResource(R.drawable.ic_person_white);
    }

    // INICIALIZA NUESTRA LIBRERIA PARA SELECCIONAR LA IMAGEN
    public void startPix()
    {
        Pix.start(ProfileActivity.this, mOptions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mImageFile = new File(mReturnValues.get(0));
            mCircleImageProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            saveImage();
        }
    }

    // Metodo para los permisos a uso de la camara y accesar a la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(ProfileActivity.this, mOptions);
            } else {
                Toast.makeText(ProfileActivity.this, "Por favor concede los permisos para accesar a la camara!!", Toast.LENGTH_LONG).show();
            }
        }
    }



    // METODO PARA SUBIR LA IMAGEN Y OBTENER LA URL
    private void saveImage() {
        mImageProvider = new ImageProvider();
        mImageProvider.save(ProfileActivity.this, mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getDownloadUri().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            mUsersProvider.updateImage(mAuthProvider.getId(), url).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "La imagen se actualizo correctamente!!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "No se pudo almacenar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // =============================================================================================


}