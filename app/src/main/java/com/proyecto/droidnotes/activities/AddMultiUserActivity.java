package com.proyecto.droidnotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.adapters.ContactsAdapter;
import com.proyecto.droidnotes.adapters.MultiUsersAdapter;
import com.proyecto.droidnotes.models.Chat;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.UsersProvider;
import com.proyecto.droidnotes.utils.MyToolbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class AddMultiUserActivity extends AppCompatActivity {

    ////////////////////// VARIABLES ////////////////////////////////////
    RecyclerView mRecyclerViewContacts;

    FloatingActionButton mFabCheck;
    MultiUsersAdapter mAdapter;

    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    ArrayList<User> mUsersSelected;

    ArrayList<String> mUsersId = new ArrayList<>();

    String id_Chat="";
    Boolean verIntegrantes=false;
    Boolean crearGrupo=false;

    Menu mMenu;
    //////////////////// CIERRE /////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_multi_user);

        // INSTANCIAS DE VARIABLES =================================================================
        mFabCheck = findViewById(R.id.fabCheck);

        mRecyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        mUsersId = getIntent().getStringArrayListExtra("ids");
        id_Chat = getIntent().getStringExtra("id");
        verIntegrantes = getIntent().getBooleanExtra("VerIntegrantes",false);
        crearGrupo = getIntent().getBooleanExtra("group",false);

        Log.i("LOG","Crear Grupo: "+ crearGrupo);

        if (verIntegrantes==true){
            MyToolbar.show(AddMultiUserActivity.this, "Integrantes Grupo", true);
        }else{
            MyToolbar.show(AddMultiUserActivity.this, "Añadir Grupo", true);
        }


        // ==========================================================================================

        //PARA QUE LO ELEMENTOS SE POSICIONEN UNO DEBAJO DEL OTRO
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddMultiUserActivity.this);
        mRecyclerViewContacts.setLayoutManager(linearLayoutManager);

        // EVENTO CLICK PARA AÑADIR PARTICIPANTES AL GRUPO
        mFabCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUsersSelected != null){
//||(mUsersSelected.size() >= 1 && mUsersId.size()>0)
                    if (id_Chat.equals("")) {
                        if (mUsersSelected.size() >= 2) {
                            createChat();
                        } else {
                            Toast.makeText(AddMultiUserActivity.this, "Seleccione al menos 2 usuarios", Toast.LENGTH_SHORT).show();
                        }
                    } else if (!id_Chat.equals("")) {
                        if (mUsersSelected.size() >= 1) {
                            createChat();
                        } else {
                            Toast.makeText(AddMultiUserActivity.this, "Seleccione al menos 1 usuario", Toast.LENGTH_SHORT).show();
                        }
                    }

                }else{
                    Toast.makeText(AddMultiUserActivity.this, "Debe agregar usuarios", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


    private void createChat(){


        Random random = new Random();
        int n = random.nextInt(100000);
        Chat chat = new Chat();

        ArrayList<String> ids = new ArrayList<>();

        if (id_Chat.equals("")) {//Se agregan todos los campos si es creacion de nuevo grupo, de
            chat.setId(UUID.randomUUID().toString()); //lo contrario solo se actualizan los ids
            chat.setTimestamp(new Date().getTime());
            chat.setIdNotification(n);
            chat.setMultichat(true);
            ids.add(mAuthProvider.getId());
        }else{
            ids = mUsersId;
        }

        for (User u: mUsersSelected){
            ids.add(u.getId());

        }

        chat.setIds(ids);
        Log.i("LOG","User: "+chat.getIds());
        Gson gson = new Gson();
        String  chatJSON = gson.toJson(chat);

        Intent intent = new Intent(AddMultiUserActivity.this, ConfirmMultiUserChatActivity.class);
        intent.putExtra("chat", chatJSON);
        if(crearGrupo==true){
            intent.putExtra("group", false);
        }else{
            intent.putExtra("group", true);
        }
        intent.putExtra("idChat", id_Chat);
        if (verIntegrantes == true){
            intent.putExtra("VerIntegrantes",true);
        }
        startActivity(intent);

    }



    // METODO PARA ESTABLECER TODOS LOS USUARIOS SELECCIONADOS Y GUARDARLOS EN LA LISTA MUSERSSELECTED
    public void setUsers(ArrayList<User> users){

        if (mMenu != null){
            mUsersSelected = users;

            if (users.size() > 0){
                mMenu.findItem(R.id.itemCount).setTitle(Html.fromHtml("<font color='#ffffff'>" + users.size() + "</font>"));
            }
            else {
                mMenu.findItem(R.id.itemCount).setTitle("");
            }
        }


    }



    @Override
    public void onStart() {
        super.onStart();
        // CONSULTA A LA BASE DE DATOS
        Query query = null;

        try{
            if (!(id_Chat.equals("")) && !verIntegrantes){
                query = mUsersProvider.getAllUserAddGroup(mUsersId);
            }else if (!(id_Chat.equals(""))  && verIntegrantes){
                query = mUsersProvider.getAllUserGroup(mUsersId);
            }else{
                query = mUsersProvider.getAllUserByname();
            }
        }catch (Exception e){
            Log.i("LOG",e.getMessage());
            Toast.makeText(getApplicationContext(), "No se pudo mostrar los contactos",Toast.LENGTH_SHORT).show();
        }

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        mAdapter = new MultiUsersAdapter(options, AddMultiUserActivity.this);
        mRecyclerViewContacts.setAdapter(mAdapter);
        // QUE EL ADAPTER ESCUCHE LOS CAMBIOS EN TIEMPO REAL
        mAdapter.startListening();
    }


    // DETENER EL METODO ONSTART
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_user_menu, menu);
        mMenu = menu;

        return true;
    }
}