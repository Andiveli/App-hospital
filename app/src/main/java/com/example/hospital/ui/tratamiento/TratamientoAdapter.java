package com.example.hospital.ui.tratamiento;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.TratamientoPaciente;

import java.util.ArrayList;
import java.util.List;

public class TratamientoAdapter extends RecyclerView.Adapter<TratamientoAdapter.TratamientoViewHolder> {

    private List<TratamientoPaciente> tratamientos = new ArrayList<>();
    private OnTratamientoClickListener listener;

    public interface OnTratamientoClickListener {
        void onTratamientoClick(TratamientoPaciente tratamiento);
        void onTratamientoDelete(TratamientoPaciente tratamiento);
        void onTratamientoShowCost(TratamientoPaciente tratamiento);
    }

    public void setOnTratamientoClickListener(OnTratamientoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TratamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tratamiento, parent, false);
        return new TratamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TratamientoViewHolder holder, int position) {
        TratamientoPaciente tratamientoPaciente = tratamientos.get(position);
        
        // Información del paciente
        holder.tvPaciente.setText(tratamientoPaciente.getPaciente().getNombre() + " " + 
                                tratamientoPaciente.getPaciente().getApellido());
        holder.tvCorreo.setText(tratamientoPaciente.getPaciente().getCorreo());
        
        // Información del tratamiento
        holder.tvNombreTratamiento.setText(tratamientoPaciente.getTratamiento().getNombre());
        holder.tvTipo.setText(getIconoTipo(tratamientoPaciente.getTratamiento().getTipo()) + " " + 
                            tratamientoPaciente.getTratamiento().getTipo());
        
        // Detalles específicos según el tipo
        String detalles = getDetallesEspecificos(tratamientoPaciente);
        holder.tvDetalles.setText(detalles);
        
        // Fecha y estado
        holder.tvFecha.setText("📅 " + tratamientoPaciente.getFechaFormateada());
        holder.tvEstado.setText("📊 " + tratamientoPaciente.getEstado());
        
        // Colores según estado
        switch (tratamientoPaciente.getEstado()) {
            case "ACTIVO":
                holder.tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "COMPLETADO":
                holder.tvEstado.setTextColor(Color.parseColor("#2196F3")); // Azul
                break;
            case "CANCELADO":
                holder.tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
        }
        
        // Costo total
        holder.tvCosto.setText("💰 $" + String.format("%.2f", tratamientoPaciente.getCostoTotal()));

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTratamientoClick(tratamientoPaciente);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTratamientoDelete(tratamientoPaciente);
            }
        });

        holder.ivShowCost.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTratamientoShowCost(tratamientoPaciente);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tratamientos.size();
    }

    public void actualizarTratamientosPaciente(List<TratamientoPaciente> nuevosTratamientos) {
        this.tratamientos = nuevosTratamientos;
        notifyDataSetChanged();
    }

    public void actualizarTratamientos(List<TratamientoPaciente> tratamientos) {
        this.tratamientos.clear();
        this.tratamientos.addAll(tratamientos);
        notifyDataSetChanged();
    }

    private String getIconoTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "MEDICACIÓN":
            case "MEDICACION":
                return "💊";
            case "CIRUGÍA":
            case "CIRUGIA":
                return "🔪";
            case "TERAPIA":
                return "🧘";
            default:
                return "📋";
        }
    }

    private String getDetallesEspecificos(TratamientoPaciente tratamientoPaciente) {
        StringBuilder detalles = new StringBuilder();
        
        switch (tratamientoPaciente.getTratamiento().getTipo().toUpperCase()) {
            case "MEDICACIÓN":
            case "MEDICACION":
                detalles.append("💊 Duración: ").append(tratamientoPaciente.getTratamiento().getDuracion())
                        .append(" días | Precio por día: $")
                        .append(String.format("%.2f", tratamientoPaciente.getTratamiento().getPrecio()));
                break;
            case "CIRUGÍA":
            case "CIRUGIA":
                detalles.append("🔪 Duración: ").append(tratamientoPaciente.getTratamiento().getDuracion())
                        .append(" horas | Precio por hora: $")
                        .append(String.format("%.2f", tratamientoPaciente.getTratamiento().getPrecio()));
                break;
            case "TERAPIA":
                detalles.append("🧘 Sesiones: ").append(tratamientoPaciente.getTratamiento().getDuracion())
                        .append(" | Precio por sesión: $")
                        .append(String.format("%.2f", tratamientoPaciente.getTratamiento().getPrecio()));
                break;
        }
        
        return detalles.toString();
    }

    static class TratamientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaciente, tvCorreo, tvNombreTratamiento, tvTipo, tvDetalles, tvFecha, tvEstado, tvCosto;
        ImageView ivDelete, ivShowCost;

        TratamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvNombreTratamiento = itemView.findViewById(R.id.tvNombreTratamiento);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvDetalles = itemView.findViewById(R.id.tvDetalles);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivShowCost = itemView.findViewById(R.id.ivShowCost);
        }
    }
}