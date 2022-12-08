package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

public class RegisterActivity extends AppCompatActivity {

    EditText correo, pass;
    Button register, crearNuevo;
    TextView txtResetPass;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ImageProvider mImageProvider;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    ProgressDialog mDialog;

    Boolean validCorreo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getObj();

        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = (FirebaseAuth.AuthStateListener)(firebaseAuth) -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();


            if(user!=null){
                      if(!user.isEmailVerified() && correo.length()>0){
                          Toast.makeText(getApplicationContext(),"Valide su correo antes de continuar.", Toast.LENGTH_SHORT).show();
                          validCorreo = false;
                          mAuthProvider.signOut();
                      }else{
                          validCorreo = true;
                          goToHomeActivity();
                      }
            }
        };

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (correo.getText().length()>0 && pass.getText().length()>0){
                    if (pass.getText().length()<6){
                        Toast.makeText(RegisterActivity.this, "Contraseña debe ser mayor a 5 digitos", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        ingresar();
//                        if(validCorreo==false){
//                            Toast.makeText(getApplicationContext(),"Valide su correo antes de continuar.", Toast.LENGTH_SHORT).show();
//                            mAuthProvider.signOut();
//                        }else
//                        {
//                            ingresar();
//                        }
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Ingrese el correo y contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });

        crearNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, NuevoRegistroActivity.class);
                startActivity(intent);
            }
        });

        txtResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResetPassActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ingresar() {
        // Metodo sobreescrito una vez que la autenticacion con Firebase se haya realizado por exito
        mAuthProvider.signInEmail(correo.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //validamos si el usuario inicio sesion
                if (task.isSuccessful()) {

                    // Hece referencia al documento que tenemos en la BDD
                    if (mAuthProvider.getId() != null){
                        mUsersProvider.getUserInfo(mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //Validamos si el documento existe o no en la BDD
                                if (!documentSnapshot.exists()) {
                                    Toast.makeText(RegisterActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                    Log.i("LOG", "Hola");
                                    Log.i("LOG", mAuthProvider.getId());
                                    mAuthProvider.signOut();
                                    // Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    // startActivity(intent);
                                    //} else if (documentSnapshot.contains("username") && documentSnapshot.contains("image")) {
                                } else if (documentSnapshot.contains("username")) {
                                    String username = documentSnapshot.getString("username");
                                    String image = documentSnapshot.getString("image");

                                    //if (username != null && image != null) {
                                    if (username != null) {
                                        //if (!username.equals("") && !image.equals("")) {
                                        if (!username.equals("")) {
                                            final User user = new User();
                                            user.setId(mAuthProvider.getId());
                                            user.setEmail(correo.getText().toString());
                                            goToHomeActivity();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Cuenta debe registrarse", Toast.LENGTH_SHORT).show();
                                            mAuthProvider.signOut();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                        mAuthProvider.signOut();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                    mAuthProvider.signOut();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });
                }
                } else {
                    Toast.makeText(RegisterActivity.this, "No se pudo autenticar el usuario", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, NuevoRegistroActivity.class);
                    startActivity(intent);
                    Log.i("LOG", task.getException().toString());
                }
            }
        });
    }

    public void registrar(){
        firebaseAuth.createUserWithEmailAndPassword(correo.getText().toString(), pass.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),task.getException().toString(), Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    user.sendEmailVerification();
                    Toast.makeText(getApplicationContext(),"Valide su correo.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void goToHomeActivity() {
        //Toast.makeText(RegisterActivity.this, "Se almaceno correctamente", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA A HOME-ACTIVITY
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getObj(){
        correo = findViewById(R.id.txtcorreo);
        pass = findViewById(R.id.txtPass);
        register = findViewById(R.id.btnRegister);
        crearNuevo = findViewById(R.id.btnNuevoRegistro);
        txtResetPass = (TextView) findViewById(R.id.txtResetPass);
    }

}