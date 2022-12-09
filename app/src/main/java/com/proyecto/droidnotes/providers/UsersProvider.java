package com.proyecto.droidnotes.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.proyecto.droidnotes.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UsersProvider {

    private CollectionReference mCollection;

    // Para utlizar las propiedades de estas clase un constructor vacio
    public UsersProvider()
    {
        mCollection = FirebaseFirestore.getInstance().collection("Users");
    }

    //METODO QUE RETORNA LOS DATOS DE UN USUARIO A TRAVES DEL ID
    public DocumentReference getUserInfo(String id)
    {
        return mCollection.document(id);
    }

    // METODO PARA ORDENAR LA LISTA POR NOMBRE
    public Query getAllUserByname(){
        return mCollection.orderBy("username");
    }

    // METODO PARA ORDENAR LA LISTA POR NOMBRE PERO SIN INCLUIR LOS DEL GRUPO ACTUAL
    public Query getAllUserAddGroup(ArrayList<String> lista){
        return mCollection.whereNotIn("id",lista);
    }

    // METODO PARA ORDENAR LA LISTA POR NOMBRE SOLO LOS DEL GRUPO ACTUAL
    public Query getAllUserGroup(ArrayList<String> lista){
        return mCollection.whereIn("id",lista);
    }

    //Metodo que permita almacenar un usuario en la BDD
    public Task<Void> create(User user)
    {
        return mCollection.document(user.getId()).set(user);
    }

    // METODO PARA EL MANEJO DE CRUD Y EL MANEJO COMPLETO DE DATOS EN FIREBASE
    public Task<Void> update(User user)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("image", user.getImage());

        return mCollection.document(user.getId()).update(map);
    }

    // METODO PARA EL MANEJO DE CRUD Y EL MANEJO COMPLETO DE DATOS EN FIREBASE
    public Task<Void> updateAll(User user)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("image", user.getImage());
        map.put("account", user.getAccount());
        map.put("career", user.getCareer());
        map.put("phone", user.getPhone());
        map.put("image", user.getImage());
        map.put("email", user.getEmail());

        return mCollection.document(user.getId()).update(map);
    }

    // METODO PARA ELIMINAR/ACTUALIZAR EL CAMPPO IMAGE DE LA BDD
    public Task<Void> updateImage(String id, String url)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("image", url);
        return mCollection.document(id).update(map);
    }

    // METODO PARA ACTUALIZAR EL USUARIOS
    public Task<Void> updateUsername(String id, String username)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        return mCollection.document(id).update(map);
    }

    public Task<Void> updateInfo(String id, String info)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("info", info);
        return mCollection.document(id).update(map);
    }


    // CREACION DEL TOKEN PARA LAS NOTIFICACIONES ========================================================================
    public void createToken(final String idUser){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                if (idUser!= null){
                    mCollection.document(idUser).update(map);
                }
            }
        });
        // ===============================================================================================================
    }





}
