package Server.ServerEngine;

import Server.ServerSettings;
import library.Library;
import network.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

public class ServerEngine implements ServerSocketThreadListener, SocketThreadListener {

    ServerListener listener;
    private static final Logger logger = LogManager.getLogger(ServerEngine.class);
    private ServerSocketThread server;
    private final Vector<SocketThread> clients = new Vector<>();
    private final List<String> badWords = new ArrayList<>();


    public ServerEngine(ServerListener listener) {
        this.listener = listener;
    }


    public void start(int port) {
        if (server == null || !server.isAlive())
            server = new ServerSocketThread(this, "Server", port, 2000);
        else
            logger.info("Server already started");
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            logger.error("Server is not running");
        } else {
            server.interrupt();
        }
    }

    /**
     * Server Socket Thread Listener Methods
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        logger.info("Server started");
        SqlClient.connect();
        init_censorship();
    }

    private void init_censorship() {

        try (BufferedReader br = new BufferedReader(new FileReader(ServerSettings.CENSORSHIP_FILE))) {
            while (br.ready()) {
                badWords.add(br.readLine());
            }
            logger.info("init_censorship - OK");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private String apply_censorship(String message) {
        String ret = null;
        for (String badWord : badWords) {
            if (message.contains(badWord)) {
                char[] replacement = new char[badWord.length()];
                Arrays.fill(replacement, '#');
                ret = message.replace(badWord, new String(replacement));
            }
        }
        return ret == null ? message : ret;
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        logger.info("Server stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    @Override
    public void onServerCreated(ServerSocketThread thread, ServerSocket server) {
        logger.info("Server created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
        logger.error("Server timeout");
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        logger.info("Client connected");
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
        logger.info("Client connected");
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
        logger.info("Client is ready to chat");
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
                init_history(newClient, login);
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
            } else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }

        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private static void init_history(ClientThread client, String login) {
        try (BufferedReader br = new BufferedReader(new FileReader(ServerSettings.LOG_PATH + client.getNickname() + ".txt"))) {
            while (br.ready()) {
                client.sendMessage(br.readLine());
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        String message = apply_censorship(arr[1]);
        switch (msgType) {
            case Library.CLIENT_MSG_BROADCAST:
                sendBroadcast(client, message);
                break;
            case Library.NICKNAME_CHANGE:
                SqlClient.changeNickName(message, arr[2]);
                client.setNickname(arr[2]);
                sendToAllAuthorizedClients(Library.getUserList(getUsers()));
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void sendBroadcast(ClientThread client, String text) {
        String message = Library.getTypeBroadcast(client.getNickname(), text);
        sendToAllAuthorizedClients(message);
        message += "\n";
        try (FileOutputStream fis = new FileOutputStream(ServerSettings.LOG_PATH + client.getNickname() + ".txt", true)){
            fis.write(message.getBytes());
        } catch (Exception e) {
            logger.error(e);
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
