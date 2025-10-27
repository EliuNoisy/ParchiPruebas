/**
 * Clase principal del juego Parchís Star - VERSIÓN TOTALMENTE CORREGIDA
 * Sincronización completa y flujo sin errores
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
        int jugadorLocalId = 1;
        
        // Crear partida
        Partida partida = new Partida(1);
        
        if (modoJuego == 2 || modoJuego == 3) {
            // ===== MODO RED P2P =====
            System.out.println("\n=== CONFIGURACION DE RED ===");
            System.out.print("Ingresa tu nombre de jugador: ");
            String nombreLocal = scanner.nextLine();
            
            System.out.print("Ingresa tu puerto (ej: 5000 para Jugador 1, 5001 para Jugador 2): ");
            int puertoLocal = scanner.nextInt();
            scanner.nextLine();
            
            // Determinar ID del jugador
            if (modoJuego == 2) {
                jugadorLocalId = 1;
            } else {
                jugadorLocalId = 2;
            }
            
            // Crear controlador de red
            controladorRed = new ControladorRed(nombreLocal, puertoLocal, partida.getTablero(), jugadorLocalId);
            
            try {
                if (modoJuego == 2) {
                    // ========== ANFITRIÓN (Jugador 1) ==========
                    System.out.println("\n╔════════════════════════════════════════════════╗");
                    System.out.println("║  MODO: ANFITRIÓN - Creando partida...         ║");
                    System.out.println("╚════════════════════════════════════════════════╝");
                    
                    controladorRed.iniciarComoAnfitrion();
                    System.out.println("\n✓ Servidor creado en puerto " + puertoLocal);
                    System.out.println("\n┌─────────────────────────────────────────────┐");
                    System.out.println("│ ESPERANDO AL JUGADOR 2...                  │");
                    System.out.println("│                                             │");
                    System.out.println("│ El otro jugador debe conectarse a:         │");
                    System.out.println("│   IP: localhost                             │");
                    System.out.println("│   (o tu IP: " + java.net.InetAddress.getLocalHost().getHostAddress() + ")                 │");
                    System.out.println("│   Puerto: " + puertoLocal + "                              │");
                    System.out.println("└─────────────────────────────────────────────┘");
                    
                    // Configurar Jugador 1 (yo mismo)
                    Jugador jugador1 = new Jugador(1, nombreLocal, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    // Esperar conexión y nombre del Jugador 2
                    System.out.println("\n⏳ Esperando que el Jugador 2 se conecte...");
                    String nombreJugador2 = controladorRed.esperarNombreOponente();
                    
                    if (nombreJugador2 == null || nombreJugador2.isEmpty()) {
                        System.out.println("\n✗ ERROR: No se conectó ningún jugador.");
                        System.out.println("Asegúrate de que el Jugador 2 use la IP y puerto correctos.");
                        controladorRed.cerrar();
                        scanner.close();
                        return;
                    }
                    
                    // Crear Jugador 2 con el nombre recibido
                    Jugador jugador2 = new Jugador(2, nombreJugador2, "Azul");
                    partida.agregarJugador(jugador2);
                    
                    System.out.println("\n✓✓✓ JUGADOR 2 CONECTADO: " + nombreJugador2 + " ✓✓✓");
                    System.out.println("\n════════════════════════════════════════════════");
                    System.out.println("  Jugadores registrados:");
                    System.out.println("  1. " + nombreLocal + " (Amarillo) - TÚ");
                    System.out.println("  2. " + nombreJugador2 + " (Azul)");
                    System.out.println("════════════════════════════════════════════════");
                    
                    System.out.println("\n>>> Presiona ENTER cuando ambos estén listos para iniciar <<<");
                    scanner.nextLine();
                    
                    // Notificar al cliente que puede iniciar
                    controladorRed.enviarInicioPartida();
                    
                } else {
                    // ========== CLIENTE (Jugador 2) ==========
                    System.out.println("\n╔════════════════════════════════════════════════╗");
                    System.out.println("║  MODO: CLIENTE - Uniéndose a partida...       ║");
                    System.out.println("╚════════════════════════════════════════════════╝");
                    
                    System.out.print("\nIP del Jugador 1 (anfitrión) [localhost]: ");
                    String ipAnfitrion = scanner.nextLine().trim();
                    if (ipAnfitrion.isEmpty()) {
                        ipAnfitrion = "localhost";
                    }
                    
                    System.out.print("Puerto del Jugador 1: ");
                    int puertoAnfitrion = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.println("\n⏳ Conectando al anfitrión...");
                    
                    if (!controladorRed.unirseAPartida(ipAnfitrion, puertoAnfitrion)) {
                        System.out.println("\n✗ ERROR: No se pudo conectar al anfitrión.");
                        System.out.println("Verifica que:");
                        System.out.println("  - El Jugador 1 haya creado la partida");
                        System.out.println("  - La IP y puerto sean correctos");
                        System.out.println("  - Ambos estén en la misma red");
                        scanner.close();
                        return;
                    }
                    
                    System.out.println("\n✓ Conectado al servidor!");
                    System.out.println("⏳ Esperando identificación del anfitrión...");
                    
                    // Esperar nombre del Jugador 1
                    String nombreJugador1 = controladorRed.esperarNombreOponente();
                    
                    if (nombreJugador1 == null || nombreJugador1.isEmpty()) {
                        System.out.println("\n✗ ERROR: No se recibió información del anfitrión.");
                        controladorRed.cerrar();
                        scanner.close();
                        return;
                    }
                    
                    // Crear ambos jugadores
                    Jugador jugador1 = new Jugador(1, nombreJugador1, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    Jugador jugador2 = new Jugador(2, nombreLocal, "Azul");
                    partida.agregarJugador(jugador2);
                    
                    System.out.println("\n✓✓✓ CONEXIÓN EXITOSA ✓✓✓");
                    System.out.println("\n════════════════════════════════════════════════");
                    System.out.println("  Jugadores registrados:");
                    System.out.println("  1. " + nombreJugador1 + " (Amarillo)");
                    System.out.println("  2. " + nombreLocal + " (Azul) - TÚ");
                    System.out.println("════════════════════════════════════════════════");
                    
                    System.out.println("\n⏳ Esperando que el anfitrión inicie la partida...");
                    
                    // Esperar señal de inicio del anfitrión
                    controladorRed.esperarInicioPartida();
                    
                    System.out.println("✓ ¡La partida está iniciando!");
                }
                
            } catch (Exception e) {
                System.err.println("\n✗ ERROR en configuración de red: " + e.getMessage());
                e.printStackTrace();
                if (controladorRed != null) {
                    controladorRed.cerrar();
                }
                scanner.close();
                return;
            }
            
        } else {
            // ===== MODO LOCAL =====
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
        
        // ===== INICIAR PARTIDA =====
        partida.iniciarPartida();
        
        // Crear controlador MVC
        ControladorPartida controlador = new ControladorPartida(partida, vista, scanner, jugadorLocalId);
        
        // Vincular controlador de red
        if (controladorRed != null) {
            controlador.setControladorRed(controladorRed);
            System.out.println("\n✓ Red P2P activada - Eres el Jugador " + jugadorLocalId);
        }
        
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║         ¡PARTIDA INICIADA!                     ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("\nPresiona ENTER para comenzar...");
        scanner.nextLine();
        
        // ===== CICLO PRINCIPAL DEL JUEGO =====
        boolean juegoActivo = true;
        int turnosJugados = 0;
        final int MAX_TURNOS = 100;
        boolean mostrarTableroEspera = true;
        
        while (juegoActivo && turnosJugados < MAX_TURNOS) {
            // Verificar si es turno local
            if (controladorRed == null || controlador.esTurnoLocal()) {
                // Es mi turno - mostrar tablero y jugar
                vista.mostrarTablero(partida);
                controlador.iniciarTurno();
                
                if (controlador.verificarFinPartida()) {
                    juegoActivo = false;
                }
                
                turnosJugados++;
                mostrarTableroEspera = true; // Resetear para próxima espera
            } else {
                // Esperar turno del otro jugador
                if (mostrarTableroEspera) {
                    vista.mostrarTablero(partida);
                    System.out.println("\n⏳ Esperando movimiento del oponente...");
                    mostrarTableroEspera = false; // Solo mostrar una vez
                }
                
                try {
                    Thread.sleep(500); // Verificar cada medio segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // ===== FINALIZACIÓN =====
        if (turnosJugados >= MAX_TURNOS) {
            System.out.println("\n=== JUEGO FINALIZADO (Límite de turnos) ===");
            partida.finalizarPartida();
        }
        
        // Cerrar conexiones
        if (controladorRed != null) {
            controladorRed.cerrar();
            System.out.println("\n✓ Conexiones de red cerradas");
        }
        
        scanner.close();
        System.out.println("\n════════════════════════════════════════════════");
        System.out.println("   Gracias por jugar Parchís Star");
        System.out.println("════════════════════════════════════════════════\n");
    }
}