package utilidades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra todos los eventos de la partida en formato JSON
 * Genera un archivo al finalizar con el historial completo
 */
public class RegistroPartidaJSON {
    private List<EventoPartida> eventos;
    private String nombreArchivoSalida;
    private LocalDateTime inicioPartida;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public RegistroPartidaJSON() {
        this.eventos = new ArrayList<>();
        this.inicioPartida = LocalDateTime.now();
        
        // Generar nombre de archivo con timestamp
        String timestamp = inicioPartida.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        this.nombreArchivoSalida = "registro_partida_" + timestamp + ".json";
    }
    
    /**
     * Registra inicio de partida
     */
    public void registrarInicio(String jugador1, String jugador2) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "INICIO_PARTIDA";
        evento.timestamp = LocalDateTime.now().toString();
        evento.descripcion = "Partida iniciada";
        evento.jugador1 = jugador1;
        evento.jugador2 = jugador2;
        eventos.add(evento);
        
        System.out.println("[JSON] Registro iniciado: " + nombreArchivoSalida);
    }
    
    /**
     * Registra tirada de dado
     */
    public void registrarTiradaDado(String jugador, int valorDado) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "TIRADA_DADO";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.valorDado = valorDado;
        evento.descripcion = jugador + " lanzo el dado y obtuvo " + valorDado;
        eventos.add(evento);
    }
    
    /**
     * Registra movimiento de ficha
     */
    public void registrarMovimiento(String jugador, int fichaId, int posicionInicial, int posicionFinal) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "MOVIMIENTO";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.fichaId = fichaId;
        evento.posicionInicial = posicionInicial;
        evento.posicionFinal = posicionFinal;
        evento.descripcion = jugador + " movio ficha " + fichaId + " de casilla " + 
                           posicionInicial + " a " + posicionFinal;
        eventos.add(evento);
    }
    
    /**
     * Registra cuando una ficha sale de casa
     */
    public void registrarSalidaCasa(String jugador, int fichaId) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "SALIDA_CASA";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.fichaId = fichaId;
        evento.descripcion = jugador + " saco ficha " + fichaId + " de casa";
        eventos.add(evento);
    }
    
    /**
     * Registra cuando se come una ficha
     */
    public void registrarFichaComida(String jugadorAtacante, String jugadorVictima, int fichaId) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "FICHA_COMIDA";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugadorAtacante;
        evento.jugadorVictima = jugadorVictima;
        evento.fichaId = fichaId;
        evento.descripcion = jugadorAtacante + " comio ficha " + fichaId + " de " + jugadorVictima;
        eventos.add(evento);
    }
    
    /**
     * Registra cuando una ficha llega a meta
     */
    public void registrarLlegadaMeta(String jugador, int fichaId) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "LLEGADA_META";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.fichaId = fichaId;
        evento.descripcion = jugador + " llego a la meta con ficha " + fichaId;
        eventos.add(evento);
    }
    
    /**
     * Registra cambio de turno
     */
    public void registrarCambioTurno(String jugadorAnterior, String jugadorNuevo) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "CAMBIO_TURNO";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugadorAnterior = jugadorAnterior;
        evento.jugador = jugadorNuevo;
        evento.descripcion = "Turno cambio de " + jugadorAnterior + " a " + jugadorNuevo;
        eventos.add(evento);
    }
    
    /**
     * Registra turno extra por sacar 6
     */
    public void registrarTurnoExtra(String jugador) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "TURNO_EXTRA";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.descripcion = jugador + " obtuvo turno extra por sacar 6";
        eventos.add(evento);
    }
    
    /**
     * Registra penalizacion por tres 6 seguidos
     */
    public void registrarPenalizacionTresSeis(String jugador, int fichaId) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "PENALIZACION_TRES_SEIS";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = jugador;
        evento.fichaId = fichaId;
        evento.descripcion = jugador + " saco tres 6 seguidos, ficha " + fichaId + " regresa a casa";
        eventos.add(evento);
    }
    
    /**
     * Registra fin de partida
     */
    public void registrarFinPartida(String ganador) {
        EventoPartida evento = new EventoPartida();
        evento.tipo = "FIN_PARTIDA";
        evento.timestamp = LocalDateTime.now().toString();
        evento.jugador = ganador;
        evento.descripcion = ganador + " ha ganado la partida";
        eventos.add(evento);
    }
    
    /**
     * Guarda el registro completo en archivo JSON
     * Se llama al finalizar la partida
     */
    public boolean guardarRegistro() {
        try {
            RegistroCompleto registro = new RegistroCompleto();
            registro.inicioPartida = inicioPartida.toString();
            registro.finPartida = LocalDateTime.now().toString();
            registro.totalEventos = eventos.size();
            registro.eventos = eventos;
            
            String json = gson.toJson(registro);
            
            try (FileWriter writer = new FileWriter(nombreArchivoSalida)) {
                writer.write(json);
            }
            
            System.out.println("\n================================================");
            System.out.println("  REGISTRO JSON GUARDADO");
            System.out.println("  Archivo: " + nombreArchivoSalida);
            System.out.println("  Total eventos: " + eventos.size());
            System.out.println("================================================");
            
            return true;
            
        } catch (IOException e) {
            System.err.println("[JSON] Error guardando registro: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Muestra el contenido del registro en consola (opcional)
     */
    public void mostrarEnConsola() {
        System.out.println("\n=== REGISTRO DE PARTIDA (JSON) ===");
        for (EventoPartida evento : eventos) {
            System.out.println(evento.descripcion);
        }
        System.out.println("===================================\n");
    }
    
    /**
     * Clase interna para representar un evento
     */
    private static class EventoPartida {
        String tipo;
        String timestamp;
        String descripcion;
        String jugador;
        String jugador1;
        String jugador2;
        String jugadorAnterior;
        String jugadorVictima;
        Integer valorDado;
        Integer fichaId;
        Integer posicionInicial;
        Integer posicionFinal;
    }
    
    /**
     * Clase para el registro completo
     */
    private static class RegistroCompleto {
        String inicioPartida;
        String finPartida;
        int totalEventos;
        List<EventoPartida> eventos;
    }
}