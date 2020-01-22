package com.isa0091.utecreporta.ListResources;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.isa0091.utecreporta.R;

import java.util.List;

public class AdapterReport extends RecyclerView.Adapter<AdapterReport.ViewHolder> {

    public interface InterfaceReportes{
        void onReporteClickeado(int i);
    }

    private List<ItemReport>itemReportList;
    private Context context;
    private InterfaceReportes _listener;

    public AdapterReport(List<ItemReport> itemReportList, Context context, InterfaceReportes listener) {
        this.itemReportList = itemReportList;
        this.context = context;
        this._listener = listener;
    }

    @Override
    public AdapterReport.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.itemreport, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterReport.ViewHolder holder, int position) {

        ItemReport itemReport = itemReportList.get(position);
        String Codd= String.valueOf(itemReport.GetCodigo());
        holder.codigorep.setText(Codd);
        holder.titulorep.setText(itemReport.GetTitulo());
        holder.descripcionrep.setText(itemReport.GetDescripcion());
        holder.tiporep.setText(itemReport.GetTipo());
        holder.estado.setText(itemReport.GetEstado());
        holder.edificioReport.setText(itemReport.GetEdificio());

        if(itemReport.Getimagenbase() != null){
            holder.imageViewgen.setImageBitmap(itemReport.Getimagenbase());

        }else{
            holder.imageViewgen.setImageResource(R.drawable.logo);
        }

        final int codre= Integer.parseInt(holder.codigorep.getText().toString());

        holder.imageViewgen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_listener != null){
                    _listener.onReporteClickeado(codre);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemReportList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView codigorep;
        public TextView titulorep;
        public TextView descripcionrep;
        public TextView tiporep;
        public TextView edificioReport;
        public TextView estado;
        public ImageView imageViewgen;
        public ItemReport _item;

        public ViewHolder(View itemView) {
            super(itemView);
            codigorep= (TextView) itemView.findViewById(R.id.codereport);
            titulorep= (TextView) itemView.findViewById(R.id.titulorep);
            descripcionrep= (TextView) itemView.findViewById(R.id.descripcionrep);
            tiporep= (TextView) itemView.findViewById(R.id.tiporeport);

            edificioReport= (TextView) itemView.findViewById(R.id.edificioReport);
            estado= (TextView) itemView.findViewById(R.id.estadoReport);
            imageViewgen = (ImageView) itemView.findViewById(R.id.imagenreporte);


        }

        public void bindearDatos(ItemReport item){
            _item = item;
            titulorep.setText(item.GetTitulo());
        }
    }
}
