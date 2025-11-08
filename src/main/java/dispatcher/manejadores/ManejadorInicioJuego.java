package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo INICIO_JUEGO
 * Procesa senales de inicio de partida
 */
public class ManejadorInicioJuego implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorInicioJuego(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarInicioJuego(mensaje);
    }
}