package com.proyecto.droidnotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ChatActivity;
import com.proyecto.droidnotes.activities.ChatMultiActivity;
import com.proyecto.droidnotes.models.Chat;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.MessagesProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.proyecto.droidnotes.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends FirestoreRecyclerAdapter<Chat, ChatsAdapter.ViewHolder> {

    // VARIABLES ===================================================================================
    Context context;
    AuthProvider authProvider;
    UsersProvider usersProvider;
    MessagesProvider messagesProvider;
    User user;
    ListenerRegistration listener, listenerLastMessage;
    // =============================================================================================

    //CREAMOS CONSTRUCTOR PARA LA CLASE PRINCIPAL
    public ChatsAdapter(FirestoreRecyclerOptions options, Context context)
    {
        super(options);
        this.context = context;
        // INSTANCIAS ===============================================
        authProvider = new AuthProvider();
        usersProvider = new UsersProvider();
        messagesProvider = new MessagesProvider();
        user = new User();
        // ==========================================================
    }


    // ESTABLECEMOS LOS VALORES QUE VIENEN DESDE LA BDD
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Chat chat) {

        String idUser = "";

        // RECORREMOS CADA UNO DE LOS CAMPOS IDS DE CHATS
        for (int i = 0; i < chat.getIds().size(); i++){
            // VALIDAMOS LA COMPARACION ENTRE LOS DOS IDS
            if (authProvider.getId()!=null){
                if (!authProvider.getId().equals(chat.getIds().get(i))){
                    idUser = chat.getIds().get(i);
                    break;
                }
            }
        }


        getLastMessage(holder, chat.getId());

        // ACCEDEMOS A CADA UNA DE LAS VISTAS
        if (chat.isMultichat()){
            getMultiChatInfo(holder, chat);
        }
        else {
            getUserInfo(holder, idUser);
        }

        clickMyView(holder, chat, idUser);

    }


    private void getMultiChatInfo(ViewHolder holder, Chat chat) {
        if (chat.getGroupImage() != null){
            if (!chat.getGroupImage().equals("")){
                Picasso.with(context).load(chat.getGroupImage()).into(holder.circleImageUser);
            }
        }

        holder.textViewUsername.setText(chat.getGroupName());
    }


    // METODO PARA MOSTRAR EL ULTIMO MENSAJE EN CHATS
    private void getLastMessage(final ViewHolder holder, String idChat) {
        // OBTENEMOS LA INFORMACION EN TIEMPO REAL
        listenerLastMessage = messagesProvider.getLastMessage(idChat).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                if (querySnapshot != null) {
                    int size = querySnapshot.size();
                    if (size > 0){
                        Message message = querySnapshot.getDocuments().get(0).toObject(Message.class);
                        holder.textViewLastMessage.setText(message.getMessage());
                        holder.textViewTimestamp.setText(RelativeTime.timeFormatAMPM(message.getTimestamp(), context));


                        if (message.getIdSender().equals(authProvider.getId())){
                            holder.imageViewCheck.setVisibility(View.VISIBLE);
                            if (message.getStatus().equals("ENVIADO")){
                                holder.imageViewCheck.setImageResource(R.drawable.icon_double_check_gray);
                            }
                            else   if (message.getStatus().equals("VISTO")){
                                holder.imageViewCheck.setImageResource(R.drawable.icon_double_check_blue);
                            }
                        }else {
                            holder.imageViewCheck.setVisibility(View.GONE);
                        }
                    }
                }

            }
        });
    }


    // METODO QUE NOS LLEVA A LA ACTIVIDAD DEL CHAT
    private void clickMyView(ViewHolder holder, Chat chat , final String idUser) {
        //EVENTO CLICK HACIA EL CHAT DEL CONTACTO
        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat.isMultichat()){
                    goToChatMultiActivity(chat);
                }
                else {
                    goToChatActivity(chat.getId(), idUser);
                }
            }
        });
    }

    // METODO PARA ENVIAR AL USUARIO AL CHAT DE MULTIPLES USUARIOS
    private void goToChatMultiActivity(Chat chat) {
        Intent intent = new Intent(context, ChatMultiActivity.class);
        Gson gson = new Gson();
        String chatJSON = gson.toJson(chat);
        intent.putExtra("chat", chatJSON);
        context.startActivity(intent);
    }


    private void getUserInfo(final ViewHolder holder, String idUser) {

        // EVENTO PARA OBTENER LA INFORMACION EL TIEMPO REAL
        listener = usersProvider.getUserInfo(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                // VALIDAMOS LA DOCUMENTACION DE LA BDD
                if (documentSnapshot != null){
                    if (documentSnapshot.exists()){
                        // OBTENEMOS LA INFORMACION DEL USUARIO POR ID
                        user = documentSnapshot.toObject(User.class);
                        holder.textViewUsername.setText(user.getUsername());

                        //VALIDAMOS QUE LA IMAGEN NO VENGA VACIA
                        if (user.getImage() != null){
                            if (!user.getImage().equals("")){
                                Picasso.with(context).load(user.getImage()).into(holder.circleImageUser);
                            }
                            else {
                                holder.circleImageUser.setImageResource(R.drawable.ic_person);
                            }
                        }else{
                            holder.circleImageUser.setImageResource(R.drawable.ic_person);
                        }
                    }
                }
            }
        });
    }

    public ListenerRegistration getListener(){
        return listener;
    }

    public ListenerRegistration getListenerLastMessage(){
        return listenerLastMessage;
    }


    // METODO HACIA EL CHAT DEL CONTACTO
    private void goToChatActivity(String idChat, String idUser)
    {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idUser", idUser);
        intent.putExtra("idChat", idChat);
        context.startActivity(intent);

    }


    // INSTANCIAMOS LA VISTA O EL LAYOUT
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chats, parent,false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // INSTANCIARLOS ==========================================================================
        TextView textViewUsername, textViewLastMessage, textViewTimestamp;
        CircleImageView circleImageUser;
        ImageView imageViewCheck;
        View myView;


        public ViewHolder(View view){
            super(view);
            //VARIABLE QUE REPRESENTA A CADA UNO DE LOS ITEMS DE LA LISTA DE CONTACTS
            myView = view;

            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewLastMessage = view.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = view.findViewById(R.id.textViewTimestamp);
            circleImageUser = view.findViewById(R.id.circleImageUser);
            imageViewCheck = view.findViewById(R.id.imageViewCheck);
        }
    }


}
