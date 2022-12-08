package com.proyecto.droidnotes.receivers;

import static com.proyecto.droidnotes.services.MyFirebaseMessagingClient.NOTIFICATION_REPLY;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ChatActivity;
import com.proyecto.droidnotes.channel.NotificationHelper;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.providers.MessagesProvider;
import com.proyecto.droidnotes.providers.NotificationProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseReceiver extends BroadcastReceiver {

    //  PODREMOS OBTENER LOS PARAMETROS A TRAVES DEL INTENT, SEA DE LA CLASE MyFirebaseMessaging...
    @Override
    public void onReceive(Context context, Intent intent) {
        getMyImage(context, intent );
    }

    private void showNotification(Context context, Intent intent, Bitmap myBitmap) {
        // OBTENEMOS EL TEXTO DIGITADO POR EL USUARIO
        String message = getMessageText(intent).toString();

        int id = intent.getExtras().getInt("idNotification");
        String messagesJSON = intent.getExtras().getString("messages");
        String usernameSender = intent.getExtras().getString("usernameSender");
        String usernameReceiver = intent.getExtras().getString("usernameReceiver");
        String imageSender = intent.getExtras().getString("imageSender");
        String imageReceiver = intent.getExtras().getString("imageReceiver");

        String idChat = intent.getExtras().getString("idChat");
        String idSender = intent.getExtras().getString("idSender");
        String idReceiver = intent.getExtras().getString("idReceiver");
        String tokenSender = intent.getExtras().getString("tokenSender");
        String tokenReceiver = intent.getExtras().getString("tokenReceiver");


        Gson gson = new Gson();
        Message[] messages = gson.fromJson(messagesJSON, Message[].class);

        NotificationHelper helper = new NotificationHelper(context);



        Intent intentResponse = new Intent(context, ResponseReceiver.class);
        // PARAMETROS A ENVIAR
        intentResponse.putExtra("idNotification", id);
        intentResponse.putExtra("messages", messagesJSON);
        intentResponse.putExtra("usernameSender", usernameSender);
        intentResponse.putExtra("usernameReceiver", usernameReceiver);
        intentResponse.putExtra("imageSender", imageSender);
        intentResponse.putExtra("imageReceiver", imageReceiver);

        intentResponse.putExtra("idChat", idChat);
        intentResponse.putExtra("idSender", idSender);
        intentResponse.putExtra("idReceiver", idReceiver);
        intentResponse.putExtra("tokenSender", tokenSender);
        intentResponse.putExtra("tokenReceiver", tokenReceiver);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intentResponse, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Tu mensaje...").build();

        NotificationCompat.Action actionResponse = new NotificationCompat.Action.Builder(
                R.mipmap.ic_logoapp,
                "Responder",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        NotificationCompat.Builder builder = helper.getNotificationMessage(messages, message, usernameSender, null, myBitmap,actionResponse );
        helper.getManager().notify(id, builder.build());

        // QUE SE CREEN LOS MENSAJES SIEMPRE Y CUANDO NO SEAN VACIOS
        if (!message.equals("")){

            // CREAMOS MODELO DE TIPO MESSAGE
            Message myMessage = new Message();
            // CHAT AL CUAL PERTENECEN LO MENSAJES QUE CREAREMOS
            myMessage.setIdChat(idChat);
            // PARAMETOS DE RECIBIR MSG
            myMessage.setIdSender(idReceiver);
            // PARAMETRO DE ENVIAR MSG
            myMessage.setIdReceiver(idSender);
            // TEXTO O MENSAJE
            myMessage.setMessage(message);
            myMessage.setStatus("ENVIADO");
            //ESTABLECEMOS EL TIPO DE MENSAJE
            myMessage.setType("texto");
            // FECHA
            myMessage.setTimestamp(new Date().getTime());

            createMessage(myMessage);

            ArrayList<Message> messageArrayList = new ArrayList<>();
            messageArrayList.add(myMessage);
            sendNotification(
                    context,
                    messageArrayList,
                    String.valueOf(id),
                    usernameReceiver,
                    usernameSender,
                    imageReceiver,
                    imageSender,
                    idChat,
                    idSender,
                    idReceiver,
                    tokenSender,
                    tokenReceiver
        );

        }

    }


    //  CREACION DEL MENSAJE
    private void createMessage(Message message) {
        MessagesProvider messagesProvider = new MessagesProvider();
            // VALIDAMOS QUE LA INFORMACION SE HAYA CREADO CORRECTAMENTE
        messagesProvider.create(message);
    }


    // METODO PARA ENVIAR LA NOTIFICACION
    private void sendNotification(
            Context context,
            ArrayList<Message> messages,
            String idNotification,
            String usernameReceiver,
            String usernameSender,
            String imageReceiver,
            String imageSender,
            String idChat,
            String idSender,
            String idReceiver,
            String tokenSender,
            String tokenReceiver
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("title", "MENSAJE");
        data.put("body", "texto mensaje");
        data.put("idNotification", idNotification);
        data.put("usernameReceiver", usernameSender);
        data.put("usernameSender", usernameReceiver);
        data.put("imageReceiver", imageSender);
        data.put("imageSender", imageReceiver);
        data.put("idChat", idChat);
        data.put("idSender", idReceiver);
        data.put("idReceiver", idSender);
        data.put("tokenSender", tokenReceiver);
        data.put("tokenReceiver", tokenSender
        );

        // CONVERTIR A UN OBJETO JSON
        Gson gson = new Gson();
        String messagesJSON = gson.toJson(messages);

        data.put("messagesJSON", messagesJSON);
        NotificationProvider notificationProvider= new NotificationProvider();

        List<String> tokens = new ArrayList<>();
        tokens.add(tokenSender);

        notificationProvider.send(context, tokens, data);
    }




    // METODO PARA OBTENER LA IMAGEN RECIBIDA
    private void getMyImage(Context context, Intent intent) {
        final String myImage = intent.getExtras().getString("imageReceiver");
        if (myImage == null){
            showNotification(context, intent, null);
            return;
        }
        if (myImage.equals("")){
            showNotification(context, intent, null);
            return;
        }
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(context)
                                .load(myImage)
                                .into(new Target() {
                                    // RETORNAR LA IMAGEN CORRECTAMENTE DESDE LA WEB
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        showNotification(context, intent, bitmap);
                                    }
                                    // EN CASO DE QUE LA IMAGEN NO EXISTA EN LA BDD
                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        showNotification(context, intent, null);
                                    }
                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                });
                    }
                });
    }




    private CharSequence getMessageText(Intent intent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null){
            return remoteInput.getCharSequence(NOTIFICATION_REPLY);
        }
        return null;
    }
}
