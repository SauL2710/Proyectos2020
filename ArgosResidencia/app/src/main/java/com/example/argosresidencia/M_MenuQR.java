package com.example.argosresidencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class M_MenuQR extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private TextView nombreUsuario; //Encabezado
    private TextView nombreActividad;
    private List<String> lista;

    private String actividad = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_menu_qr);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();

        lista = (List<String>)getIntent().getSerializableExtra("listaAlumnos");
        nombreActividad = findViewById(R.id.txtTituloActividad);
        Intent intent = getIntent();
        actividad = intent.getStringExtra("nombreActividad");
        nombreActividad.setText(actividad);
    }

    public void iniciarRegistros(View view) {
        Intent intent = new Intent(M_MenuQR.this, M_Escaner.class);
        intent.putExtra("nombreActividad", actividad);
        startActivity(intent);
    }

    public void mostrarRegistros(View view) {
        Intent intent;
        Bundle extras = new Bundle();
        intent = new Intent(M_MenuQR.this, M_Registrados.class);
        extras.putSerializable("listaAlumnos", (Serializable) lista);
        extras.putString("nombreActividad", actividad);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void muestraUsuario() {
        String noControl = sesion.getNoControl();
        db.collection("monitores")
                .whereEqualTo("no_control", noControl)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                obtieneNombre(document.getString("nombre"), document.getString("ap_paterno"));
                            }
                        } else {
                            Toast.makeText(M_MenuQR.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void obtieneNombre(String nombre, String apellido) {
        String nombreCompleto = nombre + " " + apellido;
        nombreUsuario.setText(nombreCompleto);
    }

    public void cerrarSesion() {
        Intent intent;
        sesion.setNoControl(null);
        sesion.setTipoUsuario(0);
        sesion.setCorreo(null);

        FirebaseAuth.getInstance().signOut();

        Toast msg = Toast.makeText(M_MenuQR.this, "Sesión finalizada", Toast.LENGTH_SHORT);
        msg.show();

        intent = new Intent(this, InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_desplegable, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navegacion_perfil:
                Intent intent = new Intent(this, Perfil.class);
                startActivity(intent);
                break;
            case R.id.navegacion_politicasPrivacidad:
                Intent intent2 = new Intent(this, PoliticasPrivacidad.class);
                startActivity(intent2);
                break;
            case R.id.navegacion_cerrarSesion:
                cerrarSesion();
                break;
            default:
                break;
        }
        return true;
    }

    public void despliegaMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(M_MenuQR.this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu_desplegable);
        popupMenu.show();
    }

}