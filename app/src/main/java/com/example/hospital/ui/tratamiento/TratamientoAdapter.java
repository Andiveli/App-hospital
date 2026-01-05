package com.example.hospital.ui.tratamiento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.Cirugia;
import com.example.hospital.data.models.Medicacion;
import com.example.hospital.data.models.Terapia;
import com.example.hospital.data.models.Tratamiento;

import java.util.ArrayList;
import java.util.List;

public class TratamientoAdapter extends RecyclerView.Adapter<TratamientoAdapter.TratamientoViewHolder> {
    
    private List<Tratamiento> tratamientos;
    private OnTratamientoClickListener listener;
    
    public interface OnTratamientoClickListener {
        void onTratamientoClick(Tratamiento tratamiento);
        void onTratamientoDelete(Tratamiento tratamiento);
        void onTratamientoShowCost(Tratamiento tratamiento);
    }
    
    public TratamientoAdapter() {
        this.tratamientos = new ArrayList<>();
        this.listener = null;
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
        Tratamiento tratamiento = tratamientos.get(position);
        holder.bind(tratamiento);
    }
    
    @Override
    public int getItemCount() {
        return tratamientos.size();
    }
    
    public void actualizarTratamientos(List<Tratamiento> nuevosTratamientos) {
        this.tratamientos = nuevosTratamientos;
        notifyDataSetChanged();
    }
    
    public Tratamiento getTratamientoAt(int position) {
        return tratamientos.get(position);
    }
    
    class TratamientoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipoIcono;
        private TextView tvNombre;
        private TextView tvDuracion;
        private TextView tvPrecio;
        private TextView tvCostoTotal;
        private ImageButton btnCalcularCosto;
        private ImageButton btnEliminar;
        
        public TratamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoIcono = itemView.findViewById(R.id.tvTipoIcono);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvCostoTotal = itemView.findViewById(R.id.tvCostoTotal);
            btnCalcularCosto = itemView.findViewById(R.id.btnCalcularCosto);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
        
        public void bind(Tratamiento tratamiento) {
            // Determinar tipo y configurar icono y colors
            String tipoIcono = "";
            int tipoColor = 0;
            String duracionLabel = "";
            
            if (tratamiento instanceof Cirugia) {
                tipoIcono = "🔪";
                tipoColor = itemView.getContext().getResources().getColor(android.R.color.holo_red_dark, null);
                duracionLabel = "horas";
            } else if (tratamiento instanceof Medicacion) {
                tipoIcono = "💊";
                tipoColor = itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark, null);
                duracionLabel = "días";
            } else if (tratamiento instanceof Terapia) {
                tipoIcono = "🧘";
                tipoColor = itemView.getContext().getResources().getColor(android.R.color.holo_green_dark, null);
                duracionLabel = "sesiones";
            }
            
            // Tipo con icono y color
            tvTipoIcono.setText(tipoIcono);
            tvTipoIcono.setTextColor(tipoColor);
            
            // Nombre
            tvNombre.setText(tratamiento.getNombre());
            
            // Duración
            tvDuracion.setText("⏱️ " + tratamiento.getDuracion() + " " + duracionLabel);
            
            // Precio
            tvPrecio.setText("💵 $" + String.format("%.2f", tratamiento.getPrecio()) + " por " + duracionLabel);
            
            // Costo total con formato
            double costoTotal = tratamiento.calcularCosto();
            tvCostoTotal.setText("💳 Costo total: $" + String.format("%.2f", costoTotal));
            
            // Color del costo según tipo
            tvCostoTotal.setTextColor(tipoColor);
            
            // Click en el card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTratamientoClick(tratamiento);
                }
            });

            // Click en calcular costo
            btnCalcularCosto.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTratamientoShowCost(tratamiento);
                }
            });

            // Click en eliminar
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTratamientoDelete(tratamiento);
                }
            });
        }
    }
}