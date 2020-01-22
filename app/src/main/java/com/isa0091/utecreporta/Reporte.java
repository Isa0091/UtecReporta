package com.isa0091.utecreporta;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.isa0091.utecreporta.ListResources.ItemReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Reporte extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private Spinner spinner1;
    private EditText titulo, descripcion;
    private TextView edificio;
    String tipo;
    String usuario;
    String foto_codificada64;
    boolean notomofoto = true;
    int idEdificio =1;
    String mensajeqr="Scannee QR del edificio";

    private ImageView foto;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private ZXingScannerView nScannerView;
    private Dialog miDialogo;
    private String UrlEdificio ="http://192.168.1.157:80/UtecReporta/Webservices/VerificarEdificio.php?idEdificio=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        usuario =  sharedpreferences.getString("iduser", null);//getIntent().getStringExtra("iduser", "Admin");

        if (usuario == null) {
            Intent login = new Intent(getBaseContext(), Login.class);
            startActivity(login);
        }

        titulo = (EditText) findViewById(R.id.titulo);
        descripcion = (EditText) findViewById(R.id.descripcion);
        edificio=(TextView) findViewById(R.id.edificioNombre);
        edificio.setText(mensajeqr);

        foto = (ImageView) findViewById(R.id.fototomada);

        spinner1 = (Spinner) findViewById(R.id.tipo);
        String[] opciones = {"Social", "Estructural", "Criminal", "Denuncia", "otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones);
        spinner1.setAdapter(adapter);
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


    public void GetFoto(View view) {

        try {
            Capturarfoto();
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo capturara la foto", Toast.LENGTH_LONG).show();
        }


    }


    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public boolean validaciones() {

        if (edificio.getText().toString().contains(mensajeqr)){
            Toast.makeText(getBaseContext(), "Debe scannear el QR del edificio", Toast.LENGTH_SHORT).show();
            return false;
        }else if (titulo.getText().toString().isEmpty()) {

            Toast.makeText(getBaseContext(), "Titulo Requerido", Toast.LENGTH_SHORT).show();
            return false;
        } else if (notomofoto) {
            Toast.makeText(getBaseContext(), "Debe Tomar una foto", Toast.LENGTH_SHORT).show();
            return false;

        } else if (descripcion.getText().toString().isEmpty()) {

            Toast.makeText(getBaseContext(), "descripcion Requerido", Toast.LENGTH_SHORT).show();
            return false;

        } else {
            return true;
        }

    }


    private void Capturarfoto() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //compress to which format you want.
            byte[] byte_arr = stream.toByteArray();
            foto_codificada64 = Base64.encodeToString(byte_arr, Base64.DEFAULT);
            notomofoto = false;
            foto.setImageBitmap(imageBitmap);
        }
    }


    public void EnviarReporte(View view) {

        if (validaciones()) {

            if (isNetDisponible()) {

                tipo = spinner1.getSelectedItem().toString();
                envia obj = new envia();
                obj.execute(titulo.getText().toString(), descripcion.getText().toString(), foto_codificada64, tipo, usuario);

            } else {

                Toast.makeText(getBaseContext(), "Verifique su conexion a internet", Toast.LENGTH_LONG).show();

            }


        }


    }

    @Override
    public void handleResult(Result result) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        UrlEdificio = UrlEdificio + result.getText();
        try {

            idEdificio= Integer.parseInt(result.getText());
            StringRequest urlrequest = new StringRequest(Request.Method.GET, UrlEdificio, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("Edificiodatos");
                        JSONObject datosEdificio = jsonArray.getJSONObject(0);
                        idEdificio= datosEdificio.getInt("idEdificio");
                        edificio.setText(datosEdificio.getString("nombre"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        edificio.setText(mensajeqr);
                        Toast.makeText(getBaseContext(),"Scanner QR de edificio Invalido", Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    edificio.setText(mensajeqr);
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(urlrequest);


        } catch (Exception e) {
            edificio.setText(mensajeqr);
            progressDialog.dismiss();
            Toast.makeText(getBaseContext(),"Scanee QR Valido", Toast.LENGTH_LONG).show();
        }

        nScannerView.resumeCameraPreview(this);
        if (miDialogo != null){
            miDialogo.dismiss();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(nScannerView != null){
            nScannerView.stopCamera();
        }

    }

    public void btnScanear(View view) {

        nScannerView = new ZXingScannerView(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(nScannerView);
        miDialogo = dialog.create();
        miDialogo.show();
        nScannerView.setResultHandler(this);
        nScannerView.startCamera();

    }

    class envia extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            String titulo = params[0];
            String descripcion = params[1];
            String foto_codificada64 = params[2];
            String tipo = params[3];
            String usuario = params[4];
            int iduser = Integer.parseInt(usuario);
            int edificio = idEdificio;

            String data = "";
            int tap;

            try {
                URL url = new URL("http://192.168.1.157:80/UtecReporta/Webservices/Reporte.php");

                String urlparametros = "titulo=" + titulo + "&descripcion=" + descripcion + "&imagen=" + foto_codificada64 + "&tipo=" + tipo + "&idusuario=" + iduser +"&idedificio=" + edificio;

                HttpURLConnection conectar = (HttpURLConnection) url.openConnection();
                conectar.setDoInput(true);
                //conectar.setInstanceFollowRedirects(false);
                conectar.setRequestMethod("POST");
                conectar.setRequestProperty("charset", "utf-8");

                OutputStream enviar_datos = conectar.getOutputStream();
                enviar_datos.write(urlparametros.getBytes());
                enviar_datos.flush();
                enviar_datos.close();


                InputStream datos_recibidos = conectar.getInputStream();

                while ((tap = datos_recibidos.read()) != -1) {


                    data += (char) tap;

                }

                datos_recibidos.close();
                conectar.disconnect();

                return data;

            } catch (MalformedURLException e) {
                return "error es" + e;
            } catch (IOException e) {

                return "error es" + e;
            }

        }

        @Override
        protected void onPostExecute(String verificar) {
            super.onPostExecute(verificar);

            titulo.setText("");
            foto.setImageResource(R.drawable.tomarfotos);
            descripcion.setText("");
            notomofoto = true;
            edificio.setText(mensajeqr);
            Toast.makeText(getBaseContext(), "Registrado correctamente", Toast.LENGTH_LONG).show();


        }
    }


}

