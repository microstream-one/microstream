/**
 * 
 */
package net.jadoth.entitydatamapping.test;


import static net.jadoth.reflect.JadothReflect.getAnyField;
import net.jadoth.reflect.JadothReflect;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestDeriveBooleanGetter 
{	
	boolean pBoolean;
	Boolean aBoolean;
	String normalField;


	
	public static void main(String[] args) {
		final Class<?> c = MainTestDeriveBooleanGetter.class;
		
		println(JadothReflect.deriveGetterNameFromField(getAnyField(c, "pBoolean")));
		println(JadothReflect.deriveGetterNameFromField(getAnyField(c, "aBoolean")));
		println(JadothReflect.deriveGetterNameFromField(getAnyField(c, "normalField")));
		println(JadothReflect.deriveGetterNameFromField(getAnyField(c, "pBoolean"), false));
		println(JadothReflect.deriveGetterNameFromField(getAnyField(c, "aBoolean"), false));
	}
	
	static void println(String s){
		System.out.println(s);
	}

}
