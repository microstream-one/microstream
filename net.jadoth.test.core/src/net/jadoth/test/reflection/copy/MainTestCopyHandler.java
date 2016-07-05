package net.jadoth.test.reflection.copy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.jadoth.exceptions.IllegalAccessRuntimeException;
import net.jadoth.reflect.CopyHandler;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.reflect.ReflectiveCopier;
import net.jadoth.test.reflection.MySubClassA;
import net.jadoth.test.reflection.MySubClassA1;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestCopyHandler {


	static CopyHandler loggingCopyhandler = new CopyHandler.DefaultImplementation() {
		@Override
		public void copy(final Field fieldToCopy, final Object source, final Object target) throws IllegalAccessRuntimeException {
			System.out.println("Copying\t"+fieldToCopy.getName()+" (\""+JadothReflect.getFieldValue(fieldToCopy, source)+"\")"+
					"\nfrom\t"+source+"\nto\t"+target+"\n");
			super.copy(fieldToCopy, source, target);
		}
	};



	public static void main(final String[] args) throws Exception {
		final Field FIELD_subClassAValue = MySubClassA.class.getDeclaredField("subClassAValue");


		final MySubClassA1 a1Source = new MySubClassA1();
		a1Source.subClassAValue = "source value";

		final MySubClassA1 a1Target = new MySubClassA1();


		final Map<Field, CopyHandler> fieldCopyHandlers = new HashMap<>();
		fieldCopyHandlers.put(FIELD_subClassAValue, loggingCopyhandler);

		ReflectiveCopier.copy(a1Source, a1Target, MySubClassA1.class, null, fieldCopyHandlers, null, null, null);

		System.out.println("a1Target.subClassAValue == "+a1Target.subClassAValue);

	}

}
