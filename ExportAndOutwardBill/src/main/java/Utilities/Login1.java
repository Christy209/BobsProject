package Utilities;

import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import Base.DriverManager;

public class Login1 {

protected WebDriver driver;
    protected WebDriverWait wait;
   
public void First() throws IOException {
       driver = DriverManager.getDriver();
       wait = new WebDriverWait(driver, Duration.ofSeconds(30));
       String userID = DriverManager.getProperty("userid");
         String password = DriverManager.getProperty("password");

         // Login with first user
         DriverManager.login(userID, password);
         System.out.println("Logged with " + userID);
   }

public void Second() throws IOException {
       driver = DriverManager.getDriver();
       wait = new WebDriverWait(driver, Duration.ofSeconds(30));
       String userID = DriverManager.getProperty("userid2");
      String password = DriverManager.getProperty("password2");

      // Login with first user
      DriverManager.login(userID, password);
      System.out.println("Logged with " + userID);
   }
}