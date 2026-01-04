package com.example.hospital.ui.cita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.Cita;
import com.example.hospital.data.models.EstadoCita;

import java.util.ArrayList;
import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {
    
    private List<Cita> citas;
    private OnCitaClickListener listener;
    
    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
        void onCitaDelete(Cita cita);
        void onCitaCancel(Cita cita);
        void onCitaAttend(Cita cita);
    }
    
    public CitaAdapter() {
        this.citas = new ArrayList<>();
        this.listener = null;
    }
    
    public void setOnCitaClickListener(OnCitaClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citas.get(position);
        holder.bind(cita);
    }
    
    @Override
    public int getItemCount() {
        return citas.size();
    }
    
    public void actualizarCitas(List<Cita> nuevasCitas) {
        this.citas = nuevasCitas;
        notifyDataSetChanged();
    }
    
    public Cita getCitaAt(int position) {
        return citas.get(position);
    }
    
    class CitaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvId;
        private TextView tvDiaHora;
        private TextView tvPaciente;
        private TextView tvMedico;
        private TextView tvEstado;
        private ImageButton btnCancelar;
        private ImageButton btnAtender;
        private ImageButton btnEliminar;
        
        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvDiaHora = itemView.findViewById(R.id.tvDiaHora);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvMedico = itemView.findViewById(R.id.tvMedico);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnCancelar = itemView.findViewById(R.id.btnCancelar);
            btnAtender = itemView.findViewById(R.id.btnAtender);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
        
        public void bind(Cita cita) {
            // ID
            tvId.setText("Cita #" + cita.getIdCita());
            
            // Día y hora
            String diaHora = cita.getDia().name() + " a las " + cita.getHora().toString();
            tvDiaHora.setText(diaHora);
            
            // Paciente
            tvPaciente.setText("👤 " + cita.getPaciente());
            
            // Médico
            tvMedico.setText("👨‍⚕️ " + cita.getMedico());
            
            // Estado con color y visibilidad de botones
            EstadoCita estado = cita.getEstadoCita();
            switch (estado) {
                case PROGRAMADA:
                    tvEstado.setText("📅 Programada");
                    tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark, null));
                    btnCancelar.setVisibility(View.VISIBLE);
                    btnAtender.setVisibility(View.VISIBLE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    break;
                case ATENDIDA:
                    tvEstado.setText("✅ Atendida");
                    tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark, null));
                    btnCancelar.setVisibility(View.GONE);
                    btnAtender.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    break;
                case CANCELADA:
                    tvEstado.setText("❌ Cancelada");
                    tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark, null));
                    btnCancelar.setVisibility(View.GONE);
                    btnAtender.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    break;
            }
            
            // Click en el card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaClick(cita);
                }
            });

            // Click en cancelar
            btnCancelar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaCancel(cita);
                }
            });

            // Click en atender
            btnAtender.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaAttend(cita);
                }
            });

            // Click en eliminar
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaDelete(cita);
                }
            });
        }
    }
}