package com.proyecto.droidnotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
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
import com.proyecto.droidnotes.activities.StatusDetailActivity;
import com.proyecto.droidnotes.models.Chat;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.Status;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.MessagesProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.proyecto.droidnotes.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    // VARIABLES ===================================================================================
    FragmentActivity context;
    AuthProvider authProvider;
    UsersProvider usersProvider;
    MessagesProvider messagesProvider;
    User user;

    ArrayList<Status> statusList;
    Gson gson = new Gson();
    // =============================================================================================

    //CREAMOS CONSTRUCTOR PARA LA CLASE PRINCIPAL
    public StatusAdapter(FragmentActivity context, ArrayList<Status> statusList)
    {
        this.context = context;
        this.statusList = statusList;
        // INSTANCIAS ===============================================
        authProvider = new AuthProvider();
        usersProvider = new UsersProvider();
        messagesProvider = new MessagesProvider();
        user = new User();

        // ==========================================================
    }




    private void getMultiChatInfo(ViewHolder holder, Chat chat) {
        if (chat.getGroupImage() != null){
            if (!chat.getGroupImage().equals("")){
                Picasso.with(context).load(chat.getGroupImage()).into(holder.circleImageUser);
            }
        }

        holder.textViewUsername.setText(chat.getGroupName());
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
        usersProvider.getUserInfo(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                // VALIDAMOS LA DOCUMENTACION DE LA BDD
                if (documentSnapshot != null){
                    if (documentSnapshot.exists()){
                        // OBTENEMOS LA INFORMACION DEL USUARIO POR ID
                        user = documentSnapshot.toObject(User.class);
                        holder.textViewUsername.setText(user.getUsername());
                    }
                }
            }
        });
    }



    // INSTANCIAMOS LA VISTA O EL LAYOUT
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_status, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Status[] statusGSON = gson.fromJson(statusList.get(position).getJson(), Status[].class);

        // ESTABLECEMOS LA LINEAS DEL ESTADO
        holder.circularStatusView.setPortionsCount(statusGSON.length);

        setImageStatus(statusGSON, holder);
        getUserInfo(holder, statusList.get(position).getIdUser());

        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StatusDetailActivity.class);
                intent.putExtra("status", statusList.get(position).getJson());
                context.startActivity(intent);
            }
        });

    }


    // METODO PARA MOSTRAR LA IMAGEN Y LA FECHA DEL USUARIO QUE SUBE EL ESTADO
    private void setImageStatus(Status[] statusGSON, ViewHolder holder) {
        if (statusGSON.length > 0){
            Picasso.with(context).load(statusGSON[statusGSON.length -1].getUrl()).into(holder.circleImageUser);
            String relativeTime = RelativeTime.timeFormatAMPM(statusGSON[statusGSON.length -1].getTimestamp(), context);
            holder.textViewDate.setText(relativeTime);
        }
    }

    @Override
    // RETORNAMOS EL TAMAÑO DE LA LISTA DE NUESTROS ESTADOS
    public int getItemCount() {
        return statusList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // INSTANCIARLOS ==========================================================================
        TextView textViewUsername, textViewDate;
        CircleImageView circleImageUser;
        CircularStatusView circularStatusView;
        View myView;


        public ViewHolder(View view){
            super(view);
            //VARIABLE QUE REPRESENTA A CADA UNO DE LOS ITEMS DE LA LISTA DE CONTACTS
            myView = view;

            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewDate = view.findViewById(R.id.textViewDate);
            circleImageUser = view.findViewById(R.id.circleImageUser);
            circularStatusView = view.findViewById(R.id.circularStatusView);
        }
    }


}
