package com.example.argosresidencia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.argosresidencia.modelo.dato.Sesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class A_Menu extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Sesion sesion = Sesion.getInstance();

    private AlertDialog alertDialog;

    private TextView nombreUsuario; //Encabezado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_menu);

        nombreUsuario = findViewById(R.id.lblUsuario);
        muestraUsuario();
    }

    class TaskDialogo extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            CharSequence[] valores = {"Conferencias", "Talleres", "Visitas"};

            AlertDialog.Builder builder = new AlertDialog.Builder(A_Menu.this, R.style.dialogTheme);
            builder.setTitle("Crea tu horario");

            builder.setPositiveButton("Después", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    alertDialog.dismiss();
                }
            });
            builder.setSingleChoiceItems(valores, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent;
                    switch (item) {
                        case 0:
                            intent = new Intent(A_Menu.this, A_Horario.class);
                            intent.putExtra("Actividad", "Conferencia");
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(A_Menu.this, A_Horario.class);
                            intent.putExtra("Actividad", "Taller");
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(A_Menu.this, A_Horario.class);
                            intent.putExtra("Actividad", "Visita");
                            startActivity(intent);
                            break;
                    }
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void verConferencias(View view) {
        Intent intent = new Intent(A_Menu.this, Actividades.class);
        intent.putExtra("Actividad", "Conferencia");
        startActivity(intent);
    }

    public void verTalleres(View view) {
        Intent intent = new Intent(A_Menu.this, Actividades.class);
        intent.putExtra("Actividad", "Taller");
        startActivity(intent);
    }

    public void verVisitas(View view) {
        Intent intent = new Intent(A_Menu.this, Actividades.class);
        intent.putExtra("Actividad", "Visita");
        startActivity(intent);
    }

    public void verCulturales(View view) {
        Intent intent = new Intent(A_Menu.this, Actividades.class);
        intent.putExtra("Actividad", "Visita cultural");
        startActivity(intent);
    }

    public void crearHorario(View view) {
        new TaskDialogo().execute();
    }

    public void verNotificaciones(View view) {
        Intent intent = new Intent(A_Menu.this, Notificaciones.class);
        startActivity(intent);
    }

    public void verQR(View view) {
        Intent intent = new Intent(A_Menu.this, A_QR.class);
        startActivity(intent);
    }

    public void verProgreso(View view) {
        Intent intent = new Intent(A_Menu.this, A_Progreso.class);
        startActivity(intent);
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
                Toast.makeText(A_Menu.this, "Error al conectar con la BD", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(A_Menu.this, "Sesión finalizada", Toast.LENGTH_SHORT).show();

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
        PopupMenu popupMenu = new PopupMenu(A_Menu.this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu_desplegable);
        popupMenu.show();
    }

}


