// package com.plugin.widgetfloat;

// import io.socket.client.IO;
// import io.socket.client.Socket;
// import io.socket.emitter.Emitter;
// import org.json.JSONObject;
// import java.net.URISyntaxException;
// import android.util.Log;

// public class SocketManager {
//     private static SocketManager instance;
//     private Socket mSocket;
//     private static final String SOCKET_SERVER_URL = "https://mobilidade-api-node-hml704apps.prod.704apps.com.br/";
//     private static final String TAG = "SocketManager";

//     private SocketManager() {
//         try {
//             IO.Options options = new IO.Options();
//             options.reconnection = true;  // Ativa reconexão automática
//             options.reconnectionAttempts = 5;  // Tentará reconectar 5 vezes
//             options.reconnectionDelay = 2000;  // 2 segundos entre tentativas
//             options.forceNew = true;

//             mSocket = IO.socket(SOCKET_SERVER_URL, options);
//             mSocket.connect();

//             // Adiciona um listener para a conexão
//             mSocket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "[Socket.io] Conectado"));

//             // Listener para desconexão
//             mSocket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "[Socket.io] Desconectado"));
//         } catch (URISyntaxException e) {
//             Log.e(TAG, "Erro ao inicializar o socket: " + e.getMessage());
//         }
//     }

//     public static synchronized SocketManager getInstance() {
//         if (instance == null) {
//             instance = new SocketManager();
//         }
//         return instance;
//     }

//     /**
//      * Emite um evento para o servidor com um objeto JSON.
//      * @param event Nome do evento.
//      * @param data Objeto JSON a ser enviado.
//      */
//     public void emitEvent(String event, JSONObject data) {
//         if (mSocket != null && mSocket.connected()) {
//             mSocket.emit(event, data);
//             Log.d(TAG, "Emitindo evento: " + event);
//         } else {
//             Log.e(TAG, "Socket não está conectado.");
//         }
//     }

//     /**
//      * Registra um listener para um evento específico.
//      * @param event Nome do evento.
//      * @param listener Callback para o evento.
//      */
//     public void onEvent(String event, Emitter.Listener listener) {
//         if (mSocket != null) {
//             mSocket.on(event, listener);
//             Log.d(TAG, "Listener registrado para o evento: " + event);
//         }
//     }

//     /**
//      * Remove um listener de um evento específico.
//      * @param event Nome do evento.
//      */
//     public void offEvent(String event) {
//         if (mSocket != null) {
//             mSocket.off(event);
//             Log.d(TAG, "Listener removido para o evento: " + event);
//         }
//     }

//     /**
//      * Verifica se o socket está conectado.
//      * @return true se estiver conectado, false caso contrário.
//      */
//     public boolean isConnected() {
//         return mSocket != null && mSocket.connected();
//     }

//     /**
//      * Desconecta o socket e remove todos os listeners.
//      */
//     public void disconnect() {
//         if (mSocket != null) {
//             mSocket.disconnect();
//             mSocket.off();
//             Log.d(TAG, "[Socket.io] Desconectado e todos os eventos removidos.");
//         }
//     }
// }
