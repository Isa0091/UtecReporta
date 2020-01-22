package com.isa0091.utecreporta.ListResources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ItemReport {

    private int codigo ;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String ImagenBase;
    private String edificio;
    private String estado;
    private Bitmap imagen ;


    public ItemReport(int codigo,String titulo, String descripcion, String tipo, String ImagenBase, String edificio, String estado) {
        this.codigo=codigo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.ImagenBase = ImagenBase;
        this.edificio = edificio;
        this.estado = estado;
 try{
     if( ImagenBase != null){
         byte [] byteImagen= Base64.decode(ImagenBase,Base64.DEFAULT);
         imagen= BitmapFactory.decodeByteArray(byteImagen,0,byteImagen.length);
     }

 }catch (Exception e){
     e.printStackTrace();
 }
    }


    public int GetCodigo(){return codigo;}

    public String GetTitulo(){
        return titulo;
    }

    public String GetDescripcion(){
        return descripcion;
    }

    public String GetTipo(){
        return tipo;
    }

    public Bitmap Getimagenbase(){
        return imagen;
    }

    public String Getimagen64(){
        return this.ImagenBase;
    }

    public String GetEdificio(){
        return edificio;
    }

    public String GetEstado(){
        return estado;
    }



}
