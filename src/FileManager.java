import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class FileManager extends JFrame {

    public Client client;
    public final boolean isServer;

    private int w = 600, h = 400;

    private JFrame f, cf;
    private JList logList;
    private DefaultListModel<String> logs;
    private Container cc;
    private JPanel listPanel;
    private JLabel connectingLabel;
    private FileDrop fd;
    private ImageIcon netIcon;
    private String name, address;
    private byte[] tempFile;
    private String[] fileInfo;

    public FileManager(final String name, String address, final boolean isServer) {

        this.f = this;
        this.name = name;
        this.address = address;
        this.isServer = isServer;

        loadImages();

        fileInfo = new String[2];

        cc = getContentPane();
        cc.setLayout(new GridLayout(2, 1));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (isServer) {
            client = new Client(name, "l", 9999, this);
            client.start();
        }


        // list panel
        listPanel = new JPanel();
        listPanel.setLayout(null);

        JLabel netLabel = new JLabel(netIcon);
        netLabel.setLocation((w - 64) / 2, 32);
        netLabel.setSize(64, 64);

        connectingLabel = new JLabel("Connecting");
        connectingLabel.setLocation((w - 75) / 2, 105 / 2);
        connectingLabel.setSize(80, 105);

        listPanel.add(connectingLabel);
        listPanel.add(netLabel);
        cc.add(listPanel, BorderLayout.NORTH);

        // drop file panel
        JPanel dfPanel = new JPanel();
        dfPanel.setLayout(null);
        JLabel dfLabel = new JLabel("Drop Files Here");
        dfLabel.setFont(new Font("Arial", Font.BOLD, 15));
        dfPanel.add(dfLabel);
        cc.add(dfPanel);


        // log frame
        cf = new JFrame();
        cf.setLayout(new BorderLayout());

        final JCheckBox debugCheck = new JCheckBox("Debug ", false);
        debugCheck.setSelected(true);
        cf.add(debugCheck, BorderLayout.NORTH);

        logs = new DefaultListModel<>();
        logList = new JList(logs);
        JScrollPane scrollPane = new JScrollPane(logList);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setAutoscrolls(true);

        cf.setTitle("Logs");
        cf.add(scrollPane);
        cf.setSize(300, h);
        cf.setResizable(false);
        cf.setUndecorated(true);

        debugCheck.addActionListener(e -> {
            scrollPane.setVisible(debugCheck.isSelected());

            if (debugCheck.isSelected()) {
                cf.setSize(300, h);
            } else {
                cf.setSize(300, 30);
            }
        });

        cf.setVisible(true);

        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                cf.setLocation(getX() - 303, getY());
            }
        });
        cf.setLocation(f.getX() - 303, f.getY());

        // main frame config
        setTitle(name);
        setSize(w, h);
        setResizable(false);
        setLocation((screenSize.width - w) / 2, (screenSize.height - h) / 2);
        setVisible(true);

        // on file drop listener
        fd = new FileDrop(dfPanel, dfLabel, w, dfPanel.getHeight(), files -> {
            fileInfo[0] = files[0].getName();
            fileInfo[1] = Long.toString(files[0].length());
            client.sendMessage(fileInfo[0] + "|" + fileInfo[1]);

            try {
                tempFile = Files.readAllBytes(files[0].toPath());
            } catch (IOException e) {
                print("[FileManager] problem in reading file | " + e.getMessage());
            }
            print("[Read IO] finished reading");
            client.sendFile(tempFile);
        });


        // on close listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (!isServer) {
                    if (client != null) client.sendMessage(client.name + "|-> Disconnected | :close: |");
                }
                System.exit(0);
            }
        });
    }

    public void connect() {
        client = new Client(name, address, 9999, this);
        client.start();
    }

    public void receiveFile(String path, byte[] file) {
        try {
            Files.write(new File(path).toPath(), file);
        } catch (IOException e) {
            print("[FileManager] problem in writing file | " + e.getMessage());
        }
        print("[Write IO] finished writing");
    }

    public void updateStatus() {
        fd.setEnable(true);
        connectingLabel.setText("Connected");
        connectingLabel.setForeground(new Color(138, 180, 223, 255));
        connectingLabel.setLocation((w - 70) / 2, 105 / 2);
        connectingLabel.setSize(80, 105);
    }

    private void loadImages() {
        URL url = Main.class.getResource("/img/net.png");
        netIcon = new ImageIcon(url);
    }

    public void closeConnection() {
        print("[File Manager] Server is down. file manager has been terminated.");
    }

    public void print(String output) {
        logs.add(logs.size() > 0 ? logs.size() - 1 : 0, output);
        logList.repaint();
    }
}
