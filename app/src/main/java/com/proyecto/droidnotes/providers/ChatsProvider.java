package com.proyecto.droidnotes.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.proyecto.droidnotes.models.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChatsProvider {

    CollectionReference mCollection;

    // CONTRUCTOR HACIA LA COLECCION CHATS
    public ChatsProvider()
    {
        mCollection = FirebaseFirestore.getInstance().collection("Chats");
    }

    // CREANDO LA INFORMACION EN LA BDD
    public Task<Void> create(Chat chat){
        // SE CREARA UN ID UNICO
        return mCollection.document(chat.getId()).set(chat);

    }

    // Actualizar LA INFORMACION EN LA BDD con los nuevos integrantes
    public Task<Void> update(Chat chat){
        Map<String, Object> map = new HashMap<>();
        map.put("ids", chat.getIds());

        return mCollection.document(chat.getId()).update(map);

    }

    //     ==============    CHATS     =================================
    public  Query getUserChats(String idUser){
        return mCollection.whereArrayContains("ids", idUser);
    }


    // METODO QUE BUSCARA ENTRE TODOS LOS DOCUMENTOS EL DOCUMENTO QUE TENGA POR ID LA COMBINACION DE UN USUARIO A OTRO
    public Query getChatByUser1AndUser2(String idUser1, String idUser2){
        ArrayList<String> ids = new ArrayList<>();
        ids.add(idUser1 + idUser2);
        ids.add(idUser2 + idUser1);
        return mCollection.whereIn("id", ids);

    }

    // CLASE DE ESTADO DEL USUARIO
    public DocumentReference getChatById(String idChat){
        return mCollection.document(idChat);
    }

    //   ==================   CIERRE CHATS ==============================

}
