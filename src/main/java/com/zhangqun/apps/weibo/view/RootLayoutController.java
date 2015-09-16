package com.zhangqun.apps.weibo.view;

import java.util.Locale;
import java.util.ResourceBundle;

import com.zhangqun.apps.weibo.MainApp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RootLayoutController {

	@FXML
	private RadioMenuItem languageEnUSRadioMenuItem;
	@FXML
	private RadioMenuItem languageZhCNRadioMenuItem;

	@FXML
	private ProgressBar statusProgressBar;
	@FXML
	private Label statusLabel;

	private MainApp mainApp;
	private ResourceBundle i18nBundle;

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		i18nBundle = mainApp.getI18nResourceBundle();

		STATUS_LABEL_TEXTS = new String[] {
				i18nBundle.getString("app.status.0_choose_graph"),
				i18nBundle.getString("app.status.1_graph_data_format_error"),
				i18nBundle.getString("app.status.2_start_parse_graph"),
				i18nBundle.getString("app.status.3_finish_parse_graph"),
				i18nBundle.getString("app.status.4_start_execution"),
				i18nBundle.getString("app.status.5_finish_execution"),
				i18nBundle.getString("app.status.6_terminate_execution"), };

		if (mainApp.getCurrentInUseLocale().equals(Locale.CHINA)) {
			languageZhCNRadioMenuItem.setSelected(true);
		} else {
			languageEnUSRadioMenuItem.setSelected(true);
		}
	}

	@FXML
	private void initialize() {

	}

	@FXML
	private void handleMenuExit(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	private void handleMenuInstructions(ActionEvent event) {
		InstructionDialogController controller = new InstructionDialogController(
				mainApp.getPrimaryStage(), "");
		controller.setMainApp(mainApp);
		controller.show();
	}

	@FXML
	private void handleMenuLanguageEnUS(ActionEvent event) {
		mainApp.setLocale(Locale.US);
		if (!mainApp.getCurrentInUseLocale().equals(Locale.US)) {
			showLanguageChangeDialog();
		}
	}

	@FXML
	private void handleMenuLanguageZhCN(ActionEvent event) {
		mainApp.setLocale(Locale.CHINA);
		if (!mainApp.getCurrentInUseLocale().equals(Locale.CHINA)) {
			showLanguageChangeDialog();
		}
	}

	private void showLanguageChangeDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(i18nBundle.getString("app.dialog.language_change.title"));
		alert.setHeaderText("");
		alert.setContentText(i18nBundle
				.getString("app.dialog.language_change.content"));
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(
				new Image(this.getClass().getClassLoader()
						.getResourceAsStream("images/art-icon-32.png")));
		alert.showAndWait();
	}

	@FXML
	private void handleMenuAbout(ActionEvent event) {
		// // 显示关于我们对话框
		// Alert alert = new Alert(AlertType.INFORMATION);
		// alert.setTitle(i18nBundle.getString("app.dialog.about.title"));
		// alert.setHeaderText(i18nBundle.getString("app.dialog.about.title"));
		// alert.setGraphic(new
		// ImageView(this.getClass().getResource("/images/ynu_crest.jpg").toString()));
		// alert.setContentText(i18nBundle.getString("app.dialog.about.content"));
		// Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		// stage.getIcons().add(new
		// Image(this.getClass().getClassLoader().getResourceAsStream("images/art-icon-32.png")));
		// alert.showAndWait();

		Dialog dialog = new Dialog();
		dialog.setTitle(i18nBundle.getString("app.dialog.about.title"));
		dialog.setHeaderText("");
		dialog.setGraphic(new ImageView(this.getClass()
				.getResource("/images/ynu_crest.jpg").toString()));
		StackPane pane = new StackPane();
		Label content = new Label(
				i18nBundle.getString("app.dialog.about.content"));
		pane.getChildren().add(content);
		dialog.getDialogPane().setContent(pane);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(
				new Image(this.getClass().getClassLoader()
						.getResourceAsStream("images/art-icon-32.png")));
		dialog.showAndWait();
	}

	public enum STATUS {
		NOT_INPUT_GRAPH_DATA, GRAPH_DATA_FORMAT_ERROR, START_PARSE_GRAPH, FINISH_PARSE_GRAPH, START_ALGORITHM_GENERAL, FINISH_ALGORITHM_GENERAL, TERMINATE_ALGORITHM_GENERAL
	}

	private String[] STATUS_LABEL_TEXTS;

	private double[] PROGRESS_VALUES = new double[] { 0f, 1.0f, -1.0f, 1.0f,
			-1.0f, 1.0f, 0f };

	public void updateStatusBar(STATUS status) {
		int index = status.ordinal();
		// http://docs.oracle.com/javafx/2/ui_controls/progress.htm
		statusProgressBar.setProgress(PROGRESS_VALUES[index]);
		statusLabel.setText(STATUS_LABEL_TEXTS[index]);
	}

}
