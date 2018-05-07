/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.reflect;


import static net.jadoth.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.branching.AbstractBranchingThrow;
import net.jadoth.branching.ThrowBreak;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XPrependingSequence;
import net.jadoth.collections.types.XReference;
import net.jadoth.exceptions.IllegalAccessRuntimeException;
import net.jadoth.exceptions.NoSuchFieldRuntimeException;
import net.jadoth.typing.XTypes;
import net.jadoth.util.UtilStackTrace;


/**
 * Provides additional generic util methods for working with java reflection.
 *
 * @author Thomas Muenz
 *
 */
public final class XReflect
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/*
	 * JDK workaround stuff. transient to skip fields in persistence layers collecting constants
	 * CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	 */
	private static final transient Field ArrayList_elementData = setAccessible(getArrayListElementsField());
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static Field getArrayListElementsField()
	{
		/*
		 * This should be both correct and robost enough for any change to ArrayList they might possibly come up with.
		 * Only non-primitive array type instance fields are considered. Of any class in the hierarchy.
		 * The only possible case where this method would fail would be if an ArrayList contained more than
		 * one reference array. But that would be a pretty superfluous and moronic waste of memory.
		 */
		return getAnyField(ArrayList.class, field ->
			isInstanceField(field) && field.getType().isArray() && !field.getType().getComponentType().isPrimitive()
		);
	}
	
	/**
	 * Accesses the elementData field containing the array holding the elements of <code>arrayList</code>.
	 * <p>
	 * <u><b>Warning</b></u>: Use this method wisely!<br>
	 * In almost all situations, it is not necessary to "peek" inside the <code>ArrayList</code> object and "steal" the
	 * array from it. The use of this method in such situations is bad programming style and can cause any sort of
	 * trouble. E.g. logic manipulating the array while the actual <code>ArrayList</code> object is still active.<br>
	 * <br>
	 * Handle with care!<br>
	 *
	 * @param <T>
	 * @param arrayList
	 * @return the elementData member array used by <code>arrayList</code>
	 */
	public static final Object[] accessArray(final ArrayList<?> arrayList) throws IllegalAccessRuntimeException
	{
		//can never throw an exception if field elementData has been successfully retrieved
		return (Object[])getFieldValue(ArrayList_elementData, arrayList);
	}

	public static final Field setAccessible(final Field field)
	{
		// because lol
		field.setAccessible(true);
		return field;
	}

	public static final boolean isInstanceField(final Field field)
	{
		return !Modifier.isStatic(field.getModifiers());
	}

	public static final String toFieldName(final Field field)
	{
		return field.getName();
	}

	public static final boolean isFinalField(final Field field)
	{
		return Modifier.isFinal(field.getModifiers());
	}

	public static final boolean isVariableField(final Field field)
	{
		return !Modifier.isFinal(field.getModifiers());
	}

	public static final boolean isPrimitiveField(final Field field)
	{
		return field.getType().isPrimitive();
	}

	public static final boolean isReferenceField(final Field field)
	{
		return !field.getType().isPrimitive();
	}
	
	public static boolean isInstanceReferenceField(final Field field)
	{
		return isInstanceField(field) && isReference(field);
	}

	public static final boolean isInterfaceOfType(
		final Class<?> interfaceClass           ,
		final Class<?> implementedSuperInterface
	)
	{
		if(interfaceClass == implementedSuperInterface)
		{
			return true;
		}

		final Class<?>[] interfaces = interfaceClass.getInterfaces();
		boolean isInterfaceType = false;
		for(final Class<?> i : interfaces)
		{
			isInterfaceType |= isInterfaceOfType(i, implementedSuperInterface);
		}
		return isInterfaceType;
	}

	public static final boolean implementsInterface(final Class<?> c, final Class<?> interfaceClass)
	{
		if(c == null || interfaceClass == null || !interfaceClass.isInterface())
		{
			return false;
		}

		final Class<?>[] interfaces = XReflect.getClassHierarchyInterfaces(c);
		for(final Class<?> i : interfaces)
		{
			if(isInterfaceOfType(i, interfaceClass))
			{
				return true;
			}
		}
		return false;
	}

	public static final Class<?>[] getClassHierarchyInterfaces(final Class<?> classClass)
	{
		if(classClass.isInterface() || classClass.isArray() || classClass.isPrimitive())
		{
			throw new IllegalArgumentException("Can only handle actual classes.");
		}

		final BulkList<Class<?>[]> hierarchy = new BulkList<>();
		int interfaceCount = 0;

		for(Class<?> current = classClass; current != Object.class; current = current.getSuperclass())
		{
			final Class<?>[] currentClassInterfaces;
			interfaceCount += (currentClassInterfaces = current.getInterfaces()).length;
			hierarchy.add(currentClassInterfaces);
		}

		final Class<?>[] allInterfaces = new Class<?>[interfaceCount];
		int allInterfacesIndex = 0;
		for(int i = XTypes.to_int(hierarchy.size()); i-- > 0;)
		{
			final Class<?>[] currentClassInterfaces = hierarchy.at(i);
			for(int j = 0; j < currentClassInterfaces.length; j++)
			{
				allInterfaces[allInterfacesIndex++] = currentClassInterfaces[j];
			}
		}
		return allInterfaces;
	}

	public static final boolean isOfClassType(final Class<?> c, final Class<?> superclass)
	{
		if(c.isInterface() || superclass.isInterface())
		{
			return false;
		}
		return c == superclass ? true : isSubClassOf(c, superclass);
	}

	public static final boolean isSubClassOf(final Class<?> c, final Class<?> superclass)
	{
		if(c == null || superclass == null)
		{
			return false;
		}
		Class<?> currentType = c;
		while(currentType != null)
		{
			currentType = currentType.getSuperclass();
			if(currentType == superclass)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Iterates over all declared fields of all classes upwards starting at c until class {@link Object} is reached
	 * and executes the passed {@link Consumer} on it.
	 * <p>
	 * The declared fields of each class are iterated by looping over {@link Class#getDeclaredFields()} from index
	 * {@code 0} to index {@link Class#getDeclaredFields()}{@code .length}.
	 * <p>
	 * {@link AbstractBranchingThrow} effects:<br>
	 * {@link ThrowContinue} iteration proceeds with next field (i.e. no effect).<br>
	 * {@link ThrowBreak} iteration breaks current class fields loop and proceeds with next class.<br>
	 * {@link ThrowReturn} iteration is aborted, methods returns.
	 *
	 * @param clazz the class whose fields shall be iterated
	 * @param logic the {@link Consumer} to be executed on each field.
	 */
	public static final <C extends Consumer<? super Field>> C iterateAllClassFields(
		final Class<?> clazz,
		final C        logic
	)
	{
		return iterateAllClassFields(clazz, Object.class, logic);
	}

	public static final <C extends Consumer<? super Field>> C iterateAllClassFields(
		final Class<?> clazz,
		final Class<?> bound,
		final C        logic
	)
	{
		// applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(clazz.isArray() || clazz.getSuperclass() == null)
		{
			return logic;
		}
		
		try
		{
			for(Class<?> currentClass = clazz; currentClass != bound; currentClass = currentClass.getSuperclass())
			{
				for(final Field field : currentClass.getDeclaredFields())
				{
					logic.accept(field);
				}
			}
		}
		catch(final ThrowBreak b)
		{
			/* abort inner iteration */
		}
		
		return logic;
	}

	/**
	 * Same as {@link #iterateAllClassFields(Class, Consumer)}, except that the {@link Class#getDeclaredFields()}
	 * arrays are each iterated in reverse order (from index {@link Class#getDeclaredFields()}{@code .length - 1}
	 * to index {@code 0} ).
	 * <p>
	 * This method is useful to maintain the natural declaration order of the fields, iterating from the last
	 * declared field of the lowest class (the passed class itself) to the first declared field of the highest class
	 * declaring a field.
	 *
	 * @param c the class whose fields shall be iterated.
	 * @param fieldOperation the {@link Consumer} to be executed on each field.
	 */
	public static final void reverseIterateAllClassFields(final Class<?> c, final Consumer<Field> fieldOperation)
	{
		// applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(c.isArray() || c.getSuperclass() == null)
		{
			return;
		}
		
		try
		{
			for(Class<?> currentClass = c; currentClass != Object.class; currentClass = currentClass.getSuperclass())
			{
				final Field[] fields = currentClass.getDeclaredFields();
				
				int i = fields.length;
				while(i > 0)
				{
					fieldOperation.accept(fields[--i]);
				}
			}
				
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <C extends XPrependingSequence<Field>> C collectFields(
		final C                collection,
		final Class<?>         type      ,
		final Predicate<Field> predicate
	)
	{
		XReflect.reverseIterateAllClassFields(type, field ->
		{
			if(predicate.test(field))
			{
				collection.prepend(field);
			}
		});
		
		return collection;
	}

	public static final <C extends XPrependingSequence<Field>> C collectFields(
		final C        collection,
		final Class<?> type
	)
	{
		XReflect.reverseIterateAllClassFields(type, field ->
			collection.prepend(field)
		);
		
		return collection;
	}

	public static final <C extends XPrependingSequence<Field>> C collectTypedFields(
		final C                collection,
		final Class<?>         type      ,
		final Predicate<Field> predicate
	)
	{
		//applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(type.isArray() || type.getSuperclass() == null)
		{
			return collection;
		}

		for(Class<?> currentClass = type; currentClass != Object.class; currentClass = currentClass.getSuperclass())
		{
			final Field[] fields = currentClass.getDeclaredFields();
			for(int i = fields.length; --i >= 0;)
			{
				if(predicate.test(fields[i]))
				{
					collection.prepend(fields[i]);
				}
			}
		}
		
		return collection;
	}

	public static final Field getDeclaredField(final Class<?> c, final String name) throws NoSuchFieldRuntimeException
	{
		try
		{
			return c.getDeclaredField(name);
		}
		catch(final NoSuchFieldException e)
		{
			throw new NoSuchFieldRuntimeException(e);
		}
	}

	public static final Field getField(final Class<?> c, final String name) throws NoSuchFieldRuntimeException
	{
		try
		{
			return c.getField(name);
		}
		catch(final NoSuchFieldException e)
		{
			throw new NoSuchFieldRuntimeException(e);
		}
	}

	public static final Field getAnyField(final Class<?> c, final String name) throws NoSuchFieldRuntimeException
	{
		notNull(name);
		
		try
		{
			return getAnyField(c, field ->
			name.equals(field.getName())
		);
		}
		catch(final NoSuchFieldRuntimeException e)
		{
			// (28.10.2013 TM)EXCP: proper exception (OMG).
			throw new NoSuchFieldRuntimeException(
				new NoSuchFieldException("No field with name " + name + " found in type " + c)
			);
		}
	}

	public static final Field getAnyField(final Class<?> c, final Predicate<? super Field> predicate)
		throws NoSuchFieldRuntimeException
	{
		final XReference<Field> result = X.Reference(null);
		iterateAllClassFields(c, field ->
		{
			if(predicate.test(field))
			{
				result.set(field);
				throw X.BREAK();
			}
		});
		
		if(result.get() != null)
		{
			return result.get();
		}
		
		throw new NoSuchFieldRuntimeException(new NoSuchFieldException());
	}

	public static final Field getInstanceFieldOfType(final Class<?> declaringType, final Class<?> fieldType)
		throws NoSuchFieldRuntimeException
	{
		try
		{
			return getAnyField(declaringType, f ->
				isInstanceField(f) && fieldType.isAssignableFrom(f.getType())
			);
		}
		catch(final NoSuchFieldRuntimeException e)
		{
			// (28.10.2013 TM)EXCP: proper exception (OMG).
			throw new NoSuchFieldRuntimeException(
				new NoSuchFieldException(
					"No instance field of type " + fieldType.getName() + " found in type " + declaringType
				)
			);
		}
	}

	public static final boolean isFinal(final Field field)
	{
		return Modifier.isFinal(field.getModifiers());
	}

	public static final boolean isStatic(final Field field)
	{
		return Modifier.isStatic(field.getModifiers());
	}
	
	public static final boolean isSynthetic(final Field field)
	{
		return Modifier.isSynchronized(field.getModifiers());
	}

	public static final boolean isStaticFinal(final Field field)
	{
		return isStatic(field) && isFinal(field);
	}

	public static final boolean isPrimitive(final Field field)
	{
		return field.getType().isPrimitive();
	}

	public static final boolean isReference(final Field field)
	{
		return !field.getType().isPrimitive();
	}

	public static final boolean isTransient(final Field field)
	{
		return Modifier.isTransient(field.getModifiers());
	}

	public static final boolean isNotTransient(final Field field)
	{
		return !Modifier.isTransient(field.getModifiers());
	}

	public static final boolean isPrivate(final Field field)
	{
		return Modifier.isPrivate(field.getModifiers());
	}

	public static final boolean isProtected(final Field field)
	{
		return Modifier.isProtected(field.getModifiers());
	}

	public static final boolean isPublic(final Field field)
	{
		return Modifier.isPublic(field.getModifiers());
	}

	public static final boolean isDefaultVisible(final Field field)
	{
		final int modifiers = field.getModifiers();
		return !(Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers));
	}

	public static final boolean isAbstract(final Class<?> type)
	{
		return Modifier.isAbstract(type.getModifiers());
	}

	public static final boolean isAbstract(final Method method)
	{
		return Modifier.isAbstract(method.getModifiers());
	}

	/**
	 * Calls {@link Field#get(Object)} and wraps the moronic abstraction-destroying checked
	 * {@link IllegalAccessException} with a proper {@link IllegalAccessRuntimeException}.
	 *
	 * @param field the field from which the value shall be extracted.
     * @param obj object from which the represented field's value is to be extracted
     * @return the value of the represented field in object {@code obj};
     *         primitive values are wrapped in an appropriate object before being returned
	 */
	public static final Object getFieldValue(final Field field, final Object obj)
	{
		// moronic checked exceptions
		try
		{
			return field.get(obj);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}

	/**
	 * Calls {@link Field#set(Object, Object)} and wraps the moronic abstraction-destroying checked
	 * {@link IllegalAccessException} with a proper {@link IllegalAccessRuntimeException}.
	 *
	 * @param field the field to be modified
     * @param obj the object whose field should be modified
     * @param value the new value for the field of {@code obj} being modified
	 */
	public static final void setFieldValue(final Field field, final Object obj, final Object value)
	{
		// moronic checked exceptions
		try
		{
			field.set(obj, value);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}

	public static int getField_int(final Field f, final Object obj) throws IllegalAccessRuntimeException
	{
		try
		{
			return f.getInt(obj);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}

	public static final Class<?> classForName(final String className)
		throws LinkageError, ExceptionInInitializerError, ClassNotFoundException
	{
		final Class<?> type = primitiveType(className);
		return type != null
			? type
			: Class.forName(className)
		;
	}

	public static final Class<?> primitiveType(final String className)
	{
		// Stupid JDK once again. Unbelievable.
		switch(className)
		{
			case "byte"   : return byte   .class;
			case "boolean": return boolean.class;
			case "short"  : return short  .class;
			case "char"   : return char   .class;
			case "int"    : return int    .class;
			case "float"  : return float  .class;
			case "long"   : return long   .class;
			case "double" : return double .class;
			case "void"   : return void   .class;
			default       : return null         ;
		}
	}

	public static boolean isPrimitiveTypeName(final String typeName)
	{
		return primitiveType(typeName) != null;
	}

	public static final boolean isOfAnyType(final Class<?> subject, final Class<?>... supertypes)
	{
		for(final Class<?> s : supertypes)
		{
			if(s.isAssignableFrom(subject))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * *sigh*
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getClass(final T object)
	{
		return (Class<? extends T>)object.getClass();
	}
	
	public static char fieldIdentifierDelimiter()
	{
		return '#';
	}

	public static String deriveFieldIdentifier(final java.lang.reflect.Field field)
	{
		return field.getDeclaringClass().getName() + fieldIdentifierDelimiter() + field.getName();
	}

	public static int getFieldIdentifierDelimiterIndex(final String identifier)
	{
		final int index = identifier.lastIndexOf(fieldIdentifierDelimiter());
		if(index < 0)
		{
			throw new IllegalArgumentException(); // (16.10.2013 TM)TODO: proper Exception
		}
		
		return index;
	}

	public static String getFieldIdentifierClassName(final String fieldIdentifier)
	{
		return fieldIdentifier.substring(0, getFieldIdentifierDelimiterIndex(fieldIdentifier));
	}

	public static String getFieldIdentifierFieldName(final String fieldIdentifier)
	{
		return fieldIdentifier.substring(getFieldIdentifierDelimiterIndex(fieldIdentifier) + 1);
	}

	public static <A> Class<A> validateInterfaceType(final Class<A> type)
	{
		if(!type.isInterface())
		{
			throw UtilStackTrace.cutStacktraceByOne(
				new IllegalArgumentException("Not an interface type:" + type)
			);
		}
		return type;
	}

	public static <A> Class<A> validateNonInterfaceType(final Class<A> type)
	{
		if(type.isInterface())
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException("Interface type:" + type));
		}
		return type;
	}

	public static <A> Class<A> validateNonArrayType(final Class<A> type)
	{
		if(type.isArray())
		{
			throw UtilStackTrace.cutStacktraceByOne(
				new IllegalArgumentException("Array type:" + type)
			);
		}
		return type;
	}

	public static <A> Class<A> validateArrayType(final Class<A> arrayType)
	{
		if(!arrayType.isArray())
		{
			throw UtilStackTrace.cutStacktraceByOne(
				new IllegalArgumentException("Not an array type:" + arrayType)
			);
		}
		return arrayType;
	}

	public static <A> Class<A> validatePrimitiveType(final Class<A> primitiveType)
	{
		if(!primitiveType.isPrimitive())
		{
			throw UtilStackTrace.cutStacktraceByOne(
				new IllegalArgumentException("Not a primitive type:" + primitiveType)
			);
		}
		return primitiveType;
	}

	public static <A> Class<A> validateNonPrimitiveType(final Class<A> primitiveType)
	{
		if(primitiveType.isPrimitive())
		{
			throw UtilStackTrace.cutStacktraceByOne(
				new IllegalArgumentException("Primitive type:" + primitiveType)
			);
		}
		return primitiveType;
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private XReflect()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
