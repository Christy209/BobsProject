package Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcelData {
    String sheetName();
    int[] rowIndex();  // Always as array
  //  int rowIndex1() default -1;
    
	
	
}

