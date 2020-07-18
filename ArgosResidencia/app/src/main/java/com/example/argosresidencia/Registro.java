package com.example.argosresidencia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Alumno;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private EditText txtNombre;
    private EditText txtApPaterno;
    private EditText txtApMaterno;
    private EditText txtNoControl;
    private EditText txtCorreo;
    private EditText txtContrasena;
    private EditText txtConfirmarPass;

    private CheckBox chkbTerms;
    private Spinner spInstitutos;
    private Spinner spCarreras;
    private Spinner spSemestres;

    private ProgressDialog progressDialog;

    private String nombre;
    private String apPaterno;
    private String noControl;
    private String correo;
    private String contrasena;
    private String confirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrar);

        ImageView imgUsuario = findViewById(R.id.imgUsuario);
        imgUsuario.setVisibility(View.INVISIBLE);

        txtNombre = findViewById(R.id.txtNombre);
        txtApPaterno = findViewById(R.id.txtApPaterno);
        txtApMaterno = findViewById(R.id.txtApMaterno);
        txtNoControl = findViewById(R.id.txtNoControl);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtContrasena = findViewById(R.id.txtContrasena);
        txtConfirmarPass = findViewById(R.id.txtConfirmarContrasena);

        chkbTerms = findViewById(R.id.chkNotificaciones);
        spInstitutos = findViewById(R.id.spInstituto);
        spCarreras = findViewById(R.id.spCarrera);
        spSemestres = findViewById(R.id.spSemestre);

        progressDialog = new ProgressDialog(this);

        ArrayAdapter<String> carreraAdapter = new ArrayAdapter<>(this,R.layout.spnr_carreras,getResources().getStringArray(R.array.spCarreras));
        carreraAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spCarreras.setAdapter(carreraAdapter);

        ArrayAdapter<String> institutoAdapter = new ArrayAdapter<>(this,R.layout.spnr_institutos,getResources().getStringArray(R.array.spInstitutos));
        institutoAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spInstitutos.setAdapter(institutoAdapter);

        ArrayAdapter<String> semestreAdapter = new ArrayAdapter<>(this,R.layout.spnr_semestres,getResources().getStringArray(R.array.spSemestres));
        semestreAdapter.setDropDownViewResource(R.layout.drpdn_qual);
        spSemestres.setAdapter(semestreAdapter);
    }

    //Validación de datos
    private boolean validacion () {
        nombre = txtNombre.getText().toString();
        apPaterno = txtApPaterno.getText().toString();
        noControl = txtNoControl.getText().toString();
        correo = txtCorreo.getText().toString();
        contrasena = txtContrasena.getText().toString();
        confirmar = txtConfirmarPass.getText().toString();

        if(nombre.isEmpty() || apPaterno.isEmpty() || noControl.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || contrasena.length()<=5
                || !contrasena.equals(confirmar) || spCarreras.getSelectedItemPosition()==0 || spSemestres.getSelectedItemPosition()==0
                || spInstitutos.getSelectedItemPosition()==0 || !chkbTerms.isChecked()) {
            if (nombre.isEmpty()) {
                txtNombre.setError("Campo obligatorio");
            }
            if (apPaterno.isEmpty()) {
                txtApPaterno.setError("Campo obligatorio");
            }
            if (noControl.isEmpty()) {
                txtNoControl.setError("Campo obligatorio");
            }
            if (correo.isEmpty()) {
                txtCorreo.setError("Campo obligatorio");
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
                Toast.makeText(Registro.this, "Seleccione carrera", Toast.LENGTH_SHORT).show();
            }
            if (spSemestres.getSelectedItemPosition() == 0) {
                Toast.makeText(Registro.this, "Seleccione semestre", Toast.LENGTH_SHORT).show();
            }
            if (spInstitutos.getSelectedItemPosition() == 0) {
                Toast.makeText(Registro.this, "Seleccione instituto", Toast.LENGTH_SHORT).show();
            }
            if (!chkbTerms.isChecked()) {
                Toast.makeText(this, "Tiene que aceptar la recepción de notificaciones para completar el registro", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        else return true;
    }

    public void registrar(View view) {
        noControl = txtNoControl.getText().toString();

        if (validacion()) {
            DocumentReference docRef = db.collection("alumnos").document(noControl);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Toast.makeText(Registro.this,"Número de control ya registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            crearUsuario();
                        }
                    } else {
                        Toast.makeText(Registro.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void crearUsuario() {
        nombre = txtNombre.getText().toString();
        apPaterno = txtApPaterno.getText().toString();
        String apMaterno = txtApMaterno.getText().toString();
        noControl = txtNoControl.getText().toString();
        correo = txtCorreo.getText().toString();
        contrasena = txtContrasena.getText().toString();
        confirmar = txtConfirmarPass.getText().toString();
        String instituto = spInstitutos.getSelectedItem().toString();
        String carrera = spCarreras.getSelectedItem().toString();
        String semestre = spSemestres.getSelectedItem().toString();

        final Alumno user = new Alumno();

        //Obtener información del nuevo usuario
        user.setNombre(nombre.trim());
        user.setApPaterno(apPaterno.trim());
        user.setApMaterno(apMaterno.trim());
        user.setNoControl(noControl.trim());
        user.setCorreo(correo.trim());
        user.setInstituto(instituto);
        user.setCarrera(carrera);
        user.setSemestre(semestre);

        //Registro nuevo
        firebaseAuth.createUserWithEmailAndPassword(correo, contrasena).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Comprobación de registro
                        if (task.isSuccessful()) {
                            //Mensaje de barra de progreso
                            progressDialog.setMessage("Registrando...");
                            progressDialog.show();

                            agregarAlumno(user);
                        } else {
                            Toast.makeText(Registro.this, "Correo inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void agregarAlumno(final Alumno a) {
        Map<String, Object> alumno = new HashMap<>();
        alumno.put("nombre", a.getNombre());
        alumno.put("ap_paterno", a.getApPaterno());
        alumno.put("ap_materno", a.getApMaterno());
        alumno.put("no_control", a.getNoControl());
        alumno.put("correo", a.getCorreo());
        alumno.put("instituto", a.getInstituto());
        alumno.put("carrera", a.getCarrera());
        alumno.put("semestre", a.getSemestre());

        db.collection("alumnos").document(a.getNoControl())
                .set(alumno)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        agregarA_Asistencias(a.getNoControl());

                        Toast.makeText(Registro.this,"Se registró " + correo, Toast.LENGTH_SHORT).show();

                        //Regresar a Inicio de Sesión
                        Intent registro = new Intent(Registro.this, InicioSesion.class);
                        startActivity(registro);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registro.this,"Error de registro", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void agregarA_Asistencias(final String noControl) {
        ArrayList<String> horas = new ArrayList<>();

        Map<String, Object> datos = new HashMap<>();
        datos.put("horas", horas);

        try {
            db.collection("asistencias_conferencias").document(noControl)
                    .set(datos, SetOptions.merge());
            db.collection("asistencias_talleres").document(noControl)
                    .set(datos, SetOptions.merge());
            db.collection("asistencias_visitas").document(noControl)
                    .set(datos, SetOptions.merge());
        } catch (Exception e) {
            Toast.makeText(this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelar(View view) {
        Intent intent = new Intent(Registro.this, InicioSesion.class);
        startActivity(intent);
    }

}