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

    Menu mMenu;
    //////////////////// CIERRE /////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_multi_user);

        MyToolbar.show(AddMultiUserActivity.this, "Añadir Grupo", true);

        // INSTANCIAS DE VARIABLES =================================================================
        mFabCheck = findViewById(R.id.fabCheck);

        mRecyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        // ==========================================================================================

        //PARA QUE LO ELEMENTOS SE POSICIONEN UNO DEBAJO DEL OTRO
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddMultiUserActivity.this);
        mRecyclerViewContacts.setLayoutManager(linearLayoutManager);

        // EVENTO CLICK PARA AÑADIR PARTICIPANTES AL GRUPO
        mFabCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUsersSelected != null){
                    if (mUsersSelected.size() >= 2){
                        createChat();
                    }else{
                        Toast.makeText(AddMultiUserActivity.this, "Seleccione al menos 2 usuarios", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AddMultiUserActivity.this, "Por favor seleccione los usuarios ;)", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


    private void createChat(){
        Random random = new Random();
        int n = random.nextInt(100000);
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID().toString());
        chat.setTimestamp(new Date().getTime());
        chat.setIdNotification(n);
        chat.setMultichat(true);


        ArrayList<String> ids = new ArrayList<>();
        ids.add(mAuthProvider.getId());

        for (User u: mUsersSelected){
            ids.add(u.getId());
        }

        chat.setIds(ids);
        Gson gson = new Gson();
        String  chatJSON = gson.toJson(chat);

        Intent intent = new Intent(AddMultiUserActivity.this, ConfirmMultiUserChatActivity.class);
        intent.putExtra("chat", chatJSON);
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
        Query query = mUsersProvider.getAllUserByname();
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