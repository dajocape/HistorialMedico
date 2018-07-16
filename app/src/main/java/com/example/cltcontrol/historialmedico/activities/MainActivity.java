package com.example.cltcontrol.historialmedico.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cltcontrol.historialmedico.utils.EmpleadoController;
import com.example.cltcontrol.historialmedico.utils.NetworkStateChecker;
import com.example.cltcontrol.historialmedico.utils.SessionManager;
import com.example.cltcontrol.historialmedico.R;
import com.example.cltcontrol.historialmedico.models.ConsultaMedica;
import com.example.cltcontrol.historialmedico.models.Empleado;
import com.example.cltcontrol.historialmedico.models.Enfermedad;
import com.example.cltcontrol.historialmedico.utils.EnfermedadesSQL;
import com.example.cltcontrol.historialmedico.models.Usuario;
import com.facebook.stetho.Stetho;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etContrasenia;
    private EmpleadoController miController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUsuario = findViewById(R.id.etUsuario);
        etContrasenia = findViewById(R.id.etContrasenia);
        Button btnIngresoSistema = findViewById(R.id.btnIngresar);
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
        //Registar el receiver para sincronizar datos
        //registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        miController = new EmpleadoController(getApplicationContext());
        miController.llenadoEnfermedades();
        //Si el usuario es correcto, lleva a la siguiente pantalla, caso contrario muestra mensaje
        btnIngresoSistema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(miController.ingresoSistema(etUsuario.getText().toString(),etContrasenia.getText().toString())) {
                    crearSesion();
                    siguienteActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Usuario y/o contraseña incorrecto", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
     * Lleva a la ventana de BuscarEmpleadoActivity
     * */
    private void siguienteActivity(){
        Intent inbuscarempleado = new Intent(this, BuscarEmpleadoActivity.class);
        startActivity(inbuscarempleado);
    }

    /*
     * Crea una sesión validando los datos ingresados
     * */
    private void crearSesion(){
        SessionManager sesion = new SessionManager(getApplicationContext());
        Long id = Usuario.find(Usuario.class, "usuario = ? and contrasenia = ?",
                etUsuario.getText().toString(),etContrasenia.getText().toString()).get(0).getId();
        sesion.crearSesion(id);
    }

}