package com.example.cltcontrol.historialmedico.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.cltcontrol.historialmedico.interfaces.IResult;
import com.example.cltcontrol.historialmedico.models.AtencionEnfermeria;
import com.example.cltcontrol.historialmedico.models.ConsultaMedica;
import com.example.cltcontrol.historialmedico.models.Diagnostico;
import com.example.cltcontrol.historialmedico.models.PatologiasPersonales;
import com.example.cltcontrol.historialmedico.models.PermisoMedico;
import com.example.cltcontrol.historialmedico.models.SignosVitales;
import com.example.cltcontrol.historialmedico.service.RequestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.cltcontrol.historialmedico.utils.Identifiers.NAME_SYNCED_WITH_SERVER;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_ATENCION_ENFERMERIA;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_CONSULTA_MEDICA;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_DIAGNOSTICO;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_PATOLOGIAS_PERSONALES;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_PERMISO_MEDICO;
import static com.example.cltcontrol.historialmedico.utils.Identifiers.URL_SIGNOS;

public class SincronizacionInmediata extends BroadcastReceiver {

    private Context context;
    private AtencionEnfermeria atencionEnfermeria = new AtencionEnfermeria();
    private ConsultaMedica consultaMedica= new ConsultaMedica();
    private SignosVitales signosVitales=new SignosVitales();
    private Diagnostico diagnostico = new Diagnostico();
    private PatologiasPersonales patologiasPersonales = new PatologiasPersonales();
    private PermisoMedico permisoMedico = new PermisoMedico();

    private List<ConsultaMedica> consultaMedicasUnsynced;
    private List<AtencionEnfermeria> atencionesUnsynced;
    private List<SignosVitales> signosVitalesUnsynced;
    private List<PermisoMedico> permisoMedicoUnsynced;
    private List<Diagnostico> diagnosticosUnsynced;
    private List<PatologiasPersonales> patologiasPersonalesUnsynced;

    private IResult mResultCallback;
    private String TAGATENCION="atencion", TAGCONSULTA="consulta",
            TAGDIAGNOSTICO="diagnostico", TAGPATOLOGIASPERS="patpersonales", TAGPERMISO="permiso",
            TAGSIGNOS="signos";

