package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.adapters.OptionsPagerAdapter;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ChatsProvider;
import com.proyecto.droidnotes.providers.ImageProvider;
import com.proyecto.droidnotes.providers.MessagesProvider;
import com.proyecto.droidnotes.providers.NotificationProvider;
import com.proyecto.droidnotes.utils.ExtensionFile;
import com.proyecto.droidnotes.utils.ShadowTransformer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmImageSendActivity extends AppCompatActivity {

    // VARIABLES GLOBALES =========================================================================
    ViewPager mViewPager;
    // VARIABLES DE USUARIOS EMISOR Y RECEPTOR ====
    String mExtraIdChat;
    String mExtraIdReceiver;
    String mExtraIdNotification;
    // CIERRE =====================================
    ArrayList<String> data;
    ArrayList<Message> messages = new ArrayList<>();

    User mExtraMyUser;
    User mExtraReceiverUser;

    ImageProvider mImageProvider;
    AuthProvider mAuthProvider;
    ChatsProvider mChatProvier;
    NotificationProvider mNotificationProvider;
    // ============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_image_send);
        setStatusBarColor();

        // INSTANCIAS ==============================================================================
        mViewPager = findViewById(R.id.viewPager);
        mAuthProvider = new AuthProvider();

        data = getIntent().getStringArrayListExtra("data");
        mExtraIdChat = getIntent().getStringExtra("idChat");
        mExtraIdReceiver = getIntent().getStringExtra("idReceiver");
        mExtraIdNotification = getIntent().getStringExtra("idNotification");
        mImageProvider = new ImageProvider();
        mChatProvier = new ChatsProvider();
        mNotificationProvider = new NotificationProvider();
        // ========================================================================================
        String myUser = getIntent().getStringExtra("myUser");
        String receiverUser = getIntent().getStringExtra("receiverUser");

        Gson gson = new Gson();
        mExtraMyUser = gson.fromJson(myUser, User.class);
        mExtraReceiverUser = gson.fromJson(receiverUser, User.class);


        // INCLUYE EN messages TODOS LOS MENSAJES QUE SE ALMACENARIAN EN LA BDD
        if (data != null){
            for (int i = 0; i < data.size(); i++){
                Message m = new Message();
                m.setIdChat(mExtraIdChat);
                m.setIdSender(mAuthProvider.getId());
                m.setIdReceiver(mExtraIdReceiver);
                m.setStatus("ENVIADO");
                // FECHA
                m.setTimestamp(new Date().getTime());

                // URL DE LA IMAGEN QUE SELECCIONAMOS DESDE EL CELULAR
                m.setUrl(data.get(i));


                if (ExtensionFile.isImageFile(data.get(i))){
                    m.setType("imagen");
                    // MENSAJE POR DEFECTO
                    m.setMessage("\uD83D\uDCF7imagen");
                }else  if (ExtensionFile.isVideoFile(data.get(i))){
                    m.setType("video");
                    // MENSAJE POR DEFECTO
                    m.setMessage("\uD83C\uDFA5video");
                }

                messages.add(m);
            }
        }

        OptionsPagerAdapter pagerAdapter = new OptionsPagerAdapter(
          getApplicationContext(),
          getSupportFragmentManager(),
                dpToPixels(2, this),
                data
        );
        ShadowTransformer transformer = new ShadowTransformer(mViewPager, pagerAdapter);
        transformer.enableScaling(true);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setPageTransformer(false, transformer);

    }

    // METODO PARA IMPRIMIR LOS COMENTARIOS
    public void send(){
        //for (int i = 0; i < messages.size(); i++){
            mImageProvider.uploadMultiple(ConfirmImageSendActivity.this, messages);

            // CREAMOS MODELO DE TIPO MESSAGE
            Message message = new Message();
            // CHAT AL CUAL PERTENECEN LO MENSAJES QUE CREAREMOS
            message.setIdChat(mExtraIdChat);
            // NUESTRO USUARIO YA QUE ESTAMOS ESCRIBIENDO EL MENSAJE Y ENVIANDOLO
            message.setIdSender(mAuthProvider.getId());
            // USUARIO DE RECIBE EL MENSAJE
            message.setIdReceiver(mExtraIdReceiver);
            // TEXTO O MENSAJE
            message.setMessage("\uD83D\uDCF7 Imagen");
            message.setStatus("ENVIADO");
            //ESTABLECEMOS EL TIPO DE MENSAJE
            message.setType("texto");
            // FECHA
            message.setTimestamp(new Date().getTime());
            ArrayList<Message> messages = new ArrayList<>();
            messages.add(message);

            sendNotification(messages);
            finish();
       // }

    }


    // METODO PARA ENVIAR LA NOTIFICACION
    private void sendNotification(ArrayList<Message> messages) {
        Map<String, String> data = new HashMap<>();
        data.put("title", "MENSAJE");
        data.put("body", "texto mensaje");
        data.put("idNotification", String.valueOf(mExtraIdNotification));
        data.put("usernameReceiver", mExtraReceiverUser.getUsername());
        data.put("usernameSender", mExtraMyUser.getUsername());
        data.put("imageReceiver", mExtraReceiverUser.getImage());
        data.put("imageSender", mExtraMyUser.getImage());
        data.put("idChat", mExtraIdChat);
        data.put("idSender", mAuthProvider.getId());
        data.put("idReceiver", mExtraIdReceiver);
        data.put("tokenSender", mExtraMyUser.getToken());
        data.put("tokenReceiver", mExtraReceiverUser.getToken());


        // CONVERTIR A UN OBJETO JSON
        Gson gson = new Gson();
        String messagesJSON = gson.toJson(messages);
        data.put("messagesJSON", messagesJSON);

        List<String> tokens = new ArrayList<>();
        tokens.add(mExtraReceiverUser.getToken());

        mNotificationProvider.send(ConfirmImageSendActivity.this, tokens, data);
    }


    public void setMessage(int position, String message){
        messages.get(position).setMessage(message);
    }


    public static float dpToPixels(int dp, Context context){
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }
}