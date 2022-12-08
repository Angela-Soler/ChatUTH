package com.proyecto.droidnotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.proyecto.droidnotes.R;

public class ResetPassActivity extends AppCompatActivity {

    private EditText txtEmailReset;
    private Button btnEmailReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        getObj();

        btnEmailReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmailReset.getText().toString().trim();
                if(email.isEmpty()) Toast.makeText(ResetPassActivity.this, "No debe dejar el campo vacio", Toast.LENGTH_SHORT).show();
                else sentEmail(email);
            }
        });
    }

    private void sentEmail(String _email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(_email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ResetPassActivity.this, "Se ha enviado exitosamente, revise su bandeja de entrada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ResetPassActivity.this, "Hubo un problema", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getObj(){
        txtEmailReset = (EditText) findViewById(R.id.txtEmailReset);
        btnEmailReset = (Button) findViewById(R.id.btnEmailReset);
    }
}