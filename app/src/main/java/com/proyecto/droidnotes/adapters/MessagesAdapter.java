package com.proyecto.droidnotes.adapters;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ShowImageOrVideoActivity;
import com.proyecto.droidnotes.models.Message;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.proyecto.droidnotes.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MessagesAdapter extends FirestoreRecyclerAdapter<Message, MessagesAdapter.ViewHolder> {

    // VARIABLES ===================================================================================
    Context context;
    AuthProvider authProvider;
    UsersProvider usersProvider;
    User user;
    ListenerRegistration listener;
    // =============================================================================================

    //Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    //CREAMOS CONSTRUCTOR PARA LA CLASE PRINCIPAL
    public MessagesAdapter(FirestoreRecyclerOptions options, Context context)
    {
        super(options);
        this.context = context;
        // INSTANCIAS ===============================================
        authProvider = new AuthProvider();
        usersProvider = new UsersProvider();
        user = new User();
        // ==========================================================
    }


    // ESTABLECEMOS LOS VALORES DE LAS VISTAS
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Message message) {
        //obtenemos el mensaje que queremos mostrar
        holder.textViewMessage.setText(message.getMessage());
        // CONVETIR LA FECHA DE COLLECTION
        holder.textViewDate.setText(RelativeTime.timeFormatAMPM(message.getTimestamp(), context));

        // METODO PARA SABER QUE NOSOTROS ENVIAMOS EL MENSAJE / TRABAJANDO CON ID-SENDER
        // INICIO DE LA CONFIGURACION

        // SI NOSOTROS ENVIAMOS EL MENSAJE
        if (message.getIdSender().equals(authProvider.getId())){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            // NUESTRO MENSAJE SE POSICIONARA A LA DERECHA
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            // ESTABLECEMOS MARGENES
            params.setMargins(100, 0, 0, 0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30,20,50,20);
            holder.linearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.bubble_corner_rigth));
            holder.textViewMessage.setTextColor(Color.WHITE);
            holder.textViewDate.setTextColor(Color.WHITE);
            holder.imageViewCheck.setVisibility(View.VISIBLE);

            // ESTADO DEL MENSAJE VISTO O NO VISTO
            if (message.getStatus().equals("ENVIADO")){
                holder.imageViewCheck.setImageResource(R.drawable.icon_double_check_gray);
            }else if (message.getStatus().equals("VISTO")){
                holder.imageViewCheck.setImageResource(R.drawable.icon_double_check_blue);
            }


        }else {
            // SI NOSOTROS RECIBIMOS MENSAJE
            //  PARA SABER SI SOMOS LOS USUARIOS RECEPTORES
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            // NUESTRO MENSAJE SE POSICIONARA A LA DERECHA
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0, 0, 100, 0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(80,20,30,20);
            holder.linearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.bubble_corner_left));
            holder.textViewMessage.setTextColor(Color.BLACK);
            holder.textViewDate.setTextColor(Color.BLACK);
            holder.imageViewCheck.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams marginDate = (ViewGroup.MarginLayoutParams) holder.textViewDate.getLayoutParams();
            marginDate.rightMargin = 10;

            // PREGUNTAR SI EL MENSAJE ES UNA MENSAJE DE UNA CHAT DE GRUPO
            if (message.getReceivers() != null){
                holder.textViewUsername.setVisibility(View.VISIBLE);
                holder.textViewUsername.setText(message.getUsername());
            }
            else {
                holder.textViewUsername.setVisibility(View.GONE);
            }
        }


        showImage(holder, message);
        showVideo(holder, message);
        showDocument(holder, message);
        openMessage(holder, message);

        holder.textViewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                      if (authProvider.getId().toString().equalsIgnoreCase(message.getIdSender().toString())){
                          PopupMenu pp = new PopupMenu(context,holder.textViewMessage);
                          pp.getMenuInflater().inflate(R.menu.menu_mensajes, pp.getMenu());
                          pp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                              @Override
                              public boolean onMenuItemClick(MenuItem menuItem) {
                                  switch (menuItem.getItemId()) {
                                      case R.id.Eliminar:
                                          //Toast.makeText(context, "Hola " + holder.textViewMessage.getText() + "Msj id:" + message.getId(), Toast.LENGTH_SHORT).show();
                                          mostrarAlerta("¿Desea eliminar el mensaje",message.getId(),"D");
                                          return true;
                                  }
                                  return false;
                              }
                          });
                          pp.show();
                      }

            }
        });
    }

    private void mostarMenu() {

    }

    // METODO PARA LA DESCARGA DE UN ARCHIVO
    private void openMessage(ViewHolder holder, Message message) {
        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SI ES DOCUMENTO PROCEDE A DESCARGARLO
                if(message.getType().equals("documento")){
                    File file = new File(context.getExternalFilesDir(null), "file");
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(message.getUrl()))
                            .setTitle(message.getMessage())
                            .setDescription("Download")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                }
                else if (message.getType().equals("imagen") || message.getType().equals("video")) {
                    Intent intent = new Intent(context, ShowImageOrVideoActivity.class);
                    intent.putExtra("type", message.getType());
                    intent.putExtra("url", message.getUrl());
                    context.startActivity(intent);
                }
            }
        });
    }


    // MOSTRAR LA DESCARGA DEL DOCUMENTO
    private void showDocument(ViewHolder holder, Message message) {
        if (message.getType().equals("documento")){
            if (message.getUrl() != null){
                if (!message.getUrl().equals("")){
                    holder.linearLayoutDocument.setVisibility(View.VISIBLE);
                }
                else {
                    holder.linearLayoutDocument.setVisibility(View.GONE);
                }
            }
            else {
                holder.linearLayoutDocument.setVisibility(View.GONE);
            }
        }else {
            holder.linearLayoutDocument.setVisibility(View.GONE);
        }
    }


    // CREACION DE METODO PARA MOSTRAR EL VIDEO
    private void showVideo(ViewHolder holder, Message message){
        if (message.getType().equals("video")){
            if (message.getUrl() != null){
                if (!message.getUrl().equals("")){
                    holder.frameLayoutVideo.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(message.getUrl()).into(holder.imageViewMessage);

                    if (message.getMessage().equals("\uD83C\uDFA5video")){
                        holder.textViewMessage.setVisibility(View.GONE);

                        ViewGroup.MarginLayoutParams marginDate = (ViewGroup.MarginLayoutParams) holder.textViewDate.getLayoutParams();
                        ViewGroup.MarginLayoutParams marginCheck = (ViewGroup.MarginLayoutParams) holder.imageViewCheck.getLayoutParams();
                        marginDate.topMargin = 15;
                        marginCheck.topMargin = 15;
                    }
                    else {
                        holder.textViewMessage.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    holder.frameLayoutVideo.setVisibility(View.GONE);
                    holder.textViewMessage.setVisibility(View.VISIBLE);
                }
            }else {
                holder.frameLayoutVideo.setVisibility(View.GONE);
                holder.textViewMessage.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.frameLayoutVideo.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.VISIBLE);
        }

    }



    // CREACION DE METODO PARA MOSTRAR EL MENSAJE
    private void showImage(ViewHolder holder, Message message){

        if (message.getType().equals("imagen")){
            if (message.getUrl() != null){
                if (!message.getUrl().equals("")){
                    holder.imageViewMessage.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(message.getUrl()).into(holder.imageViewMessage);

                    if (message.getMessage().equals("\uD83D\uDCF7imagen")){
                        holder.textViewMessage.setVisibility(View.GONE);

                        ViewGroup.MarginLayoutParams marginDate = (ViewGroup.MarginLayoutParams) holder.textViewDate.getLayoutParams();
                        ViewGroup.MarginLayoutParams marginCheck = (ViewGroup.MarginLayoutParams) holder.imageViewCheck.getLayoutParams();
                        marginDate.topMargin = 15;
                        marginCheck.topMargin = 15;
                    }
                    else {
                        holder.textViewMessage.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    holder.imageViewMessage.setVisibility(View.GONE);
                    holder.textViewMessage.setVisibility(View.VISIBLE);
                }
            }else {
                holder.imageViewMessage.setVisibility(View.GONE);
                holder.textViewMessage.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.imageViewMessage.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.VISIBLE);
        }

    }



    public ListenerRegistration getListener(){
        return listener;
    }



    // INSTANCIAMOS LA VISTA O EL LAYOUT
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_message, parent,false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // VARIABLES ==========================================================================
        TextView textViewMessage, textViewDate;
        TextView textViewUsername;
        ImageView imageViewCheck;
        LinearLayout linearLayoutMessage;
        LinearLayout linearLayoutDocument;

        ImageView imageViewMessage;

        FrameLayout frameLayoutVideo;
        View viewVideo;
        ImageView imageViewVideo;


        View myView;
        // CIERRE DE VARIABLES ====================================================================

        public ViewHolder(View view){
            super(view);

            //VARIABLE QUE REPRESENTA A CADA UNO DE LOS ITEMS DE LA LISTA DE CONTACTS
            myView = view;

            textViewMessage = view.findViewById(R.id.textViewMessage);
            textViewDate = view.findViewById(R.id.textViewDate);
            textViewUsername = view.findViewById(R.id.textViewUsername);
            imageViewCheck = view.findViewById(R.id.imageViewCheck);
            imageViewMessage = view.findViewById(R.id.imageViewMessage);
            linearLayoutMessage = view.findViewById(R.id.linearLayoutMessage);
            linearLayoutDocument = view.findViewById(R.id.linearLayoutDocument);
            frameLayoutVideo = view.findViewById(R.id.frameLayoutVideo);
            viewVideo = view.findViewById(R.id.viewVideo);
            imageViewVideo = view.findViewById(R.id.imageViewVideo);
        }
    }

    private void mostrarAlerta(String mensaje, String id, String tipo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(mensaje)
                .setTitle("Confirmación")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (tipo.equalsIgnoreCase("D")){ //Delete
                                    eliminarRegistro(id);
                                    //Toast.makeText(context, "Eliminado Mensaje id:" +id, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                ).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();

                            }
                        }
                );

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void eliminarRegistro(String id) {
        mFirestore.collection("Messages").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Mensaje Eliminado." +id, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "No se pudo eliminar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}