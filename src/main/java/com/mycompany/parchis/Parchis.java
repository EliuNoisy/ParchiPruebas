package com.mycompany.parchis;

import modelo.*;
import vista.PantallaPartida;
import controlador.ControladorPartida;
import controlador.ControladorRed;
import red.DescubrimientoRed;
import java.util.List;
import java.util.Scanner;
import java.net.InetAddress;

/**
 * Clase principal del juego Parchis Star 
 */
public class Parchis {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PantallaPartida vista = new PantallaPartida();
        
        Ficha.resetearContador();
        
        System.out.println("\n============================================================");
        System.out.println("|                                                          |");
        System.out.println("|                 PARCHIS STAR                             |");
        System.out.println("|                                                          |");
        System.out.println("============================================================");
        
        System.out.println("\n=== SELECCION DE MODO ===");
        System.out.println("1. Juego Local (sin red)");
        System.out.println("2. Juego Online (busca automaticamente en tu red)");
        System.out.print("Selecciona una opcion: ");
        
        int modoJuego = scanner.nextInt();
        scanner.nextLine();
        
        ControladorRed controladorRed = null;
        int jugadorLocalId = 1;
        
        Partida partida = new Partida(1);
        
        if (modoJuego == 2) {
            System.out.println("\n=== MODO ONLINE AUTOMATICO ===");
            System.out.print("Ingresa tu nombre: ");
            String nombreLocal = scanner.nextLine();
            
            int puertoBase = 5000 + (int)(Math.random() * 1000);
            
            DescubrimientoRed descubrimiento = new DescubrimientoRed(nombreLocal, puertoBase);
            
            // Iniciar modo respuesta para ser descubierto
            descubrimiento.iniciarModoRespuesta();
            
            System.out.println("\n================================================");
            System.out.println("  Buscando otros jugadores en la red local...");
            System.out.println("  Tiempo de espera: 10 segundos");
            System.out.println("================================================");
            
            // Buscar jugadores durante 10 segundos
            List<DescubrimientoRed.JugadorEncontrado> jugadoresEncontrados = 
                descubrimiento.buscarJugadores(10);
            
            // Dar tiempo para recibir respuestas finales
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            // Detener modo respuesta para evitar spam
            descubrimiento.detenerModoRespuesta();
            
            if (jugadoresEncontrados.isEmpty()) {
                System.out.println("\n================================================");
                System.out.println("  No se encontraron otros jugadores");
                System.out.println("================================================");
                System.out.println("\nAsegurate de que:");
                System.out.println("  1. Ambas computadoras esten en la misma red WiFi/LAN");
                System.out.println("  2. El otro jugador haya iniciado el juego");
                System.out.println("  3. El firewall permita las conexiones\n");
                scanner.close();
                return;
            }
            
            // ========================================
            // CONEXION AUTOMATICA AL PRIMER JUGADOR
            // ========================================
            DescubrimientoRed.JugadorEncontrado oponente = jugadoresEncontrados.get(0);
            
            System.out.println("\n================================================");
            System.out.println("  OPONENTE DETECTADO AUTOMATICAMENTE!");
            System.out.println("================================================");
            System.out.println("  Oponente: " + oponente.nombre);
            System.out.println("  IP: " + oponente.ip + ":" + oponente.puerto);
            
            if (jugadoresEncontrados.size() > 1) {
                System.out.println("\n  [i] Se encontraron " + jugadoresEncontrados.size() + 
                                 " jugadores, conectando al primero");
            }
            
            System.out.println("\n  Conectando...");
            System.out.println("================================================\n");
            
            // Determinar rol usando hash de nombres (mas confiable)
            int miHash = nombreLocal.hashCode();
            int oponenteHash = oponente.nombre.hashCode();
            boolean soyAnfitrion = miHash < oponenteHash;
            
            // Si los hash son iguales, usar puerto
            if (miHash == oponenteHash) {
                soyAnfitrion = puertoBase < oponente.puerto;
            }
            
            if (soyAnfitrion) {
                jugadorLocalId = 1;
                System.out.println("\n[CONEXION] Seras el ANFITRION (Jugador 1 - Amarillo)");
                System.out.println("[INFO] Tu puerto: " + puertoBase);
            } else {
                jugadorLocalId = 2;
                System.out.println("\n[CONEXION] Seras el CLIENTE (Jugador 2 - Azul)");
                System.out.println("[INFO] Puerto del anfitrion: " + oponente.puerto);
            }
            
            String miColor = (jugadorLocalId == 1) ? "Amarillo" : "Azul";
            String oponenteColor = (jugadorLocalId == 1) ? "Azul" : "Amarillo";
            
            System.out.println("\n================================================");
            System.out.println("  OPONENTE SELECCIONADO!");
            System.out.println("================================================");
            System.out.println("  Tu: " + nombreLocal + " (" + miColor + ")");
            System.out.println("  VS: " + oponente.nombre + " (" + oponenteColor + ")");
            System.out.println("================================================\n");
            
            controladorRed = new ControladorRed(nombreLocal, puertoBase, 
                                               partida.getTablero(), jugadorLocalId);
            
            try {
                if (soyAnfitrion) {
                    System.out.println("[ANFITRION] Iniciando servidor en puerto " + puertoBase + "...");
                    controladorRed.iniciarComoAnfitrion();
                    
                    Jugador jugador1 = new Jugador(1, nombreLocal, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    System.out.println("[ANFITRION] Servidor listo. Esperando conexion de " + 
                                     oponente.nombre + "...");
                    String nombreJugador2 = controladorRed.esperarNombreOponente();
                    
                    if (nombreJugador2 == null || nombreJugador2.isEmpty()) {
                        System.out.println("\nERROR: El oponente no se conecto.");
                        controladorRed.cerrar();
                        scanner.close();
                        return;
                    }
                    
                    Jugador jugador2 = new Jugador(2, nombreJugador2, "Azul");
                    partida.agregarJugador(jugador2);
                    
                    System.out.println("\n[ANFITRION] " + nombreJugador2 + " conectado!");
                    System.out.println("\n================================================");
                    System.out.println("  Jugadores listos:");
                    System.out.println("  1. " + nombreLocal + " (Amarillo) - TU");
                    System.out.println("  2. " + nombreJugador2 + " (Azul)");
                    System.out.println("================================================");
                    
                    System.out.println("\n>>> Presiona ENTER para iniciar la partida <<<");
                    scanner.nextLine();
                    
                    controladorRed.enviarInicioPartida();
                    
                } else {
                    System.out.println("[CLIENTE] Iniciando servidor local en puerto " + (puertoBase + 1) + "...");
                    
                    System.out.println("[CLIENTE] Esperando 5 segundos para que el anfitrion este listo...");
                    Thread.sleep(5000);
                    
                    System.out.println("[CLIENTE] Conectando a " + oponente.ip + ":" + oponente.puerto + "...");
                    
                    if (!controladorRed.unirseAPartida(oponente.ip, oponente.puerto)) {
                        System.out.println("\nERROR: No se pudo conectar al oponente.");
                        System.out.println("Posibles causas:");
                        System.out.println("  - El anfitrion cerro el programa");
                        System.out.println("  - Firewall bloqueando conexion");
                        System.out.println("  - Puerto incorrecto");
                        scanner.close();
                        return;
                    }
                    
                    System.out.println("[CLIENTE] Conectado!");
                    
                    String nombreJugador1 = controladorRed.esperarNombreOponente();
                    
                    if (nombreJugador1 == null || nombreJugador1.isEmpty()) {
                        System.out.println("\nERROR: Error en la conexion.");
                        controladorRed.cerrar();
                        scanner.close();
                        return;
                    }
                    
                    Jugador jugador1 = new Jugador(1, nombreJugador1, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    Jugador jugador2 = new Jugador(2, nombreLocal, "Azul");
                    partida.agregarJugador(jugador2);
                    
                    System.out.println("\n================================================");
                    System.out.println("  Jugadores listos:");
                    System.out.println("  1. " + nombreJugador1 + " (Amarillo)");
                    System.out.println("  2. " + nombreLocal + " (Azul) - TU");
                    System.out.println("================================================");
                    
                    System.out.println("\n[CLIENTE] Esperando que " + nombreJugador1 + " inicie la partida...");
                    
                    controladorRed.esperarInicioPartida();
                    System.out.println("Iniciando partida!");
                }
                
            } catch (Exception e) {
                System.err.println("\nERROR en la conexion: " + e.getMessage());
                e.printStackTrace();
                if (controladorRed != null) {
                    controladorRed.cerrar();
                }
                scanner.close();
                return;
            }
            
        } else {
            System.out.println("\n=== CONFIGURACION DE JUGADORES ===\n");
            
            System.out.print("Ingresa el nombre del Jugador 1 (Amarillo): ");
            String nombre1 = scanner.nextLine();
            Jugador jugador1 = new Jugador(1, nombre1, "Amarillo");
            partida.agregarJugador(jugador1);
            
            System.out.print("Ingresa el nombre del Jugador 2 (Azul): ");
            String nombre2 = scanner.nextLine();
            Jugador jugador2 = new Jugador(2, nombre2, "Azul");
            partida.agregarJugador(jugador2);
        }
        
        partida.iniciarPartida();
        
        ControladorPartida controlador = new ControladorPartida(partida, vista, scanner, jugadorLocalId);
        
        if (controladorRed != null) {
            controlador.setControladorRed(controladorRed);
            System.out.println("\nRed P2P activada - Eres el Jugador " + jugadorLocalId);
        }
        
        System.out.println("\n================================================");
        System.out.println("            PARTIDA INICIADA!");
        System.out.println("================================================");
        System.out.println("\nPresiona ENTER para comenzar...");
        scanner.nextLine();
        
        boolean juegoActivo = true;
        int turnosJugados = 0;
        final int MAX_TURNOS = 100;
        boolean mostrarTableroEspera = true;
        
        while (juegoActivo && turnosJugados < MAX_TURNOS) {
            if (controladorRed != null && controladorRed.getPeersConectados().isEmpty()) {
                System.out.println("\n================================================");
                System.out.println("  OPONENTE DESCONECTADO");
                System.out.println("  La partida terminara...");
                System.out.println("================================================");
                partida.finalizarPartida();
                break;
            }
            
            if (controladorRed == null || controlador.esTurnoLocal()) {
                vista.mostrarTablero(partida);
                controlador.iniciarTurno();
                
                if (controlador.verificarFinPartida()) {
                    juegoActivo = false;
                }
                
                turnosJugados++;
                mostrarTableroEspera = true;
            } else {
                if (mostrarTableroEspera) {
                    vista.mostrarTablero(partida);
                    System.out.println("\nEsperando movimiento del oponente...");
                    mostrarTableroEspera = false;
                }
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (turnosJugados >= MAX_TURNOS) {
            System.out.println("\n=== JUEGO FINALIZADO (Limite de turnos) ===");
            partida.finalizarPartida();
        }
        
        if (controladorRed != null) {
            controladorRed.cerrar();
            System.out.println("\nConexiones de red cerradas");
        }
        
        scanner.close();
        System.out.println("\n================================================");
        System.out.println("   Gracias por jugar Parchis Star");
        System.out.println("================================================\n");
    }
}