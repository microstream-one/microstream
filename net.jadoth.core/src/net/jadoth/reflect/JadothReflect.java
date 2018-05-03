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


import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.DownwrapList;
import net.jadoth.collections.JadothArrays;
import net.jadoth.collections.ListView;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XPrependingSequence;
import net.jadoth.exceptions.IllegalAccessRuntimeException;
import net.jadoth.exceptions.InstantiationRuntimeException;
import net.jadoth.exceptions.InvocationTargetRuntimeException;
import net.jadoth.exceptions.NoSuchFieldRuntimeException;
import net.jadoth.exceptions.NoSuchMethodRuntimeException;
import net.jadoth.util.JadothTypes;
import net.jadoth.util.aspects.AspectWrapper;
import net.jadoth.util.branching.AbstractBranchingThrow;
import net.jadoth.util.branching.ThrowBreak;
import net.jadoth.util.chars.JadothChars;


/**
 * Provides additional generic util methods for working with java reflection.
 *
 * @author Thomas Muenz
 *
 */
public final class JadothReflect
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// transient to skip fields in persistence layers collecting constants
	public static final transient String CODE_CONVENTION_GETTER_PREFIX   = "get";
	public static final transient String CODE_CONVENTION_ISGETTER_PREFIX = "is" ;
	public static final transient String CODE_CONVENTION_SETTER_PREFIX   = "set";

	/*
	 * JDK workaround stuff. transient to skip fields in persistence layers collecting constants
	 * CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	 */
	private static final transient Field ArrayList_elementData;
	private static final transient Field ArrayList_size       ;
	private static final transient Field HashMap_hashDensity  ;
	private static final transient Field HashSet_map          ;
	// CHECKSTYLE.ON: ConstantName


	static
	{
		/*
		 * will most likely never throw an exception, since those classes are unlikely to change.
		 * Except for non-sun implementations, of course.
		 */
		ArrayList_elementData = setAccessible(getDeclaredFieldOrNull(ArrayList.class, "elementData"));
		ArrayList_size        = setAccessible(getDeclaredFieldOrNull(ArrayList.class, "size"       ));
		HashMap_hashDensity   = setAccessible(getDeclaredFieldOrNull(HashMap.class  , "loadFactor" ));
		HashSet_map           = setAccessible(getDeclaredFieldOrNull(HashSet.class  , "map"        ));

		//handle string class completely. If anything goes wrong, disable reflective access (fields are null)
		//see access~() methods
//		final Field offset = getDeclaredFieldOrNull(String.class, "offset");
//		String_value = offset == null ? null :getDeclaredFieldOrNull(String.class, "value");
//		String_offset = String_value == null? null :offset;

//		AbstractStringBuilder_value = getDeclaredFieldOrNull(classForName("java.lang.AbstractStringBuilder"), "value");
	}
	private static Field getDeclaredFieldOrNull(final Class<?> c, final String name)
	{
		try
		{
			return getDeclaredField(c, name);
		}
		catch(final Exception e)
		{
			return null;
		}
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



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Checks if is interface of type.
	 *
	 * @param interfaceClass the interface class
	 * @param implementedSuperInterface the implemented super interface
	 * @return true, if is interface of type
	 */
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

	/**
	 * Implements interface.
	 *
	 * @param c the c
	 * @param interfaceClass the interface class
	 * @return true, if successful
	 */
	public static final boolean implementsInterface(final Class<?> c, final Class<?> interfaceClass)
	{
		if(c == null || interfaceClass == null || !interfaceClass.isInterface())
		{
			return false;
		}

		final Class<?>[] interfaces = JadothReflect.getClassHierarchyInterfaces(c);
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
		for(int i = JadothTypes.to_int(hierarchy.size()); i-- > 0;)
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

	@SuppressWarnings("null")
	public static Class<?> determineMostSpecificCommonClass(final Object[] objects)
	{
		/* lowestCommonClass can only stay null if the whole array consists only of nulls or is empty.
		 * In which case it is correct. This cant't cause an NPE later on because this special case
		 * is tied to i reaching the array bounds, in which case the second loop is a no-op.
		 *
		 */
		Class<?> lowestCommonClass = null;
		int i = -1;

		// leading nulls special case handling loop
		while(++i < objects.length)
		{
			if(objects[i] != null)
			{
				lowestCommonClass = objects[i].getClass();
				break;
			}
		}

		// main logic loop
		while(++i < objects.length)
		{
			if(objects[i] == null)
			{
				continue;
			}
			// will never throw a NPE here, despite IDE warning (IDE can't figure out the array bounds dependancy)
			while(!lowestCommonClass.isInstance(objects[i]))
			{
				// automatically aborts at Object.class
				lowestCommonClass = lowestCommonClass.getSuperclass();
			}
		}
		return lowestCommonClass;
	}

	public static final ArrayList<Field> getAllFields(final Class<?> c)
	{
		return getAllFields(c, null);
	}

	/**
	 * <b><u>BranchingThrows</u></b>: <br>
	 * - <code>ThrowBreak</code> and <code>ThrowContinue</code> will affect the
	 * outer class loop, not the inner field loop, because after adding (or not adding) a field, the inner loop
	 * does not contain any further logic.<br>
	 * - A <code>ThrowReturn</code> causes the immediate end of the method, returning all fields that have been
	 * found so far.<br>
	 * - Hint objects are ignored in any case.<br>
	 * <p>
	 *
	 * @param c the class of which all fields shall be retrieved
	 * @param fieldFilter the filter for excluding fields. May be null to increase performance
	 * @return all apropriate fields as controlled by <code>fieldFilter</code>.
	 */
	@SuppressWarnings("null") //noExclude constant ensures existance of fieldFilter.
	public static final ArrayList<Field> getAllFields(final Class<?> c, final Predicate<Field> fieldFilter)
	{
		//applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(c.isArray() || c.getSuperclass() == null)
		{
			return new ArrayList<>(0);
		}

		final boolean noExclude = fieldFilter == null; //increase performance if no exclusion is possible
		final ArrayList<Field[]> classFields = new ArrayList<>(20);
		int elementCount = 0;

		Class<?> currentClass = c;
		Field[] currentClassFields;
		while(currentClass != Object.class)
		{
			currentClassFields = currentClass.getDeclaredFields();
			elementCount += currentClassFields.length;
			classFields.add(currentClassFields); // add reversed to keep total order intact
			currentClass = currentClass.getSuperclass();
		}

		final ArrayList<Field> allFields = new ArrayList<>(elementCount);

		// case A: no exclusion
		if(noExclude)
		{
			for(int i = classFields.size(); i-- > 0;)
			{
				for(final Field f : classFields.get(i))
				{
					allFields.add(f);
				}
			}
			return allFields;
		}

		// case B: with exclusion
		Field loopField;
		for(int i = classFields.size(); i-- > 0;)
		{
			currentClassFields = classFields.get(i);

			try
			{
				final int len = currentClassFields.length;
				int j = 0;
				while(j < len)
				{
					if(fieldFilter.test(loopField = currentClassFields[j++]))
					{
						allFields.add(loopField);
					}
				}
			}
			catch(final ThrowBreak b)
			{
				// abort iteration
			}
		}
		return allFields;
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
	 * @param c the class whose fields shall be iterated
	 * @param fieldOperation the {@link Consumer} to be executed on each field.
	 */
	public static final void iterateAllClassFields(final Class<?> c, final Consumer<Field> fieldOperation)
	{
		iterateAllClassFields(c, Object.class, fieldOperation);
	}

	public static final void iterateAllClassFields(
		final Class<?>         c             ,
		final Class<?>         stopClass     ,
		final Consumer<Field> fieldOperation
	)
	{
		// applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(c.isArray() || c.getSuperclass() == null)
		{
			return;
		}
		for(Class<?> currentClass = c; currentClass != stopClass; currentClass = currentClass.getSuperclass())
		{
			final Field[] fields = currentClass.getDeclaredFields();
			try
			{
				int i = 0;
				while(i < fields.length)
				{
					fieldOperation.accept(fields[i++]);
				}
			}
			catch(final ThrowBreak b)
			{
				/* abort inner iteration */
			}
		}
	}

	public static Field[] queryAllFields(final Class<?> type, final Predicate<? super Field> selector)
	{
		final BulkList<Field> fields = BulkList.New(100);

		JadothReflect.iterateAllClassFields(type, field ->
		{
			if(selector.test(field))
			{
				field.setAccessible(true);
				fields.add(field);
			}
		});

		return fields.toArray(Field.class);
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
		//applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(c.isArray() || c.getSuperclass() == null)
		{
			return;
		}

		for(Class<?> currentClass = c; currentClass != Object.class; currentClass = currentClass.getSuperclass())
		{
			final Field[] fields = currentClass.getDeclaredFields();
			try
			{
				int i = fields.length;
				while(i > 0)
				{
					fieldOperation.accept(fields[--i]);
				}
			}
			catch(final ThrowBreak b)
			{
				// abort iteration
			}
		}
	}

	/**
	 * List all fields.
	 *
	 * @param c the c
	 * @param excludedModifiers the excluded modifiers
	 * @return the list
	 */
	public static final ArrayList<Field> getAllFields(final Class<?> c, final int excludedModifiers)
	{
		// note that interfaces can contain constants fields. However interface hiearchy is ignored here intentionally

		Class<?> currentClass = c;
		//10 parent classes should normally be sufficient
		final ArrayList<Field[]> classes = new ArrayList<>();
		int elementCount = 0;
		final boolean noExclude = excludedModifiers == 0;

		Field[] currentClassFields;
		while(currentClass != null && currentClass != Object.class)
		{
			currentClassFields = currentClass.getDeclaredFields();
			elementCount += currentClassFields.length;
			classes.add(currentClassFields);
			currentClass = currentClass.getSuperclass();
		}

		final ArrayList<Field> allFields = new ArrayList<>(elementCount);
		Field loopField;
		for(int i = classes.size() - 1, stop = 0; i >= stop; i--)
		{
			currentClassFields = classes.get(i);
			for(int j = 0, len = currentClassFields.length; j < len; j++)
			{
				if(noExclude)
				{
					allFields.add(currentClassFields[j]);
				}
				else
				{
					loopField = currentClassFields[j];
					if((loopField.getModifiers() & excludedModifiers) == 0)
					{
						allFields.add(loopField);
					}
				}
			}
		}
		return allFields;
	}

	/**
	 * Adds the all fields.
	 *
	 * @param <C> the generic type
	 * @param c the c
	 * @param excludedModifiers the excluded modifiers
	 * @param collection the collection
	 * @return the c
	 */
	public static <C extends Collection<Field>>
	C addAllFields(final Class<?> c, final int excludedModifiers, final C collection)
	{
		collection.addAll(getAllFields(c, excludedModifiers));
		return collection;
	}

	public static final <C extends XPrependingSequence<Field>>
	C collectFields(final C collection, final Class<?> type, final Predicate<Field> predicate)
	{
		JadothReflect.reverseIterateAllClassFields(type,
			new Consumer<Field>()
			{
				@Override
				public void accept(final Field e)
				{
					if(predicate.test(e))
					{
						collection.prepend(e);
					}
				}
			}
		);
		return collection;
	}

	public static final <C extends XPrependingSequence<Field>> C collectFields(final C collection, final Class<?> type)
	{
		JadothReflect.reverseIterateAllClassFields(type,
			new Consumer<Field>()
			{
				@Override
				public void accept(final Field e)
				{
					collection.prepend(e);
				}
			}
		);
		return collection;
	}

	public static final <C extends XPrependingSequence<Field>>
	C collectTypedFields(
		final C collection,
		final Class<?> type,
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

	/**
	 * Gets the any field.
	 *
	 * @param c the c
	 * @param name the name
	 * @return the any field
	 * @throws NoSuchFieldRuntimeException the no such field runtime exception
	 */
	public static final Field getAnyField(final Class<?> c, final String name) throws NoSuchFieldRuntimeException
	{
		// (28.03.2014 TMuenz)XXX: getAnyField: fix useless copying
		final List<Field> allFields = getAllFields(c, 0);
		for(final Field f : allFields)
		{
			if(f.getName().equals(name))
			{
				return f;
			}
		}
		throw new NoSuchFieldRuntimeException(new NoSuchFieldException(name));
	}

	public static final Field getAnyField(final Class<?> c, final Predicate<? super Field> predicate)
		throws NoSuchFieldRuntimeException
	{
		final List<Field> allFields = getAllFields(c, 0);
		for(final Field f : allFields)
		{
			if(predicate.test(f))
			{
				return f;
			}
		}
		return null;
	}

	public static final Field getInstanceFieldOfType(final Class<?> declaringType, final Class<?> fieldType)
		throws NoSuchFieldRuntimeException
	{
		final Field field = JadothReflect.getAnyField(declaringType, new Predicate<Field>()
		{
			@Override
			public boolean test(final Field e)
			{
				return isInstanceField(e) && fieldType.isAssignableFrom(e.getType());
			}
		});
		if(field == null)
		{
			// (28.10.2013 TM)EXCP: proper exception (OMG).
			throw new NoSuchFieldRuntimeException(
				new NoSuchFieldException("Instance field of type " + fieldType.getName() + " in type " + declaringType)
			);
		}
		return field;
	}

	public static final boolean isFinal(final Field field)
	{
		return Modifier.isFinal(field.getModifiers());
	}

	public static final boolean isStatic(final Field field)
	{
		return Modifier.isStatic(field.getModifiers());
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
	 * Accesses the elementData field containint the array holding the elements of <code>arrayList</code>.
	 * <p>
	 * <u><b>Warning</b></u>: Use this method wisely!<br>
	 * In almost all situations, it is not necessary to "peek" inside the <code>ArrayList</code> object and "steal" the
	 * array from it. The use of this method in such situations is bad programming style and can cause any sort of
	 * trouble. E.g. logic manipulating the array while the actual <code>ArrayList</code> object is still active.<br>
	 * <br>
	 * Handle with care!<br>
	 *
	 *
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

	/**
	 * Use this method with care! E.g. only on newly created {@link ArrayList} instances.
	 *
	 * @param arrayList
	 * @param elementData
	 * @param size
	 */
	public static final void setArrayListSize(final ArrayList<?> arrayList, final int size)
	{
		if(size < 0)
		{
			throw new IllegalArgumentException("size may not be negative");
		}
		try
		{
			if(ArrayList_size.isAccessible())
			{
				ArrayList_size.setInt(arrayList, size);
			}
			else
			{
				ArrayList_size.setAccessible(true);
				ArrayList_size.setInt(arrayList, size);
				ArrayList_size.setAccessible(false);
			}
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}


	public static final void setArrayListMembers(
		final ArrayList<?> arrayList  ,
		final Object[]     elementData,
		final int          size
	)
	{
		if(size < 0)
		{
			throw new IllegalArgumentException("size may not be negative");
		}
		if(elementData == null)
		{
			throw new NullPointerException("elementData may not be null");
		}
		if(size > elementData.length)
		{
			throw new IllegalArgumentException("size cannot be greater than elementData length");
		}
		try
		{
			if(ArrayList_size.isAccessible())
			{
				ArrayList_size.setInt(arrayList, size);
			}
			else
			{
				ArrayList_size.setAccessible(true);
				ArrayList_size.setInt(arrayList, size);
				ArrayList_size.setAccessible(false);
			}
			if(ArrayList_elementData.isAccessible())
			{
				ArrayList_elementData.set(arrayList, elementData);
			}
			else
			{
				ArrayList_elementData.setAccessible(true);
				ArrayList_elementData.set(arrayList, elementData);
				ArrayList_elementData.setAccessible(false);
			}
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}





	/**
	 * Thie methods sets the hashDensity of an existing HashMap.<br>
	 * It's hard to understand why HashMap provides no setter for it so it could be changed in a normal way.
	 *
	 * @param hashMap
	 * @param hashDensity see {@link HashMap} for allowed values. May not be null.
	 * @throws IllegalArgumentException if <code>hashDensity</code> is illegal for {@link HashMap}
	 */
	public static final void setHashDensity(final HashMap<?, ?> hashMap, final Float hashDensity)
		throws IllegalArgumentException
	{
		final float hashDensityValue = hashDensity.floatValue(); //provoke NullPointer
		if(hashDensityValue <= 0 || Float.isNaN(hashDensityValue))
		{
            throw new IllegalArgumentException("Illegal hash density: " + hashDensity);
		}
		setFieldValue(HashMap_hashDensity, hashMap, hashDensity);
	}



	public static final void setHashDensity(final HashSet<?> hashSet, final Float hashDensity)
		throws IllegalArgumentException
	{
		//works for LinkedHashSet as well
		setHashDensity((HashMap<?, ?>)getFieldValue(HashSet_map, hashSet), hashDensity);
	}





	// Method Tools //

	/* (08.09.2009 TM)NOTE:
	 * The Method block is genereted out of the Field block
	 * by replacing "Field" with "Method".
	 * Except the single getXXXMethod() Methods.
	 *
	 * For all other methods: Do not edit twice! Delete and replace again instead!
	 *
	 */
	public static final Method[] getAllMethods(final Class<?> c)
	{
		return getAllMethods(c, 0);
	}

	public static final Method[] getAllMethods(final Class<?> c, final int excludedModifiers)
	{
		final List<Method> allMethods = listAllMethods(c, excludedModifiers);
		return allMethods.toArray(new Method[allMethods.size()]);
	}

	public static final List<Method> listAllMethods(final Class<?> c, final int excludedModifiers)
	{
		if(c == Object.class || c.isInterface())
		{
			return new ArrayList<>();
		}
		Class<?> currentClass = c;
		//10 parent classes should normally be sufficient
		final ArrayList<Method[]> classes = new ArrayList<>(10);
		int elementCount = 0;
		final boolean noExclude = excludedModifiers == 0;

		Method[] currentClassMethods;
		while(currentClass != null && currentClass != Object.class)
		{
			currentClassMethods = currentClass.getDeclaredMethods();
			elementCount += currentClassMethods.length;
			classes.add(currentClassMethods);
			currentClass = currentClass.getSuperclass();
		}

		final ArrayList<Method> allMethods = new ArrayList<>(elementCount);
		for(int i = classes.size() - 1, stop = 0; i >= stop; i--)
		{
			currentClassMethods = classes.get(i);
			for(int j = 0, len = currentClassMethods.length; j < len; j++)
			{
				if(noExclude)
				{
					allMethods.add(currentClassMethods[j]);
				}
				else
				{
					if((currentClassMethods[j].getModifiers() & excludedModifiers) == 0)
					{
						allMethods.add(currentClassMethods[j]);
					}
				}
			}
		}
		return allMethods;
	}

	public static <C extends Collection<Method>> C addAllMethods(
		final Class<?> c,
		final int excludedModifiers,
		final C collection
	)
	{
		collection.addAll(listAllMethods(c, excludedModifiers));
		return collection;
	}

	public static final Method getDeclaredMethod(final Class<?> c, final String name, final Class<?>... parameterTypes)
		throws NoSuchMethodRuntimeException
	{
		try
		{
			return c.getDeclaredMethod(name, parameterTypes);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}
	}

	public static final Method getDeclaredMethodFirstNamed(final Class<?> c, final String name)
		throws NoSuchMethodRuntimeException
	{
		final Method foundMethod = JadothArrays.search(
			c.getDeclaredMethods(),
			t ->
				t.getName().equals(name)
		);

		if(foundMethod == null)
		{
			throw new NoSuchMethodRuntimeException(new NoSuchMethodException(name));
		}

		return foundMethod;
	}

	public static final Method getMethod(final Class<?> c, final String name, final Class<?>... parameterTypes)
		throws NoSuchMethodRuntimeException
	{
		try
		{
			return c.getMethod(name, parameterTypes);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}
	}

	public static final Method getAnyMethod(final Class<?> c, final String name, final Class<?>... parameterTypes)
		throws NoSuchMethodRuntimeException
	{
		final Class<?>[] nonNullParamterTypes = parameterTypes != null
			? parameterTypes
			: new Class<?>[0]
		;

		final List<Method> allMethods = listAllMethods(c, 0);
		for(final Method f : allMethods)
		{
			if(f.getName().equals(name) && Arrays.equals(f.getParameterTypes(), nonNullParamterTypes))
			{
				return f;
			}
		}

		throw new NoSuchMethodRuntimeException(new NoSuchMethodException(name));
	}

	public static final boolean validateLabelValue(final Label label, final String value)
	{
		if(label == null)
		{
			return false;
		}
		if(value == null)
		{
			return true;
		}

		final String[] values = label.value();
		for(final String v : values)
		{
			if(v.equals(value))
			{
				return true;
			}
		}
		return false;
	}

	public static final <E extends AnnotatedElement, C extends Collection<E>> C getMemberCollectionByLabel(
		final String label,
		final E[] elements,
		final C collection
	)
	{
		if(collection == null || elements == null)
		{
			return null;
		}
		for(final E f : elements)
		{
			if(f.isAnnotationPresent(Label.class))
			{
				for(final String s : f.getAnnotation(Label.class).value())
				{
					if(s.equals(label))
					{
						collection.add(f);
					}
				}
			}
		}
		return collection;
	}

	public static final <E extends AnnotatedElement> E[] getMembersByLabel(final String label, final E[] elements)
	{
		if(elements == null)
		{
			return null;
		}
		final ArrayList<E> labeledElements = getMemberCollectionByLabel(label, elements, new ArrayList<E>());
		return labeledElements.toArray(
			JadothArrays.newArrayBySample(elements, labeledElements.size())
		);
	}

	public static final <E extends AnnotatedElement> E getMemberByLabel(final String label, final E[] elements)
	{
		if(elements == null)
		{
			return null;
		}
		for(final E f : elements)
		{
			if(f.isAnnotationPresent(Label.class))
			{
				for(final String s : f.getAnnotation(Label.class).value())
				{
					if(s.equals(label))
					{
						return f;
					}
				}
			}
		}
		return null;
	}

	public static final <E extends AnnotatedElement, C extends Collection<E>> C getMemberCollectionWithAnnotation(
		final Class<? extends Annotation> annotation,
		final E[] elements,
		final C collection
	)
	{
		if(collection == null || elements == null)
		{
			return null;
		}
		for(final E f : elements)
		{
			if(f.isAnnotationPresent(annotation))
			{
				collection.add(f);
			}
		}
		return collection;
	}

	public static final <E extends AnnotatedElement> E[] getMembersWithAnnotation(
		final Class<? extends Annotation> annotation, final E[] elements
	)
	{
		if(elements == null)
		{
			return null;
		}
		final ArrayList<E> labeledElements = getMemberCollectionWithAnnotation(annotation, elements, new ArrayList<E>());
		return labeledElements.toArray(JadothArrays.newArrayBySample(elements, labeledElements.size()));
	}

	public static final <E extends AnnotatedElement> E getMemberWithAnnotation(
		final Class<? extends Annotation> annotation, final E[] elements
	)
	{
		if(elements == null)
		{
			return null;
		}
		for(final E f : elements)
		{
			if(f.isAnnotationPresent(annotation))
			{
				return f;
			}
		}
		return null;
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



	  /////////////////////////////////////////////////////////////////////////
	 // Array Tools    //
	////////////////////

	public static final Class<?>[] getTypes(final Object... objects)
	{
		final Class<?>[] paramTypes = new Class<?>[objects.length];
		for(int i = 0; i < objects.length; i++)
		{
			paramTypes[i] = objects[i].getClass();
		}
		return paramTypes;
	}

	/**
	 * Returns the simple name of the class preceeded by all of its enclosing classes, connected with a "$".
	 *
	 * @param c the c
	 * @return the full enclosing class name
	 * @return
	 */
	public static final String getFullEnclosingClassName(final Class<?> c)
	{
		final String name = c.getName();
		final int lastDotIndex = name.lastIndexOf('.');
		if(lastDotIndex == -1)
		{
			return name;
		}
		return name.substring(lastDotIndex + 1);
	}


	/**
	 * Checks if is static nested class.
	 *
	 * @param c the c
	 * @return true, if is static nested class
	 */
	public static final boolean isStaticNestedClass(final Class<?> c)
	{
		return c.isMemberClass() && !isInnerNestedClass(c);
	}

	/**
	 * Checks if is inner nested class.
	 *
	 * @param c the c
	 * @return true, if is inner nested class
	 */
	public static final boolean isInnerNestedClass(final Class<?> c)
	{
		if(!c.isMemberClass())
		{
			return false;
		}
		final Class<?> enclosingClass = c.getEnclosingClass();

		final Field[] declFields = c.getDeclaredFields();

		// at least one field must be a reference to the enclosing class
		boolean enclClassRefPresent = false;
		for(final Field f : declFields)
		{
			if(f.getType() == enclosingClass)
			{
				enclClassRefPresent = true;
			}
		}
		if(!enclClassRefPresent)
		{
			return false;
		}

		// every constructor must have the type of the enclosing class as a first parameter
		final Constructor<?>[] cons = c.getConstructors();
		for(final Constructor<?> con : cons)
		{
			final Class<?>[] pTypes = con.getParameterTypes();
			if(pTypes.length == 0 || pTypes[0] != enclosingClass)
			{
				return false;
			}
		}
		//all tests passed: class is most likely a nested inner class (directly or self-tinkered static wise)
		return true;
	}




	/**
	 * Gets the full class name.
	 *
	 * @param c the c
	 * @return the full class name
	 */
	public static final String getFullClassName(final Class<?> c)
	{
		return getFullClassName(c, null);
	}

	/**
	 * Returns the full class name without packages of class <code>c</code>.
	 * <p>
	 * Examples:<br>
	 * full class name of String.class: "String"
	 * full class name of HashMap.Entry.class: "HashMap.Entry"
	 * <p>
	 * This is useful if the enclosing class has already been imported,
	 * so that the inner class does not have to be referenced full qualified.
	 * <p>
	 * If <code>enclosingClassParameters</code> is not null, then generic bounds parameter
	 * will be applied to enclosing classes. If <code>enclosingClassParameters</code> does not
	 * contain a bounds parameter string for a parametrized class, "?" will be used as a bounds parameter.
	 * <p>
	 * If <code>enclosingClassParameters</code> is null, no generics bounds parameter will be applied
	 *
	 * @param c the c
	 * @param enclosingClassParameters the enclosing class parameters
	 * @return the full class name
	 * @return
	 */
	public static final String getFullClassName(final Class<?> c, final Map<Class<?>, String> enclosingClassParameters)
	{
		Class<?> currentClass = c;
		final List<Class<?>> enclosingClasses = new ArrayList<>();
		final StringBuilder sb = new StringBuilder(256);

		while((currentClass = currentClass.getEnclosingClass()) != null)
		{
			enclosingClasses.add(currentClass);
		}

		int i = enclosingClasses.size();
		while(i-- > 0)
		{
			currentClass = enclosingClasses.get(i);
			sb.append(currentClass.getSimpleName());
			if(enclosingClassParameters != null && currentClass.getTypeParameters().length > 0)
			{
				final String param =  enclosingClassParameters.get(currentClass);
				sb.append('<').append(param == null ? '?' : param).append('>');
			}
			sb.append('.');
		}
		sb.append(c.getSimpleName());
		return sb.toString();
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
		// stupid JDK once again. Unbelievable
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

	/**
	 * Class new instance.
	 *
	 * @param <I> the generic type
	 * @param constructor the constructor
	 * @param initargs the initargs
	 * @return the i
	 * @throws InvocationTargetRuntimeException the invocation target runtime exception
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public static final <I> I classNewInstance(final Constructor<I> constructor, final Object... initargs)
		throws InvocationTargetRuntimeException, IllegalAccessRuntimeException, InvocationTargetRuntimeException
	{
		try
		{
			return constructor.newInstance(initargs);
		}
		catch(final InvocationTargetException e)
		{
			throw new InvocationTargetRuntimeException(e);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
		catch(final InstantiationException e)
		{
			throw new InstantiationRuntimeException(e);
		}
	}

	/**
	 * Class get constructor.
	 *
	 * @param <I> the generic type
	 * @param c the c
	 * @param parameterTypes the parameter types
	 * @return the constructor
	 * @throws NoSuchMethodRuntimeException the no such method runtime exception
	 */
	public static final <I> Constructor<I> classGetConstructor(final Class<I> c, final Class<?>... parameterTypes)
		throws NoSuchMethodRuntimeException
	{
		try
		{
			return c.getConstructor(parameterTypes);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}
	}

	/**
	 * Inner class get constructor.
	 *
	 * @param <I> the generic type
	 * @param c the c
	 * @param parameterTypes the parameter types
	 * @return the constructor
	 * @throws NoSuchMethodRuntimeException the no such method runtime exception
	 */
	public static final <I> Constructor<I> innerClassGetConstructor(final Class<I> c, final Class<?>... parameterTypes)
		throws NoSuchMethodRuntimeException
	{
		final Class<?> outerClass = c.getEnclosingClass();
		final Class<?>[] actualParamTypes = new Class<?>[parameterTypes == null ? 1 : parameterTypes.length + 1];
		actualParamTypes[0] = outerClass;
		if(parameterTypes != null)
		{
			for(int i = 0; i < parameterTypes.length; i++)
			{
				actualParamTypes[i + 1] = parameterTypes[i];
			}
		}
		return classGetConstructor(c, actualParamTypes);
	}

	/**
	 * Inner class new instance.
	 *
	 * @param <I> the generic type
	 * @param constructor the constructor
	 * @param enclosingInstance the enclosing instance
	 * @param initargs the initargs
	 * @return the i
	 * @throws InvocationTargetRuntimeException the invocation target runtime exception
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public static final <I> I innerClassNewInstance(
		final Constructor<I> constructor      ,
		final Object         enclosingInstance,
		final Object...      initargs
	)
		throws InvocationTargetRuntimeException, IllegalAccessRuntimeException, InvocationTargetRuntimeException
	{
		return classNewInstance(constructor, enclosingInstance, initargs);
	}

	/**
	 * Validate setter.
	 *
	 * @param setter the setter
	 * @param type the type
	 * @param mustReturnVoid the must return void
	 * @return true, if successful
	 */
	public static boolean validateSetter(final Method setter, final Class<?> type, final boolean mustReturnVoid)
	{
		final Class<?>[] paramTypes = setter.getParameterTypes();
		if(paramTypes.length != 1)
		{
			return false;
		}
		if(paramTypes[0] != type)
		{
			return false;
		}
		if(mustReturnVoid && setter.getReturnType() != Void.TYPE)
		{
			return false;
		}
		return true;
	}

	/**
	 * Validate getter.
	 *
	 * @param getter the getter
	 * @param type the type
	 * @return true, if successful
	 */
	public static boolean validateGetter(final Method getter, final Class<?> type)
	{
		final Class<?>[] paramTypes = getter.getParameterTypes();
		if(paramTypes.length != 0)
		{
			return false;
		}
		if(getter.getReturnType() != type)
		{
			return false;
		}
		return true;
	}

	/**
	 * Derive setter name from field name.
	 *
	 * @param fieldName the field name
	 * @return the string
	 */
	public static String deriveSetterNameFromFieldName(final String fieldName)
	{
		return JadothChars.createMedialCapitalsString(CODE_CONVENTION_SETTER_PREFIX, fieldName);
	}

	/**
	 * Derive getter name from field name.
	 *
	 * @param fieldName the field name
	 * @param usePrefix_is the use prefix_is
	 * @return the string
	 */
	public static String deriveGetterNameFromFieldName(final String fieldName, final boolean usePrefix_is)
	{
		return JadothChars.createMedialCapitalsString(
			usePrefix_is ? CODE_CONVENTION_ISGETTER_PREFIX : CODE_CONVENTION_GETTER_PREFIX,
			fieldName
		);
	}

	/**
	 * Derive setter name from field.
	 *
	 * @param field the field
	 * @return the string
	 */
	public static String deriveSetterNameFromField(final Field field)
	{
		return deriveSetterNameFromFieldName(field.getName());
	}

	/**
	 * Derive getter name from field.
	 *
	 * @param field the field
	 * @param usePrefix_is_forBoolean the use prefix_is_for boolean
	 * @return the string
	 */
	public static String deriveGetterNameFromField(final Field field, final boolean usePrefix_is_forBoolean)
	{
		return deriveGetterNameFromFieldName(
			field.getName(),
			usePrefix_is_forBoolean && JadothTypes.isBoolean(field.getType())
		);
	}


	/**
	 * Derive getter name from field.
	 *
	 * @param field the field
	 * @return the string
	 */
	public static String deriveGetterNameFromField(final Field field)
	{
		return deriveGetterNameFromField(field, true);
	}

	public static <E, T, C extends Consumer<T>> C selectByType(
		final XGettingCollection<E> collection ,
		final C                     target     ,
		final Class<T>              queriedType
	)
	{
		collection.iterate(table ->
		{
			if(queriedType.isInstance(table))
			{
				target.accept(queriedType.cast(table));
			}
		});
		return target;
	}

//	public static <T, S> ConstList<S> selectByType(
//		final XGettingCollection<T> collection,
//		final Class<S> queriedType
//	)
//	{
//		return selectByType(collection, new BulkList<S>(collection.size()), queriedType).immure();
//	}




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

	
	public static char fieldIdentifierDelimiter()
	{
		return '#';
	}
	
	public static String deriveFieldIdentifier(final Field field)
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


//	/**
//	 * Call default constructor.
//	 *
//	 * @param <O> the generic type
//	 * @param c the c
//	 * @param enclosingInstance the enclosing instance
//	 * @return the o
//	 * @return
//	 */
//	public static final <O> O callDefaultConstructor(final Class<O> c, final Object enclosingInstance)
//	{
//		// (17.01.2010)FIX-ME: Check AnonymousClass handling. Static reference, etc.
//		if(isInnerNestedClass(c) || c.isAnonymousClass())
//		{
//			final Class<?> enclosingClass = c.getEnclosingClass();
//			if(enclosingInstance != null && enclosingInstance.getClass() != enclosingClass)
//			{
//				throw new InvalidClassRuntimeException(
//					new InvalidClassException(enclosingInstance.getClass().getName(), null)
//				);
//			}
//			return callConstructor(c, new Class<?>[]{enclosingClass}, new Object[]{enclosingInstance});
//		}
//		return callConstructor(c, null, (Object[])null);
//	}
//
//	/**
//	 * Call default constructor.
//	 *
//	 * @param <O> the generic type
//	 * @param c the c
//	 * @return the o
//	 */
//	public static final <O> O callDefaultConstructor(final Class<O> c)
//	{
//		return callConstructor(c, null, (Object[])null);
//	}
//
//	public static final <O> O callConstructor(final Class<O> c, final Object... paramValues)
//	{
//		if(paramValues == null || paramValues.length == 0)
//		{
//			return callConstructor(c, null, (Object[])null);
//		}
//		return callConstructor(c, getTypes(paramValues), paramValues);
//	}
//
//	/**
//	 * Call constructor.
//	 *
//	 * @param <O> the generic type
//	 * @param c the c
//	 * @param paramTypes the param types
//	 * @param paramValues the param values
//	 * @return the o
//	 */
//	public static final <O> O callConstructor(
//		final Class<O> c,
//		final Class<?>[] paramTypes,
//		final Object... paramValues
//	)
//	{
//		Constructor<O> cr;
//		try
//		{
//			if(paramTypes == null)
//			{
//				cr = c.getDeclaredConstructor();
//			}
//			else
//			{
//				cr = c.getDeclaredConstructor(paramTypes);
//				if(paramValues == null)
//				{
//					//expand null paramValues to array of nulls
//					paramValues = new Object[paramTypes.length];
//				}
//			}
//		}
//		catch(final NoSuchMethodException e)
//		{
//			throw new NoSuchMethodRuntimeException(e);
//		}
//
//		synchronized(cr)
//		{
//			if(cr.isAccessible())
//			{
//				try
//				{
//					return cr.newInstance(paramValues);
//				}
//				catch(final IllegalAccessException e)
//				{
//					throw new IllegalAccessRuntimeException(e);
//				}
//				catch(final InvocationTargetException e)
//				{
//					throw new InvocationTargetRuntimeException(e);
//				}
//				catch(final InstantiationException e)
//				{
//					throw new InstantiationRuntimeException(e);
//				}
//			}
//			cr.setAccessible(true);
//			try
//			{
//				return cr.newInstance(paramValues);
//			}
//			catch(final IllegalAccessException e)
//			{
//				throw new IllegalAccessRuntimeException(e);
//			}
//			catch(final InvocationTargetException e)
//			{
//				throw new InvocationTargetRuntimeException(e);
//			}
//			catch(final InstantiationException e)
//			{
//				throw new InstantiationRuntimeException(e);
//			}
//			finally
//			{
//				cr.setAccessible(false);
//			}
//		}
//	}



	private JadothReflect()
	{
		// static only
		throw new UnsupportedOperationException();
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

	@SuppressWarnings("unchecked") // safe by method parameter
	public static <T> Class<T> getClass1(final T instance)
	{
		return (Class<T>)instance.getClass(); // why oh why?
	}

	/**
	 * Magically causes an instance of type {@code T} to be usable as if it was of type {@code S extends T}.
	 * It can best be seen as a reflective-wise "hard" downcast wrapper (hence the name).
	 * <p>
	 * <b>Caution: This technique is pure sin!</b>
	 * <p>
	 * It is the type-wise complementary to {@link #downwrap(Object, Class)} and a generic decorator version of
	 * explicit downwrapping implementations like {@link DownwrapList}. It's documentation applies to the mechanics
	 * of this method as well: it has to be seen as a workaround tool for special situations (e.g. compatibility to a
	 * foreign codebase API). Relying on it by design is nothing but bad and broken.
	 * <p>
	 * If this intentionally scarce documentation was not enough, do not use this method!
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be downwrapped to the given downwrap type.
	 * @param downwrapType the interface sub type the passed subject shall be downwrapped to.
	 * @return a reflection wise downwrapped instance of type {@literal S} of the passed subject.
	 */
	@SuppressWarnings("unchecked")
	public static final <T, S extends T> S downwrap(final T subject, final Class<S> downwrapType)
	{
		if(!downwrapType.isInterface())
		{
			throw new IllegalArgumentException("downwrap type is not an interface");
		}
		return (S)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{downwrapType},
			new AspectWrapper<>(subject)
		);
	}

	/**
	 * Reduces the type of the passed instance to a super type interface. It can best be seen as a reflective-wise
	 * "hard" upcast wrapper (hence the name).
	 * <p>
	 * This is effectively a generic decorater implementation realized via dynamic proxy instantiation.
	 * <p>
	 * A very good example is a read-only access on a mutable collection instance:<br>
	 * The type {@link XList} extends the type {@link XGettingList} (and combines it with other aspects like
	 * adding, removing, etc. to create a full scale general purpose list type).<br>
	 * In certain situations, it is necessary that certain code (e.g. an external framework) can only read
	 * but never modify the collection's content. Just casting the {@link XList} instance won't suffice here,
	 * as the receiving code could still do an {@code instanceof } check and downcast the passed instance.<br>
	 * What is really needed is an actual decorator instance, wrapping the general purpose type instance and
	 * relaying only the reading procedures.<br>
	 * For this particular example, there's an explicit decorator type, {@link ListView}.<br>
	 * For other situations, where there is no explicit decorator type (or not yet), this method provides
	 * a solution to create a generic decorator instance.
	 * <p>
	 * Note that the genericity comes at the price of performance, as it purely consists of reflection calls.
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be upwrapped to the given upwrap type.
	 * @param upwrapType the interface super type the passed subject shall be upwrapped to.
	 * @return a reflection wise upwrapped instance of type {@literal T} of the passed subject.
	 * @throws IllegalArgumentException if the passed unwrap type is not an interface.
	 */
	@SuppressWarnings("unchecked")
	public static final <T, S extends T> T upwrap(final S subject, final Class<T> upwrapType)
	{
		if(!upwrapType.isInterface())
		{
			throw new IllegalArgumentException("upwrap type is not an interface");
		}
		return (T)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{upwrapType},
			new AspectWrapper<T>(subject)
		);
	}
}
