/**
 * 
 */
package net.jadoth.entitydatamapping.test;


import static net.jadoth.reflect.JadothReflect.getAnyField;

import net.jadoth.util.code.Code;

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
		
		println(Code.deriveGetterNameFromField(getAnyField(c, "pBoolean")));
		println(Code.deriveGetterNameFromField(getAnyField(c, "aBoolean")));
		println(Code.deriveGetterNameFromField(getAnyField(c, "normalField")));
		println(Code.deriveGetterNameFromField(getAnyField(c, "pBoolean"), false));
		println(Code.deriveGetterNameFromField(getAnyField(c, "aBoolean"), false));
	}
	
	static void println(String s){
		System.out.println(s);
	}

}
