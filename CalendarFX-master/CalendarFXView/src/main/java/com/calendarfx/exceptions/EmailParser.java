/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calendarfx.exceptions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author alexpont
 */

public class EmailParser {
    
    // Field
    
    private String input; 
    
    
    // Constructor 
    
    public EmailParser(String _inp)
    {
        this.input = _inp;
    }
    
    // Methods 
    
    public boolean parse() throws IncorrectEmailInput
    {
        Pattern valid_email = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher_valid_email = valid_email.matcher(this.input);
        
        if (matcher_valid_email.matches())
        {
            System.out.println("This is a good email");
            return true;
            
            // return matcher.find() 
        }
        else
        {
            throw new IncorrectEmailInput();
        }
    }
    
    
   
}
    

