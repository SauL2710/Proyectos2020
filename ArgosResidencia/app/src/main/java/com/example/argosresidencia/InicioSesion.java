package com.example.argosresidencia;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class InicioSesion extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    Sesion sesion = Sesion.getInstance();

    private EditText txtCorreo;
    private EditText txtContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);

        Sesion s = Sesion.getInstance();

        //Evalúa tipo de usuario para iniciar
        if (s.getNoControl() == null) {

            String lblMsn ="¿Olvidaste tu contraseña?";

            txtCorreo = findViewById(R.id.txtCorreo);
            txtContrasena = findViewById(R.id.txtContrasena);

            progressDialog = new ProgressDialog(this);

            //Recuperar contraseña
            TextView lblRecuperarPass = findViewById(R.id.lblRecuperarContrasena);
            SpannableString sMsn = new SpannableString(lblMsn);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(InicioSesion.this, Contrasena.class);
                    startActivity(intent);
                }
            };
            sMsn.setSpan(clickableSpan, 0,25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            lblRecuperarPass.setText(sMsn);
            lblRecuperarPass.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else {
            if(s.getTipoUsuario() == 1) {  //Menú Alumno
                Intent intent = new Intent(InicioSesion.this, A_Menu.class);
                startActivity(intent);
            }
            if(s.getTipoUsuario() == 2){  //Menú Monitor
                Intent intent = new Intent(InicioSesion.this, M_Menu.class);
                startActivity(intent);
            }
        }
    }

    public void ingresar(View view) {
        final String correo = txtCorreo.getText().toString().trim();
        String contrasena = txtContrasena.getText().toString().trim();

        /* Ingresar a Firebase */
        if(!(correo.isEmpty()) && !(contrasena.isEmpty())) {
            firebaseAuth.signInWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressDialog.setMessage("Iniciando sesión...");
                        progressDialog.show();
                        configuraSesion(correo);
                    } else {
                        Toast.makeText(InicioSesion.this,"No se pudo iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } //if
        else {
            Toast.makeText(InicioSesion.this,"Los campos no pueden quedar vacíos", Toast.LENGTH_SHORT).show();
        }
    }

    public void configuraSesion(final String correo) {
        db.collection("alumnos")
                .whereEqualTo("correo", correo)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            sesion.setNoControl(document.getId());
                            sesion.setCorreo(correo);
                            esMonitor(sesion.getNoControl());
                            MiFirebaseMessagingService firebaseMessaging = new MiFirebaseMessagingService();
                            firebaseMessaging.sendRegistrationToServer();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(InicioSesion.this,"No Control no registrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void esMonitor(String noControl) {
        DocumentReference docRef = db.collection("monitores").document(noControl);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        progressDialog.dismiss();
                        sesion.setTipoUsuario(2);
                        crearDialogoAlertaConBotonDeRadio();
                    } else {
                        progressDialog.dismiss();
                        sesion.setTipoUsuario(1);
                        Toast.makeText(InicioSesion.this, "Inició sesión como alumno", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(InicioSesion.this, A_Menu.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(InicioSesion.this,"Error al conectarse",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void crearDialogoAlertaConBotonDeRadio() {
        CharSequence[] valores = {"Alumno", "Monitor"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialogTheme);
        builder.setTitle("¿Cómo desea iniciar sesión?");

        builder.setSingleChoiceItems(valores, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Intent intent;
                switch (item) {
                    case 0:
                        Toast.makeText(InicioSesion.this, "Inició sesión como alumno", Toast.LENGTH_SHORT).show();
                        intent = new Intent(InicioSesion.this, A_Menu.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Toast.makeText(InicioSesion.this, "Inició sesión como monitor", Toast.LENGTH_SHORT).show();
                        intent = new Intent(InicioSesion.this, M_Menu.class);
                        startActivity(intent);
                        break;
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void registrar(View view) {
        Intent intent = new Intent(InicioSesion.this, Registro.class);
        startActivity(intent);
    }

}



