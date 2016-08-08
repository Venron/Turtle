package de.turtle.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.stream.Stream;

import javax.naming.SizeLimitExceededException;
import javax.swing.text.AttributeSet.CharacterAttribute;

import de.turtle.client.TurtleClient;
import de.turtle.imp.models.Connection;
import de.turtle.imp.models.Role;
import de.turtle.imp.models.TransferPacket;
import de.turtle.imp.models.User;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * To Do: - ListView rechts mit allen Usern im Chatraum
 */

public class ClientChat {
	private static final int SERV_PORT = 55021;
	private static final int AUTH_PORT = 55030;
	private static final String CON_SUCCESS = "CON_EST";
	private static final String CON_FAILURE = "CON_FAIL";
	private static ObjectOutputStream authOut = null;
	private static ObjectInputStream authIn = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static User sendUser;

	private static Stage window;
	private static Stage loginWindow;
	static TextArea chatTextarea;
	TextField inputTextfield;
	TextField userTextfield;
	PasswordField passwordTextfield;
	Text usernameErrorText;
	Text passwordErrorText;

	Socket socket = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;

	private static Connection connection;
	private static String conName;
	private static String hostName;
	private static int port;
	private static String username;
	private static String password;
	boolean authAnswer;

	public ClientChat() {

	}

	public ClientChat(Connection connection, String conName, String hostName, int port) {
		this.connection = connection;
		this.conName = conName;
		this.hostName = hostName;
		this.port = port;
	}

