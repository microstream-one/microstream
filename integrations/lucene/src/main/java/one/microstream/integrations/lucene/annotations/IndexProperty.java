package one.microstream.integrations.lucene.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.lucene.document.Field.Store;

@Retention(RUNTIME)
@Target(METHOD)
public @interface IndexProperty
{
	String name() default "";
	
	Store store() default Store.YES;
	
	boolean tokenize() default true;
}
