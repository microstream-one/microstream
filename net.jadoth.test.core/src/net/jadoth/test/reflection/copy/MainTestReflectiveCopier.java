package net.jadoth.test.reflection.copy;

import java.lang.reflect.Field;

import net.jadoth.exceptions.IllegalAccessRuntimeException;
import net.jadoth.reflect.CopyHandler;
import net.jadoth.reflect.ReflectiveCopier;
import net.jadoth.test.reflection.MySubClassA;
import net.jadoth.test.reflection.MySubClassA1;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestReflectiveCopier {


	static ReflectiveCopier initCopier(){
		final ReflectiveCopier copier = new ReflectiveCopier();

		CopyHandler genericStringCopyhandler = new CopyHandler.DefaultImplementation() {
			@Override
			public void copy(Field fieldToCopy, Object source, Object target) throws IllegalAccessRuntimeException {
				System.out.println("Called genericStringCopyhandler on "+fieldToCopy.getName());
				super.copy(fieldToCopy, source, target);
			}
		};
		CopyHandler subClassA1ValueCopyhandler = new CopyHandler.DefaultImplementation() {
			@Override
			public void copy(Field fieldToCopy, Object source, Object target) throws IllegalAccessRuntimeException {
				System.out.println("Called subClassA1ValueCopyhandler on "+fieldToCopy.getName());
				super.copy(fieldToCopy, source, target);
			}
		};
		CopyHandler classA1StringCopyhandler = new CopyHandler.DefaultImplementation() {
			@Override
			public void copy(Field fieldToCopy, Object source, Object target) throws IllegalAccessRuntimeException {
				System.out.println("Called classA1StringCopyhandler on "+fieldToCopy.getName());
				super.copy(fieldToCopy, source, target);
			}
		};
		CopyHandler annotationCopyhandler = new CopyHandler.DefaultImplementation() {
			@Override
			public void copy(Field fieldToCopy, Object source, Object target) throws IllegalAccessRuntimeException {
				System.out.println("Called annotationCopyhandler on "+fieldToCopy.getName());
				super.copy(fieldToCopy, source, target);
			}
		};
		CopyHandler genericHandler = new CopyHandler.DefaultImplementation() {
			@Override
			public void copy(Field fieldToCopy, Object source, Object target) throws IllegalAccessRuntimeException {
				System.out.println("Called genericHandler on "+fieldToCopy.getName());
				super.copy(fieldToCopy, source, target);
			}
		};

		Field FIELD_subClassA1Value = null;
		try
		{
			FIELD_subClassA1Value = MySubClassA1.class.getDeclaredField("subClassA1Value");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		copier
			.addCopyHandlerByCopyClass(genericStringCopyhandler, String.class)
			.addCopyHandlerByCopyField(subClassA1ValueCopyhandler, FIELD_subClassA1Value)
			.addCopyHandler(classA1StringCopyhandler, MySubClassA1.class, String.class, null, null)
			.addCopyHandlerByAnnotation(annotationCopyhandler, TestAnnotation.class)
			.setGenericCopyHandler(genericHandler)
		;

		return copier;
	}


	public static void main(final String[] args) throws Exception
	{
		ReflectiveCopier copier = initCopier();

		MySubClassA1 a1Source = new MySubClassA1();
		a1Source.subClassAValue = "source value";

		MySubClassA1 a1Target = new MySubClassA1();

		System.out.println(MySubClassA1.class.getSimpleName()+"-level copy");
		copier.execute(a1Source, a1Target, MySubClassA1.class, null);
		System.out.println("a1Target.subClassAValue == "+a1Target.subClassAValue);

		System.out.println("");

		System.out.println(MySubClassA.class.getSimpleName()+"-level copy");
		copier.execute(a1Source, a1Target, MySubClassA.class, null);
		System.out.println("a1Target.subClassAValue == "+a1Target.subClassAValue);

	}

}
