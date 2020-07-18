package com.example.argosresidencia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class M_Escaner extends AppCompatActivity implements  ZXingScannerView.ResultHandler, PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private ZXingScannerView mScannerView;
    private TextView nombreUsuario; //Encabezado

    private static final int REQUEST_CAMERA = 1;
    private SoundPool soundPool;
    private int  soundbeep;

    private EditText txtNoControl;
    private TextView nombreAlumno; //Alumno
    private Button btnReescanear;
    private boolean siExiste = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_escanear);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();

        mScannerView = new ZXingScannerView(this);

        txtNoControl = findViewById(R.id.txtNoControl);

        btnReescanear = findViewById(R.id.btnReescanear);
        btnReescanear.setVisibility(View.INVISIBLE);

        FrameLayout visor = findViewById(R.id.frmVisor);
        visor.addView(mScannerView);

        nombreAlumno = findViewById(R.id.txtAlumno);

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(),"Permiso de cámara activo",Toast.LENGTH_SHORT).show();
            } else {
                requestPermission();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundbeep = soundPool.load(this, R.raw.beep, 1);
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
                            Toast.makeText(M_Escaner.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void obtieneNombre(String nombre, String apellido) {
        String nombreCompleto = nombre + " " + apellido;
        nombreUsuario.setText(nombreCompleto);
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void handleResult(Result rawResult) {
        final String result = rawResult.getText();
        txtNoControl.setText(result);
        consultaAlumno(result);
        btnReescanear.setVisibility(View.VISIBLE);
        soundPool.play(soundbeep,1,1,0,0,1);
    }

    public void consultaAlumno(String noControl) {
        db.collection("alumnos")
                .whereEqualTo("no_control", noControl)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                muestraNombre(document.getString("nombre"), document.getString("ap_paterno"));
                                siExiste = true;
                            }
                        } else {
                            Toast.makeText(M_Escaner.this, "Alumno no registrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void muestraNombre(String nombre, String apellido) {
        String nombreCompleto = "Alumno: " + nombre + " " + apellido;
        nombreAlumno.setText(nombreCompleto);
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    Toast.makeText(getApplicationContext(), "Permiso aceptado, ahora puede acceder a la cámara", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            showMessageOKCancel(
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                        }
                                    });
                        }
                    }
                }
            }
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(M_Escaner.this)
                .setMessage("Necesita aceptar los permisos de cámara.")
                .setPositiveButton("Aceptar", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    public void mostrarAlumno(View view) {
        btnReescanear.setVisibility(View.VISIBLE);
        String noControl = txtNoControl.getText().toString();
        consultaAlumno(noControl);
    }

    public void ingresarAsistencia(View view) {
        if(siExiste) {
            Intent intent = getIntent();
            String actividad = intent.getStringExtra("nombreActividad");

            final String noControl = txtNoControl.getText().toString();

            db.collection("actividades")
                    .whereEqualTo("nombre", actividad)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    if (Objects.equals(document.getString("categoria"), "Conferencia")) {
                                        agregarConferencia_Asistencia(noControl);
                                    }
                                    if (Objects.equals(document.getString("categoria"), "Taller")) {
                                        agregarTaller_Asistencia(noControl);
                                    }
                                    if (Objects.equals(document.getString("categoria"), "Visita")) {
                                        agregarVisita_Asistencia(noControl);
                                    }
                                }
                            } else {
                                Toast.makeText(M_Escaner.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else Toast.makeText(M_Escaner.this, "Volver a escanear", Toast.LENGTH_SHORT).show();
        limpiarEscaner();
    }

    public void agregarConferencia_Asistencia(final String noControl) {
        final DocumentReference docRef = db.collection("asistencias_conferencias").document(noControl);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        horas.add(Timestamp.now());
                        try {
                            docRef.update("horas", horas);
                            Toast.makeText(M_Escaner.this,"Se registró la asistencia en la conferencia",Toast.LENGTH_SHORT).show();
                        }catch (Exception e) {
                            Toast.makeText(M_Escaner.this,"Error al ingresar la asistencia",Toast.LENGTH_SHORT).show();
                        }
                    }
                } else Toast.makeText(M_Escaner.this,"Ocurrió un error en el registro",Toast.LENGTH_SHORT).show();
            }
        });
        mScannerView.resumeCameraPreview(this);
    }

    public void agregarTaller_Asistencia(final String noControl) {
        final DocumentReference docRef = db.collection("asistencias_talleres").document(noControl);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        horas.add(Timestamp.now());
                        try {
                            docRef.update("horas", horas);
                            Toast.makeText(M_Escaner.this,"Se registró la asistencia en el taller",Toast.LENGTH_SHORT).show();
                        }catch (Exception e) {
                            Toast.makeText(M_Escaner.this,"Error al ingresar la asistencia",Toast.LENGTH_SHORT).show();
                        }
                    }
                } else Toast.makeText(M_Escaner.this,"Ocurrió un error en el registro",Toast.LENGTH_SHORT).show();
            }
        });
        mScannerView.resumeCameraPreview(this);
    }

    public void agregarVisita_Asistencia(final String noControl) {
        final DocumentReference docRef = db.collection("asistencias_visitas").document(noControl);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        horas.add(Timestamp.now());
                        try {
                            docRef.update("horas", horas);
                            Toast.makeText(M_Escaner.this,"Se registró la asistencia en la visita",Toast.LENGTH_SHORT).show();
                        }catch (Exception e) {
                            Toast.makeText(M_Escaner.this,"Error al ingresar la asistencia",Toast.LENGTH_SHORT).show();
                        }
                    }
                } else Toast.makeText(M_Escaner.this,"Ocurrió un error en el registro",Toast.LENGTH_SHORT).show();
            }
        });
        mScannerView.resumeCameraPreview(this);
    }

    public void reescanear(View view) {
        limpiarEscaner();
    }

    public void limpiarEscaner() {
        txtNoControl.setText(" ");
        nombreAlumno.setText(" ");
        mScannerView.resumeCameraPreview(this);
        btnReescanear.setVisibility(View.INVISIBLE);
        siExiste = false;
    }

    public void cerrarSesion() {
        Intent intent;
        sesion.setNoControl(null);
        sesion.setTipoUsuario(0);
        sesion.setCorreo(null);

        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();

        intent = new Intent(this, InicioSesion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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