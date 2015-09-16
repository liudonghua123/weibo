package com.zhangqun.apps.weibo;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;

import com.zhangqun.apps.weibo.service.FollowService;
import com.zhangqun.apps.weibo.service.WeiboService;
import com.zhangqun.apps.weibo.view.MainLayoutController;
import com.zhangqun.apps.weibo.view.RootLayoutController;
import com.zhangqun.apps.weibo.view.RootLayoutController.STATUS;

@Controller
public class MainApp extends Application implements CommandLineRunner {

	@Autowired
	private FollowService followService;
	
	@Autowired
	private WeiboService weiboService;
	private static FollowService followService1;
	private static WeiboService weiboService1;

	private Logger logger = Logger.getLogger(MainApp.class);

	private Stage primaryStage;
	private BorderPane rootLayout;
	
	private RootLayoutController rootController;
	private MainLayoutController mainController;
	
	private Locale locale;
	private ResourceBundle i18nBundle;


	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Weibo Key User Discover Application");
		
		// http://fxexperience.com/2013/01/modena-new-theme-for-javafx-8/
//		setUserAgentStylesheet(STYLESHEET_MODENA);

        // Set the application icon.
        this.primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("images/art-icon-32.png")));
		
		locale = getLocale();
		i18nBundle = ResourceBundle.getBundle("com.zhangqun.apps.weibo.ApplicationResources", locale);
        
		initRootLayout();
		
		initMainLayout();
		  
  		// inject followService and weiboService
  		// MainLayoutController annotated with @Component does not work, why?
  		mainController.setFollowService(followService1);
  		mainController.setWeiboService(weiboService1);
		
		primaryStage.setOnCloseRequest(e -> {
			//Platform.exit();
			System.exit(0);
		});
	}

	private void initRootLayout() {
		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(i18nBundle);
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());
            
            rootController = loader.getController();
            rootController.setMainApp(this);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

	}

	private void initMainLayout() {
        try {
            // Load main layout
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(i18nBundle);
            loader.setLocation(MainApp.class.getResource("view/MainLayout.fxml"));
            TabPane mainLayout = (TabPane) loader.load();

            // Set main layout into the center of root layout.
            rootLayout.setCenter(mainLayout);
            
            mainController = loader.getController();
            mainController.setMainApp(this);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public void updateStatusBar(STATUS startParseGraph) {
		rootController.updateStatusBar(startParseGraph);
	}

    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public ResourceBundle getI18nResourceBundle() {
    	return i18nBundle;
    }
	
	public Locale getLocale() {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    String lang = prefs.get("locale_lang", null);
	    if(lang == null) {
	    	return Locale.getDefault();
	    }
	    String country = prefs.get("locale_country", "");
	    return new Locale(lang, country);
	}
	
	public void setLocale(Locale locale) {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    prefs.put("locale_lang", locale.getLanguage());
	    prefs.put("locale_country", locale.getCountry());
	}
	
	public Locale getCurrentInUseLocale() {
		return locale;
	}

	@Override
	public void run(String... args) throws Exception {
		followService1 = followService;
		weiboService1 = weiboService;
		launch(args);
	}
}
