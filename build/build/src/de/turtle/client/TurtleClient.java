package de.turtle.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.tools.FileObject;

import de.turtle.imp.models.Connection;
import de.turtle.modules.*;
import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TurtleClient extends Application {
	public static final int PORT_PARSE_ERROR = 50;
	private static final String INV_HOSTNAME = "INVALID";
	private static final Pattern PATTERN = Pattern
			.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	Stage window;
	Stage newConWindow;
	static TableView<Connection> connTable;
	ObservableList<Connection> conns;
	static TableColumn nameColumn;
	static TableColumn hostColumn;
	static TableColumn portColumn;
	Connection selectedConn;

	/*
	 * New Connection Resources
	 */

	TextField conNameTextfield;
	Text conNameErrorText;
	TextField hostNameTextfield;
	Text hostNameErrorText;
	TextField portTextfield;
	Text portErrorText;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
		window.setTitle("Turtle v0.1 BETA");

		BorderPane border = new BorderPane();
		border.setTop(getMenuBar());
		border.setCenter(getConnectionList());
		border.setRight(getButtonList());

		Scene mainScene = new Scene(border, 600, 550);
		window.setScene(mainScene);
		mainScene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		/**
		 * !!!
		 */
		window.getIcons().add(new Image("file:icon.png"));
		window.show();
	}

	private void newConnection() {
		Stage newConWindow = new Stage();
		newConWindow.initModality(Modality.APPLICATION_MODAL);
		newConWindow.setTitle("New Connection");
		newConWindow.setMinWidth(250);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 25, 10, 10));
		// grid.setGridLinesVisible(true);

		Text newConText = new Text("New Connection");
		newConText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 20));
		grid.add(newConText, 0, 0);

		Label conNameLabel = new Label("Connection Name: ");
		conNameLabel.setAlignment(Pos.CENTER_RIGHT);
		grid.add(conNameLabel, 0, 1);

		conNameTextfield = new TextField();
		grid.add(conNameTextfield, 1, 1);

		conNameErrorText = new Text();
		conNameErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		conNameErrorText.setFill(Color.FIREBRICK);
		grid.add(conNameErrorText, 2, 1);

		Label hostNameLabel = new Label("Hostname: ");
		hostNameLabel.setAlignment(Pos.CENTER_RIGHT);
		grid.add(hostNameLabel, 0, 2);

		hostNameTextfield = new TextField();
		hostNameTextfield.setPromptText("i.e. 85.23.11.8, localhost");
		grid.add(hostNameTextfield, 1, 2);

		hostNameErrorText = new Text();
		hostNameErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		hostNameErrorText.setFill(Color.FIREBRICK);
		grid.add(hostNameErrorText, 2, 2);

		Label portLabel = new Label("Port: ");
		grid.add(portLabel, 0, 3);

		portTextfield = new TextField();
		grid.add(portTextfield, 1, 3);

		portErrorText = new Text();
		portErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		portErrorText.setFill(Color.FIREBRICK);
		grid.add(portErrorText, 2, 3);

		Button createBtn = new Button("Create");
		createBtn.setOnAction(e -> createNewConnAction());
		grid.add(createBtn, 1, 4);

		GridPane.setHalignment(conNameLabel, HPos.LEFT);
		GridPane.setHalignment(hostNameLabel, HPos.LEFT);
		GridPane.setHalignment(portLabel, HPos.LEFT);

		GridPane.setHalignment(conNameTextfield, HPos.RIGHT);
		GridPane.setHalignment(hostNameTextfield, HPos.RIGHT);
		GridPane.setHalignment(portTextfield, HPos.RIGHT);

		GridPane.setHalignment(createBtn, HPos.RIGHT);

		Scene createConScene = new Scene(grid, 450, 200);
		newConWindow.setScene(createConScene);
		createConScene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		newConWindow.getIcons().add(new Image("file:icon.png"));
		newConWindow.showAndWait();
	}

	private void createNewConnAction() {
		conNameErrorText.setText("");
		hostNameErrorText.setText("");
		portErrorText.setText("");
		String conName = conNameTextfield.getText().trim();
		String hostName = hostNameTextfield.getText().trim();
		String portName = portTextfield.getText().trim();
		int port = 0;

		if (hostName.equals("localhost")) {
			hostName = "127.0.0.1";
		} else {
			if (!validateHostName(hostName)) {
				hostNameErrorText.setText("Invalid host name.");
			}
		}

		if (portName.equals("")) {
			port = 55021;
		} else {
			try {
				port = Integer.parseInt(portName);
			} catch (NumberFormatException e) {
				portErrorText.setText("Invalid port.");
				// Exit the function without executing the following statements
				return;
			}
			if (port < 49152) {
				portErrorText.setText("Port > 49152");
				return;
			}

		}

		if (conName == null || conName.equals("")) {
			conName = hostName;
			conNameErrorText.setFill(Color.ORANGE);
			conNameErrorText.setText("Using host name.");
		}

		connTable.getItems().add(new Connection(conName, hostName, port));
		writeToConfigFile(new String("\n" + conName + "@" + hostName + "@" + port + "\n"));
	}

	private static void writeToConfigFile(String newConn) {
		try {
			Files.write(Paths.get("connections.trtl"), newConn.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("Could not write the new connection to the connections file.");
			e.printStackTrace();
			displayFileNotFoundPopup();
		}
	}

	public static boolean validateHostName(final String ip) {
		return PATTERN.matcher(ip).matches();
	}

	private void editConnection() {
		SelectionModel x = connTable.getSelectionModel();
		selectedConn = connTable.getSelectionModel().getSelectedItem();
		selectedConn = (Connection) x.getSelectedItem();
		if (selectedConn == null) {
			displayNoFilePickedPopup();
			return;
		}

		Stage editConWindow = new Stage();
		editConWindow.initModality(Modality.APPLICATION_MODAL);
		editConWindow.setTitle("New Connection");
		editConWindow.setMinWidth(250);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 25, 10, 10));
		// grid.setGridLinesVisible(true);

		Text newConText = new Text("Edit Connection");
		newConText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 20));
		grid.add(newConText, 0, 0);

		Label conNameLabel = new Label("Connection Name: ");
		conNameLabel.setAlignment(Pos.CENTER_RIGHT);
		grid.add(conNameLabel, 0, 1);

		conNameTextfield = new TextField();
		conNameTextfield.setText(selectedConn.getName());
		grid.add(conNameTextfield, 1, 1);

		conNameErrorText = new Text();
		conNameErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		conNameErrorText.setFill(Color.FIREBRICK);
		grid.add(conNameErrorText, 2, 1);

		Label hostNameLabel = new Label("Hostname: ");
		hostNameLabel.setAlignment(Pos.CENTER_RIGHT);
		grid.add(hostNameLabel, 0, 2);

		hostNameTextfield = new TextField();
		hostNameTextfield.setText(selectedConn.getHost());
		hostNameTextfield.setPromptText("i.e. 85.23.11.8, localhost");
		grid.add(hostNameTextfield, 1, 2);

		hostNameErrorText = new Text();
		hostNameErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		hostNameErrorText.setFill(Color.FIREBRICK);
		grid.add(hostNameErrorText, 2, 2);

		Label portLabel = new Label("Port: ");
		grid.add(portLabel, 0, 3);

		String portString = "" + selectedConn.getPort();
		portTextfield = new TextField();
		portTextfield.setText(portString);
		grid.add(portTextfield, 1, 3);

		portErrorText = new Text();
		portErrorText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 10));
		portErrorText.setFill(Color.FIREBRICK);
		grid.add(portErrorText, 2, 3);

		/**
		 * Add a listener to the selected item property, so the values of the
		 * fields of the row will be automatically loaded into the textfields,
		 * where they can be edited
		 */
		connTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				conNameTextfield.setText(newVal.getName());
				hostNameTextfield.setText(newVal.getHost());
				portTextfield.setText(new String("" + newVal.getPort()));
			}
		});

		Button createBtn = new Button("Apply");
		createBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Connection conn = connTable.getSelectionModel().getSelectedItem();
				conn.setName(conNameTextfield.getText().trim());
				conn.setHost(hostNameTextfield.getText().trim());
				conn.setPort(Integer.parseInt(portTextfield.getText().trim()));
				connTable.toFront();
				connTable.refresh();
				// Edit the entry in the file or rewrite the whole file
				rewriteConnectionFile();
			}
		});
		grid.add(createBtn, 1, 4);

		GridPane.setHalignment(conNameLabel, HPos.LEFT);
		GridPane.setHalignment(hostNameLabel, HPos.LEFT);
		GridPane.setHalignment(portLabel, HPos.LEFT);

		GridPane.setHalignment(conNameTextfield, HPos.RIGHT);
		GridPane.setHalignment(hostNameTextfield, HPos.RIGHT);
		GridPane.setHalignment(portTextfield, HPos.RIGHT);

		GridPane.setHalignment(createBtn, HPos.RIGHT);

		Scene editConScene = new Scene(grid, 450, 205);
		editConWindow.setScene(editConScene);
		editConScene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		editConWindow.getIcons().add(new Image("file:icon.png"));
		editConWindow.showAndWait();
	}

	private static void deleteConnection() {
		if (connTable.getSelectionModel().getSelectedItem() == null) {
			displayNoFilePickedPopup();
		}
		ObservableList<Connection> allItems, selectedItems;

		allItems = connTable.getItems();
		selectedItems = connTable.getSelectionModel().getSelectedItems();

		selectedItems.forEach(allItems::remove);

		rewriteConnectionFile();
	}

	private static void rewriteConnectionFile() {
		try (PrintWriter pw = new PrintWriter("connections.trtl");) {
			ObservableList<Connection> values = connTable.getItems();
			for (Connection c : values) {
				pw.write("\n" + c.getName() + "@" + c.getHost() + "@" + c.getPort());
			}
			pw.close();
		} catch (FileNotFoundException e) {
			displayFileNotFoundPopup();
		}
	}

	private static void connectToServer() {
		if(connTable.getSelectionModel().getSelectedItem() == null) {
			displayNoFilePickedPopup();
			return;
		}
		Connection toConn = connTable.getSelectionModel().getSelectedItem();
		ClientChat cc = new ClientChat(toConn, toConn.getName(), toConn.getHost(), toConn.getPort());
		cc.start();
	}
	
	private VBox getButtonList() {
		VBox buttonBox = new VBox();
		buttonBox.setPadding(new Insets(62, 20, 10, 0));
		buttonBox.setSpacing(15);

		Button newBtn = new Button("New");
		newBtn.setMaxWidth(Double.MAX_VALUE);
		newBtn.setPrefWidth(70);
		newBtn.setOnAction(e -> newConnection());

		Button editBtn = new Button("Edit");
		editBtn.setMaxWidth(Double.MAX_VALUE);
		editBtn.setOnAction(e -> editConnection());

		Button deleteBtn = new Button("Delete");
		deleteBtn.setMaxWidth(Double.MAX_VALUE);
		deleteBtn.setOnAction(e -> deleteConnection());

		StackPane stack = new StackPane();
		stack.setPadding(new Insets(0, 2, 40, 2));
		Button connectBtn = new Button("Connect");
		connectBtn.setOnAction(e -> connectToServer());

		stack.getChildren().add(connectBtn);
		stack.setAlignment(Pos.BOTTOM_CENTER);

		buttonBox.getChildren().addAll(stack, newBtn, editBtn, deleteBtn);

		return buttonBox;
	}

	private VBox getConnectionList() {
		conns = FXCollections.observableArrayList();

		connTable = new TableView();
		connTable.setEditable(true);
		connTable.setMaxWidth(400);
		connTable.setStyle(
				"-fx-table-cell-border-color: transparent; -fx-border-width: 1; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		connTable.setEditable(true);

		final Label connLabel = new Label("Turtle Connections");
		connLabel.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 20));
		connLabel.setAlignment(Pos.CENTER);
		connLabel.setPadding(new Insets(15, 0, 0, 0));

		nameColumn = new TableColumn("Connection Name");
		nameColumn.setMinWidth(150);
		nameColumn.setPrefWidth(150);
		nameColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("name"));

		hostColumn = new TableColumn("Host Name");
		hostColumn.setMinWidth(100);
		hostColumn.setStyle("-fx-alignment: CENTER;");
		hostColumn.setPrefWidth(100);
		hostColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("host"));

		portColumn = new TableColumn("Port");
		portColumn.setMinWidth(70);
		portColumn.setPrefWidth(70);
		portColumn.setStyle("-fx-alignment: CENTER;");
		portColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("port"));

		String line = "";
		String username = "";
		String host = "";
		int port = 0;
		String[] splitter;
		File connFile = new File("connections.trtl");
		try (BufferedReader fin = new BufferedReader(new FileReader(connFile));) {
			while ((line = fin.readLine()) != null) {
				splitter = line.split("@");
				if (splitter.length != 3) {
					continue;
				}
				username = splitter[0];
				host = splitter[1];
				/**
				 * If the port could not be parsed, the user will be prompted to
				 * re-enter a valid port number
				 */
				try {
					port = Integer.parseInt(splitter[2]);
				} catch (NumberFormatException e) {
					port = PORT_PARSE_ERROR;
					e.printStackTrace();
				}
				conns.add(new Connection(username, host, port));
			}
		} catch (FileNotFoundException e) {
			displayFileNotFoundPopup();
			e.printStackTrace();
		} catch (IOException e) {
			displayFileNotFoundPopup();
			e.printStackTrace();
		}

		connTable.setItems(conns);
		connTable.getColumns().addAll(nameColumn, hostColumn, portColumn);

		VBox tableBox = new VBox();

		tableBox.setSpacing(5);
		tableBox.setPadding(new Insets(10, 10, 10, 20));
		tableBox.getChildren().addAll(connLabel, connTable);

		return tableBox;
	}

	private MenuBar getMenuBar() {
		Menu startMenu = new Menu("_Start");
		Menu connectMenu = new Menu("_Connect");

		MenuItem exitTurtle = new MenuItem("Exit");
		exitTurtle.setOnAction(e -> window.close());

		MenuItem newConn = new MenuItem("New...");
		newConn.setOnAction(e -> newConnection());

		startMenu.getItems().add(exitTurtle);
		connectMenu.getItems().add(newConn);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(startMenu);
		menuBar.getMenus().add(connectMenu);

		return menuBar;
	}

	/**
	 * Auxiliary help methods
	 */

	private static void displayFileNotFoundPopup() {
		AlertPopup_Small.display("File not found", "The connections.trtl file could not be found.");
	}

	private static void displayNoFilePickedPopup() {
		AlertPopup_Small.display("No connection selected.", "Please select a connection from the table.");
	}

}
