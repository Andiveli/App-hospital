package com.example.hospital.data.repository;

import android.content.Context;

import com.example.hospital.data.models.Cirugia;
import com.example.hospital.data.models.Medicacion;
import com.example.hospital.data.models.Terapia;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.data.storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TratamientoRepository {
    // Archivos separados para cada tipo de tratamiento
    private static final String CIRUGIAS_FILE = "cirugias.dat";
    private static final String MEDICACIONES_FILE = "medicaciones.dat";
    private static final String TERAPIAS_FILE = "terapias.dat";
    
    private final FileStorageManager storageManager;
    private List<Cirugia> cirugiasCache;
    private List<Medicacion> medicacionesCache;
    private List<Terapia> terapiasCache;

    public TratamientoRepository(Context context) {
        this.storageManager = new FileStorageManager(context);
        this.cirugiasCache = new ArrayList<>();
        this.medicacionesCache = new ArrayList<>();
        this.terapiasCache = new ArrayList<>();
        cargarTratamientos();
    }

    // Métodos generales que retornan todos los tratamientos polimórficamente
    public List<Tratamiento> getAllTratamientos() {
        List<Tratamiento> todos = new ArrayList<>();
        todos.addAll(cirugiasCache);
        todos.addAll(medicacionesCache);
        todos.addAll(terapiasCache);
        return todos;
    }

    public List<Tratamiento> getTratamientosPorTipo(String tipo) {
        List<Tratamiento> resultado = new ArrayList<>();
        switch (tipo) {
            case "Cirugía":
                resultado.addAll(cirugiasCache);
                break;
            case "Medicación":
                resultado.addAll(medicacionesCache);
                break;
            case "Terapia":
                resultado.addAll(terapiasCache);
                break;
        }
        return resultado;
    }

    public List<Tratamiento> buscarTratamientos(String termino) {
        List<Tratamiento> resultado = new ArrayList<>();
        String terminoLower = termino.toLowerCase();
        
        for (Tratamiento tratamiento : getAllTratamientos()) {
            if (tratamiento.getNombre().toLowerCase().contains(terminoLower)) {
                resultado.add(tratamiento);
            }
        }
        return resultado;
    }

    public List<Tratamiento> getTratamientosPorPrecioMaximo(double precioMax) {
        List<Tratamiento> resultado = new ArrayList<>();
        for (Tratamiento tratamiento : getAllTratamientos()) {
            if (tratamiento.getPrecio() <= precioMax) {
                resultado.add(tratamiento);
            }
        }
        return resultado;
    }

    public List<Tratamiento> getTratamientosPorDuracionMaxima(int duracionMax) {
        List<Tratamiento> resultado = new ArrayList<>();
        for (Tratamiento tratamiento : getAllTratamientos()) {
            if (tratamiento.getDuracion() <= duracionMax) {
                resultado.add(tratamiento);
            }
        }
        return resultado;
    }

    // Métodos específicos para cada tipo
    public List<Cirugia> getAllCirugias() {
        return new ArrayList<>(cirugiasCache);
    }

    public List<Medicacion> getAllMedicaciones() {
        return new ArrayList<>(medicacionesCache);
    }

    public List<Terapia> getAllTerapias() {
        return new ArrayList<>(terapiasCache);
    }

    // Métodos para guardar tratamientos polimórficos
    public boolean guardarTratamiento(Tratamiento tratamiento) {
        try {
            if (tratamiento instanceof Cirugia) {
                Cirugia cirugia = (Cirugia) tratamiento;
                cirugia.setnuevoId(cirugiasCache.size() + 1);
                cirugiasCache.add(cirugia);
                return guardarCambiosCirugias();
            } else if (tratamiento instanceof Medicacion) {
                Medicacion medicacion = (Medicacion) tratamiento;
                medicacion.setnuevoId(medicacionesCache.size() + 1);
                medicacionesCache.add(medicacion);
                return guardarCambiosMedicaciones();
            } else if (tratamiento instanceof Terapia) {
                Terapia terapia = (Terapia) tratamiento;
                terapia.setnuevoId(terapiasCache.size() + 1);
                terapiasCache.add(terapia);
                return guardarCambiosTerapias();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarTratamiento(Tratamiento tratamiento) {
        try {
            if (tratamiento instanceof Cirugia) {
                return actualizarCirugia((Cirugia) tratamiento);
            } else if (tratamiento instanceof Medicacion) {
                return actualizarMedicacion((Medicacion) tratamiento);
            } else if (tratamiento instanceof Terapia) {
                return actualizarTerapia((Terapia) tratamiento);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarTratamiento(Tratamiento tratamiento) {
        try {
            if (tratamiento instanceof Cirugia) {
                boolean resultado = cirugiasCache.remove(tratamiento);
                if (resultado) guardarCambiosCirugias();
                return resultado;
            } else if (tratamiento instanceof Medicacion) {
                boolean resultado = medicacionesCache.remove(tratamiento);
                if (resultado) guardarCambiosMedicaciones();
                return resultado;
            } else if (tratamiento instanceof Terapia) {
                boolean resultado = terapiasCache.remove(tratamiento);
                if (resultado) guardarCambiosTerapias();
                return resultado;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Métodos de actualización específicos
    private boolean actualizarCirugia(Cirugia cirugia) {
        for (int i = 0; i < cirugiasCache.size(); i++) {
            if (cirugiasCache.get(i).hashCode() == cirugia.hashCode()) {
                cirugiasCache.set(i, cirugia);
                return guardarCambiosCirugias();
            }
        }
        return false;
    }

    private boolean actualizarMedicacion(Medicacion medicacion) {
        for (int i = 0; i < medicacionesCache.size(); i++) {
            if (medicacionesCache.get(i).hashCode() == medicacion.hashCode()) {
                medicacionesCache.set(i, medicacion);
                return guardarCambiosMedicaciones();
            }
        }
        return false;
    }

    private boolean actualizarTerapia(Terapia terapia) {
        for (int i = 0; i < terapiasCache.size(); i++) {
            if (terapiasCache.get(i).hashCode() == terapia.hashCode()) {
                terapiasCache.set(i, terapia);
                return guardarCambiosTerapias();
            }
        }
        return false;
    }

    // Métodos de carga y guardado
    private void cargarTratamientos() {
        try {
            cirugiasCache = storageManager.loadList(CIRUGIAS_FILE);
            medicacionesCache = storageManager.loadList(MEDICACIONES_FILE);
            terapiasCache = storageManager.loadList(TERAPIAS_FILE);
        } catch (Exception e) {
            cirugiasCache = new ArrayList<>();
            medicacionesCache = new ArrayList<>();
            terapiasCache = new ArrayList<>();
        }
    }

    private boolean guardarCambiosCirugias() {
        try {
            storageManager.saveList(CIRUGIAS_FILE, cirugiasCache);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean guardarCambiosMedicaciones() {
        try {
            storageManager.saveList(MEDICACIONES_FILE, medicacionesCache);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean guardarCambiosTerapias() {
        try {
            storageManager.saveList(TERAPIAS_FILE, terapiasCache);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Métodos útiles para estadísticas
    public int getTotalTratamientos() {
        return cirugiasCache.size() + medicacionesCache.size() + terapiasCache.size();
    }

    public double getCostoPromedio() {
        List<Tratamiento> todos = getAllTratamientos();
        if (todos.isEmpty()) return 0.0;
        
        double total = 0;
        for (Tratamiento tratamiento : todos) {
            total += tratamiento.calcularCosto();
        }
        return total / todos.size();
    }

    public Tratamiento getTratamientoMasCaro() {
        List<Tratamiento> todos = getAllTratamientos();
        if (todos.isEmpty()) return null;
        
        Tratamiento masCaro = todos.get(0);
        for (Tratamiento tratamiento : todos) {
            if (tratamiento.calcularCosto() > masCaro.calcularCosto()) {
                masCaro = tratamiento;
            }
        }
        return masCaro;
    }
}