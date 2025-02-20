package com.plugin.widgetfloat;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private static final String TAG = "SocketManager";

    private static final String SOCKET_URL = "https://mobilidade-api-node-uby.prod.704apps.com.br?driverId=35&latitude=-3.722926557567814&longitude=-38.53897610560655";

    private SocketManager() {
        try {
            socket = IO.socket(SOCKET_URL);
            Log.e(TAG, "Socket criado com sucesso");
        } catch (URISyntaxException e) {
            Log.e(TAG, "Erro ao criar o socket: " + e.getMessage());
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d(TAG, "Socket conectado");
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Log.d(TAG, "Socket desconectado");
        }
    }

    public void emit(String event, JSONObject data) {
        if (socket != null && socket.connected()) {
            socket.emit(event, data);
            Log.d(TAG, "Evento emitido: " + event + " com dados: " + data.toString());
        } else {
            Log.e(TAG, "Socket não está conectado. Impossível emitir evento.");
        }
    }

    public void on(String event, Emitter.Listener listener) {
        if (socket != null) {
            socket.on(event, listener);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
