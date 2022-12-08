package com.proyecto.droidnotes.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.proyecto.droidnotes.R;
import com.proyecto.droidnotes.adapters.ContactsAdapter;
import com.proyecto.droidnotes.models.User;
import com.proyecto.droidnotes.providers.AuthProvider;
import com.proyecto.droidnotes.providers.UsersProvider;


public class ContactsFragment extends Fragment {


    // VARIABLES GLOBALES ==========================================================================
    View mView;
    RecyclerView mRecyclerViewContacts;

    ContactsAdapter mAdapter;

    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    // =============================================================================================

    public ContactsFragment() {

    }



    // METODO DONDE SE INSTANCIA LA VISTA QUE ESTAMOS UTILIZANDO
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacs, container, false);
        // INSTANCIAS DE VARIABLES =================================================================
        mRecyclerViewContacts = mView.findViewById(R.id.recyclerViewContacts);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

       // ==========================================================================================

        //PARA QUE LO ELEMENTOS SE POSICIONEN UNO DEBAJO DEL OTRO
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewContacts.setLayoutManager(linearLayoutManager);

       return  mView;
    }


    // FIREBASE UI /NOS PERMITE LISTAR LOS DATOS
    // METODO DEL CICLO DE VIDA onStart
    @Override
    public void onStart() {
        super.onStart();
        // CONSULTA A LA BASE DE DATOS
        Query query = mUsersProvider.getAllUserByname();
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        mAdapter = new ContactsAdapter(options, getContext());
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
}