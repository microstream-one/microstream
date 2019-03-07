package one.microstream.test.reflection.copy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * @author Thomas Muenz
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,
	ElementType.METHOD,  ElementType.CONSTRUCTOR,
	ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.FIELD
})
public @interface TestAnnotation {/**/}
