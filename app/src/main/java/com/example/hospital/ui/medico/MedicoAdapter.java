package com.example.hospital.ui.medico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.Medico;

import java.util.ArrayList;
import java.util.List;

public class MedicoAdapter extends RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder> {
    
    private List<Medico> medicos;
    private OnMedicoClickListener listener;
    
    public interface OnMedicoClickListener {
        void onMedicoClick(Medico medico);
        void onMedicoDelete(Medico medico);
        void onMedicoToggleStatus(Medico medico);
    }
    
    public MedicoAdapter() {
        this.medicos = new ArrayList<>();
        this.listener = null;
    }
    
    public void setOnMedicoClickListener(OnMedicoClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MedicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medico, parent, false);
        return new MedicoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MedicoViewHolder holder, int position) {
        Medico medico = medicos.get(position);
        holder.bind(medico);
    }
    
    @Override
    public int getItemCount() {
        return medicos.size();
    }
    
    public void actualizarMedicos(List<Medico> nuevosMedicos) {
        this.medicos = nuevosMedicos;
        notifyDataSetChanged();
    }
    
    public Medico getMedicoAt(int position) {
        return medicos.get(position);
    }
    
    class MedicoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombreCompleto;
        private TextView tvCedula;
        private TextView tvCorreo;
        private TextView tvEspecialidad;
        private TextView tvGenero;
        private TextView tvEstado;
        private ImageButton btnEliminar;
        private ImageButton btnToggleStatus;
        
        public MedicoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompleto);
            tvCedula = itemView.findViewById(R.id.tvCedula);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidad);
            tvGenero = itemView.findViewById(R.id.tvGenero);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
        }
        
        public void bind(Medico medico) {
            // Nombre completo con título
            String nombreCompleto = "Dr. " + medico.getNombre() + " " + medico.getApellido();
            tvNombreCompleto.setText(nombreCompleto);
            
            // Cédula
            tvCedula.setText("Cédula: " + medico.getCedulaString());
            
            // Correo
            tvCorreo.setText(medico.getCorreo());
            
            // Especialidad
            tvEspecialidad.setText("Especialidad: " + medico.getEspecialidad());
            
            // Género
            tvGenero.setText("Género: " + medico.getGenero());
            
            // Estado (Activo/Inactivo)
            String estado = medico.isActivo() ? "Activo" : "Inactivo";
            int colorEstado = medico.isActivo() ? 
                itemView.getContext().getResources().getColor(android.R.color.holo_green_dark, null) :
                itemView.getContext().getResources().getColor(android.R.color.holo_red_dark, null);
            tvEstado.setText(estado);
            tvEstado.setTextColor(colorEstado);
            
            // Configurar botón de toggle estado
            if (medico.isActivo()) {
                btnToggleStatus.setImageResource(R.drawable.ic_pause); // Ícono para desactivar
                btnToggleStatus.setContentDescription("Desactivar médico");
            } else {
                btnToggleStatus.setImageResource(R.drawable.ic_play_arrow); // Ícono para activar
                btnToggleStatus.setContentDescription("Activar médico");
            }
            
            // Click en el card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicoClick(medico);
                }
            });

            // Click en eliminar
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicoDelete(medico);
                }
            });

            // Click en toggle estado
            btnToggleStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicoToggleStatus(medico);
                }
            });
        }
    }
}