/**
 * Representa una casilla del tablero
 * Puede ser normal, segura, de salida o meta
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Casilla {
    private int numeroCasilla;
    private String tipo;
    private boolean esSegura;
    private List<Ficha> fichas;
    
    /**
     * Constructor de casilla
     * @param numero Numero de casilla (0-67)
     * @param tipo Tipo de casilla (normal, segura, salida, meta)
     */
    public Casilla(int numero, String tipo) {
        this.numeroCasilla = numero;
        this.tipo = tipo;
        this.esSegura = tipo.equals("segura");
        this.fichas = new ArrayList<>();
    }
    
    /**
     * Agrega una ficha a esta casilla
     * @param ficha Ficha a agregar
     */
    public void agregarFicha(Ficha ficha) {
        fichas.add(ficha);
    }
    
    /**
     * Remueve una ficha de esta casilla
     * @param ficha Ficha a remover
     */
    public void removerFicha(Ficha ficha) {
        fichas.remove(ficha);
    }
    
    /**
     * Verifica si hay una barrera en esta casilla
     * Una barrera se forma con 2 fichas del mismo color
     * @return true si hay barrera, false si no
     */
    public boolean verificarBarrera() {
        if (fichas.size() == 2) {
            String colorPrimera = fichas.get(0).getColor();
            String colorSegunda = fichas.get(1).getColor();
            return colorPrimera.equals(colorSegunda);
        }
        return false;
    }
    
    // Getters
    public List<Ficha> getFichas() { return fichas; }
    public boolean esSegura() { return esSegura; }
    public int getNumeroCasilla() { return numeroCasilla; }
    public String getTipo() { return tipo; }
    public boolean tieneBarrera() { return verificarBarrera(); }
}