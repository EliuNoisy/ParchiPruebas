/**
 * Clase principal del juego Parchís Star - VERSIÓN CON RED P2P
 * Permite jugar en red usando arquitectura Peer-to-Peer
 */
package com.mycompany.parchis;

import modelo.*;
import vista.PantallaPartida;
import controlador.ControladorPartida;
import controlador.ControladorRed;
import java.util.Scanner;

public class Parchis {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PantallaPartida vista = new PantallaPartida();
        
        // Pantalla de bienvenida
        System.out.println("\n============================================================");
        System.out.println("|                                                          |");
        System.out.println("|            ⭐ PARCHIS STAR ⭐                            |");
        System.out.println("|            Versión Red P2P                               |");
        System.out.println("|                                                          |");
        System.out.println("============================================================");
        
        // NUEVO: Seleccionar modo de juego
        System.out.println("\n=== SELECCION DE MODO ===");
        System.out.println("1. Juego Local (sin red)");
        System.out.println("2. Crear Partida P2P (Anfitrión)");
        System.out.println("3. Unirse a Partida P2P");
        System.out.print("Selecciona una opción: ");
        
        int modoJuego = scanner.nextInt();
        scanner.nextLine();
        
        ControladorRed controladorRed = null;
        
        // Crear partida
        Partida partida = new Partida(1);
        
        if (modoJuego == 2 || modoJuego == 3) {
            // Modo RED P2P
            System.out.println("\n=== CONFIGURACION DE RED ===");
            System.out.print("Ingresa tu nombre de jugador: ");
            String nombreLocal = scanner.nextLine();
            
            System.out.print("Ingresa tu puerto (ej: 5000): ");
            int puertoLocal = scanner.nextInt();
            scanner.nextLine();
            
            // Crear controlador de red
            controladorRed = new ControladorRed(nombreLocal, puertoLocal, partida.getTablero());
            
            try {
                if (modoJuego == 2) {
                    // ANFITRIÓN
                    controladorRed.iniciarComoAnfitrion();
                    System.out.println("\n✓ Servidor creado en puerto " + puertoLocal);
                    System.out.println("Los otros jugadores deben conectarse a:");
                    System.out.println("  IP: localhost (o tu IP local)");
                    System.out.println("  Puerto: " + puertoLocal);
                    
                    System.out.println("\nPresiona ENTER cuando los jugadores estén conectados...");
                    scanner.nextLine();
                    
                } else {
                    // CLIENTE - Unirse a partida
                    System.out.print("IP del anfitriOn: ");
                    String ipAnfitrion = scanner.nextLine();
                    
                    System.out.print("Puerto del anfitriOn: ");
                    int puertoAnfitrion = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (controladorRed.unirseAPartida(ipAnfitrion, puertoAnfitrion)) {
                        System.out.println("\n✓ Conectado a la partida exitosamente!");
                    } else {
                        System.out.println("\n✗ Error al conectar. Continuando sin red...");
                        controladorRed = null;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en configuración de red: " + e.getMessage());
                controladorRed = null;
            }
        }
        
        // Configuración de jugadores
        System.out.println("\n=== CONFIGURACIÓN DE JUGADORES ===\n");
        
        // Configurar Jugador 1
        System.out.print("Ingresa el nombre del Jugador 1 (Amarillo): ");
        String nombre1 = scanner.nextLine();
        Jugador jugador1 = new Jugador(1, nombre1, "Amarillo");
        partida.agregarJugador(jugador1);
        
        // Configurar Jugador 2
        System.out.print("Ingresa el nombre del Jugador 2 (Azul): ");
        String nombre2 = scanner.nextLine();
        Jugador jugador2 = new Jugador(2, nombre2, "Azul");
        partida.agregarJugador(jugador2);
        
        // Iniciar partida
        partida.iniciarPartida();
        
        // Crear controlador MVC
        ControladorPartida controlador = new ControladorPartida(partida, vista, scanner);
        
        // NUEVO: Vincular controlador de red
        if (controladorRed != null) {
            controlador.setControladorRed(controladorRed);
            System.out.println("\n✓ Red P2P activada");
        }
        
        // Variables de control del ciclo de juego
        boolean juegoActivo = true;
        int turnosJugados = 0;
        final int MAX_TURNOS = 100;
        
        System.out.println("\nPresiona ENTER para comenzar");
        scanner.nextLine();
        
        // Ciclo principal del juego
        while (juegoActivo && turnosJugados < MAX_TURNOS) {
            vista.mostrarTablero(partida);
            controlador.iniciarTurno();
            
            if (controlador.verificarFinPartida()) {
                juegoActivo = false;
            }
            
            turnosJugados++;
        }
        
        // Finalizar por límite de turnos
        if (turnosJugados >= MAX_TURNOS) {
            System.out.println("\n=== JUEGO FINALIZADO (Límite de turnos) ===");
            partida.finalizarPartida();
        }
        
        // NUEVO: Cerrar conexiones de red
        if (controladorRed != null) {
            controladorRed.cerrar();
            System.out.println("\n✓ Conexiones de red cerradas");
        }
        
        scanner.close();
        System.out.println("\nGracias por jugar Parchís Star");
    }
}