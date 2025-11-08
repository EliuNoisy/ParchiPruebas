package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo CHAT
 * Procesa mensajes de chat entre jugadores
 */
public class ManejadorChat implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorChat(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarChat(mensaje);
    }
}