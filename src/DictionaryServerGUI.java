import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class DictionaryServerGUI extends JFrame implements ActionListener {
    private static ServerSocket serverSocket;
    private int port;
    private String dictionaryFile;
    private static JSONObject jsonObject = null;
    private JLabel headerLabel;
    private JTextArea logDisplay;
    private JPanel btnPanel;
    private JButton exitBtn;

    public DictionaryServerGUI(int port, String dictionaryFile) {
        super("Dictionary Server");
        setLayout(new GridLayout(3, 1));

        headerLabel = new JLabel("Server log");
        add(headerLabel);

        logDisplay = new JTextArea(10, 15);
        add(logDisplay);

        btnPanel = new JPanel();
        exitBtn = new JButton("Exit");
        exitBtn.addActionListener(this);
        btnPanel.add(exitBtn);
        add(btnPanel);

        this.setSize(550, 300);
        this.setVisible(true);
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent event) {
                        //Check whether a socket is open…
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException ioEx) {
                                logDisplay.setText(logDisplay.getText() + "\nUnable to close socket!\n");
                                System.exit(1);
                            }
                        }
                        System.exit(0);
                    }
                }
        );

        this.port = port;
        this.dictionaryFile = dictionaryFile;

        try {
            serverSocket = new ServerSocket(port);
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(dictionaryFile));
        } catch (IOException ioEx) {
            logDisplay.setText(logDisplay.getText() + "\nUnable to set up port!");
            System.exit(1);
        } catch (ParseException paEx) {
            logDisplay.setText(logDisplay.getText() + "\nUnable to parse json file!");
        }

        try {
            do {
                Socket client = serverSocket.accept();
                logDisplay.setText(logDisplay.getText() + "\nNew client accepted.\n");
                ClientHandler handler = new ClientHandler(client, jsonObject);
                handler.start();
            } while (true);
        } catch (IOException ioex) {
            logDisplay.setText(logDisplay.getText() + "IO Exception has occurred!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }

    public static void main(String[] args) throws IOException {
        var enteredPort = Integer.parseInt(args[0]);
        var enteredDictionaryFile = args[1];
        var gui = new DictionaryServerGUI(enteredPort, enteredDictionaryFile);
    }

    class ClientHandler extends Thread {
        private Socket client;
        private final JSONObject jsonObject;
        private Scanner input;
        private PrintWriter output;

        public ClientHandler(Socket client, JSONObject jsonObject) {
            this.client = client;
            this.jsonObject = jsonObject;

            try {
                input = new Scanner(client.getInputStream());
                output = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }

        public void run() {
            String received;
            do {
                received = input.nextLine();
                String[] userInput = received.split(",");

                switch (userInput[0]) {
                    case "query": {
                        if (userInput.length < 2) {
                            output.println("Nothing entered");
                            break;
                        }
                        output.println(queryDictionary(userInput[1]));
                        break;
                    }
                    case "add": {
                        if (userInput.length < 2) {
                            output.println("Nothing entered");
                            break;
                        }
                        if (userInput.length < 3 || userInput[2].equals(" ")) {
                            output.println("No definition provided");
                            break;
                        }
                        output.println(addToDictionary(userInput[1], userInput[2]));
                        break;
                    }
                    case "remove": {
                        if (userInput.length < 2) {
                            output.println("Nothing entered");
                            break;
                        }
                        output.println(removeFromDictionary(userInput[1]));
                        break;
                    }
                }
            } while (!received.equals("QUIT"));
            try {
                if (client != null) {
                    logDisplay.setText(logDisplay.getText() + "Closing down connection…");
                    client.close();
                }
            } catch (IOException ioEx) {
                logDisplay.setText( logDisplay.getText() + "Unable to disconnect!");
            }
        }

        public String queryDictionary(String query) {
            var result = jsonObject.get(query);
            if (result == null)
                return "No definition found for " + query;

            return result.toString();
        }

        public synchronized String addToDictionary(String term, String definition) {
            if (jsonObject.containsKey(term))
                return "Term " + term + " is already in the dictionary";

            jsonObject.put(term, definition);
            saveDictionary();
            return "Term " + term + " added to dictionary";
        }

        public synchronized String removeFromDictionary(String term) {
            if (jsonObject.containsKey(term)) {
                jsonObject.remove(term);
                saveDictionary();
                return "Term " + term + " removed from dictionary";
            }
            return "Term was not found in the dictionary";
        }

        public void saveDictionary() {
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(dictionaryFile);
                printWriter.write(jsonObject.toJSONString());
                printWriter.flush();
                printWriter.close();
            } catch (FileNotFoundException fex) {
                logDisplay.setText( logDisplay.getText() + "File " + dictionaryFile + " not found!");
            }
        }
    }
}
