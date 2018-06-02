package com.example.cltcontrol.historialmedico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.example.cltcontrol.historialmedico.R;
import com.example.cltcontrol.historialmedico.models.Empleado;

import java.util.List;

public class MenuEmpleadoActivity extends FragmentActivity {

    private String idEmpleado;
    private Empleado empleado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_empleado);

        TextView tvNombresEmpleado = findViewById(R.id.tvNombresEmpleado);

        //Recibe la cedula del empleado desde BuscarEmpleadoActivity
        Intent inBuscarEmpleadoActivity = getIntent();
        idEmpleado = inBuscarEmpleadoActivity.getStringExtra("ID");

        //Busca al empleado por su cedula y muestra en un fragment los datos
        empleado = Empleado.findById(Empleado.class,Long.parseLong(idEmpleado));
        tvNombresEmpleado.setText(empleado.getApellido()+" "+empleado.getNombre());
    }

    public void aperturaHistorialConsultaMedica(View v) {

        //Envia el id del empleado a HistorialConsultaMedica
        Intent inHistorialConsultaMedica = new Intent(getApplicationContext(), HistorialConsultaMedica.class);
        //inHistorialConsultaMedica.putExtra("CEDULA", cedulaEmpleado);
        inHistorialConsultaMedica.putExtra("ID", idEmpleado);
        startActivity(inHistorialConsultaMedica);
    }

    public void aperturaHistorialAtencionEnfermeria(View v){
        //Envia el id del empleado a HistorialAtencionEnfermeria
        Intent inHistorialAtencionEnfermeria = new Intent(getApplicationContext(), HistorialAtencionEnfermeria.class);
        //inHistorialAtencionEnfermeria.putExtra("CEDULA", cedulaEmpleado);
        inHistorialAtencionEnfermeria.putExtra("ID", idEmpleado);
        startActivity(inHistorialAtencionEnfermeria);
    }
}
