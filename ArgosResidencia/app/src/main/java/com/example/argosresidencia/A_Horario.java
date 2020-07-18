package com.example.argosresidencia;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Horario;
import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class A_Horario extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private TextView nombreUsuario; //Encabezado
    private ListView listaActividades;
    public ArrayList<Horario> actividades;

    private String categoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_horario);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();

        TextView txtNombreActividad = findViewById(R.id.txtTituloActividad);
        categoria = getIntent().getStringExtra("Actividad");
        txtNombreActividad.setText(categoria);

        //ListView
        listaActividades = findViewById(R.id.listaActividades);
        listaActividades.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        actividades = new ArrayList<>();
        llenarLista();

        listaActividades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView chkView = (CheckedTextView) view;
                boolean checkActual = chkView.isChecked();
                Horario horario = (Horario) listaActividades.getItemAtPosition(position);
                horario.setActivo(!checkActual);
            }
        });
    }

    public void agregar(View view) {
        //Solo 1 taller - 1 Visita industrial
        if(categoria.equals("Taller") || categoria.equals("Visita")) {
            mostrarActividadesSeleccionadas();
        }
        else mostrarConferenciasSeleccionadas();
    }

    public void cancelar(View view) {
        Intent intent = new Intent(A_Horario.this, A_Menu.class);
        startActivity(intent);
    }

    public void mostrarActividadesSeleccionadas() {
        int total = 0;

        SparseBooleanArray sp = listaActividades.getCheckedItemPositions();

        for(int i = 0; i < sp.size(); i++) {
            if(sp.valueAt(i)) {
                total++;
            }
        }

        if(total == 1) {
            for(int i = 0; i < sp.size(); i++) {
                if(sp.valueAt(i)) {
                    Horario horario = (Horario) listaActividades.getItemAtPosition(i);
                    String nombre = horario.getActividad();
                    agregarAColeccion(nombre);
                }
            }
        } else {
            Toast.makeText(A_Horario.this, "Solo se puede seleccionar una actividad de " + categoria, Toast.LENGTH_SHORT).show();
        }
    }

    public void mostrarConferenciasSeleccionadas() {
        SparseBooleanArray sp = listaActividades.getCheckedItemPositions();

        for(int i = 0; i < sp.size(); i++) {
            if(sp.valueAt(i)) {
                Horario horario = (Horario) listaActividades.getItemAtPosition(i);
                String nombre = horario.getActividad();
                agregarAColeccion(nombre);
            }
        }
    }

    public void agregarAColeccion(final String nombre) {
        db.collection("actividades").whereEqualTo("nombre", nombre)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                ArrayList<String> alumnos = (ArrayList<String>) document.get("alumnos");
                                assert alumnos != null;
                                alumnos.add(sesion.getNoControl());
                                try {
                                    db.collection("actividades").document(document.getId()).update("alumnos", alumnos);
                                    Toast.makeText(A_Horario.this, "Se registró su horario", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(A_Horario.this, A_Menu.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(A_Horario.this, "Error al ingresar el horario", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(A_Horario.this, "Error al conectar con la BD", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void llenarLista() {
          db.collection("actividades")
                .whereEqualTo("categoria", categoria)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                ArrayList<String> alumnos = (ArrayList<String>) document.get("alumnos");
                                assert alumnos != null;
                                if(!alumnos.contains(sesion.getNoControl())) {
                                    if((Long) Objects.requireNonNull(document.get("capacidad")) > alumnos.size()) {
                                        agregarElemento(document.getString("nombre"));
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(A_Horario.this, "Error al conectar con la BD", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void agregarElemento(String nombreActividad) {
        Horario actividad = new Horario(nombreActividad);
        actividades.add(actividad);
        ArrayAdapter<Horario> adapter = new ArrayAdapter<>(this, R.layout.elemento_horario, actividades);
        listaActividades.setAdapter(adapter);

        for(int i = 0; i < actividades.size(); i++) {
            listaActividades.setItemChecked(i, actividades.get(i).esActivo());
        }
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
                            Toast.makeText(A_Horario.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
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

       Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
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
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu_desplegable);
        popupMenu.show();
    }
}
