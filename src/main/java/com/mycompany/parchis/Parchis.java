
/**
 * Clase principal del juego Parchís Star - VERSIÓN CON RED P2P CORREGIDA
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
        
        // IMPORTANTE: Resetear contador de fichas al inicio
        Ficha.resetearContador();
        
        // Pantalla de bienvenida
        System.out.println("\n============================================================");
        System.out.println("|                                                          |");
        System.out.println("|            ⭐ PARCHIS STAR ⭐                            |");
        System.out.println("|            Versión Red P2P                               |");
        System.out.println("|                                                          |");
        System.out.println("============================================================");
        
        // Seleccionar modo de juego
        System.out.println("\n=== SELECCION DE MODO ===");
        System.out.println("1. Juego Local (sin red)");
        System.out.println("2. Crear Partida P2P (Jugador 1 - Amarillo)");
        System.out.println("3. Unirse a Partida P2P (Jugador 2 - Azul)");
        System.out.print("Selecciona una opción: ");
        
        int modoJuego = scanner.nextInt();
        scanner.nextLine();
        
        ControladorRed controladorRed = null;
        int jugadorLocalId = 1; // Por defecto jugador 1
        
        // Crear partida
        Partida partida = new Partida(1);
        
        if (modoJuego == 2 || modoJuego == 3) {
            // Modo RED P2P
            System.out.println("\n=== CONFIGURACION DE RED ===");
            System.out.print("Ingresa tu nombre de jugador: ");
            String nombreLocal = scanner.nextLine();
            
            System.out.print("Ingresa tu puerto (ej: 5000 para Jugador 1, 5001 para Jugador 2): ");
            int puertoLocal = scanner.nextInt();
            scanner.nextLine();
            
            // Determinar ID del jugador según el modo
            if (modoJuego == 2) {
                jugadorLocalId = 1; // Anfitrión es Jugador 1 (Amarillo)
            } else {
                jugadorLocalId = 2; // Cliente es Jugador 2 (Azul)
            }
            
            // Crear controlador de red
            controladorRed = new ControladorRed(nombreLocal, puertoLocal, partida.getTablero(), jugadorLocalId);
            
            try {
                if (modoJuego == 2) {
                    // ANFITRIÓN (Jugador 1)
                    controladorRed.iniciarComoAnfitrion();
                    System.out.println("\n✓ Servidor creado en puerto " + puertoLocal);
                    System.out.println("Esperando a que Jugador 2 se conecte...");
                    System.out.println("El otro jugador debe usar:");
                    System.out.println("  IP: localhost (o tu IP: " + java.net.InetAddress.getLocalHost().getHostAddress() + ")");
                    System.out.println("  Puerto: " + puertoLocal);
                    
                    // Configurar jugadores
                    Jugador jugador1 = new Jugador(1, nombreLocal, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    System.out.print("\nIngresa el nombre del Jugador 2 (Azul): ");
                    String nombre2 = scanner.nextLine();
                    Jugador jugador2 = new Jugador(2, nombre2, "Azul");
                    partida.agregarJugador(jugador2);
                    
                    System.out.println("\nPresiona ENTER cuando el Jugador 2 esté conectado...");
                    scanner.nextLine();
                    
                } else {
                    // CLIENTE (Jugador 2)
                    System.out.print("IP del Jugador 1 (anfitrión): ");
                    String ipAnfitrion = scanner.nextLine();
                    
                    System.out.print("Puerto del Jugador 1: ");
                    int puertoAnfitrion = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (controladorRed.unirseAPartida(ipAnfitrion, puertoAnfitrion)) {
                        System.out.println("\n✓ Conectado a la partida exitosamente!");
                        
                        // Configurar jugadores (mismo orden que anfitrión)
                        System.out.print("Ingresa el nombre del Jugador 1 (Amarillo): ");
                        String nombre1 = scanner.nextLine();
                        Jugador jugador1 = new Jugador(1, nombre1, "Amarillo");
                        partida.agregarJugador(jugador1);
                        
                        Jugador jugador2 = new Jugador(2, nombreLocal, "Azul");
                        partida.agregarJugador(jugador2);
                        
                        System.out.println("\nEsperando que Jugador 1 inicie la partida...");
                        scanner.nextLine();
                    } else {
                        System.out.println("\n✗ Error al conectar. Saliendo...");
                        scanner.close();
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en configuración de red: " + e.getMessage());
                e.printStackTrace();
                scanner.close();
                return;
            }
        } else {
            // Modo local
            System.out.println("\n=== CONFIGURACIÓN DE JUGADORES ===\n");
            
            System.out.print("Ingresa el nombre del Jugador 1 (Amarillo): ");
            String nombre1 = scanner.nextLine();
            Jugador jugador1 = new Jugador(1, nombre1, "Amarillo");
            partida.agregarJugador(jugador1);
            
            System.out.print("Ingresa el nombre del Jugador 2 (Azul): ");
            String nombre2 = scanner.nextLine();
            Jugador jugador2 = new Jugador(2, nombre2, "Azul");
            partida.agregarJugador(jugador2);
        }
        
        // Iniciar partida
        partida.iniciarPartida();
        
        // Crear controlador MVC
        ControladorPartida controlador = new ControladorPartida(partida, vista, scanner, jugadorLocalId);
        
        // Vincular controlador de red
        if (controladorRed != null) {
            controlador.setControladorRed(controladorRed);
            System.out.println("\n✓ Red P2P activada - Eres el Jugador " + jugadorLocalId);
        }
        
        // Variables de control del ciclo de juego
        boolean juegoActivo = true;
        int turnosJugados = 0;
        final int MAX_TURNOS = 100;
        
        System.out.println("\n¡Presiona ENTER para comenzar!");
        scanner.nextLine();
        
        // Ciclo principal del juego
        while (juegoActivo && turnosJugados < MAX_TURNOS) {
            vista.mostrarTablero(partida);
            
            // Verificar si es turno local
            if (controladorRed == null || controlador.esTurnoLocal()) {
                controlador.iniciarTurno();
                
                if (controlador.verificarFinPartida()) {
                    juegoActivo = false;
                }
                
                turnosJugados++;
            } else {
                // Esperar turno del otro jugador
                System.out.println("\n⏳ Esperando movimiento del oponente...");
                try {
                    Thread.sleep(2000); // Esperar 2 segundos antes de verificar de nuevo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Finalizar por límite de turnos
        if (turnosJugados >= MAX_TURNOS) {
            System.out.println("\n=== JUEGO FINALIZADO (Límite de turnos) ===");
            partida.finalizarPartida();
        }
        
        // Cerrar conexiones de red
        if (controladorRed != null) {
            controladorRed.cerrar();
            System.out.println("\n✓ Conexiones de red cerradas");
        }
        
        scanner.close();
        System.out.println("\nGracias por jugar Parchís Star");
    }
}