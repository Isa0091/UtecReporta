package com.isa0091.utecreporta;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.isa0091.utecreporta.ListResources.AdapterReport;
import com.isa0091.utecreporta.ListResources.ItemReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Listado extends AppCompatActivity implements AdapterReport.InterfaceReportes {

    private  String UrlReportes = "http://192.168.1.157:80/UtecReporta/Webservices/ReportesUsuario.php?idusuario=";
    private String UrlGetImagen = "http://192.168.1.157:80/UtecReporta/Webservices/GetImagen.php?idimagen=";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ItemReport> itemReportList;
    String usuario;
    String imagen64b = null;
    private Dialog miDialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        usuario =  sharedpreferences.getString("iduser", null);//getIntent().getStringExtra("iduser", "Admin");
        UrlReportes=UrlReportes + usuario;
        if (usuario == null) {
            Intent login = new Intent(getBaseContext(), Login.class);
            startActivity(login);
        }


        recyclerView = (RecyclerView) findViewById(R.id.reciclerlista);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemReportList = new ArrayList<>();

        CargarDatos();
    }

    public void cerrarsession(View view) {

        usuario=null;
        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("iduser");
        editor.clear();
        editor.apply();

        Intent Reporte = new Intent(getBaseContext(), Login.class);
        startActivity(Reporte);
        finish();
    }
    private void CargarDatos(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        StringRequest urlrequest = new StringRequest(Request.Method.GET, UrlReportes, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("reportes");

                    for(int i=0; i< jsonArray.length(); i++){
                        JSONObject o = jsonArray.getJSONObject(i);

                        ItemReport itemReport = new ItemReport(
                                o.getInt("idreporte"),
                                o.getString("titulo"),
                                o.getString("descripcion"),
                                o.getString("tipo"),
                                o.getString("imagen"),
                                o.getString("edificio"),
                                o.getString("estado"));

                        itemReportList.add(itemReport);
                    }
                    configurarRecycler(itemReportList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(urlrequest);

    }

    private void configurarRecycler(List<ItemReport> itemReportList) {
        adapter = new AdapterReport(itemReportList,getApplicationContext(), Listado.this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onReporteClickeado(int codigo) {


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String cod = String.valueOf(codigo);
        String Urlimagen = UrlGetImagen +cod;

            StringRequest urlrequest = new StringRequest(Request.Method.GET, Urlimagen, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("imagendata");
                        JSONObject imagendata = jsonArray.getJSONObject(0);
                         imagen64b= imagendata.getString("imagenbase64");
                        MostrarPrevia(imagen64b);

                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(),"No posee imagen", Toast.LENGTH_LONG).show();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(urlrequest);

    }


    public void MostrarPrevia(String imagen){

        Bitmap imagenbitmap=null;
        ImageView previa;
        if( !imagen.isEmpty()){
            byte [] byteImagen= Base64.decode(imagen,Base64.DEFAULT);
            imagenbitmap= BitmapFactory.decodeByteArray(byteImagen,0,byteImagen.length);
        }

        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.activity_previaimagen, null);
        previa =(ImageView) view.findViewById(R.id.imagenpreview);
        previa.setImageBitmap(imagenbitmap);
        alertadd.setView(view);
      //  alertadd.setNeutralButton("OK!", new DialogInterface.OnClickListener() {
      //      public void onClick(DialogInterface dlg, int sumthin) {

     //       }
      //  });

        alertadd.show();


    }
}