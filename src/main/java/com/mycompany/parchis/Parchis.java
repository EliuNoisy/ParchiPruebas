package com.mycompany.parchis;

import modelo.*;
import vista.PantallaPartida;
import controlador.ControladorPartida;
import controlador.ControladorRed;
import red.DescubrimientoRed;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal del juego Parchis Star - MEJORADA
 * Conexion automatica confiable en: misma laptop, 2 laptops, VM, netbeans+cmd
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
        System.out.println("2. Juego Online (conexion automatica)");
        System.out.print("Selecciona una opcion: ");
        
        int modoJuego = scanner.nextInt();
        scanner.nextLine();
        
        ControladorRed controladorRed = null;
        int jugadorLocalId = 1;
        
        Partida partida = new Partida(1);
        
        if (modoJuego == 2) {
            System.out.println("\n=== MODO ONLINE AUTOMATICO MEJORADO ===");
            System.out.print("Ingresa tu nombre: ");
            String nombreLocal = scanner.nextLine();
            
            int puertoBase = 5000 + (int)(Math.random() * 1000);
            
            DescubrimientoRed descubrimiento = new DescubrimientoRed(nombreLocal, puertoBase);
            
            // Iniciar modo respuesta
            descubrimiento.iniciarModoRespuesta();
            
            System.out.println("\n================================================");
            System.out.println("  Buscando jugadores en la red...");
            System.out.println("  Tiempo de busqueda: 5 segundos");
            System.out.println("  Escenarios soportados:");
            System.out.println("  - Misma laptop (NetBeans + CMD)");
            System.out.println("  - 2 Laptops en misma red");
            System.out.println("  - Virtual Machine");
            System.out.println("================================================");
            
            // Buscar durante 5 segundos (aumentado para mayor confiabilidad)
            List<DescubrimientoRed.JugadorEncontrado> jugadoresEncontrados = 
                descubrimiento.buscarJugadores(5);
            
            // Espera adicional para recibir respuestas finales
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            // Detener modo respuesta
            descubrimiento.detenerModoRespuesta();
            
            // DECISION DE ROL BASADA EN TIMESTAMP
            boolean soyAnfitrion = true;
            DescubrimientoRed.JugadorEncontrado oponente = null;
            
            if (jugadoresEncontrados.isEmpty()) {
                // Nadie encontrado - soy anfitrion por defecto
                System.out.println("\n================================================");
                System.out.println("  No se encontraron otros jugadores");
                System.out.println("  >>> SERAS EL ANFITRION <<<");
                System.out.println("  (Jugador 1 - Amarillo)");
                System.out.println("================================================");
                
            } else {
                // Alguien encontrado - comparar timestamps
                oponente = jugadoresEncontrados.get(0);
                long miTimestamp = descubrimiento.getTimestamp();
                long timestampOponente = oponente.timestamp;
                
                System.out.println("\n================================================");
                System.out.println("  Jugador detectado: " + oponente.nombre);
                System.out.println("  Tu timestamp: " + miTimestamp);
                System.out.println("  Su timestamp: " + timestampOponente);
                System.out.println("================================================");
                
                // El que tenga MENOR timestamp es anfitrion
                if (miTimestamp < timestampOponente) {
                    soyAnfitrion = true;
                    System.out.println("\n>>> TU TIMESTAMP ES MENOR <<<");
                    System.out.println(">>> SERAS EL ANFITRION <<<");
                    System.out.println("(Jugador 1 - Amarillo)");
                } else {
                    soyAnfitrion = false;
                    System.out.println("\n>>> SU TIMESTAMP ES MENOR <<<");
                    System.out.println(">>> SERAS EL CLIENTE <<<");
                    System.out.println("(Jugador 2 - Azul)");
                }
                System.out.println("================================================");
            }
            
            // CONFIGURACION SEGUN ROL
            if (soyAnfitrion) {
                // =============================================
                // ANFITRION
                // =============================================
                jugadorLocalId = 1;
                controladorRed = new ControladorRed(nombreLocal, puertoBase, 
                                                   partida.getTablero(), jugadorLocalId);
                
                try {
                    System.out.println("\n[ANFITRION] Iniciando servidor en puerto " + puertoBase + "...");
                    controladorRed.iniciarComoAnfitrion();
                    
                    Jugador jugador1 = new Jugador(1, nombreLocal, "Amarillo");
                    partida.agregarJugador(jugador1);
                    
                    if (oponente == null) {
                        // Esperar conexion nueva
                        System.out.println("[ANFITRION] Esperando que otro jugador se conecte...");
                        String nombreJugador2 = controladorRed.esperarNombreOponente();
                        
                        if (nombreJugador2 == null || nombreJugador2.isEmpty()) {
                            System.out.println("\nERROR: No se recibio conexion.");
                            controladorRed.cerrar();
                            scanner.close();
                            return;
                        }
                        
                        Jugador jugador2 = new Jugador(2, nombreJugador2, "Azul");
                        partida.agregarJugador(jugador2);
                        
                    } else {
                        // Esperar que el cliente detectado se conecte
                        System.out.println("[ANFITRION] Esperando conexion de " + oponente.nombre + "...");
                        String nombreJugador2 = controladorRed.esperarNombreOponente();
                        
                        if (nombreJugador2 == null || nombreJugador2.isEmpty()) {
                            System.out.println("\nERROR: El oponente no se conecto.");
                            controladorRed.cerrar();
                            scanner.close();
                            return;
                        }
                        
                        Jugador jugador2 = new Jugador(2, nombreJugador2, "Azul");
                        partida.agregarJugador(jugador2);
                    }
                    
                    System.out.println("\n[ANFITRION] Conexion establecida!");
                    System.out.println("\n================================================");
                    System.out.println("  Jugadores listos:");
                    System.out.println("  1. " + nombreLocal + " (Amarillo) - TU");
                    System.out.println("  2. " + partida.getJugadores().get(1).getNombre() + " (Azul)");
                    System.out.println("================================================");
                    
                    System.out.println("\n>>> Presiona ENTER para iniciar la partida <<<");
                    scanner.nextLine();
                    
                    controladorRed.enviarInicioPartida();
                    
                } catch (Exception e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    e.printStackTrace();
                    if (controladorRed != null) {
                        controladorRed.cerrar();
                    }
                    scanner.close();
                    return;
                }
                
            } else {
                // =============================================
                // CLIENTE
                // =============================================
                if (oponente == null) {
                    System.out.println("\nERROR: No se puede ser cliente sin oponente");
                    scanner.close();
                    return;
                }
                
                jugadorLocalId = 2;
                controladorRed = new ControladorRed(nombreLocal, puertoBase, 
                                                   partida.getTablero(), jugadorLocalId);
                
                try {
                    System.out.println("\n[CLIENTE] Iniciando servidor local en puerto " + (puertoBase + 1) + "...");
                    
                    // Espera mas larga para garantizar que anfitrion este listo
                    System.out.println("[CLIENTE] Esperando 5 segundos para sincronizacion...");
                    Thread.sleep(5000);
                    
                    System.out.println("[CLIENTE] Conectando a " + oponente.ip + ":" + oponente.puerto + "...");
                    
                    // Reintentos automaticos
                    boolean conectado = false;
                    int intentos = 0;
                    int maxIntentos = 3;
                    
                    while (!conectado && intentos < maxIntentos) {
                        intentos++;
                        System.out.println("[CLIENTE] Intento " + intentos + "/" + maxIntentos + "...");
                        
                        conectado = controladorRed.unirseAPartida(oponente.ip, oponente.puerto);
                        
                        if (!conectado && intentos < maxIntentos) {
                            System.out.println("[CLIENTE] Reintentando en 2 segundos...");
                            Thread.sleep(2000);
                        }
                    }
                    
                    if (!conectado) {
                        System.out.println("\nERROR: No se pudo conectar despues de " + maxIntentos + " intentos");
                        System.out.println("Posibles causas:");
                        System.out.println("  - Firewall bloqueando conexion");
                        System.out.println("  - El anfitrion cerro el programa");
                        System.out.println("  - Problema de red");
                        scanner.close();
                        return;
                    }
                    
                    System.out.println("[CLIENTE] Conectado exitosamente!");
                    
                    String nombreJugador1 = controladorRed.esperarNombreOponente();
                    
                    if (nombreJugador1 == null || nombreJugador1.isEmpty()) {
                        System.out.println("\nERROR: Error recibiendo datos del anfitrion");
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
                    System.out.println("[CLIENTE] Partida iniciada por el anfitrion!");
                    
                } catch (Exception e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    e.printStackTrace();
                    if (controladorRed != null) {
                        controladorRed.cerrar();
                    }
                    scanner.close();
                    return;
                }
            }
            
        } else {
            // Modo local sin cambios
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
        System.out.println("            PARTIDA INICIADA");
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