package com.example.argosresidencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Contrasena extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private EditText txtRestaurarPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recuperar_contrasena);

        ImageView imgUsuario = findViewById(R.id.imgUsuario);
        imgUsuario.setVisibility(View.INVISIBLE);

        txtRestaurarPass = findViewById(R.id.txtCorreo);
    }

    public void enviarCorreo(View view) {
        String correo = txtRestaurarPass.getText().toString();

        if(!correo.isEmpty()) {
            firebaseAuth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Contrasena.this,"Se ha enviado un correo electrónico",Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(Contrasena.this, InicioSesion.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(Contrasena.this,"No se ha podido enviar, verifique el correo que ingresó", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(Contrasena.this, "Ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
        }
    }

}