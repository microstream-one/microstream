
package one.microstream.wrapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Helper annotation for the wrapper annotation processor. List type names for arbitrary interfaces, for which wrappers
 * should be generated.
 * <pre>
 * &#64;GenerateWrapperFor({"com.myapp.MyType1","com.myapp.MyType2"})
 * public class WrapperGenerationDummy
 * {
 * }
 * </pre>
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateWrapperFor
{
	String[] value();
}
