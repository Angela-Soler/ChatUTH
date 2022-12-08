package com.proyecto.droidnotes.providers;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.utils.FileUtil;

import java.util.ArrayList;
import java.util.Date;

public class FilesProvider {
    StorageReference mStorage;
    MessagesProvider mMessageProvider;
    AuthProvider mAuthProvider;

    // CONTRUCTOR PARA INSTANCIAR LA CLASE
    public FilesProvider(){
        mStorage = FirebaseStorage.getInstance().getReference();
        mMessageProvider = new MessagesProvider();
        mAuthProvider = new AuthProvider();
    }


    // METODO QUE NOS PERMITA GUARDAR LOS ARCHIVOS
    public void saveFiles(final Context context, ArrayList<Uri> files,final String idChat, final String idReceiver){

        for (int i = 0; i < files.size(); i++){
            final Uri f = files.get(i);
            final StorageReference ref = mStorage.child(FileUtil.getFileName(context, f));
            ref.putFile(f).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                       ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                            String url = uri.toString();
                               Message message = new Message();
                               message.setIdChat(idChat);
                               message.setIdReceiver(idReceiver);
                               message.setIdSender(mAuthProvider.getId());
                               message.setType("documento");
                               message.setUrl(url);
                               message.setStatus("ENVIADO");
                               message.setTimestamp(new Date().getTime());
                               // MENSAJE POR DEFECTO
                               message.setMessage(FileUtil.getFileName(context, f));

                               mMessageProvider.create(message);
                           }
                       });
                    }
                    else {
                        Toast.makeText(context, "No se pudo guardar el archivo", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


}
