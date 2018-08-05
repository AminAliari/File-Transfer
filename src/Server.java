import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class Server extends Thread {

    public Stack<Client> cs;

    private ServerSocket server;
    private Socket socket;

    private int port;
    private boolean isRun = false;

    private FileManager fileManager;

    public Server(int port) {
        this.port = port;
        cs = new Stack();
    }

    public void run() {

        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            fileManager.print("[Server] problem in initiating | " + e.getMessage());
        }
        isRun = true;

        while (isRun) {
            try {
                socket = server.accept();
                cs.push(new Client(this, socket));
                cs.peek().start();
                fileManager.print("[Server] Client : " + cs.peek().getSocket().getInetAddress() + " Connected.");
                if (cs.size() > 2) fileManager.updateStatus();

            } catch (Exception e) {
                fileManager.print("[Server] problem in listener | " + e.getMessage());
            }
        }
    }

    public void sendFile(byte[] file, Client s) {

        for (Client c : cs) {
            if (c.equals(s)) continue;
            c.sendFile(file);
        }
    }

    public void sendMessage(String message, Client s) {

        if (message.split("\\|").length > 2) {
            cs.remove(s);
            return;
        }

        for (Client c : cs) {
            if (c.equals(s)) continue;
            c.sendMessage(message);
        }
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean isRunning() {
        return isRun;
    }

    public void stopServer() {
        isRun = false;
    }
}
