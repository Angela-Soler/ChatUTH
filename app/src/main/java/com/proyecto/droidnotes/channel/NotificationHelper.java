package com.proyecto.droidnotes.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.models.Message;

import java.util.Date;

import kotlin.text.UStringsKt;


//////////////////////////   CONFIGURACIONES PARA CREAR EL CANAL DE NOTIFICACIONES ///////////////////////////////////////
public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.proyecto.droidnotes";
    private static final String CHANNEL_NAME = "DroidNotes";

    private NotificationManager manager;



    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if (manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    // CONFIGURACION DE NUESTRA NOTIFICACION
    public NotificationCompat.Builder getNotification(String title, String body){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(Color.GRAY)
                .setSmallIcon(R.mipmap.ic_logoapp)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }



    public NotificationCompat.Builder getNotificationMessage(
            Message[] messages,
            String myMessage,
            String usernameSender,
            Bitmap bitmapReceiver,
            Bitmap myBitmap,
            NotificationCompat.Action actionResponse
    ){

        // YO RESPONDO EL MENSAJE
        Person myPerson = null;
        Person receiverPerson = null;

        if (bitmapReceiver == null){
            // USUARIO QUE RECIBIRIA EL MENSAJE
            receiverPerson =  new Person.Builder()
                    .setName(usernameSender)
                    .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.ic_person))
                    .build();
        }
        else{
            receiverPerson =  new Person.Builder()
                    .setName(usernameSender)
                    .setIcon(IconCompat.createWithBitmap(bitmapReceiver))
                    .build();

        }



        if (myBitmap == null){
            // USUARIO QUE RECIBIRIA EL MENSAJE
            myPerson =  new Person.Builder()
                    .setName("Tu")
                    .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.ic_person))
                    .build();
        }
        else{
            myPerson =  new Person.Builder()
                    .setName("Tu")
                    .setIcon(IconCompat.createWithBitmap(myBitmap))
                    .build();

        }


        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(receiverPerson);


        //  QUE SE MUESTREN LOS MENSAJES DEL USUARIO Y MI RESPUESTA
        for (Message m: messages){
            // AÑADIR NUEVOS MENSAJES A LA NOTIFICACION
            NotificationCompat.MessagingStyle.Message messageNotification = new NotificationCompat.MessagingStyle.Message(
                    m.getMessage(),
                    m.getTimestamp(),
                    receiverPerson
            );
            // AÑADIMOS EL MENSAJE QUE ACABAMOS DE CREAR
            messagingStyle.addMessage(messageNotification);
        }

        if (!myMessage.equals("")){
            NotificationCompat.MessagingStyle.Message myMessageNotification = new NotificationCompat.MessagingStyle.Message(
                    myMessage,
                    new Date().getTime(),
                    myPerson
            );
            // AÑADIMOS EL MENSAJE QUE ACABAMOS DE CREAR
            messagingStyle.addMessage(myMessageNotification);

        }
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_logoapp)
                .setStyle(messagingStyle)
                .addAction(actionResponse);
    }

}