    private SessionManager sesion;
    private String token;


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sesion = new SessionManager(context);
        token = sesion.obtenerInfoUsuario().get("token");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }

        //Si hay conexión
        if (activeNetwork != null) {
            //Si está conectado con wifi o con datos
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //Obtiene todas las consultas medicas que no han sido sincronizadas
                consultaMedicasUnsynced = consultaMedica.getConsultaMedicaUnsynced();
                //Obtiene todas las atenciones enfermería que no han sido sincronizadas
                atencionesUnsynced = atencionEnfermeria.getAtencionEnfermeriaUnsynced();
                //Obtiene todos los signos vitales unsynced
                signosVitalesUnsynced = signosVitales.getSignosVitalesUnsynced();
                //Obtiene todos los diagnósticos unsynced
                permisoMedicoUnsynced = permisoMedico.getPermisoMedicoUnsynced();
                //Obtiene todos los diagnósticos unsynced
                diagnosticosUnsynced = diagnostico.getDiagnosticoUnsynced();
                //Obtiene todos las patologías personales unsynced
                patologiasPersonalesUnsynced = patologiasPersonales.getPatologiasPersonalesUnsynced();
                if(atencionesUnsynced.size()==0 && consultaMedicasUnsynced.size()==0){
                    recorrerSignos("no","no");

                }
                if(consultaMedicasUnsynced.size()==0){
                    if(diagnosticosUnsynced.size()==0)
                        recorrerPermisos("no","no");
                    else
                        recorrerDiagnostico("no");
                    recorrerPatologias("no");
                }

                recorrerConsultas();
                recorrerAtenciones();


            }
        }
    }
    /*
     * Toma todos los argumentos de las patologias personales
     * Los almacena en el servidor
     * */
    private void guardarPatologiaPersonal(PatologiasPersonales patPers, int id_ficha, int id_consulta, String lugar, String detalle) {
        initRequestCallback(TAGPATOLOGIASPERS, null, null, null, patPers, null, null);
        String id_ficha_servidor="";
        String id_consulta_medica = "";

        if(id_consulta!=0){
            id_consulta_medica = String.valueOf(id_consulta);
        }
        if(id_ficha!=0){
            id_ficha_servidor = String.valueOf(id_ficha);
        }

        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = PatologiasPersonales.getHashMapPatologiasPersonales(id_ficha_servidor,
                id_consulta_medica,lugar,detalle);
        requestService.postDataRequest("POSTCALL", URL_PATOLOGIAS_PERSONALES, sendObj, token);
    }

    /*
     * Toma todos los argumentos de los diagnósticos
     * Los almacena en el servidor
     * */
    public void guardarDiagnosticoLocal(Diagnostico diagnostico, String id_consulta, String id_enfermedad, String tipo_enfermedad){
        initRequestCallback(TAGDIAGNOSTICO, null, null, diagnostico, null, null, null);

        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = Diagnostico.getHashMapDiagnostico(id_consulta,
                tipo_enfermedad, id_enfermedad);
        requestService.postDataRequest("POSTCALL", URL_DIAGNOSTICO, sendObj, token);
    }

    /*
     * Toma todos los argumentos de los signos vitales
     * Los almacena en el servidor
     * */
    private void guardarSignosVitalesLocal(SignosVitales signosVit, String id_empleado, String presionDistolica,
                                           String presionSistolica, String pulso,  String temperatura,
                                           String id_atencion, String  id_consulta, Date fecha_signos) {
        initRequestCallback(TAGSIGNOS, null, null, null, null, null, signosVit);
        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = SignosVitales.getHashMapSignosVitales(id_empleado,id_consulta,
                id_atencion,presionSistolica,presionDistolica,pulso,temperatura, fecha_signos);
        Log.d("SENDOBJ", String.valueOf(sendObj));
        requestService.postDataRequest("POSTCALL", URL_SIGNOS, sendObj, token);
    }

    /*
     * Toma todos los argumentos de los consulta médica
     * Los almacena en el servidor
     * */
    private void guardarConsultaMedicaLocal(ConsultaMedica consultaMed, String id_empleado, Date fecha_consulta, String problema_actual,
                                       String revision_medica, String prescripcion, String examen_fisico,
                                       String motivo) {
        if (problema_actual == null) {
            problema_actual = "";
        }
        if (revision_medica == null) {
            revision_medica = "";
        }
        if (prescripcion == null) {
            prescripcion = "";
        }
        if (examen_fisico == null) {
            examen_fisico = "";
        }
        if (motivo == null) {
            motivo = "";
        }
        initRequestCallback(TAGCONSULTA, null,consultaMed, null, null, null, null);

        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = ConsultaMedica.getHashMapConsultaMedica(id_empleado,fecha_consulta, motivo, problema_actual, revision_medica, prescripcion, examen_fisico);
        requestService.postDataRequest("POSTCALL", URL_CONSULTA_MEDICA, sendObj, token);
    }

    /*
     * Toma todos los argumentos de los consulta médica
     * Los almacena en el servidor
     * */
    private void guardarAtencionEnfermeriaLocal(AtencionEnfermeria atencionEnf, String id_empleado, Date fecha_consulta,
                                                String motivo, String diagnostico, String plan) {
        if (motivo == null) {
            motivo = "";
        }
        if (diagnostico == null) {
            diagnostico = "";
        }
        if (plan == null) {
            plan = "";
        }
        initRequestCallback(TAGATENCION, atencionEnf, null, null, null, null, null);

        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = AtencionEnfermeria.getHashMapAtencionEnfermeria(id_empleado,fecha_consulta, motivo, diagnostico, plan);
        requestService.postDataRequest("POSTCALL", URL_ATENCION_ENFERMERIA, sendObj, token);
    }

    /*
     * Toma todos los argumentos de los consulta médica
     * Los almacena en el servidor
     * */
    private void guardarPermisoMedicoLocal(PermisoMedico permisoMed, int id_empleado, String id_diagnostico, String id_consulta,Date fecha_inicio,
                                           Date fecha_fin, int dias, String observaciones, String nombre_doctor) {
        initRequestCallback(TAGPERMISO,null, null, null, null, permisoMed, null);
        String id_empleado_servidor = "";

        if(id_empleado!=0)
            id_empleado_servidor = String.valueOf(id_empleado);
        RequestService requestService = new RequestService(mResultCallback, context);

        Map<String, String> sendObj = PermisoMedico.getHashMapPermisoMedico(id_empleado_servidor, id_diagnostico,
                id_consulta, fecha_inicio, fecha_fin, String.valueOf(dias), observaciones, nombre_doctor);
        requestService.postDataRequest("POSTCALL", URL_PERMISO_MEDICO, sendObj, token);
    }

    /*
     * Inicializar las llamadas a Request
     * Dependiendo de las respuestas, ejecuta una de las siguientes funciones
     * */
    private void initRequestCallback(final String TAG, final AtencionEnfermeria atencionEnf, final ConsultaMedica consulta,
                                     final Diagnostico diagnosticoI, final PatologiasPersonales patologias,
                                     final PermisoMedico permiso, final SignosVitales signosV){
        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType,JSONObject response) {
                Log.d("NOTIFY", "SUCCESSSSS");
                    try {
                        String pk = response.getString("pk");
                        if(TAG.equalsIgnoreCase(TAGATENCION)){
                            atencionEnf.setId_serv(Integer.parseInt(pk));
                            atencionEnf.setStatus(NAME_SYNCED_WITH_SERVER);
                            atencionEnf.save();

                            recorrerSignos("", pk);
                        }else if(TAG.equalsIgnoreCase(TAGCONSULTA)){
                            consulta.setId_serv(Integer.parseInt(pk));
                            consulta.setStatus(NAME_SYNCED_WITH_SERVER);
                            consulta.save();
                            if(atencionesUnsynced.size()==0){
                                recorrerSignos(pk, "");
                            }else{
                                recorrerAtenciones();
                            }
                            recorrerPatologias(pk);
                            if(diagnosticosUnsynced.size()==0){
                                recorrerPermisos(pk, "");
                            }else{
                                recorrerDiagnostico(pk);
                            }

                        }else if(TAG.equalsIgnoreCase(TAGDIAGNOSTICO)){
                            diagnosticoI.setId_serv(Integer.parseInt(pk));
                            diagnosticoI.setStatus(NAME_SYNCED_WITH_SERVER);
                            diagnosticoI.save();

                            recorrerPermisos(String.valueOf(diagnosticoI.getConsulta_medica().getId()), pk);

                        }else if(TAG.equalsIgnoreCase(TAGPATOLOGIASPERS)){
                            patologias.setId_serv(Integer.parseInt(pk));
                            patologias.setStatus(NAME_SYNCED_WITH_SERVER);
                            patologias.save();
                        }else if(TAG.equalsIgnoreCase(TAGPERMISO)){
                            permiso.setId_serv(Integer.parseInt(pk));
                            permiso.setStatus(NAME_SYNCED_WITH_SERVER);
                            permiso.save();
                        }else if(TAG.equalsIgnoreCase(TAGSIGNOS)){
                            signosV.setId_serv(Integer.parseInt(pk));
                            signosV.setStatus(NAME_SYNCED_WITH_SERVER);
                            signosV.save();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.e("ERROR", String.valueOf(error));
            }

            @Override
            public void notifyMsjError(String requestType, String error) {
                Log.d("HEREMSJERROR", String.valueOf(error));
            }

            @Override
            public void notifyJSONError(String requestType, JSONException error) {
                Log.d("HEREJSONERROR", String.valueOf(error));
            }
        };

    }
    /*
    * Recorre la lista de consultas no sincronizadas
    * Guarda en la db local
    * */
    public void recorrerConsultas(){
        for(ConsultaMedica consultas : consultaMedicasUnsynced) {
            //Si se ha creado una consulta médica
            if (consultas.getFechaConsulta()!=null) {
                String id_empleado = "";
                if(consultas.getEmpleado()!=null && consultas.getEmpleado().getId_serv()!=0)
                    id_empleado = String.valueOf(consultas.getEmpleado().getId_serv());

                guardarConsultaMedicaLocal(consultas, id_empleado, consultas.getFechaConsulta(), consultas.getProb_actual(),
                        consultas.getRevision_medica(), consultas.getPrescripcion(), consultas.getExamen_fisico(), consultas.getMotivo());
            }
        }
    }
    /*
     * Recorre la lista de atenciones no sincronizadas
     * Guarda en la db local
     * */
    public void recorrerAtenciones(){
        for(AtencionEnfermeria atenciones : atencionesUnsynced) {
            //Si se ha creado una consulta médica
            if (atenciones.getFecha_atencion()!=null) {
                String id_empleado="";
                if(atenciones.getEmpleado()!=null && atenciones.getEmpleado().getId_serv()!=0)
                    id_empleado = String.valueOf(atenciones.getEmpleado().getId_serv());

                guardarAtencionEnfermeriaLocal(atenciones, id_empleado, atenciones.getFecha_atencion(), atenciones.getMotivoAtencion(), atenciones.getDiagnosticoEnfermeria(), atenciones.getPlanCuidados());
            }
        }
    }
    /*
     * Recorre la lista de diagnóstico no sincronizados
     * Guarda en la db local
     * */
    public void recorrerDiagnostico(String id_consulta){
        for(Diagnostico diagnosticos : diagnosticosUnsynced){
            String id_enfermedad="";
            if(diagnosticos.getEnfermedad()!=null && diagnosticos.getEnfermedad().getId_serv()!=0){
                id_enfermedad = String.valueOf(diagnosticos.getEnfermedad().getId_serv());
            }

            //si la consulta ya existe en el servidor
            if(id_consulta.equals("no")){
                id_consulta = String.valueOf(diagnosticos.getConsulta_medica().getId_serv());
            }

            guardarDiagnosticoLocal(diagnosticos, id_consulta, id_enfermedad, diagnosticos.getTipoEnfermedad());

        }
    }
    /*
     * Recorre la lista de patologías no sincronizadas
     * Guarda en la db local
     * */
    public void recorrerPatologias(String id_consulta){

        for(PatologiasPersonales patologias : patologiasPersonalesUnsynced){
            //si la consulta ya existe en el servidor
            if(id_consulta.equals("no")){
                id_consulta = String.valueOf(patologias.getConsultaMedica().getId_serv());
            }
            guardarPatologiaPersonal(patologias, patologias.getId_ficha(), patologias.getConsultaMedica().getId_serv(),
                    patologias.getLugar(), patologias.getDetalle());

        }
    }
    /*
     * Recorre la lista de permisos no sincronizados
     * Guarda en la db local
     * */
    public void recorrerPermisos(String id_consulta, String id_diagnostico){
        for(PermisoMedico permisosMedico : permisoMedicoUnsynced){
            //si la consulta ya existe en el servidor
            if(id_consulta.equals("no")){
                id_consulta = String.valueOf(permisosMedico.getConsulta_medica().getId_serv());
            }
            //si el diagnóstico ya existe en el servidor
            if(id_diagnostico.equals("no")){
                id_consulta = String.valueOf(permisosMedico.getDiagnostico().getId_serv());
            }
            guardarPermisoMedicoLocal(permisosMedico, permisosMedico.getEmpleado().getId_serv(), id_diagnostico,
                    id_consulta, permisosMedico.getFecha_inicio(), permisosMedico.getFecha_fin(),
                    permisosMedico.getDias_permiso(), permisosMedico.getObsevaciones_permiso(), permisosMedico.getDoctor());

        }
    }
    /*
     * Recorre la lista de signos no sincronizados
     * Guarda en la db local
     * */
    public void recorrerSignos(String id_consulta, String id_enf){
        for(SignosVitales signos : signosVitalesUnsynced){
            String id_empleado="";
            if(signos.getEmpleado()!=null && signos.getEmpleado().getId_serv()!=0){
                id_empleado = String.valueOf(signos.getEmpleado().getId_serv());
            }
            //si la consulta ya existe en el servidor
            if(id_consulta.equals("no")){
                id_consulta = String.valueOf(signos.getConsultaMedica().getId_serv());
            }
            //si la atención ya existe en el servidor
            if(id_enf.equals("no")){
                id_enf = String.valueOf(signos.getAtencion_enfermeria().getId_serv());
            }

            guardarSignosVitalesLocal(signos, id_empleado, String.valueOf(signos.getPresion_distolica()),
                    String.valueOf(signos.getPresion_sistolica()), String.valueOf(signos.getPulso()),
                    String.valueOf(signos.getTemperatura()), id_enf,
                    id_consulta, signos.getFecha());
        }
    }

}
