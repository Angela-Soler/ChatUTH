package com.proyecto.droidnotes.providers;



import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

// MANEJO DE AUTENTICACION
public class AuthProvider {

    private FirebaseAuth mAuth;

    //Creamos nuestro constructor
    public AuthProvider()
    {
        mAuth = FirebaseAuth.getInstance();

    }

    // Validacion de autenticacion
    public void sendCodeVerification(String phone, PhoneAuthProvider.OnVerificationStateChangedCallbacks callback)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber (
                phone,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                callback
        );
    }


    public Task<AuthResult> signInPhone(String verificacationId, String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificacationId, code);
        return mAuth.signInWithCredential(credential);

    }

    public Task<AuthResult> signInEmail(String email, String code)
    {
        //PhoneAuthCredential credential = PhoneAuthProvider.getCredential(email, code);
        return mAuth.signInWithEmailAndPassword(email, code);

    }

    // METODO SIEMPRE Y CUANDO HAYA INICIADO SESION CORRECTAMENTE
    public FirebaseUser getSessionUser()
    {
        return mAuth.getCurrentUser();
    }


    //Metodo que decuelve la sesion del usuario
    public String getId()
    {
        if (mAuth.getCurrentUser() != null)
        {
//            System.out.println(mAuth.getCurrentUser());
            return mAuth.getCurrentUser().getUid();
        }
        else
        {
            return null;
        }
    }

    // METODO VOID YA QUE NO RETORNARA NADA Y CERRAR SESION
    public void signOut()
    {
        mAuth.signOut();
    }

}
