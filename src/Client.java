import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {

    public String name;

    private Server server;
    private Socket socket;
    private FileManager fileManager;

    private DataOutputStream dos;
    private DataInputStream dis;
    private OutputStream os;
    private InputStream is;

    private int port;
    private boolean isRunning, isForServer;

    private String address, message;
    private String[] tempMsg;
    private byte[] file;

    public Client(String name, String address, int port, FileManager fileManagerm) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.fileManager = fileManagerm;
        isRunning = false;
        isForServer = false;
    }

    public Client(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        isForServer = true;
    }

    public void run() {
        if (!isForServer) {
            // init
            try {
                if ("l".equals(address)) {
                    socket = new Socket(InetAddress.getLocalHost(), port);
                } else {
                    socket = new Socket(address, port);
                }
                if (!fileManager.isServer) fileManager.updateStatus();
                fileManager.print("[Client] connected to server");
            } catch (Exception e) {
                fileManager.print("[Client] running problem | " + e.getMessage());
            }
        } else {
            fileManager = server.getFileManager();
        }

        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();
        } catch (Exception e) {
            fileManager.print("[Client] I/O problem | " + e.getMessage());

        }
        dos = new DataOutputStream(os);
        dis = new DataInputStream(is);

        isRunning = true;

        while (isRunning) {

            try {
                message = dis.readUTF();
                tempMsg = message.split("\\|");

                file = new byte[Integer.parseInt(tempMsg[1])];
                dis.read(file);

                if (file.length > 0) {
                    if (isForServer) {
                        server.sendMessage(message, this);
                        server.sendFile(file, this);
                    } else {
                        fileManager.receiveFile(tempMsg[0], file);
                    }
                }
            } catch (Exception e) {

                fileManager.print("[Client] receiving file/message problem | " + e.getMessage() + " | " + e.getStackTrace());
                fileManager.print("[Client] no server found | " + e.getMessage());

                if (isForServer) {
                    server.cs.remove(this);
                } else {
                    fileManager.closeConnection();
                }

                isRunning = false;
            }
        }
    }

    public void sendFile(byte[] file) {
        try {
            dos.write(file);
        } catch (Exception e) {
            fileManager.print("[Client] sending file problem | " + e.getMessage() + " | " + e.getStackTrace());
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (Exception e) {
            fileManager.print("[Client] sending message problem | " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void stopClient() {
        isRunning = false;
    }
}
