package Utilities;

import java.io.IOException;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import Base.DriverManager1;

public class Login1 {

protected WebDriver driver;
    protected WebDriverWait wait;
   
public void First() throws IOException {
       driver = DriverManager1.getDriver();
       wait = new WebDriverWait(driver, Duration.ofSeconds(30));
       String userID = DriverManager1.getProperty("userid");
         String password = DriverManager1.getProperty("password");

         // Login with first user
         DriverManager1.login(userID, password);
         System.out.println("Logged with " + userID);
   }

public void Second() throws IOException {
       driver = DriverManager1.getDriver();
       wait = new WebDriverWait(driver, Duration.ofSeconds(30));
       String userID = DriverManager1.getProperty("userid1");
      String password = DriverManager1.getProperty("password1");

      // Login with first user
      DriverManager1.login(userID, password);
      System.out.println("Logged with " + userID);
   }
public void Third() throws IOException {
    driver = DriverManager1.getDriver();
    wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    String userID = DriverManager1.getProperty("userid2");
   String password = DriverManager1.getProperty("password2");

   // Login with first user
   DriverManager1.login(userID, password);
   System.out.println("Logged with " + userID);
}
public void Fourth() throws IOException {
    driver = DriverManager1.getDriver();
    wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    String userID = DriverManager1.getProperty("userid3");
   String password = DriverManager1.getProperty("password3");

   // Login with first user
   DriverManager1.login(userID, password);
   System.out.println("Logged with " + userID);
}

}