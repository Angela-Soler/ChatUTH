package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

public class CodeVerificationActivity extends AppCompatActivity {


    Button mButtonCodeVerification;
    EditText mEditTextCode;
    TextView mTextViewSMS;
    ProgressBar mProgressBar;

    String mExtraPhone;
    String mVerificationId;

    // Utilizamos nuestra clase Auth
    AuthProvider mAuthProvider;

    UsersProvider mUsersProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification);
        setStatusBarColor();

        mButtonCodeVerification = findViewById(R.id.btnCodeVerification);
        mEditTextCode = findViewById(R.id.editTextCodeVerification);
        mTextViewSMS = findViewById(R.id.textViewSms);
        mProgressBar = findViewById(R.id.progressBar);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        //Obtenemos el telefono
        mExtraPhone = getIntent().getStringExtra("phone");

        // Peticion para que se envie un SMS con sodigo de verificacion
        mAuthProvider.sendCodeVerification(mExtraPhone, mCallBacks);


        // Evento setOnclick
        mButtonCodeVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // La accion  que queremos que haga el boton

                String code = mEditTextCode.getText().toString();
                if (!code.equals("") && code.length() >= 6)
                {
                    signIn(code);
                }
                else
                {
                    Toast.makeText(CodeVerificationActivity.this, "Por favor ingrese el codigo", Toast.LENGTH_SHORT).show();
                }

           }
        });
    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // Metodo una vez que la verificacion del codigo haya sido exitosa
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            // Señal de Carga en Espera
            mProgressBar.setVisibility(View.GONE);
            mTextViewSMS.setVisibility(View.GONE);

                // Obtener el codigo que se le envio al usuario por mensaje de Texto
               // Y que se rellene automaticamente
            String code = phoneAuthCredential.getSmsCode();

            if (code != null)
            {
                mEditTextCode.setText(code);
                signIn(code);
            }
        }

        // Mostrar al usuario un error.
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // Señal de Carga en Espera
            mProgressBar.setVisibility(View.GONE);
            mTextViewSMS.setVisibility(View.GONE);

            Toast.makeText(CodeVerificationActivity.this, "Se produjo un error, ingrese un numero de celular Valido: "+mExtraPhone + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.i("LOG",e.getMessage());
            // SI EL CELULAR ES INCORRECTO REGRESARA AL MAIN ACTIVITY
            /*Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
            startActivity(intent);*/
        }

        //Metodo para obtener la variable verificationId
        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            Toast.makeText(CodeVerificationActivity.this, "El codigo se ha enviado", Toast.LENGTH_SHORT).show();
            mVerificationId = verificationId;
        }
    };

    private void signIn(String code)
    {
        // Metodo sobreescrito una vez que la autenticacion con Firebase se haya realizado por exito
        mAuthProvider.signInPhone(mVerificationId, code).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //validamos si el usuario inicio sesion
                if (task.isSuccessful()) {

                    // Hece referencia al documento que tenemos en la BDD
                    mUsersProvider.getUserInfo(mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //Validamos si el documento existe o no en la BDD
                            if (!documentSnapshot.exists()) {
                                Toast.makeText(CodeVerificationActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                Log.i("LOG","Hola");
                                Log.i("LOG",mAuthProvider.getId());
                                mAuthProvider.signOut();
                                Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                                startActivity(intent);
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
                                        user.setPhone(mExtraPhone);
                                        goToHomeActivity();
                                    } else {
                                        Toast.makeText(CodeVerificationActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                        mAuthProvider.signOut();
                                        Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(CodeVerificationActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                    mAuthProvider.signOut();
                                    Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                            else {
                                Toast.makeText(CodeVerificationActivity.this, "Esta cuenta no existe", Toast.LENGTH_SHORT).show();
                                mAuthProvider.signOut();
                                Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                                startActivity(intent);
                            }

                        }
                    });

                } else {
                    Toast.makeText(CodeVerificationActivity.this, "No se pudo autenticar el usuario", Toast.LENGTH_SHORT).show();
                    Log.i("LOG", task.getException().toString());
                }
            }
        });
    }


    // METODO PARA IR AL ACTIVITY MENU O HOME ----------------------------------------------------------
    private void goToHomeActivity()
    {
        Intent intent = new Intent(CodeVerificationActivity.this, HomeActivity.class);
        //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA A HOME-ACTIVITY
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
   // ----------------------------------------------------------------------------------------------------

    // Metodo para pasar al acivity CompleteInfoActivity
    private void goToCompleteInfo()
    {
        Intent intent = new Intent(CodeVerificationActivity.this, CompleteInfoActivity.class);
        // ELIMINAR EL HISTORIAL DE ACTIVIDADES ANTERIORES
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }


}