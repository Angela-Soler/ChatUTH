package com.proyecto.droidnotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.activities.ChatActivity;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends FirestoreRecyclerAdapter<User, ContactsAdapter.ViewHolder> {

    // VARIABLES ===================================================================================
    Context context;
    AuthProvider authProvider;
    // =============================================================================================

    //CREAMOS CONSTRUCTOR PARA LA CLASE PRINCIPAL
    public ContactsAdapter(FirestoreRecyclerOptions options, Context context)
    {
        super(options);
        this.context = context;
        authProvider = new AuthProvider();
    }


    // ESTABLECEMOS LOS VALORES QUE VIENEN DESDE LA BDD
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final User user) {

        // ACULTAR DE SESION DEL USUARIO
        if (user.getId() != null && authProvider.getId() != null){
            if (user.getId().equals(authProvider.getId())){
                RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                param.height = 0;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                param.topMargin = 0;
                param.bottomMargin = 0;
                holder.itemView.setVisibility(View.VISIBLE);
            }
        }

        holder.textViewInfo.setText(user.getInfo());
        holder.textViewUsername.setText(user.getUsername());

        if (user.getImage() != null){
            if (!user.getImage().equals("")){
                // LLAMAMOS LA IMAGEN
                Picasso.with(context).load(user.getImage()).into(holder.circleImageUser);
                //holder.circleImageUser.setImageResource(R.drawable.ic_person);
            }
            else{
                holder.circleImageUser.setImageResource(R.drawable.ic_person);
            }

        }
        else{
            holder.circleImageUser.setImageResource(R.drawable.ic_person);
        }

        // EVENTO CLICK HACIA EL CHAT DEL CONTACTO
        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatActivity(user.getId());
            }
        });

    }

    // METODO HACIA EL CHAT DEL CONTACTO
    private void goToChatActivity(String id)
    {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idUser", id);
        context.startActivity(intent);
    }


    // INSTANCIAMOS LA VISTA O EL LAYOUT
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_contacts, parent,false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // INSTANCIARLOS ==========================================================================
        TextView textViewUsername;
        TextView textViewInfo;
        CircleImageView circleImageUser;
        View myView;


        public ViewHolder(View view){
            super(view);
            //VARIABLE QUE REPRESENTA A CADA UNO DE LOS ITEMS DE LA LISTA DE CONTACTS
            myView = view;

            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewInfo = view.findViewById(R.id.textViewInfo);
            circleImageUser = view.findViewById(R.id.circleImageUser);
        }
    }


}
