package com.zhangqun.apps.weibo.view;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.zhangqun.apps.weibo.MainApp;
import com.zhangqun.apps.weibo.Utils;


public class InstructionDialogController extends Stage implements Initializable {
	
	@FXML
	private Pagination pagination;
	
	private MainApp mainApp;
	private ResourceBundle i18nBundle;
	
	private String instructionDirPath = "images/instructions/";
	
	private String[] instructionImagePaths = null;
	
	
	
	public InstructionDialogController(Stage ownerStage, String title) {
		setTitle(title);
		getIcons().add(new Image(getClass().getClassLoader().getResource("images/art-icon-32.png").toString()));
		initModality(Modality.WINDOW_MODAL);
		initOwner(ownerStage);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InstructionDialog.fxml"));
		fxmlLoader.setController(this);
		try {
			setScene(new Scene((Parent) fxmlLoader.load()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
    	pagination.setFocusTraversable(false);
    	try {
			instructionImagePaths = Utils.getResourceListing(this.getClass(), instructionDirPath);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		if (instructionImagePaths != null && instructionImagePaths.length > 0) {
//			pagination = new Pagination(instructionImagePaths.length, 0);
			pagination.setPageCount(instructionImagePaths.length);
	        pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
			pagination.setPageFactory(new Callback<Integer, Node>() {

				@Override
				public Node call(Integer pageIndex) {
					return createPage(pageIndex);
				}
			});
		}

    }
    

	public Node createPage(int pageIndex) {        
        ImageView imageView = null;
        URL resource = this.getClass().getClassLoader().getResource(instructionDirPath + instructionImagePaths[pageIndex]);
        imageView = new ImageView(resource.toString());
        return imageView;
    }

	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		i18nBundle = mainApp.getI18nResourceBundle();
		
	}
	

}
