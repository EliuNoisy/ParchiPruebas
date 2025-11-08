package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo TIRADA_DADO
 * Procesa resultados de lanzamiento de dados
 */
public class ManejadorTiradaDado implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorTiradaDado(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarTiradaDado(mensaje);
    }
}