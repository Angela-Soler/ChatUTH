package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class NuevoRegistroActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    ProgressBar mProgressBar;

    // VARIABLES GLOBALES ==========================================================================
    TextInputEditText mTextInputUsername,
            mTextInputPass,
            mTextInputCuenta,
            mTextInputCorreo,
            mTextInputCarrera,
            mTextInputPhone;
    Button mButtonConfirm;
    CircleImageView mCircleImagePhoto;

    //Foto
    String currentPhotoPath = "";
    Uri uri_foto_galeria;
    static final int REQUESTCAMERA = 100;
    static final int TAKEFOTO = 101;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ImageProvider mImageProvider;

    Options mOptions;
    // Arreglo que almacene las url de las imagenes que seleccionemos
    ArrayList<String> mReturnValues = new ArrayList<>();

    File mImageFile;
    String mUsername = "";
    String mUserCuenta = "";
    String mUserCorreo = "";
    String mUserCarrera = "";
    String mUserPhone = "";

    ProgressDialog mDialog;

    boolean cam = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_registro);

        mTextInputUsername = findViewById(R.id.textName);
        mTextInputPass = findViewById(R.id.textPass);
        mTextInputCuenta = findViewById(R.id.textInputCuenta);
        mTextInputCorreo = findViewById(R.id.textInputCorreo);
        mTextInputCarrera = findViewById(R.id.textInputCarrera);
        mTextInputPhone = findViewById(R.id.phone);

        mButtonConfirm = findViewById(R.id.btnConfirm);
        mCircleImagePhoto = findViewById(R.id.circleImagePhoto);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = (FirebaseAuth.AuthStateListener) (firebaseAuth) -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                //Toast.makeText(getApplicationContext(),"Valide su correo antes de continuar.", Toast.LENGTH_SHORT).show();
                if (!user.isEmailVerified() && mTextInputCorreo.length() > 0) {
                    Toast.makeText(getApplicationContext(), "Valide su correo antes de continuar.", Toast.LENGTH_SHORT).show();
                } else {

                }
                //mAuthProvider.signOut();
            }
        };

        //PROGRESS DIALOG
        mDialog = new ProgressDialog(NuevoRegistroActivity.this);
        mDialog.setTitle("Espere un momento");
        mDialog.setMessage("Guardando informacion");

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
                //startPix();
                menuFoto();
            }
        });

        // Evento onclick para el boton CONFIRMAR
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsername = mTextInputUsername.getText().toString();
                mUserCuenta = mTextInputCuenta.getText().toString();
                mUserCorreo = mTextInputCorreo.getText().toString();
                mUserCarrera = mTextInputCarrera.getText().toString();
                mUserPhone = mTextInputPhone.getText().toString();


                if (!mUsername.equals("") &&
                        !mUserCuenta.equals("") &&
                        !mUserCorreo.equals("") &&
                        !mUserCuenta.equals("") &&
                        (mImageFile != null || uri_foto_galeria!= null ) &&
                        mUserPhone != null && mTextInputPass!=null)  {
                    registrar();
                    //saveImage();
                } else if (mTextInputPass.length() < 6) {
                    Toast.makeText(NuevoRegistroActivity.this, "Contraseña debe ser mayor a 5 digitos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NuevoRegistroActivity.this, "Debe llenar toda la informacion", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //INICIALIZA NUESTRA LIBRERIA PARA SELECCIONAR LA IMAGEN
    private void startPix() {
        Pix.start(NuevoRegistroActivity.this, mOptions);
    }

    // Actualizar info al momento de seleccionar confirmar -------------------------------
    private void updateUserInfo(String url) {
        mUsername = mTextInputUsername.getText().toString();
        mUserCuenta = mTextInputCuenta.getText().toString();
        mUserCorreo = mTextInputCorreo.getText().toString();
        mUserCarrera = mTextInputCarrera.getText().toString();
        mUserPhone = mTextInputPhone.getText().toString();

        //Validar que el nombre de usuario no este vacio.
        User user = new User();
        user.setUsername(mUsername);
        user.setAccount(mUserCuenta);
        user.setPhone(mUserPhone);
        user.setEmail(mUserCorreo);
        user.setCareer(mUserCarrera);
        user.setId(mAuthProvider.getId());
        user.setImage(url);

        // INGRESO DE INFORMACION EN LA BDD //////////////////////////////////////////////////////////////////////////////////
        mUsersProvider.updateAll(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDialog.dismiss();
                mUsersProvider.createToken(mAuthProvider.getId());
                Toast.makeText(NuevoRegistroActivity.this, "Se almaceno correctamente", Toast.LENGTH_LONG).show();
                // mAuthProvider.signOut();
                mAuthProvider.signOut();
                Intent intent = new Intent(NuevoRegistroActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        ///////////////////// OUT ///////////////////////////////////////////////////////////////////////////////////////////
    }

    public void registrar() {
        firebaseAuth.createUserWithEmailAndPassword(mTextInputCorreo.getText().toString(), mTextInputPass.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    user.sendEmailVerification();
                    Toast.makeText(getApplicationContext(), "Se ha enviado correo para validacion.", Toast.LENGTH_SHORT).show();
                    signIn(mTextInputCorreo.getText().toString(), mTextInputPass.getText().toString());

                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }


    private void goToHomeActivity() {
        //mDialog.dismiss();
        //Toast.makeText(NuevoRegistroActivity.this, "Se almaceno correctamente", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(NuevoRegistroActivity.this, HomeActivity.class);
        //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA A HOME-ACTIVITY
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    // METODO PARA SUBIR LA IMAGEN Y OBTENER LA URL
    private void saveImage() {

        mDialog.show();
        if (cam == true) {
            mImageProvider.save(NuevoRegistroActivity.this, mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageProvider.getDownloadUri().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                //registrar(); //creamos el registro

                                updateUserInfo(url); //lo modificamos

                            }
                        });
                    } else {
                        //DETENER EL DIALOG
                        mDialog.dismiss();
                        Toast.makeText(NuevoRegistroActivity.this, "No se pudo almacenar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            String url = mImageProvider.saveI(NuevoRegistroActivity.this, uri_foto_galeria);
            if (url.length() > 0) {
                //registrar(); //creamos el registro
                updateUserInfo(url); //lo modificamos

            } else {
                //DETENER EL DIALOG
                mDialog.dismiss();
                Toast.makeText(NuevoRegistroActivity.this, "No se pudo almacenar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // ------------------------------------------------------------------------------------------------

    // MOSTRARA LAS IMAGENES DE LA GALERIA =========================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Antes
       /* if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mImageFile = new File(mReturnValues.get(0));
            mCircleImagePhoto.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
        }*/
        //Nuevo
        if (requestCode == TAKEFOTO && resultCode == RESULT_OK) {
            File foto = new File(currentPhotoPath);
            //imageView.setImageURI(Uri.fromFile(foto));
            mCircleImagePhoto.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
        }

        if (requestCode == 10 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            uri_foto_galeria = data.getData();
            mCircleImagePhoto.setImageURI(uri);
            currentPhotoPath = uri.getPath();
            try {
                mImageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // Metodo para los permisos a uso de la camara y accesar a la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(NuevoRegistroActivity.this, mOptions);
            } else {
                Toast.makeText(NuevoRegistroActivity.this, "Por favor concede los permisos para accesar a la camara!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void signIn(String mail, String pass) {
        // Metodo sobreescrito una vez que la autenticacion con Firebase se haya realizado por exito
        mAuthProvider.signInEmail(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //validamos si el usuario inicio sesion
                if (task.isSuccessful()) {
                    final User user = new User();
                    user.setId(mAuthProvider.getId());
                    user.setEmail(mail);

                    // Hece referencia al documento que tenemos en la BDD
                    mUsersProvider.getUserInfo(mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //Validamos si el documento existe o no en la BDD
                            if (!documentSnapshot.exists()) {
                                // Permite conocer si la informacion se almaceno correctamente en la BDD
                                mUsersProvider.create(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        saveImage();
                                        //goToCompleteInfo();
                                    }
                                });
                            } /*else if (documentSnapshot.contains("username") && documentSnapshot.contains("image")) {
                                String username = documentSnapshot.getString("username");
                                String image = documentSnapshot.getString("image");

                                if (username != null && image != null) {
                                    if (!username.equals("") && !image.equals("")) {
                                        goToHomeActivity();
                                    } else {
                                        goToCompleteInfo();
                                    }
                                } else {
                                    goToCompleteInfo();
                                }
                            }
                            else {
                                goToCompleteInfo();
                            }*/

                        }
                    });

                } else {
                    Toast.makeText(NuevoRegistroActivity.this, "No se pudo crear el usuario. Verifique Datos", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void goToCompleteInfo() {
        Intent intent = new Intent(NuevoRegistroActivity.this, HomeActivity.class);
        // ELIMINAR EL HISTORIAL DE ACTIVIDADES ANTERIORES
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void menuFoto() {
        PopupMenu pp = new PopupMenu(this, mCircleImagePhoto);
        pp.getMenuInflater().inflate(R.menu.menu_foto, pp.getMenu());
        pp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tomarFoto:
                        Toast.makeText(getApplicationContext(), "Abriendo camara...", Toast.LENGTH_SHORT).show();
                        PermisosFoto();
                        cam = true;
                        return true;
                    case R.id.verGaleria:
                        Toast.makeText(getApplicationContext(), "Abriendo Galeria ", Toast.LENGTH_SHORT).show();
                        // abrirGaleria();
                        //cam = false;
                        //return true;
                }
                return false;
            }
        });
        pp.show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione la aplicación"), 10);
    }

    private void PermisosFoto() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTCAMERA);
        } else {
            tomarfoto();
        }
    }

    private void tomarfoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            mImageFile = null;
            try {
                mImageFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (mImageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.proyecto.droidnotes.fileprovider",
                        mImageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKEFOTO);
            }
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}