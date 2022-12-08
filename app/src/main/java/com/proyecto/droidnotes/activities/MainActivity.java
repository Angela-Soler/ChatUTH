package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.providers.AuthProvider;


public class MainActivity extends AppCompatActivity {

    Button mButtonSendCode, btnRegister;
    EditText mEditTextPhone;
    CountryCodePicker mCountryCode;

    AuthProvider mAuthProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor();

        mButtonSendCode = findViewById(R.id.btnSendCode);
        mEditTextPhone = findViewById(R.id.txtcorreo);
        mCountryCode = findViewById(R.id.ccp);
        btnRegister = findViewById(R.id.btnRegister);

        mAuthProvider = new AuthProvider();


        // Boton Enviar Codigo
        mButtonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goToVerificationActivity();
                getData();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }

    // VALIDAMOS SI EL USUARIO YA SE AUTENTICO O NO, SE SESION SE CERRARA
    @Override
    protected void onStart()
    {
        super.onStart();
        if (mAuthProvider.getSessionUser() != null)
        {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            //ELIMINAR EL HISTORIAL DE VISTAS UNA VEZ EL USUARIO INGRESA A HOME-ACTIVITY
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    // Capturaremos la informacion que el usuario ingrese
    private void getData() {
        // Capturamos el codigo de pais seleccionado por el usuario
        String code = mCountryCode.getSelectedCountryCodeWithPlus();
        // Metodo que nos permita obtener el texto digitado por el usuario.
        String phone = mEditTextPhone.getText().toString();


        // Validaremos que el usuario haya ingresado el numero de telefono
        if (phone.equals("")) {
            Toast.makeText(this, "Upps, Debe de ingresar un numero de telefono!!", Toast.LENGTH_LONG).show();
        } else {
            goToVerificationActivity(code + phone);
        }
    }


    // Creamos un metodo que nos enviara a la otra actividad
    private void goToVerificationActivity(String phone)
    {
        Intent intent = new Intent(MainActivity.this, CodeVerificationActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }


    // LA BARRA SUPOERIOR COLOREARLA DE COLOR NEGRO
    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }
}