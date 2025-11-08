package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo SALUDO
 * Procesa la identificacion inicial de jugadores
 */
public class ManejadorSaludo implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorSaludo(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarSaludo(mensaje);
    }
}