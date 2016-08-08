package de.turtle.modules;

import de.turtle.client.TurtleClient;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertPopup_Small {

	public static void display(String title, String text) {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinWidth(250);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 20, 10, 20));

		Text displayText = new Text();
		displayText.setText(text);
		displayText.setFont(Font.font("Segoe UI Lightweight", FontWeight.NORMAL, 14));
		displayText.setFill(Color.BLACK);
		grid.add(displayText, 0, 0);
		GridPane.setHalignment(displayText, HPos.CENTER);

		Button okBtn = new Button("Ok");
		okBtn.setOnAction(e -> window.close());
		grid.add(okBtn, 1, 1);

		Scene scene = new Scene(grid, 395, 120);
		scene.getStylesheets().add(TurtleClient.class.getResource("mainscene.css").toExternalForm());
		window.getIcons().add(new Image("file:icon.png"));
		window.setScene(scene);
		window.showAndWait();
	}
}
