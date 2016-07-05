package net.jadoth.entitydatamapping.test;

import java.io.Externalizable;
import java.io.Serializable;

import net.jadoth.reflect.JadothReflect;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestSuperclassStuff {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		System.out.println(JadothReflect.isSubClassOf(Integer.class, Number.class));
		System.out.println(JadothReflect.isSubClassOf(Integer.class, Object.class));
		System.out.println(JadothReflect.isSubClassOf(Number.class, Number.class));

		System.out.println(JadothReflect.isSubClassOf(Externalizable.class, Serializable.class));

		System.out.println("");
		System.out.println(JadothReflect.isOfClassType(Integer.class, Number.class));
		System.out.println(JadothReflect.isOfClassType(Integer.class, Object.class));
		System.out.println(JadothReflect.isOfClassType(Number.class, Number.class));

		System.out.println(JadothReflect.isOfClassType(Externalizable.class, Serializable.class));

	}

}
