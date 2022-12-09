//Hola
package com.proyecto.droidnotes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.adapters.ChatsAdapter;
import com.proyecto.droidnotes.adapters.MessagesAdapter;
import com.proyecto.droidnotes.models.Chat;
import com.proyecto.droidnotes.models.FCMBody;
import com.proyecto.droidnotes.models.FCMResponse;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.ChatsProvider;
import com.proyecto.droidnotes.providers.FilesProvider;
import com.proyecto.droidnotes.providers.MessagesProvider;
import com.proyecto.droidnotes.providers.NotificationProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    //////////////////////////////////// VARIABLES GLOBALES /////////////////////////////////////////
    String mExtraIdUser;
    String mExtraIdChat;


    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ChatsProvider mChatsProvider;
    MessagesProvider mMessageProvider;
    FilesProvider mFilesProvider;
    NotificationProvider mNotificationProvider;

    ImageView mImageViewBack;
    TextView mTextViewUsername;
    CircleImageView mCircleImageUser;

    // MESSAGE
    EditText mEditextMessage;
    ImageView mImageViewSend;
    ImageView add_user, viewVerIntegrantes;
    // ==================

    ImageView mImageViewSelectFile;
    ImageView mImageViewSelectPictures;

    // NOTIFICACIONES
    User mUserReceiver;
    User mMyUser;
    // ==============

    MessagesAdapter mAdapter;
    RecyclerView mRecyclerViewMessages;
    LinearLayoutManager mLinearLayoutManager;

    Options mOptions;
    // Arreglo que almacene las url de las imagenes que seleccionemos
    ArrayList<String> mReturnValues = new ArrayList<>();
    ArrayList<Uri> mFileList;
    final int ACTION_FILE = 2;
    Chat mChat;
    /////////////////////////////////// CIERRE DE VARIABLES ////////////////////////////////////////

    /////////////////////////////////// INICIO DEL CREATE //////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setStatusBarColor();

        // INSTANCIAS ==============================================================================
        mExtraIdUser = getIntent().getStringExtra("idUser");
        mExtraIdChat = getIntent().getStringExtra("idChat");
        //mUser = new User();
        Log.i("LOG", "Ingresando a Chat Activity");
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mChatsProvider = new ChatsProvider();
        mMessageProvider = new MessagesProvider();
        mFilesProvider = new FilesProvider();
        mNotificationProvider = new NotificationProvider();

        mEditextMessage = findViewById(R.id.editTextMessage);
        mImageViewSend = findViewById(R.id.imageViewSend);
        mImageViewSelectFile = findViewById(R.id.imageViewSelectFile);
        mRecyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        mImageViewSelectPictures = findViewById(R.id.imageViewSelectPictures);
        // CIERRE INTANCIAS ========================================================================



        // LA INFORMACION QUE SE MOSTRARA SE REFLEJARA UNA DEBAJO DEL OTRO
        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessages.setLayoutManager(mLinearLayoutManager);


        mOptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(mReturnValues)                            //Pre selected Image Urls
                .setExcludeVideos(false)
                .setSpanCount(4)                                               //Span count for gallery min 1 & max 5
                .setMode(Options.Mode.All)                                     //Option to select only pictures or videos or both
                .setVideoDurationLimitinSeconds(0)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/pix/images");                                       //Custom Path For media Storage


        // =========================================================================================
        showChatToolbar(R.layout.chat_toolbar);
        getUserReceiverInfo();
        getMyUserInfo();
        checkIfExistChat();



        // EVENTO CLICK AL BOTON ENVIAR DEL CHAT
        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMessage();
            }
        });


        // AL SELECCIONAR LA IMAGEN ABRIRA LA SELECCION DE IMAGENES
        mImageViewSelectPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPix();
            }
        });

        mImageViewSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFiles();
            }
        });

    }
    ///////////////////////////// CIERRE DEL CREATE /////////////////////////////////////////////////


    // OBTENEMOS LOS TIPOS DE ARCHIVOS QUE VAN A SER VALIDOS PARA SUBIR EN NUESTRO CHAT
    private void selectFiles() {
        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), ACTION_FILE);
    }


    //  INSTANCIAR EL ADAPTER =================================================

    // SE EJECUTA AL ABRIR EL ACTIVITY
    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null){
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null){
            mAdapter.stopListening();
        }

    }

    //INICIALIZA NUESTRA LIBRERIA PARA SELECCIONAR LA IMAGEN
    private void startPix()
    {
        Pix.start(ChatActivity.this, mOptions);
    }

    // ========================================================================



    //  CREACION DEL MENSAJE
    private void createMessage() {
        String textMessage = mEditextMessage.getText().toString();
        if (!textMessage.equals("")) {
            // CREAMOS MODELO DE TIPO MESSAGE
            Message message = new Message();
            // CHAT AL CUAL PERTENECEN LO MENSAJES QUE CREAREMOS
            message.setIdChat(mExtraIdChat);
            // NUESTRO USUARIO YA QUE ESTAMOS ESCRIBIENDO EL MENSAJE Y ENVIANDOLO
            message.setIdSender(mAuthProvider.getId());
            // USUARIO DE RECIBE EL MENSAJE
            message.setIdReceiver(mExtraIdUser);
            // TEXTO O MENSAJE
            message.setMessage(textMessage);
            message.setStatus("ENVIADO");
            //ESTABLECEMOS EL TIPO DE MENSAJE
            message.setType("texto");
            // FECHA
            message.setTimestamp(new Date().getTime());

            // VALIDAMOS QUE LA INFORMACION SE HAYA CREADO CORRECTAMENTE
            mMessageProvider.create(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    mEditextMessage.setText("");
                    if (mAdapter != null){
                        mAdapter.notifyDataSetChanged();
//                        // Toast.makeText(ChatActivity.this, "El mensaje se creo correctamente", Toast.LENGTH_SHORT).show();
                    }
                    getLastMessages(message);
                }
            });
        }
        else {
            Toast.makeText(this, "Ingresa el mensaje", Toast.LENGTH_SHORT).show();
        }
    }


    // OBTENER LOS MENSAJES APILADOS NO LEIDOS
    private void getLastMessages(final Message message){
        mMessageProvider.getLastMessageByChatAndSender(mExtraIdChat, mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot != null){
                    ArrayList<Message> messages = new ArrayList<>();

                    for (DocumentSnapshot document: querySnapshot.getDocuments()){
                        Message m = document.toObject(Message.class);
                        messages.add(m);
                    }
                    if (messages.size() == 0){
                        messages.add(message);
                    }

                    // VOLTEAR LA LISTA DE MENSAJES EN LAS NOTIFICACIONES
                    Collections.reverse(messages);
                    sendNotification(messages);

                }
            }
        });
    }


    // METODO PARA ENVIAR LA NOTIFICACION
    private void sendNotification(ArrayList<Message> messages) {
        Map<String, String> data = new HashMap<>();
        data.put("title", "MENSAJE");
        data.put("body", "texto mensaje");
        data.put("idNotification", String.valueOf(mChat.getIdNotification()));
        data.put("usernameReceiver", mUserReceiver.getUsername());
        data.put("usernameSender", mMyUser.getUsername());
        data.put("imageReceiver", mUserReceiver.getImage());
        data.put("imageSender", mMyUser.getImage());
        data.put("idChat", mExtraIdChat);
        data.put("idSender", mAuthProvider.getId());
        data.put("idReceiver", mUserReceiver.getId());
        data.put("tokenSender", mMyUser.getToken());
        data.put("tokenReceiver", mUserReceiver.getToken());


        // CONVERTIR A UN OBJETO JSON
        Gson gson = new Gson();
        String messagesJSON = gson.toJson(messages);
        data.put("messagesJSON", messagesJSON);

        List<String> tokens = new ArrayList<>();
        tokens.add(mUserReceiver.getToken());

        mNotificationProvider.send(ChatActivity.this, tokens, data);
    }

    // METODO PARA VERIFICAR SI EL CHAT EXISTE
    private void checkIfExistChat() {
        mChatsProvider.getChatByUser1AndUser2(mExtraIdUser, mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null){
                    if (queryDocumentSnapshots.size() == 0){
                        createChat();
                    }
                    else {
                        //OTENEMOS EL ID DEL CHAT
                        mExtraIdChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                        getMessageByChat();
                        updateStatus();
                        getChatInfo();
//                        Toast.makeText(ChatActivity.this, "El chat ya existe entre estos dos usuarios", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getChatInfo() {
        mChatsProvider.getChatById(mExtraIdChat).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null){
                    if (documentSnapshot.exists()){
                        mChat = documentSnapshot.toObject(Chat.class);
                    }
                }
            }
        });
    }

    private void updateStatus() {
        mMessageProvider.getMessageNotRead(mExtraIdChat).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                    Message message = document.toObject(Message.class);

                    // UNICAMENTE VALIDAR QUE ACTUALICE EL ESTADO DE LOS MENSAJES QUE ME ENVIAn
                    if (!message.getIdSender().equals(mAuthProvider.getId())){
                        mMessageProvider.updateStatus(message.getId(), "VISTO");
                    }
                }
            }
        });
    }

    // METODO PARA OBTENER LOS MENSAJES
    private void getMessageByChat() {
        // CONSULTA A LA BASE DE DATOS
        Query query = mMessageProvider.getMessageByChat(mExtraIdChat);
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        mAdapter = new MessagesAdapter(options, ChatActivity.this);
        mRecyclerViewMessages.setAdapter(mAdapter);

        // QUE EL ADAPTER ESCUCHE LOS CAMBIOS EN TIEMPO REAL
        mAdapter.startListening();

        // METODO PARA SABER SI SE CREO UN MENSAJE NUEVO EN LA BDD
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //CONFIGURACIONES
                updateStatus();
                int numberMessage = mAdapter.getItemCount();
                int LastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (LastMessagePosition == -1 || (positionStart >= (numberMessage -1) && LastMessagePosition == (positionStart -1))){
                    mRecyclerViewMessages.scrollToPosition(positionStart);
                }
            }
        });

    }


    private void createChat() {
        Random random = new Random();
        int n = random.nextInt(100000);

        mChat = new Chat();
        mChat.setId(mAuthProvider.getId() + mExtraIdUser);
        mChat.setTimestamp(new Date().getTime());
        mChat.setIdNotification(n);

        ArrayList<String> ids = new ArrayList<>();
        ids.add(mAuthProvider.getId());
        ids.add(mExtraIdUser);

        mChat.setIds(ids);

        mExtraIdChat = mChat.getId();

        // METODO PARA SABER SI LA INFORMACION SE CREO CORRECTAMENTE
        mChatsProvider.create(mChat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // LLAMAMOS AL METODO OBTENER MENSAJES POR CHAT
                getMessageByChat();
//                Toast.makeText(ChatActivity.this, "El chat se creo correctamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMyUserInfo(){
        mUsersProvider.getUserInfo(mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    mMyUser = documentSnapshot.toObject(User.class);
                }
            }
        });
    }



    // VALIDACION EN TIEMPO REAL
    private void getUserReceiverInfo() {
        mUsersProvider.getUserInfo(mExtraIdUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null){
                    if(documentSnapshot.exists()){
                        //OBTENIENDO LA INFO DEL USUARIO
                        mUserReceiver = documentSnapshot.toObject(User.class);
                        mTextViewUsername.setText(mUserReceiver.getUsername());

                        //MOSTRAR LA IMAGEN
                        if (mUserReceiver.getImage() != null){
                            if (!mUserReceiver.getImage().equals("")){
                                Picasso.with(ChatActivity.this).load(mUserReceiver.getImage()).into(mCircleImageUser);
                            }
                        }
                    }
                }
            }
        });
    }

    //TOOLBAR PERSONALIZADO
    private  void showChatToolbar(int resource){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resource, null);
        actionBar.setCustomView(view);


        // REGRESAR AL HOME ACTIVITY <-
        mImageViewBack = view.findViewById(R.id.imageViewBack);
        mTextViewUsername = view.findViewById(R.id.textViewUsername);
        mCircleImageUser = view.findViewById(R.id.circleImageUser);
        add_user = view.findViewById(R.id.img_add_user);
        viewVerIntegrantes = view.findViewById(R.id.imgVerIntegrantes);

        add_user.setVisibility(View.INVISIBLE);
        viewVerIntegrantes.setVisibility(View.INVISIBLE);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // METODO QUE NOS PERMITE CAPTURAR LOS VALORES QUE EL USUARIO SELECCIONO=========================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Intent intent = new Intent(ChatActivity.this, ConfirmImageSendActivity.class);
            intent.putExtra("data", mReturnValues);
            intent.putExtra("idChat", mExtraIdChat);
            intent.putExtra("idReceiver", mExtraIdUser);

            Gson gson = new Gson();
            String myUserJSON = gson.toJson(mMyUser);
            String receiverUserJSON = gson.toJson(mUserReceiver);

            intent.putExtra("myUser", myUserJSON);
            intent.putExtra("receiverUser", receiverUserJSON);

            Log.i("LOG", "ReceiverJson"+receiverUserJSON);

            intent.putExtra("idNotification", String.valueOf(mChat.getIdNotification()));
            startActivity(intent);
        }

        if (requestCode == ACTION_FILE && resultCode == RESULT_OK)
        {
            mFileList = new ArrayList<>();
            ClipData clipData = data.getClipData();

            // SELECCIONO UN SOLO ARCHIVO
            if (clipData == null){
                Uri uri = data.getData();
                mFileList.add(uri);
            }
            // SELECCIONO VARIOS ARCHIVOS
            else   {
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++){
                    Uri uri = clipData.getItemAt(i).getUri();
                    mFileList.add(uri);
                }
            }
            mFilesProvider.saveFiles(ChatActivity.this, mFileList, mExtraIdChat, mExtraIdUser);


            // CREAMOS MODELO DE TIPO MESSAGE
            Message message = new Message();
            // CHAT AL CUAL PERTENECEN LO MENSAJES QUE CREAREMOS
            message.setIdChat(mExtraIdChat);
            // NUESTRO USUARIO YA QUE ESTAMOS ESCRIBIENDO EL MENSAJE Y ENVIANDOLO
            message.setIdSender(mAuthProvider.getId());
            // USUARIO DE RECIBE EL MENSAJE
            message.setIdReceiver(mExtraIdUser);
            // TEXTO O MENSAJE
            message.setMessage("\uD83D\uDCC4 Documento");
            message.setStatus("ENVIADO");
            //ESTABLECEMOS EL TIPO DE MENSAJE
            message.setType("texto");
            // FECHA
            message.setTimestamp(new Date().getTime());
            ArrayList<Message> messages = new ArrayList<>();
            messages.add(message);
            sendNotification(messages);

        }
    }

    // Metodo para los permisos a uso de la camara y accesar a la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(ChatActivity.this, mOptions);
            } else {
                Toast.makeText(ChatActivity.this, "Por favor concede los permisos para accesar a la camara!!", Toast.LENGTH_LONG).show();
            }
        }
    }
// =======================================================================================================

    // LA BARRA SUPOERIOR COLOREARLA DE COLOR NEGRO
    private void setStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack, this.getTheme()));
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorFullBlack));
        }
    }

}