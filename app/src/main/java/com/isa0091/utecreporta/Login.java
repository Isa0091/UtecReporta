package com.isa0091.utecreporta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Login extends AppCompatActivity {

    private EditText correo, pass;
    private Button ingresar1;
    private String correo_, pass_, nombre_;
    private int edad_, iduser_;
    public static final String MyPREFERENCES = "sesiones" ;
    Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);


        correo = (EditText) findViewById(R.id.usuario);
        correo.requestFocus();
        pass = (EditText) findViewById(R.id.pass);
        ingresar1 = (Button) findViewById(R.id.ingresar11);

        ingresar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(isNetDisponible()){

                    correo_ = correo.getText().toString();
                    pass_ = pass.getText().toString();

                    if (correo.getText().toString().isEmpty()) {

                        Toast.makeText(getBaseContext(), "ingrese un correo", Toast.LENGTH_SHORT).show();


                    }  else if (pass.getText().toString().isEmpty()) {

                        Toast.makeText(getBaseContext(), "Debe ingresar la contrasena", Toast.LENGTH_SHORT).show();
                    }else{

                        enviar_paraautenticar enviar = new enviar_paraautenticar();
                        enviar.execute(correo_, pass_);

                    }

                }else{

                    Toast.makeText(ctx,"Verifica tu Acceso a Internet ",Toast.LENGTH_LONG).show();
                }



            }
        });

    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }


    class enviar_paraautenticar extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            String c = params[0];
            String p = params[1];

            int tap;
            String data="";

            try {


                URL url = new URL("http://192.168.1.157:80/UtecReporta/Webservices/login.php");
                String urlparametros = "correo=" + c + "&pass=" + p;

                HttpURLConnection conectar = (HttpURLConnection) url.openConnection();
                conectar.setRequestProperty("charset","utf-8");
                conectar.setDoOutput(true);

                OutputStream enviar = conectar.getOutputStream();
                enviar.write(urlparametros.getBytes());
                enviar.flush();
                enviar.close();

                InputStream datos_recibidos = conectar.getInputStream();

                while ((tap=datos_recibidos.read())!=-1){


                    data += (char)tap;

                }

                datos_recibidos.close();
                conectar.disconnect();


                return data;




            } catch (MalformedURLException e) {
                return "error" + e;
            } catch (IOException e) {
                return "error" + e;
            }

        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            String error = "no hay";
            String id_s="";
            String edad_s="";
            String antes=data;

            try {




                JSONObject miembros_arreglo1= new JSONObject(data);
                JSONObject miembros_arreglo=miembros_arreglo1.getJSONObject("data");



                id_s =miembros_arreglo.getString("iduser");
                if(id_s.equals("null")){

                    Toast.makeText(ctx,"Correo o contrasena Incorrectos",Toast.LENGTH_LONG).show();
                    Intent login = new Intent(getBaseContext(), Login.class);
                    startActivity(login);
                }else{
                    //iduser_ =Integer.parseInt(id_s);
                    nombre_ = miembros_arreglo.getString("nombre");
                    edad_s=miembros_arreglo.getString("edad");
                    //edad_ = Integer.parseInt(edad_s);
                    correo_ = miembros_arreglo.getString("correo");


                    SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("iduser",id_s);
                    editor.commit();

                   // Intent Reporte = new Intent(getBaseContext(), Reporte.class);
                 //   Reporte.putExtra("iduser", id_s);
                  //  Reporte.putExtra("nombre", nombre_);
                 //   Reporte.putExtra("edad", edad_s);
                  //  Reporte.putExtra("correo", correo_);
                  //  startActivity(Reporte);

                    Intent menu = new Intent(getBaseContext(), Menu.class);
                    startActivity(menu);

                }


            } catch (JSONException e) {

                error = "Ocurrio este error " + e;


            }



        }
    }


}
