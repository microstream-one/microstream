
package one.microstream.wrapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation for interfaces for which the wrapper annotation processor should generate wrapper types.
 * 
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateWrapper
{
	// Marker annotation
}
