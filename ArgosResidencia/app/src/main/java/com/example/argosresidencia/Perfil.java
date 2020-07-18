package com.example.argosresidencia;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Alumno;
import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Perfil extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private EditText txtNombre;
    private EditText txtApPaterno;
    private EditText txtApMaterno;
    private EditText txtContrasena;
    private EditText txtConfirmarPass;

    private Spinner spInstitutos;
    private Spinner spCarreras;
    private Spinner spSemestres;

    private String nombre;
    private String apPaterno;
    private String contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mostrar_perfil);

        String noControl = sesion.getNoControl();
        String correo = sesion.getCorreo();
        EditText txtNoControl = findViewById(R.id.txtNoControl);
        EditText txtCorreo = findViewById(R.id.txtCorreo);
        txtNoControl.setText(noControl);
        txtCorreo.setText(correo);

        txtNombre = findViewById(R.id.txtNombre);
        txtApPaterno = findViewById(R.id.txtApPaterno);
        txtApMaterno = findViewById(R.id.txtApMaterno);
        txtContrasena = findViewById(R.id.txtContrasena);
        txtConfirmarPass = findViewById(R.id.txtConfirmarContrasena);

        spInstitutos = findViewById(R.id.spInstituto);
        spCarreras = findViewById(R.id.spCarrera);
        spSemestres = findViewById(R.id.spSemestre);

        ArrayAdapter<CharSequence> semestreAdapter = ArrayAdapter.createFromResource(this, R.array.spSemestres, R.layout.spnr_semestres);
        semestreAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spSemestres.setAdapter(semestreAdapter);

        ArrayAdapter<CharSequence> carreraAdapter = ArrayAdapter.createFromResource(this, R.array.spCarreras, R.layout.spnr_carreras);
        carreraAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spCarreras.setAdapter(carreraAdapter);

        ArrayAdapter<CharSequence> institutosAdapter = ArrayAdapter.createFromResource(this, R.array.spInstitutos, R.layout.spnr_institutos);
        institutosAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spInstitutos.setAdapter(institutosAdapter);

        llenarDatos(noControl);
    }

    public void actualizar(View view) {
        int tipoUsuario = sesion.getTipoUsuario();

        if (validacion()) {
            nombre = txtNombre.getText().toString();
            apPaterno = txtApPaterno.getText().toString();
            String apMaterno = txtApMaterno.getText().toString();
            contrasena = txtContrasena.getText().toString();
            String instituto = spInstitutos.getSelectedItem().toString();
            String carrera = spCarreras.getSelectedItem().toString();
            String semestre = spSemestres.getSelectedItem().toString();

            final Alumno a_user = new Alumno();

            //Obtener información del nuevo usuario
            a_user.setNombre(nombre.trim());
            a_user.setApPaterno(apPaterno.trim());
            a_user.setApMaterno(apMaterno.trim());
            a_user.setInstituto(instituto);
            a_user.setCarrera(carrera);
            a_user.setSemestre(semestre);

            //Actualizar datos
            if(tipoUsuario == 1) {
                actualizarAlumno(a_user);
            }
            else
            if(tipoUsuario == 2) {
                actualizarAlumno(a_user);
                actualizarMonitor(a_user);
            }

            actualizarPass(contrasena);
        }
    }

    public void actualizarAlumno(Alumno a) {
        Map<String, Object> dato = new HashMap<>();
        dato.put("nombre", a.getNombre());
        dato.put("ap_paterno", a.getApPaterno());
        dato.put("ap_materno", a.getApMaterno());
        dato.put("instituto", a.getInstituto());
        dato.put("carrera", a.getCarrera());
        dato.put("semestre", a.getSemestre());

        try {
            db.collection("alumnos").document(sesion.getNoControl())
                    .set(dato, SetOptions.merge());

            Toast.makeText(this, "Se realizó la actualización", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            Toast.makeText(this, "Error en la actualización", Toast.LENGTH_SHORT).show();
        }
    }

    public void actualizarMonitor(Alumno a) {
        Map<String, Object> dato = new HashMap<>();
        dato.put("carrera", a.getCarrera());

        try {
            db.collection("monitores").document(sesion.getNoControl())
                    .set(dato, SetOptions.merge());
        }catch (Exception e) {
            Toast.makeText(this, "No es un monitor", Toast.LENGTH_SHORT).show();
        }

    }

    public void regresar(View view) {
        finish();
    }

    public void actualizarPass(final String contrasena) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        user.updatePassword(contrasena)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        task.isSuccessful();
                    }
                });
    }

    //Validación de datos
    private boolean validacion () {
        nombre = txtNombre.getText().toString();
        apPaterno = txtApPaterno.getText().toString();
        contrasena = txtContrasena.getText().toString();
        String confirmar = txtConfirmarPass.getText().toString();

        if(nombre.isEmpty() || apPaterno.isEmpty() || contrasena.isEmpty() || contrasena.length()<=5 || !contrasena.equals(confirmar)
                || spCarreras.getSelectedItemPosition()==0 || spSemestres.getSelectedItemPosition()==0 || spInstitutos.getSelectedItemPosition()==0) {
            if (nombre.isEmpty()) {
                txtNombre.setError("Campo obligatorio");
            }
            if (apPaterno.isEmpty()) {
                txtApPaterno.setError("Campo obligatorio");
            }
            if (contrasena.isEmpty()) {
                txtContrasena.setError("Campo obligatorio");
            }
            if (contrasena.length()<=5) {
                txtContrasena.setError("Mínimo 6 caracteres");
            }
            if (confirmar.isEmpty()) {
                txtConfirmarPass.setError("Campo obligatorio");
            }
            if (!contrasena.equals(confirmar)) {
                txtConfirmarPass.setError("Contraseñas no coinciden");
            }
            if (spCarreras.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Seleccione carrera", Toast.LENGTH_SHORT).show();
            }
            if (spSemestres.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Seleccione semestre", Toast.LENGTH_SHORT).show();
            }
            if (spInstitutos.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Seleccione instituto", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        else return true;
    }

    public void llenarDatos(String noControl) {
        db.collection("alumnos")
                .whereEqualTo("no_control", noControl)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                obtieneDatos(document.getString("nombre"), document.getString("ap_paterno"),
                                        document.getString("ap_materno"), document.getString("instituto"),
                                        document.getString("carrera"), document.getString("semestre"));
                            }
                        } else {
                            Toast msg = Toast.makeText(Perfil.this, "Error al conectar con la BD", Toast.LENGTH_SHORT);
                            msg.show();
                        }
                    }
                });
    }

    public void obtieneDatos(String nombre, String paterno, String materno, String instituto, String carrera, String semestre) {
        txtNombre.setText(nombre);
        txtApPaterno.setText(paterno);
        txtApMaterno.setText(materno);

        spInstitutos.setSelection(obtenerPosicionItem(spInstitutos, instituto));
        spCarreras.setSelection(obtenerPosicionItem(spCarreras, carrera));
        spSemestres.setSelection(obtenerPosicionItem(spSemestres, semestre));
    }

    //Método para obtener la posición de un ítem del spinner
    public static int obtenerPosicionItem(Spinner spinner, String valor) {
        int posicion = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(valor)) {
                posicion = i;
            }
        }
        return posicion;
    }

    public void cerrarSesion() {
        Intent intent;
        sesion.setNoControl(null);
        sesion.setTipoUsuario(0);
        sesion.setCorreo(null);

        FirebaseAuth.getInstance().signOut();

        Toast msg = Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT);
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
                Toast msg = Toast.makeText(this, "Está en Perfil", Toast.LENGTH_SHORT);
                msg.show();
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