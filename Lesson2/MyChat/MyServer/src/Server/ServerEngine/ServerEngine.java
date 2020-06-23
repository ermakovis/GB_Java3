package Server.ServerEngine;

import Server.ServerSettings;
import library.Library;
import network.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ServerEngine implements ServerSocketThreadListener, SocketThreadListener {

    ServerListener listener;
    private Vector<SocketThread> clients = new Vector<>();

    public ServerEngine(ServerListener listener) {
        this.listener = listener;
    }

    private ServerSocketThread server;



    public void start(int port) {
        if (server == null || !server.isAlive())
            server = new ServerSocketThread(this, "Server", port, 2000);
        else
            putLog("Server already started");
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Server is not running");
        } else {
            server.interrupt();
        }
    }

    private void putLog(String msg) {
        msg = ServerSettings.DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    /**
     * Server Socket Thread Listener Methods
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server started");
        SqlClient.connect();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    @Override
    public void onServerCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
        //putLog("Server timeout");
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(name, this, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();

    }

    /**
     * Socket Thread Listener Methods
     * */

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        System.out.println(msg);
        if (!client.isAuthorized()) {
            handleAuthMessage(client, msg);
        } else
            handleNonAuthMessage(client, msg);
    }

    private void handleAuthMessage(ClientThread newClient, String msg) {
        String[] arr = msg.split(Library.DELIMITER);

        // [/auth_request, login, password]
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            newClient.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login);
        if (nickname == null) {
            newClient.authFail();
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            newClient.authAccept(nickname);
            if (oldClient == null) {
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
            } else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }

        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.CLIENT_MSG_BROADCAST:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(
                        client.getNickname(), arr[1]));
                break;
            case Library.NICKNAME_CHANGE:
                SqlClient.changeNickName(arr[1], arr[2]);
                client.setNickname(arr[2]);
                sendToAllAuthorizedClients(Library.getUserList(getUsers()));
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
        thread.close();
    }

    private synchronized String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }
}
