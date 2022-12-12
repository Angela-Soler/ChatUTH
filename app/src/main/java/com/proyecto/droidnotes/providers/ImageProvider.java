package com.proyecto.droidnotes.providers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.Status;
import com.proyecto.droidnotes.utils.CompressorBitmapImage;
import com.proyecto.droidnotes.utils.ExtensionFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ImageProvider {

    // VARIABLE GLOBALES ==========================================================================
    StorageReference mStorage;
    FirebaseStorage mFirebaseStorage;
    int index=0;
    MessagesProvider mMessageProvider;
    StatusProvider mStatusProvider;
    DatabaseReference myRef;
    // ============================================================================================

    public ImageProvider()
    {
        // INSTANCIAS EN EL CONSTRUCTOR ===================================
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorage = mFirebaseStorage.getReference();
        mMessageProvider = new MessagesProvider();
        mStatusProvider = new StatusProvider();
        index = 0;
       // =================================================================
    }

    public UploadTask save(Context context, File file)
    {
        byte[] imageByte = CompressorBitmapImage.getImage(context, file.getPath(), 500, 500);
        StorageReference storage = mStorage.child(new Date() + ".jpg");
        mStorage = storage;
        UploadTask task = storage.putBytes(imageByte);
        return task;
    }

    public String saveI(Context context, Uri urii)
    {
        final String [] url = {""};
       // StorageReference Folder = FirebaseStorage.getInstance().getReference().child("Users");
        Uri FileUri = urii;
        final StorageReference filename = mStorage.child(new Date() +FileUri.getLastPathSegment());
        mStorage = filename;
        filename.putFile(FileUri).addOnSuccessListener(taskSnapshot -> filename.getDownloadUrl().addOnSuccessListener(uri -> {
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("image", String.valueOf(uri));
            myRef.setValue(hashMap);

            url[0] = (String.valueOf(uri));
        }));
        Log.i("LOG", String.valueOf(url[0]));
        return url[0];
    }

    //METODO PARA ALMACENAR MULTIPLES ARCHIVOS
    public void uploadMultiple(final Context context, ArrayList<Message> messages){
        Uri[] uri = new Uri[messages.size()];
        File file = null;
//        for (int i = 0; i < messages.size(); i++){

        // CREAMOS EL ARCHIVOS DEPENDIENDO SI ES IMAGEN O VIDEO
        if (ExtensionFile.isImageFile(messages.get(index).getUrl())){
            file = new File(messages.get(index).getUrl()); //CompressorBitmapImage.reduceImageSize(new File(messages.get(index).getUrl()));
        }
        else {
            file = new File(messages.get(index).getUrl());
        }

        uri[index] = Uri.parse("file://" + file.getPath());
        String name = UUID.randomUUID().toString();

        if (ExtensionFile.isImageFile(messages.get(index).getUrl())){
            name = name + ".jpg";
        }
        else {
            name = name + ".mp4";
        }

        final StorageReference ref = mStorage.child(name);
        // VERIFICAMOS SI YA SE TERMINO DE GUARDAR LA IMAGEN EN FIREBASE
        ref.putFile(uri[index]).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()){
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        // NOS DEVUELVE LA URL ASIGANDA POR FIREBASE A LA IMG
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            messages.get(index).setUrl(url);
                            mMessageProvider.create(messages.get(index));
                            index++;

                            if (index < messages.size()){
                                uploadMultiple(context, messages);
                            }
                        }
                    });

                }else{
                    Toast.makeText(context, "Hubo un error al almacenar la image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        }

    }


    public void uploadMultipleStatus(final Context context, final ArrayList<Status> statusList) {

        Uri[] uri = new Uri[statusList.size()];
        File file = new File(statusList.get(index).getUrl()); //CompressorBitmapImage.reduceImageSize(new File(statusList.get(index).getUrl()));

        uri[index] = Uri.parse("file://" + file.getPath());
        final StorageReference ref = mStorage.child(uri[index].getLastPathSegment());
        ref.putFile(uri[index]).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            statusList.get(index).setUrl(url);
                            mStatusProvider.create(statusList.get(index));
                            index++;

                            if (index < statusList.size()) {
                                uploadMultipleStatus(context, statusList);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(context, "Hubo un error al almacenar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }




    // Metodo que nos retornara la URL de la imagen que guardaremos
    public Task<Uri> getDownloadUri()
    {
        return  mStorage.getDownloadUrl();
    }


    // METODO QUE NOS PERMITA ELIMINAR UNA IMAGEN A TRAVES DE LA URL
    public Task<Void> delete(String url)
    {
        return  mFirebaseStorage.getReferenceFromUrl(url).delete();
    }
}
