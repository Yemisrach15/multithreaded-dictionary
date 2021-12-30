import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DictionaryClientGUI extends JFrame implements ActionListener {
    private JTextField userInput;
    private JTextArea responseDisplay;
    private JLabel infoLabel;
    private JLabel userInputLabel;
    private JLabel responseDisplayLabel;
    private JPanel buttonPanel;
    private JButton queryBtn;
    private JButton addBtn;
    private JButton removeBtn;
    private static Socket socket = null;
    private Scanner inputStream;
    private PrintWriter outputStream;
    private int serverPort;
    private String serverAddress;

    public DictionaryClientGUI(String serverAddress, int serverPort) {
        super("Dictionary");
        setLayout(new GridLayout(6, 1));

        infoLabel = new JLabel("For query > query\n | To add (separated by a comma) > term,definition\n | To remove > term");
        add(infoLabel);

        userInputLabel = new JLabel("Input: ");
        userInput = new JTextField(15);
        add(userInputLabel);
        add(userInput);

        responseDisplayLabel = new JLabel("Response: ");
        responseDisplay = new JTextArea(10, 15);
        add(responseDisplayLabel);
        add(responseDisplay);

        buttonPanel = new JPanel();
        queryBtn = new JButton("Query");
        queryBtn.addActionListener(this);
        addBtn = new JButton("Add");
        addBtn.addActionListener(this);
        removeBtn = new JButton("Remove");
        removeBtn.addActionListener(this);
        buttonPanel.add(queryBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        add(buttonPanel);

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        try {
            socket = new Socket(serverAddress, serverPort);
            inputStream = new Scanner(socket.getInputStream());
            outputStream = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException ioex) {
            System.out.println("IO exception");
        }
    }

    public static void main(String[] args) {
        var address = args[0];
        var port = Integer.parseInt(args[1]);
        var gui = new DictionaryClientGUI(address, port);
        gui.setSize(550,300);
        gui.setVisible(true);
        gui.addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent event)
                    {
                        //Check whether a socket is open…
                        if (socket != null)
                        {
                            try
                            {
                                socket.close();
                            }
                            catch (IOException ioEx)
                            {
                                System.out.println("\nUnable to close socket!\n");
                                System.exit(1);
                            }
                        }
                        System.exit(0);
                    }
                }
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == queryBtn) {
            outputStream.println("query," + userInput.getText());
        }
        if (e.getSource() == addBtn) {
            outputStream.println("add," + userInput.getText());
        }
        if (e.getSource() == removeBtn) {
            outputStream.println("remove," + userInput.getText());
        }

        String response = inputStream.nextLine();
        responseDisplay.setText(response);
    }
}
