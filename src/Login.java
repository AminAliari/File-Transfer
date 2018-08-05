import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {

    private boolean isServer;

    public Login(final int width, final int height) {

        final JLabel nameLabel = new JLabel("Name");
        final JLabel serverLabel = new JLabel("Host IP");
        serverLabel.setToolTipText("example : 192.168.1.1");
        serverLabel.setVisible(false);

        final JTextField nameText = new JTextField();

        final JTextField serverText = new JTextField();
        serverText.setToolTipText("example : 192.168.1.1");
        serverText.setVisible(false);

        final JCheckBox serverCheck = new JCheckBox("Server ", false);
        serverCheck.setSelected(true);
        isServer = true;

        serverCheck.addActionListener(e -> {

            isServer = serverCheck.isSelected();
            serverLabel.setVisible(!isServer);
            serverText.setVisible(!isServer);

            if (!isServer) {
                setSize(width, height + 30);
            } else {
                setSize(width, height);
            }
        });

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            if (isServer) {
                if (nameText.getText().isEmpty()) {
                    nameText.setText("[" + System.getProperty("user.name") + "] [Server]");
                }

                Server server = new Server(9999);
                FileManager fileManager = new FileManager(nameText.getText(), "l", isServer);
                server.setFileManager(fileManager);
                server.start();
                fileManager.connect();

                setVisible(false);


            } else {
                if (nameText.getText().isEmpty()) {
                    nameText.setText("[" + System.getProperty("user.name") + "] [Client]");
                }

                if (!serverText.getText().isEmpty()) {
                    int c = 0;
                    for (char t : serverText.getText().toCharArray()) {
                        if (t == '.') {
                            c++;
                        }
                    }
                    if (c == 3) {
                        new FileManager(nameText.getText(), serverText.getText(), isServer).connect();
                        setVisible(false);
                    } else {
                        showError("invalid ip address");
                    }
                } else {
                    new FileManager(nameText.getText(), "127.0.0.1", isServer).connect();
                    setVisible(false);
                }
            }
        });

        Container cc = getContentPane();

        JPanel c = new JPanel(new BorderLayout());
        JPanel pUp = new JPanel(new BorderLayout());
        JPanel pDown = new JPanel(new BorderLayout());

        pUp.add(nameLabel, BorderLayout.WEST);
        pUp.add(nameText);
        pDown.add(serverCheck, BorderLayout.NORTH);
        pDown.add(serverLabel, BorderLayout.WEST);
        pDown.add(serverText);

        c.add(pUp, BorderLayout.NORTH);
        c.add(pDown);
        c.add(loginButton, BorderLayout.SOUTH);

        c.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
        cc.add(c);

        setSize(width, height);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void showError(String error) {
        JOptionPane.showMessageDialog(this, error);
    }
}