	public void start() {
		// buildWindow();
		window = new Stage();
		window.setTitle(conName);

		BorderPane border = new BorderPane();
		border.setTop(getMenuBar());
		// border.setCenter(chatWindow());

		chatTextarea = new TextArea();
		chatTextarea.setEditable(false);
		chatTextarea.setWrapText(true);
		chatTextarea.setMaxWidth(Double.MAX_VALUE);
		chatTextarea.setMaxHeight(Double.MAX_VALUE);
		chatTextarea.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

		chatTextarea.appendText(getTimestamp() + "Waiting for credentials..." + "\n");

		border.setCenter(chatTextarea);

		inputTextfield = new TextField();
		inputTextfield.setPrefWidth(465);
		inputTextfield.setMaxHeight(Double.MAX_VALUE);
		inputTextfield.setMaxWidth(Double.MAX_VALUE);
		/**
		 * !!!!
		 */
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				inputTextfield.requestFocus();
			}
		});

		Button sendButton = new Button("Send");
		sendButton.setOnAction(e -> sendMessage());
		sendButton.setMinWidth(85);
		sendButton.setMaxWidth(85);
		sendButton.setPrefWidth(85);
		sendButton.setMaxHeight(Double.MAX_VALUE);

		installEventHandlerSendMessage(inputTextfield);

		GridPane grid = new GridPane();
		grid.add(inputTextfield, 0, 0);
		grid.add(sendButton, 1, 0);

		border.setBottom(grid);

		/**
		 * Popup login dialog*
		 */
		boolean answerLoginDialog = loginDialog();
		if (answerLoginDialog == false) {
			AlertPopup_Small.display("Could not log in", "Invalid credentials provided");
			return;
		} else {
			printTo("Authentification successful");
		}
		/**
		 * Test and establish the connection
		 */
		printTo("Establishing connection...");

		try {
			socket = new Socket(connection.getHost(), connection.getPort());
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			/**
			 * Send our username
			 */
			out.writeObject(username);

			/**
			 * Test connection with testPacket
			 */

			TransferPacket testCon = new TransferPacket(TransferPacket.TEST, ".", sendUser);
			out.writeObject(testCon);

			TransferPacket testConResp = (TransferPacket) in.readObject();
			if (!testConResp.getMessage().equals(CON_SUCCESS) || testConResp.getMessage().equals(CON_FAILURE)) {
				printTo("Connection was not tested successfully. Connection could not be established");
				return;
			}
			printTo("Connection established");
			printTo("You can now chat with others");

			/**
			 * Keep socket open, else the server won't find a connection to read
			 * from
			 */
			// listenerService();
		} catch (IOException e) {
			printTo("The server is not reachable. Please try again later.");
		} catch (ClassNotFoundException e) {
			printTo("ClassNotFoundException at testAndEstablishConnection()");
			System.out.println(getTimestamp() + "ChatClient.main() [ClassNotFoundException]");
			e.printStackTrace();
		}
		Scene scene = new Scene(border, 550, 600);
		scene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		window.getIcons().add(new Image("file:icon.png"));
		window.setScene(scene);
		window.show();
	}

	/**
	 * !!!
	 */
	private void installEventHandlerSendMessage(final Node keyNode) {
		final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					sendMessage();
					event.consume();
				}
			}
		};
		keyNode.setOnKeyPressed(keyEventHandler);
	}

	/**
	 * !!!
	 */
	private void installEventHandlerLogin(final Node keyNode) {
		final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					authAnswer = sendAuthentication();
					event.consume();
				}
			}
		};
		keyNode.setOnKeyPressed(keyEventHandler);
	}

	private void sendMessage() {
		try {
			String msg = inputTextfield.getText().trim();
			inputTextfield.clear();
			String msg_subset = "";
			if (msg.length() >= 2) {
				msg_subset = msg.substring(0, 2);
			} else {
				msg_subset = msg;
			}
			if (msg_subset.equals("--")) {
				if (msg.equals("--quit")) {
					out.writeObject(
							new TransferPacket(TransferPacket.DISCONNECT, "#", new User(0, username, new Role())));
					disconnect();
					window.close();
				} else if (msg.equals("--showall")) {
					out.writeObject(new TransferPacket(TransferPacket.SHOWALL, "#", new User(0, username, new Role())));
					TransferPacket clientNames = (TransferPacket) in.readObject();
					printTo("Connected users: " + clientNames.getMessage());
				} else if (msg.equals("--help")) {
					// HELP
					displayHelp();
				} else {
					printTo("Unknown command. Type --help for all help.");
				}
			} else {
				out.writeObject(new TransferPacket(TransferPacket.MESSAGE, msg, new User(0, username, new Role())));
				TransferPacket tp = (TransferPacket) in.readObject();
				printTo("<" + tp.getSender().getUserName() + "> " + tp.getMessage());
			}
		} catch (IOException e) {
			System.out.println("The server is not reachable. Please try again later.");
		} catch (ClassNotFoundException e) {
			System.out.println(getTimestamp() + "ChatClient.main() [ClassNotFoundException]");
			e.printStackTrace();
		}
	}

	private void disconnect() {
		try {
			if (socket != null) {
				socket.close();
			}
			if (out != null) {
				out.close();
			}

			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			printTo("Error at closing socket, in or out");
		}
	}

	private static void displayHelp() {
		printTo("--quit: Quit");
		printTo("--showall: Display all connected users");
	}

	private MenuBar getMenuBar() {
		Menu startMenu = new Menu("_Start");
		MenuItem exitTurtle = new MenuItem("Exit");
		exitTurtle.setOnAction(e -> window.close());
		startMenu.getItems().add(exitTurtle);
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(startMenu);
		return menuBar;
	}

	private boolean loginDialog() {
		loginWindow = new Stage();
		loginWindow.initModality(Modality.APPLICATION_MODAL);
		loginWindow.setTitle("Enter credentials");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Text loginText = new Text("Please enter your credentials");
		loginText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 20));

		Label userNameLabel = new Label("Username: ");
		grid.add(userNameLabel, 0, 1);

		userTextfield = new TextField();
		grid.add(userTextfield, 1, 1);

		usernameErrorText = new Text();
		usernameErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		usernameErrorText.setFill(Color.FIREBRICK);
		grid.add(usernameErrorText, 2, 1);

		Label passwordLabel = new Label("Password: ");
		grid.add(passwordLabel, 0, 2);

		passwordTextfield = new PasswordField();
		grid.add(passwordTextfield, 1, 2);
		installEventHandlerLogin(passwordTextfield);

		passwordErrorText = new Text();
		passwordErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		passwordErrorText.setFill(Color.FIREBRICK);
		grid.add(passwordErrorText, 2, 2);

		Button signInBtn = new Button("Send");
		signInBtn.setOnAction(e -> {
			authAnswer = sendAuthentication();
		});

		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(0, 0, 15, 0));
		hbox.setAlignment(Pos.BOTTOM_RIGHT);
		hbox.getChildren().add(signInBtn);

		grid.add(hbox, 1, 4);
		grid.setAlignment(Pos.CENTER_LEFT);

		Scene scene = new Scene(grid, 405, 145);
		loginWindow.setScene(scene);
		scene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		loginWindow.getIcons().add(new Image("file:icon.png"));
		loginWindow.showAndWait();
		// loginWindow.show();
		return authAnswer;
	}

	/**
	 * To Do: Hash (and encrypt) password
	 */
	private boolean sendAuthentication() {
		boolean answer = false;
		this.usernameErrorText.setText("");
		this.passwordErrorText.setText("");
		this.username = userTextfield.getText().trim();
		this.password = passwordTextfield.getText().trim();

		if (username.equals("") || username.trim().isEmpty()) {
			this.usernameErrorText.setText("Please enter a username.");
			if (password.equals("") || password.trim().isEmpty()) {
				this.passwordErrorText.setText("Please enter a password.");
				return false;
			}
			answer = false;
		}

		if (password.equals("") || password.trim().isEmpty()) {
			this.passwordErrorText.setText("Please enter a password.");
			answer = false;
		}

		/**
		 * Establish the connection to the authentication server
		 */

		try (Socket authSocket = new Socket(connection.getHost(), AUTH_PORT);) {
			sendUser = new User(0, username, new Role());
			TransferPacket helloPacket = new TransferPacket(TransferPacket.AUTH, password, sendUser);
			authOut = new ObjectOutputStream(authSocket.getOutputStream());
			authIn = new ObjectInputStream(authSocket.getInputStream());
			authOut.writeObject(helloPacket);

			/**
			 * Await the authentification response from the server
			 * 
			 */
			try {
				boolean authResponse = false;
				authResponse = (boolean) authIn.readObject();
				if (authResponse == false) {
					loginWindow.close();
					answer = false;
				} else {
					// printTo("Authentification successful");
					loginWindow.close();
					answer = true;
				}
			} catch (ClassNotFoundException e) {
				System.out.println(getTimestamp() + "ClientChat.sendAuthentication() [ClassNotFoundException]");
				e.printStackTrace();
			}
		} catch (NumberFormatException e) {
			printTo("NumberFormatException");
			System.out.println(getTimestamp() + "ChatClient.main() [NumberFormatException]");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			printTo("Unknown Host");
			System.out.println(getTimestamp() + "ChatClient.main() [UnknownHostException]");
			e.printStackTrace();
		} catch (IOException e) {
			printTo("The authentication server is not reachable.");
			AlertPopup_Small.display("Server not reachable", "The authentification server is not reachable.");
			System.out.println("The authentication server is not reachable.");
			// System.exit(1);
		}
		return answer;
	}

	/**
	 * Send a test packet to the specified server. If the test packet response
	 * was successful, establish a connection that will be used for further
	 * client communication
	 */
	private void testAndEstablishConnection() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		printTo("Establishing connection...");
		try (Socket socket = new Socket(connection.getHost(), connection.getPort());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			/**
			 * Send our username
			 */
			out.writeObject(username);

			/**
			 * Test connection with testPacket
			 */

			TransferPacket testCon = new TransferPacket(TransferPacket.TEST, ".", sendUser);
			out.writeObject(testCon);

			TransferPacket testConResp = (TransferPacket) in.readObject();
			if (!testConResp.getMessage().equals(CON_SUCCESS) || testConResp.getMessage().equals(CON_FAILURE)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				printTo("Connection was not tested successfully. Connection could not be established");
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				printTo("Connection established.");
			}
			printTo("You can now chat with others");
		} catch (IOException e) {
			printTo("The server is not reachable. Please try again later.");
		} catch (ClassNotFoundException e) {
			printTo("ClassNotFoundException at testAndEstablishConnection()");
			System.out.println(getTimestamp() + "ChatClient.main() [ClassNotFoundException]");
			e.printStackTrace();
		}
		return;
	}

	private static String getTimestamp() {
		return "[" + sdf.format(new Date()) + "] ";
	}

	private static void printTo(String text) {
		chatTextarea.appendText(getTimestamp() + text + "\n");
	}

	private void listenerService() {
		try {
			while (in.available() != 0) {
				TransferPacket tp = (TransferPacket) in.readObject();
				printTo(getTimestamp() + "<" + tp.getSender().getUserName() + "> " + tp.getMessage());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
