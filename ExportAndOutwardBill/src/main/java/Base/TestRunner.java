package Base;

import org.testng.TestNG;
import java.util.Collections;

public class TestRunner {
    public static void main(String[] args) {
        // Create TestNG instance
        TestNG testng = new TestNG();

        // Point to your TestNG XML file
        testng.setTestSuites(Collections.singletonList("testng.xml"));

        // Run the suite
        testng.run();
    }
}
