package com.proyecto.droidnotes.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.proyecto.droidnotes.models.Message;

import java.util.HashMap;
import java.util.Map;

public class MessagesProvider {

    CollectionReference mCollection;

    public MessagesProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Messages");
    }

    public Task<Void> create(Message message){
        //OBTENER EL ID DE LA COLECCION MESSAGES
        DocumentReference document = mCollection.document();
        message.setId(document.getId());
        return document.set(message);
    }

    // CONSULTA A LA BASE DE DATOS // OBTENER LOS MENSAJES POR CHAT
    public Query getMessageByChat(String idChat){
        // OBTENIENDO MENSAJES CUYO ID SEA DEL ID DEL CHAT y ORDENAR LA INFO A TRAVES DEL TIMESTAMP
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.ASCENDING);
    }

    // Consulta Para Apilar Los Ultimos 5 Mensajes
    public Query getLastMessageByChatAndSender(String idChat, String idSender){
        // Se Obtienen Mensajes Cuyo ID Sea Del ID Del Chat y Ordena LA Info A Traves Del Timestamp
        return mCollection
                .whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .whereEqualTo("status", "ENVIADO")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5);
    }

    // Actualizar Estado Del Mensaje
    public Task<Void> updateStatus(String idMessage, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mCollection.document(idMessage).update(map);
    }

    // Obtener Mensajes No Leidos
    public Query getMessageNotRead(String idChat){
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("status", "ENVIADO");
    }


    // Listar El Ultimo Mensaje
    public Query getLastMessage(String idChat){
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }
}
