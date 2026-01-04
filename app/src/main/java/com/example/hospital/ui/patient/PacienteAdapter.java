package com.example.hospital.ui.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospital.R;
import com.example.hospital.data.models.Paciente;

import java.util.ArrayList;
import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> {
    
    private List<Paciente> pacientes;
    private OnPacienteClickListener listener;
    
    public interface OnPacienteClickListener {
        void onPacienteClick(Paciente paciente);
        void onPacienteDelete(Paciente paciente);
    }
    
    public PacienteAdapter() {
        this.pacientes = new ArrayList<>();
        this.listener = null;
    }
    
    public void setOnPacienteClickListener(OnPacienteClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false);
        return new PacienteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Paciente paciente = pacientes.get(position);
        holder.bind(paciente);
    }
    
    @Override
    public int getItemCount() {
        return pacientes.size();
    }
    
    public void actualizarPacientes(List<Paciente> nuevosPacientes) {
        this.pacientes = nuevosPacientes;
        notifyDataSetChanged();
    }
    
    public Paciente getPacienteAt(int position) {
        return pacientes.get(position);
    }
    
    class PacienteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombreCompleto;
        private TextView tvCedula;
        private TextView tvCorreo;
        private TextView tvSeguro;
        private ImageButton btnEliminar;
        
        public PacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompleto);
            tvCedula = itemView.findViewById(R.id.tvCedula);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvSeguro = itemView.findViewById(R.id.tvSeguro);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
        
        public void bind(Paciente paciente) {
            // Nombre completo
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            tvNombreCompleto.setText(nombreCompleto);
            
            // Cédula
            tvCedula.setText("Cédula: " + paciente.getCedulaString());
            
            // Correo
            tvCorreo.setText(paciente.getCorreo());
            
            // Tipo de seguro
            tvSeguro.setText("Seguro: " + paciente.getTipoSeguro().name());
            
            // Click en el card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPacienteClick(paciente);
                }
            });

            // Click en eliminar
            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPacienteDelete(paciente);
                }
            });
        }
    }
}