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

public class BottonSheetInfo extends BottomSheetDialogFragment
{

    // VARIABLES GLOBALES ==========================================================================
    Button mButtonSave, mButtonCancel;
    EditText mEditTextInfo;
    ImageProvider mImageProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;


    String info;
    // =============================================================================================

    // RECIBIREMOS LA URL DE LA IMAGEN
    public static BottonSheetInfo newIntence(String info)
    {
     BottonSheetInfo bottonShettSelectImage = new BottonSheetInfo();
     Bundle argumentos = new Bundle();
     argumentos.putString("username", info);
     bottonShettSelectImage.setArguments(argumentos);
     return bottonShettSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        info = getArguments().getString("info");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.botton_sheet_info, container, false);
    // INTANCIAS ===================================================================================

        mButtonSave = view.findViewById(R.id.btnSave);
        mButtonCancel = view.findViewById(R.id.btnCancel);
        mEditTextInfo = view.findViewById(R.id.editTextInfo);
        mEditTextInfo.setText(info);

        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
    // =============================================================================================


        // EVENTO PARA GUARDAR O ACTUALIZAR INFORMACION DEL PERFIL
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInfo();
            }
        });

        // EVENTO CANCELAR EN LA ACTUALIZACION LA INFO DEL PERFIL
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }


    // METODO PARA ACTUALIZAR LA INFO EN EL PERFIL
    private void updateInfo()
    {
        String info = mEditTextInfo.getText().toString();
        if (!info.equals(""))
        {
            mUsersProvider.updateInfo(mAuthProvider.getId(), info).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dismiss();
                    Toast.makeText(getContext(), "El estado se actualizo correctamente!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
