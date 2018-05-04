package net.jadoth.entitydatamapping.test;

import java.io.Externalizable;
import java.io.Serializable;

import net.jadoth.reflect.XReflect;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestSuperclassStuff {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		System.out.println(XReflect.isSubClassOf(Integer.class, Number.class));
		System.out.println(XReflect.isSubClassOf(Integer.class, Object.class));
		System.out.println(XReflect.isSubClassOf(Number.class, Number.class));

		System.out.println(XReflect.isSubClassOf(Externalizable.class, Serializable.class));

		System.out.println("");
		System.out.println(XReflect.isOfClassType(Integer.class, Number.class));
		System.out.println(XReflect.isOfClassType(Integer.class, Object.class));
		System.out.println(XReflect.isOfClassType(Number.class, Number.class));

		System.out.println(XReflect.isOfClassType(Externalizable.class, Serializable.class));

	}

}
