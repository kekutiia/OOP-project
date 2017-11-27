
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calendarfx.login;

import com.calendarfx.exceptions.EmailParser;
import com.calendarfx.exceptions.IncorrectEmailInput;

import com.calendarfx.app.CalendarApp;
import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author danml
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private JFXButton login;
    private JFXButton cancel;
    private Login main;
    private Label pickTitle;
   
    @FXML private TextField username;

    @FXML
    private void handleLoginButton(ActionEvent event) {
        //CalendarApp cal= new CalendarApp();
        try
        {
           String email = username.getText(); 
           EmailParser myParser = new EmailParser(email);
           myParser.parse();
           this.switchWindow((Stage) login.getScene().getWindow(), new CalendarApp());
        }
        
        catch(IncorrectEmailInput e)
	{
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid email input");
            alert.setContentText("This is not a valid email address. Please input valid email");
            alert.showAndWait();
            username.clear();
 
					
	}
        
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO 2196f3 colour...

    }

    public void setMain(Login main) {
        this.main = main;
    }

    public static void switchWindow(Stage window, Application app) {
        try {
            app.start(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setTitle(String txt) {
    pickTitle.setText(txt);
}

}
