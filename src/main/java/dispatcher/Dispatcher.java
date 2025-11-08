package dispatcher;

import red.MensajeJuego;
import red.ConexionPeer;
import java.util.HashMap;
import java.util.Map;

/**
 * Dispatcher central que distribuye mensajes a sus manejadores correspondientes
 * Implementa el patron Dispatcher para desacoplar la logica de procesamiento
 */
public class Dispatcher {
    
    private Map<MensajeJuego.TipoMensaje, ManejadorMensaje> manejadores;
    
    /**
     * Constructor del Dispatcher
     * Inicializa el mapa de manejadores
     */
    public Dispatcher() {
        this.manejadores = new HashMap<>();
    }
    
    /**
     * Registra un manejador para un tipo especifico de mensaje
     * @param tipo Tipo de mensaje a manejar
     * @param manejador Manejador que procesara este tipo de mensaje
     */
    public void registrar(MensajeJuego.TipoMensaje tipo, ManejadorMensaje manejador) {
        manejadores.put(tipo, manejador);
        System.out.println("[DISPATCHER] Manejador registrado para: " + tipo);
    }
    
    /**
     * Despacha un mensaje al manejador apropiado
     * @param mensaje Mensaje a procesar
     * @param desde Peer que envio el mensaje
     */
    public void despachar(MensajeJuego mensaje, ConexionPeer desde) {
        MensajeJuego.TipoMensaje tipo = mensaje.getTipo();
        ManejadorMensaje manejador = manejadores.get(tipo);
        
        if (manejador != null) {
            System.out.println("[DISPATCHER] Despachando mensaje: " + tipo);
            manejador.manejar(mensaje, desde);
        } else {
            System.out.println("[DISPATCHER] No hay manejador registrado para: " + tipo);
        }
    }
    
    /**
     * Verifica si existe un manejador para un tipo de mensaje
     * @param tipo Tipo de mensaje
     * @return true si existe manejador, false si no
     */
    public boolean tieneManejador(MensajeJuego.TipoMensaje tipo) {
        return manejadores.containsKey(tipo);
    }
    
    /**
     * Elimina el manejador de un tipo de mensaje
     * @param tipo Tipo de mensaje
     */
    public void desregistrar(MensajeJuego.TipoMensaje tipo) {
        manejadores.remove(tipo);
        System.out.println("[DISPATCHER] Manejador eliminado para: " + tipo);
    }
    
    /**
     * Limpia todos los manejadores registrados
     */
    public void limpiar() {
        manejadores.clear();
        System.out.println("[DISPATCHER] Todos los manejadores eliminados");
    }
    
    /**
     * Obtiene el numero de manejadores registrados
     * @return Cantidad de manejadores
     */
    public int cantidadManejadores() {
        return manejadores.size();
    }
}