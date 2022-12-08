package com.proyecto.droidnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

public class BottonSheetUsername extends BottomSheetDialogFragment
{

    // VARIABLES GLOBALES ==========================================================================
    Button mButtonSave, mButtonCancel;
    EditText mEditTextUsername;
    ImageProvider mImageProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;


    String username;
    // =============================================================================================

    // RECIBIREMOS LA URL DE LA IMAGEN
    public static BottonSheetUsername newIntence(String username)
    {
     BottonSheetUsername bottonShettSelectImage = new BottonSheetUsername();
     Bundle argumentos = new Bundle();
     argumentos.putString("username", username);
     bottonShettSelectImage.setArguments(argumentos);
     return bottonShettSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getArguments().getString("username");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.botton_sheet_username, container, false);
    // INTANCIAS ===================================================================================

        mButtonSave = view.findViewById(R.id.btnSave);
        mButtonCancel = view.findViewById(R.id.btnCancel);
        mEditTextUsername = view.findViewById(R.id.editTextUsername);
        mEditTextUsername.setText(username);

        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
    // =============================================================================================


        // EVENTO PARA GUARDAR O ACTUALIZAR EL NOMBRE DE USUARIO
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsername();
            }
        });

        // EVENTO CANCELAR EN LA ACTUALIZACION DEL NOMBRE DEL USUARIO
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }


    // METODO PARA ACTUALIZAR EL NOMBRE DEL USUARIO
    private void updateUsername()
    {
        String username = mEditTextUsername.getText().toString();
        if (!username.equals(""))
        {
            mUsersProvider.updateUsername(mAuthProvider.getId(), username).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dismiss();
                    Toast.makeText(getContext(), "El nombre de usuario se ha actualizado!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
