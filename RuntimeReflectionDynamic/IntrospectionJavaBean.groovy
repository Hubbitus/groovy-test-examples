import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation {
    String str() default "";
}

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotationSecond {
    String str() default "";
}

class Test{
    private int i;
    private Integer ii;
    private String s;

    @TestAnnotation
    @TestAnnotationSecond
    public int getI(){
        return i;
    }


    public void setI(int i){
        this.i = i
    }

    @TestAnnotation    
    public Integer getIi(){
        return i;
    }
    
    public void setIi(Integer i){
        this.i = i
    }
    
    public String getS(){
        return s;
    }
    
    public void setS(String s){
        this.s = s
    }
}

Test t = new Test(i: 7, ii: 77, s: 'some')

            Arrays.asList(Introspector.getBeanInfo(t.getClass(), Object.class).getPropertyDescriptors())
                .stream()
                .forEach({propertyDescriptor ->
                    Method method = propertyDescriptor.getReadMethod();
println "======="
println "propertyDescriptor: $propertyDescriptor"
println "property name: ${propertyDescriptor.getName()}"
println "property display name: ${propertyDescriptor.getDisplayName()}"
println "property description: ${propertyDescriptor.getShortDescription()}"
println "value: ${method.invoke(t)}"
println "annotations: ${method.getDeclaredAnnotations()}"
                });


int i = 7

i.equals(0)