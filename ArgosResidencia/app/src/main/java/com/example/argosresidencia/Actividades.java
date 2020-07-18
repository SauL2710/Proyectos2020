package com.example.argosresidencia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class Actividades extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();
    ViewGroup layout;

    private TextView nombreUsuario; //Encabezado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mostrar_actividades);

        layout = findViewById(R.id.content);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();

        TextView txtNombreActividad = findViewById(R.id.txtTipoActividad);
        Intent intent = getIntent();
        String categoria = intent.getStringExtra("Actividad");
        txtNombreActividad.setText(categoria);

        assert categoria != null;
        obtenerDocumento(categoria);
    }

    class TaskDialogo extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Actividades.this);
            builder.setView(getLayoutInflater().inflate(R.layout.dialogo_actividades, null));

            final AlertDialog mDialog = builder.create();
            mDialog.show();
            mDialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return null;
        }
    }

    public void mostrarDialogo() {
        if (!hasChildren(layout)) {
            new TaskDialogo().execute();
        }
    }

    public void obtenerDocumento(String categoria) {
        if(categoria.equals("Visita cultural")) {
            db.collection("actividades")
                    .whereEqualTo("categoria", categoria)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                                    agregarActividad(document.getString("nombre"), Objects.requireNonNull(document.getTimestamp("fecha")), document.getString("lugar"));
                                }
                            }
                            else {
                                Toast.makeText(Actividades.this, "No existe registro aún", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            db.collection("actividades")
                    .whereEqualTo("categoria", categoria)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                                    ArrayList<String> lista = (ArrayList<String>) document.get("alumnos");
                                    assert lista != null;
                                    for (String alumno:lista) {
                                        if(alumno.equals(sesion.getNoControl())) {
                                            agregarActividad(document.getString("nombre"), Objects.requireNonNull(document.getTimestamp("fecha")), document.getString("lugar"));
                                        }
                                    }
                                }
                                mostrarDialogo();
                            }
                            else {
                                Toast.makeText(Actividades.this, "No existe registro aún", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @SuppressLint("InlinedApi")
    private void agregarActividad(String nombre, Timestamp fecha, String lugar) {
        LayoutInflater inflater = LayoutInflater.from(this);
        int id = R.layout.detalles_actividad;

        RelativeLayout relativeLayout =  (RelativeLayout) inflater.inflate(id, null, false);

        TextView txtNombre = relativeLayout.findViewById(R.id.txtNombre);
        TextView txtFecha = relativeLayout.findViewById(R.id.txtFecha);
        TextView txtLugar = relativeLayout.findViewById(R.id.txtLugar);
        txtNombre.setText(nombre);
        txtFecha.setText(fecha.toDate().toString());
        txtLugar.setText(lugar);

        layout.addView(relativeLayout);
    }

    public static boolean hasChildren(ViewGroup viewGroup) {
        return viewGroup.getChildCount() > 0;
    }

    public void muestraUsuario() {
        String noControl = sesion.getNoControl();
        db.collection("alumnos")
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
                            Toast msg = Toast.makeText(Actividades.this, "Error al conectar con la BD", Toast.LENGTH_SHORT);
                            msg.show();
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

        Toast msg = Toast.makeText(Actividades.this, "Sesión finalizada", Toast.LENGTH_SHORT);
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
        PopupMenu popupMenu = new PopupMenu(Actividades.this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu_desplegable);
        popupMenu.show();
    }
}