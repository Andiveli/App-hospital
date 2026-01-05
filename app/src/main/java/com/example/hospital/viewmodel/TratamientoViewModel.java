package com.example.hospital.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hospital.data.models.Cirugia;
import com.example.hospital.data.models.Medicacion;
import com.example.hospital.data.models.Terapia;
import com.example.hospital.data.models.Tratamiento;
import com.example.hospital.data.repository.TratamientoRepository;

import java.util.List;

public class TratamientoViewModel extends AndroidViewModel {
    private static final String TAG = "TratamientoViewModel";
    
    private final TratamientoRepository tratamientoRepository;
    private final MutableLiveData<List<Tratamiento>> tratamientos;
    private final MutableLiveData<String> mensaje;
    private final MutableLiveData<Boolean> loading;
    private Tratamiento tratamientoActual;

    public TratamientoViewModel(Application application) {
        super(application);
        this.tratamientoRepository = new TratamientoRepository(application.getApplicationContext());
        this.tratamientos = new MutableLiveData<>();
        this.mensaje = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        cargarTratamientos();
    }

    public LiveData<List<Tratamiento>> getTratamientos() {
        return tratamientos;
    }
    
    public LiveData<String> getMensaje() {
        return mensaje;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void cargarTratamientos() {
        loading.setValue(true);
        try {
            List<Tratamiento> lista = tratamientoRepository.getAllTratamientos();
            tratamientos.setValue(lista);
            mensaje.setValue("Tratamientos cargados: " + lista.size());
            Log.d(TAG, "Cargados " + lista.size() + " tratamientos");
        } catch (Exception e) {
            mensaje.setValue("Error al cargar tratamientos: " + e.getMessage());
            Log.e(TAG, "Error cargando tratamientos", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void guardarTratamiento(String nombre, String duracionStr, String precioStr, String tipo) {
        guardarTratamiento(nombre, duracionStr, precioStr, tipo, false);
    }

    public void guardarTratamiento(String nombre, String duracionStr, String precioStr, String tipo, boolean esEdicion) {
        loading.setValue(true);
        
        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty()) {
            mensaje.setValue("Error: El nombre es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (duracionStr == null || duracionStr.trim().isEmpty()) {
            mensaje.setValue("Error: La duración es obligatoria");
            loading.setValue(false);
            return;
        }
        
        if (precioStr == null || precioStr.trim().isEmpty()) {
            mensaje.setValue("Error: El precio es obligatorio");
            loading.setValue(false);
            return;
        }
        
        if (tipo == null || tipo.trim().isEmpty()) {
            mensaje.setValue("Error: El tipo de tratamiento es obligatorio");
            loading.setValue(false);
            return;
        }
        
        try {
            int duracion = Integer.parseInt(duracionStr.trim());
            double precio = Double.parseDouble(precioStr.trim());
            
            if (duracion <= 0) {
                mensaje.setValue("Error: La duración debe ser mayor a 0");
                loading.setValue(false);
                return;
            }
            
            if (precio <= 0) {
                mensaje.setValue("Error: El precio debe ser mayor a 0");
                loading.setValue(false);
                return;
            }
            
            Tratamiento nuevoTratamiento;
            switch (tipo) {
                case "Cirugía":
                    nuevoTratamiento = new Cirugia(nombre.trim(), duracion, precio);
                    break;
                case "Medicación":
                    nuevoTratamiento = new Medicacion(nombre.trim(), duracion, precio);
                    break;
                case "Terapia":
                    nuevoTratamiento = new Terapia(nombre.trim(), duracion, precio);
                    break;
                default:
                    mensaje.setValue("Error: Tipo de tratamiento no válido");
                    loading.setValue(false);
                    return;
            }
            
            boolean resultado;
            if (esEdicion && tratamientoActual != null) {
                // Mantener el ID del tratamiento original
                nuevoTratamiento.setnuevoId(tratamientoActual.getId());
                resultado = tratamientoRepository.actualizarTratamiento(nuevoTratamiento);
                
                if (resultado) {
                    mensaje.setValue("Tratamiento actualizado exitosamente");
                    cargarTratamientos();
                } else {
                    mensaje.setValue("No se pudo actualizar el tratamiento");
                }
            } else {
                resultado = tratamientoRepository.guardarTratamiento(nuevoTratamiento);
                
                if (resultado) {
                    mensaje.setValue("Tratamiento guardado exitosamente");
                    cargarTratamientos();
                } else {
                    mensaje.setValue("No se pudo guardar el tratamiento");
                }
            }
            
        } catch (NumberFormatException e) {
            mensaje.setValue("Error: Formato de número inválido");
        } catch (Exception e) {
            mensaje.setValue("Error al guardar tratamiento: " + e.getMessage());
            Log.e(TAG, "Error guardando tratamiento", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorTipo(String tipo) {
        loading.setValue(true);
        try {
            List<Tratamiento> filtrados = tratamientoRepository.getTratamientosPorTipo(tipo);
            tratamientos.setValue(filtrados);
            mensaje.setValue("Tratamientos de tipo " + tipo + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por tipo: " + e.getMessage());
            Log.e(TAG, "Error filtrando por tipo", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void buscarTratamientos(String termino) {
        loading.setValue(true);
        try {
            List<Tratamiento> resultados = tratamientoRepository.buscarTratamientos(termino);
            tratamientos.setValue(resultados);
            mensaje.setValue("Resultados de búsqueda: " + resultados.size() + " tratamientos");
        } catch (Exception e) {
            mensaje.setValue("Error en búsqueda: " + e.getMessage());
            Log.e(TAG, "Error en búsqueda", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorPrecioMaximo(double precioMax) {
        loading.setValue(true);
        try {
            List<Tratamiento> filtrados = tratamientoRepository.getTratamientosPorPrecioMaximo(precioMax);
            tratamientos.setValue(filtrados);
            mensaje.setValue("Tratamientos con precio máximo $" + precioMax + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por precio: " + e.getMessage());
            Log.e(TAG, "Error filtrando por precio", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void filtrarPorDuracionMaxima(int duracionMax) {
        loading.setValue(true);
        try {
            List<Tratamiento> filtrados = tratamientoRepository.getTratamientosPorDuracionMaxima(duracionMax);
            tratamientos.setValue(filtrados);
            mensaje.setValue("Tratamientos con duración máxima " + duracionMax + ": " + filtrados.size());
        } catch (Exception e) {
            mensaje.setValue("Error al filtrar por duración: " + e.getMessage());
            Log.e(TAG, "Error filtrando por duración", e);
        } finally {
            loading.setValue(false);
        }
    }

    public void eliminarTratamiento(Tratamiento tratamiento) {
        loading.setValue(true);
        try {
            boolean resultado = tratamientoRepository.eliminarTratamiento(tratamiento);
            
            if (resultado) {
                mensaje.setValue("Tratamiento eliminado exitosamente");
                cargarTratamientos();
            } else {
                mensaje.setValue("No se pudo eliminar el tratamiento");
            }
            
        } catch (Exception e) {
            mensaje.setValue("Error al eliminar tratamiento: " + e.getMessage());
            Log.e(TAG, "Error eliminando tratamiento", e);
        } finally {
            loading.setValue(false);
        }
    }

    // Métodos específicos para cada tipo
    public void cargarCirugias() {
        filtrarPorTipo("cirugia");
    }

    public void cargarMedicaciones() {
        filtrarPorTipo("medicacion");
    }

    public void cargarTerapias() {
        filtrarPorTipo("terapia");
    }

    // Métodos para estadísticas
    public int getTotalTratamientos() {
        return tratamientoRepository.getTotalTratamientos();
    }

    public double getCostoPromedio() {
        return tratamientoRepository.getCostoPromedio();
    }

    public Tratamiento getTratamientoMasCaro() {
        return tratamientoRepository.getTratamientoMasCaro();
    }

    public void setTratamientoActual(Tratamiento tratamiento) {
        this.tratamientoActual = tratamiento;
    }

    public Tratamiento getTratamientoActual() {
        return tratamientoActual;
    }

    public void limpiarTratamientoActual() {
        this.tratamientoActual = null;
    }

    public void limpiarMensaje() {
        mensaje.setValue("");
    }

    // Métodos útiles para obtener tratamientos específicos
    public List<Cirugia> getCirugias() {
        return tratamientoRepository.getAllCirugias();
    }

    public List<Medicacion> getMedicaciones() {
        return tratamientoRepository.getAllMedicaciones();
    }

    public List<Terapia> getTerapias() {
        return tratamientoRepository.getAllTerapias();
    }
}