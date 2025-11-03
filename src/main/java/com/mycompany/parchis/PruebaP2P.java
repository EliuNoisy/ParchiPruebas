package com.mycompany.parchis;

import java.util.Scanner;
import red.ConexionPeer;
import red.EscuchaRed;
import red.MensajeJuego;
import red.P2PNetworkManager;

/**
 * Clase de prueba para el sistema P2P
 * Permite probar la conexion entre peers antes de integrar con el juego
 */
public class PruebaP2P {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== PRUEBA DE RED P2P PARA PARCHIS ===");
        System.out.print("Ingresa tu nombre: ");
        String nombre = scanner.nextLine();
        
        System.out.print("Ingresa el puerto para este peer (ej: 5000): ");
        int puerto = scanner.nextInt();
        scanner.nextLine(); // Consumir salto de línea
        
        P2PNetworkManager red = new P2PNetworkManager(nombre, puerto);
        
        // Configurar escucha
        red.setEscuchaRed(new EscuchaRed() {
            @Override
            public void alRecibirMensaje(MensajeJuego mensaje, ConexionPeer desde) {
                if (mensaje.getTipo() == MensajeJuego.TipoMensaje.CHAT) {
                    System.out.println("\n[" + mensaje.getEmisor() + "]: " + 
                                     mensaje.getContenido());
                    System.out.print("> ");
                } else {
                    System.out.println("\nMensaje recibido: " + mensaje);
                    System.out.print("> ");
                }
            }
            
            @Override
            public void alDesconectarPeer(ConexionPeer peer) {
                System.out.println("\n*** " + peer.getNombrePeer() + 
                                 " se ha desconectado ***");
                System.out.print("> ");
            }
        });
        
        // Iniciar servidor
        try {
            red.iniciarServidor();
            System.out.println("Servidor iniciado en puerto " + puerto);
        } catch (Exception e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
            return;
        }
        
        // Menú de opciones
        boolean ejecutando = true;
        while (ejecutando) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Conectar a otro peer");
            System.out.println("2. Enviar mensaje a todos");
            System.out.println("3. Enviar movimiento de prueba");
            System.out.println("4. Ver peers conectados");
            System.out.println("5. Salir");
            System.out.print("Opcion: ");
            
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir salto de línea
            
            switch (opcion) {
                case 1:
                    System.out.print("IP del peer (localhost para local): ");
                    String ip = scanner.nextLine();
                    System.out.print("Puerto del peer: ");
                    int puertoPeer = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (red.conectarAPeer(ip, puertoPeer)) {
                        System.out.println("¡Conectado exitosamente!");
                    } else {
                        System.out.println("Error al conectar");
                    }
                    break;
                    
                case 2:
                    System.out.print("Mensaje: ");
                    String mensaje = scanner.nextLine();
                    red.enviarMensajeChat(mensaje);
                    break;
                    
                case 3:
                    System.out.println("Enviando movimiento de prueba...");
                    red.enviarMovimiento(1, 2, 6);
                    System.out.println("Movimiento enviado: Jugador 1, Ficha 2, Dado 6");
                    break;
                    
                case 4:
                    System.out.println("Peers conectados: " + 
                                     red.getPeersConectados().size());
                    for (ConexionPeer peer : red.getPeersConectados()) {
                        System.out.println("  - " + peer.getNombrePeer() + 
                                         " (" + peer.getDireccion() + ")");
                    }
                    break;
                    
                case 5:
                    ejecutando = false;
                    break;
                    
                default:
                    System.out.println("Opcion invalida");
            }
        }
        
        System.out.println("Cerrando conexiones...");
        red.cerrar();
        scanner.close();
        System.out.println("¡Adios!");
    }
}