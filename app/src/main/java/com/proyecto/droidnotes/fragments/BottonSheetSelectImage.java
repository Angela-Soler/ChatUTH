package com.proyecto.droidnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ProfileActivity;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.UsersProvider;

public class BottonSheetSelectImage extends BottomSheetDialogFragment
{

    // VARIABLES GLOBALES ==========================================================================
    LinearLayout mLinearLayoutDeleteImage, mLinearLayoutSelectImage;
    ImageProvider mImageProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;


    String image;
    // =============================================================================================

    // RECIBIREMOS LA URL DE LA IMAGEN
    public static BottonSheetSelectImage newIntence(String url)
    {
     BottonSheetSelectImage bottonSheetSelectImage = new BottonSheetSelectImage();
     Bundle argumentos = new Bundle();
     argumentos.putString("image", url);
     bottonSheetSelectImage.setArguments(argumentos);
     return bottonSheetSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        image = getArguments().getString("image");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.botton_sheet_select_image, container, false);
    // INTANCIAS ===================================================================================
        mLinearLayoutDeleteImage = view.findViewById(R.id.linearLayoutDeleteImage);
        mLinearLayoutSelectImage = view.findViewById(R.id.linearLayoutSelectImage);

        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
    // =============================================================================================
        mLinearLayoutDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });

        mLinearLayoutSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImage();
            }
        });


        return view;
    }

    // METODO PARA ACTUALIZAR SU IMAGEM
    private void updateImage()
    {
        ((ProfileActivity)getActivity()).startPix();
    }


    // METODO PARA ELIMINAR LA IMAGEN
    private void deleteImage()
    {
        mImageProvider.delete(image).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful())
               {
                   // SI LA TAREA ES EXITOSA PROCEDEREMOS A ELIMINAR LA IMAGEN
                   mUsersProvider.updateImage(mAuthProvider.getId(), null).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task2) {
                          if (task2.isSuccessful())
                          {
                              //setImageDefault();
                              Toast.makeText(getContext(), "La imagen se elimino correctamente!!", Toast.LENGTH_SHORT).show();
                          }
                          else
                          {
                              Toast.makeText(getContext(), "No se pudo eliminar el dato de la imagen!!", Toast.LENGTH_SHORT).show();
                          }
                       }
                   });
               }
               else
               {
                   Toast.makeText(getContext(), "No se pudo eliminar la image!!", Toast.LENGTH_SHORT).show();
               }
            }
        });
    }

    private void setImageDefault()
    {
        ((ProfileActivity)getActivity()).setImageDefault();
    }
}
