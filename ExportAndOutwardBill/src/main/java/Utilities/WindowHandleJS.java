package Utilities;

import org.openqa.selenium.*;

public class WindowHandleJS {

    @SuppressWarnings("unused")
	private WebDriver driver;
    private JavascriptExecutor js;

    public WindowHandleJS(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }

    /**
     * Set value in an input field using JS (IE-safe).
     */
    public void setValue(WebElement element, String value) {
        js.executeScript(
            "arguments[0].value = arguments[1];" +
            "if(arguments[0].fireEvent) { arguments[0].fireEvent('onchange'); } " +
            "else { var evt=document.createEvent('HTMLEvents'); evt.initEvent('change', true, false); arguments[0].dispatchEvent(evt); }",
            element, value
        );
    }

    /**
     * Click an element using JS (IE-safe).
     */
    public void click(WebElement element) {
        js.executeScript(
            "if(arguments[0].fireEvent) { arguments[0].fireEvent('onclick'); } " +
            "else { var evt=document.createEvent('HTMLEvents'); evt.initEvent('click', true, false); arguments[0].dispatchEvent(evt); }",
            element
        );
    }
}
