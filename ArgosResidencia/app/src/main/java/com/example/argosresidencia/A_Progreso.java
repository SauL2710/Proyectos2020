package com.example.argosresidencia;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class A_Progreso extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private TextView nombreUsuario; //Encabezado

    private int status = 0;
    private long horasTotales = 0;
    private double taller = 0;
    private double visita = 0;
    private Handler handler = new Handler();
    private TextView porcentaje;
    private ProgressBar pProgreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_progreso_asistencia);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular);

        porcentaje = findViewById(R.id.lblPorcentaje);
        pProgreso = findViewById(R.id.circularProgressbar);
        pProgreso.setProgress(0); //Main Progress
        pProgreso.setSecondaryProgress(100); //Secondary Progress
        pProgreso.setMax(100); //Maximum Progress
        pProgreso.setProgressDrawable(drawable);

        consultaProgreso(sesion.getNoControl());
    }

    private void consultaProgreso(final String noControl) {
        //Consulta asistencia de taller
        DocumentReference docRefT = db.collection("asistencias_talleres").document(noControl);
        docRefT.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        calculaProgreso_Taller(horas);
                    }
                } else {
                    Toast.makeText(A_Progreso.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Consulta asistencia de visita
        DocumentReference docRefV = db.collection("asistencias_visitas").document(noControl);
        docRefV.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        calculaProgreso_Visita(horas);
                    }
                } else {
                    Toast.makeText(A_Progreso.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Consulta asistencias de conferencias
        DocumentReference docRefC = db.collection("asistencias_conferencias").document(noControl);
        docRefC.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Timestamp> horas = (ArrayList<Timestamp>) document.get("horas");
                        assert horas != null;
                        calculaProgreso_Conferencia(horas);
                    }
                } else {
                    Toast.makeText(A_Progreso.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Calcula progreso de conferencias
    public void calculaProgreso_Conferencia(ArrayList<Timestamp> hora) {
        int size = hora.size();

        for (int i = 0; i < size; i++) {
            Date fecha1 = hora.get(i).toDate();
            i += 1;
            try {
                Date fecha2 = hora.get(i).toDate();
                long diferencia = fecha2.getTime() - fecha1.getTime();
                long horas = diferencia / (1000 * 60 * 60);
                horasTotales = horasTotales + horas;
            }catch (Exception e) { }
        }

        generaProgreso(horasTotales);
    }

    public void calculaProgreso_Taller(ArrayList<Timestamp> hora) {
        if(hora.size() > 0) taller = 1;
    }

    public void calculaProgreso_Visita(ArrayList<Timestamp> hora) {
        if(hora.size() > 0) visita = 1;
    }

    //Genera progreso
    public void generaProgreso(long horasT) {
        if(horasT >= 7 && taller == 1 && visita == 1)
        {
            Toast.makeText(A_Progreso.this, "¡Obtuviste un crédito complementario!", Toast.LENGTH_SHORT).show();
        }

        double progreso = (Math.round(horasT)) * 11.11;
        if (progreso > 100) {
            progreso = 100;
        }

        final double finalProgreso = progreso + taller + visita;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(status < finalProgreso) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String estado = status + "%";
                            pProgreso.setProgress(status);
                            porcentaje.setText(estado);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(16); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    status += 1;
                }
            }
        }).start();
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
                            Toast.makeText(A_Progreso.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
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

