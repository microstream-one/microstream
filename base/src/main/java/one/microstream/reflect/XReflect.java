package one.microstream.reflect;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */


import static one.microstream.X.notEmpty;
import static one.microstream.X.notNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XReference;
import one.microstream.exceptions.IllegalAccessRuntimeException;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.exceptions.MemoryException;
import one.microstream.exceptions.NoSuchFieldRuntimeException;
import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.functional.Instantiator;
import one.microstream.functional.XFunc;
import one.microstream.memory.XMemory;
import one.microstream.typing.XTypes;
import one.microstream.util.UtilStackTrace;


/**
 * Provides additional generic util methods for working with java reflection.
 *
 */
public final class XReflect
{
	public static final <T> T defaultInstantiate(final Class<T> type)
		throws NoSuchMethodRuntimeException, InstantiationRuntimeException
	{
		final Constructor<T> defaultConstructor;
		try
		{
			defaultConstructor = type.getConstructor();
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}

		try
		{
			return defaultConstructor.newInstance();
		}
		catch(final InstantiationException e)
		{
			throw new InstantiationRuntimeException(e);
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static final Field setAccessible(final Class<?> actualClass, final Field field)
	{
		try
		{
			return setAccessible(field);
		}
		catch(final SecurityException e)
		{
			throw new SecurityException(toFullQualifiedFieldName(actualClass, field), e);
		}
		catch(final RuntimeException e)
		{
			throw new RuntimeException(toFullQualifiedFieldName(actualClass, field), e);
		}
		catch(final Error e)
		{
			throw new Error(toFullQualifiedFieldName(actualClass, field), e);
		}
	}

	// convenience method to allow simpler functional programming via method reference for that often needed use-case.
	public static final Field setAccessible(final Field field)
		throws SecurityException
	{
		field.setAccessible(true);

		return field;
	}
	
	// convenience method to allow simpler functional programming via method reference for that often needed use-case.
	public static final Method setAccessible(final Method method)
		throws SecurityException
	{
		method.setAccessible(true);

		return method;
	}
	
	// convenience method to allow simpler functional programming via method reference for that often needed use-case.
	public static final <T> Constructor<T> setAccessible(final Constructor<T> constructor)
		throws SecurityException
	{
		constructor.setAccessible(true);

		return constructor;
	}

	public static final boolean isInstanceField(final Field field)
	{
		return !Modifier.isStatic(field.getModifiers());
	}

	public static final String toFieldName(final Field field)
	{
		return field.getName();
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

	/*
	 * Welcome to a method checking if a "Class" is a class
	 */
	public static final boolean isActualClass(final Class<?> type)
	{
		return !type.isInterface()
			&& !type.isPrimitive()
			&& !type.isAnnotation()
			&& !type.isArray()
			&& !type.isSynthetic()
		;
	}

	/**
	 * Utility method fixing the WRONGLY implemented {@link Class#isEnum()}.
	 * <p>
	 * Their description is weird (<i>"if this class was declared as an enum in the source code"</i>) and the
	 * implemented behavior fails to identify all classes of instances that are enums.
	 * <p>
	 * For enum anonymous inner class instances (writing { ... } behind an enum constant), {@link Class#isEnum()}
	 * returns {@literal false} on the generated type. That is a bug since the type is still an enum, a sub class
	 * of {@link java.lang.Enum}. So the correct way of testing a class for being an enum is using
	 * {@code java.lang.Enum.class.isAssignableFrom(...)}. This method does that.
	 *
	 * @param c the {@link Class} to be tested.
	 *
	 * @return whether the passed {@link Class} is a sub class of {@link java.lang.Enum}, i.e. an {@literal enum}.
	 */
	public static boolean isEnum(final Class<?> c)
	{
		return java.lang.Enum.class.isAssignableFrom(c);
	}

	public static boolean isDeclaredEnum(final Class<?> c)
	{
		return c != null && c.isEnum();
	}

	public static boolean isSubEnum(final Class<?> c)
	{
		return c != null && isDeclaredEnum(c.getSuperclass());
	}

	public static Class<?> getDeclaredEnumClass(final Class<?> c)
	{
		return !isEnum(c)
			? null
			: isDeclaredEnum(c)
				? c
				: c.getSuperclass()
		;
	}

	public static Object resolveEnumConstantInstance(final Class<?> type, final int ordinal)
	{
		validateIsEnum(type);

		// Class detour required for AIC-like special subclass enums constants.
		final Object[] jvmEnumConstants = XReflect.getDeclaredEnumClass(type).getEnumConstants();

		return jvmEnumConstants[ordinal];
	}

	public static <T> T resolveEnumConstantInstanceTyped(final Class<T> type, final int ordinal)
	{
		/*
		 * Required for AIC-like special subclass enums constants:
		 * The instance is actually of type T, but it is stored in a "? super T" array of its parent enum type.
		 */
		final Object enumConstantInstance = XReflect.resolveEnumConstantInstance(type, ordinal);

		// compensate the subclass typing hassle
		@SuppressWarnings("unchecked")
		final T enumConstantinstance = (T)enumConstantInstance;

		return enumConstantinstance;
	}



	public static <T> Class<T> validateIsEnum(final Class<T> type)
	{
		if(XReflect.isEnum(type))
		{
			return type;
		}
		throw new IllegalArgumentException("Not an enum type: " + type);
	}

	/**
	 * Alias for {@code iterateDeclaredFieldsUpwards(startingClass, Object.class, logic)}.
	 *
	 * @param <L> The logic's contextual type.
	 *
	 * @param startingClass the class whose fields shall be iterated.
	 * @param logic the {@link Consumer} to be executed on each field.
	 *
	 * @return the passed {@literal logic}.
	 */
	public static final <L extends Consumer<Field>> L iterateDeclaredFieldsUpwards(
		final Class<?> startingClass ,
		final L        logic
	)
	{
		return iterateDeclaredFieldsUpwards(startingClass, Object.class, logic);
	}

	/**
	 * Iterates over every declared field of all classes upwards starting at {@literal startingClass}
	 * until class {@literal boundingClass} is reached and executes the passed {@link Consumer} on it.
	 * <p>
	 * The declared fields of each class are iterated in reverse order
	 * (from index {@link Class#getDeclaredFields()}{@code .length - 1} to index {@code 0} ).
	 * <p>
	 * This method is useful to maintain the natural declaration order of the fields, iterating from the last
	 * declared field of the lowest class (the passed class itself) to the first declared field of the highest class
	 * declaring a field.
	 *
	 * @param <L> The logic's contextual type.
	 *
	 * @param startingClass the class whose fields shall be iterated.
	 * @param boundingClass the class in the hierarchy at which to stop iterating, exclusive bound.
	 * @param logic the {@link Consumer} to be executed on each field.
	 *
	 * @return the passed {@literal logic}.
	 */
	public static final <L extends Consumer<Field>> L iterateDeclaredFieldsUpwards(
		final Class<?> startingClass,
		final Class<?> boundingClass,
		final L        logic
	)
	{
		// applies to Object.class, Void.class, interfaces, primitives. See Class.getSuperclass() JavaDoc.
		if(startingClass.isArray() || startingClass.getSuperclass() == null)
		{
			return logic;
		}

		try
		{
			for(Class<?> currentClass = startingClass; currentClass != Object.class; currentClass = currentClass.getSuperclass())
			{
				final Field[] fields = currentClass.getDeclaredFields();
				for(int i = fields.length; i-- > 0;)
				{
					logic.accept(fields[i]);
				}
			}

		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}

		return logic;
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
	
	public static final Method getDeclaredMethod(final Class<?> c, final String name, final Class<?>... parameterTypes) throws NoSuchFieldRuntimeException
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
	
	public static final <T> Constructor<T> getDeclaredConstructor(final Class<T> c, final Class<?>... parameterTypes) throws NoSuchFieldRuntimeException
	{
		try
		{
			return c.getDeclaredConstructor(parameterTypes);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
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
			// (28.10.2013 TM)EXCP: proper exception
			throw new NoSuchFieldRuntimeException(
				new NoSuchFieldException("No field with name " + name + " found in type " + c)
			);
		}
	}

	public static final Field getAnyField(final Class<?> c, final Predicate<? super Field> predicate)
		throws NoSuchFieldRuntimeException
	{
		final XReference<Field> result = X.Reference(null);
		iterateDeclaredFieldsUpwards(c, field ->
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
			// (28.10.2013 TM)EXCP: proper exception
			throw new NoSuchFieldRuntimeException(
				new NoSuchFieldException(
					"No instance field of type " + fieldType.getName() + " found in type " + declaringType
				)
			);
		}
	}

	public static final Method getAnyMethod(
		final Class<?> c   ,
		final String   name
	)
		throws NoSuchMethodRuntimeException
	{
		notNull(name);

		try
		{
			return getAnyMethod(c, method ->
			name.equals(method.getName())
		);
		}
		catch(final NoSuchFieldRuntimeException e)
		{
			// (28.10.2013 TM)EXCP: proper exception
			throw new NoSuchMethodRuntimeException(
				new NoSuchMethodException("No method with name " + name + " found in type " + c)
			);
		}
	}

	public static final Method getAnyMethod(
		final Class<?>                  c        ,
		final Predicate<? super Method> predicate
	)
		throws NoSuchMethodRuntimeException
	{
		final XReference<Method> result = X.Reference(null);
		iterateAllClassMethods(c, field ->
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

		throw new NoSuchMethodRuntimeException(new NoSuchMethodException());
	}

	public static final <C extends Consumer<? super Method>> C iterateAllClassMethods(
		final Class<?> clazz,
		final C        logic
	)
	{
		return iterateAllClassMethods(clazz, Object.class, logic);
	}

	public static final <C extends Consumer<? super Method>> C iterateAllClassMethods(
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
				for(final Method method : currentClass.getDeclaredMethods())
				{
					logic.accept(method);
				}
			}
		}
		catch(final ThrowBreak b)
		{
			/* abort inner iteration */
		}

		return logic;
	}

	public static final boolean isFinal(final Member field)
	{
		return Modifier.isFinal(field.getModifiers());
	}

	public static final boolean isStatic(final Member field)
	{
		return Modifier.isStatic(field.getModifiers());
	}

	public static final boolean isSynthetic(final Member field)
	{
		return Modifier.isSynchronized(field.getModifiers());
	}

	public static final boolean isStaticFinal(final Member field)
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

	public static final boolean isPrivate(final Member field)
	{
		return Modifier.isPrivate(field.getModifiers());
	}

	public static final boolean isProtected(final Member field)
	{
		return Modifier.isProtected(field.getModifiers());
	}

	public static final boolean isPublic(final Member field)
	{
		return Modifier.isPublic(field.getModifiers());
	}

	public static final boolean isDefaultVisible(final Member field)
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
	 * Calls {@link Field#get(Object)} and wraps the abstraction-destroying checked
	 * {@link IllegalAccessException} with a proper {@link IllegalAccessRuntimeException}.
	 *
	 * @param field the field from which the value shall be extracted.
	 * @param obj object from which the represented field's value is to be extracted
	 * @return the value of the represented field in object {@code obj};
	 *         primitive values are wrapped in an appropriate object before being returned
	 */
	public static final Object getFieldValue(final Field field, final Object obj)
	{
		try
		{
			return field.get(obj);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}
	
	public static final <T> T invoke(final Constructor<T> constructor, final Object... args)
	{
		try
		{
			return constructor.newInstance(args);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T invoke(final Method method, final Object instance, final Object... args)
	{
		try
		{
			return (T)method.invoke(instance, args);
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calls {@link Field#set(Object, Object)} and wraps the abstraction-destroying checked
	 * {@link IllegalAccessException} with a proper {@link IllegalAccessRuntimeException}.
	 *
	 * @param field the field to be modified
	 * @param obj the object whose field should be modified
	 * @param value the new value for the field of {@code obj} being modified
	 */
	public static final void setFieldValue(final Field field, final Object obj, final Object value)
	{
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

	/**
	 * Resolves the passed type name to a runtime type (instance of type {@link Class}).
	 * In contrary to JDK's type resolving mechanisms, this method resolves primitive type names, as well.
	 * <p>
	 * Note on naming:<br>
	 * 1.) Looking up a runtime type instance for a type name string is best described as "resolving" the type.<br>
	 * 2.) The things that are resolved are TYPES
	 * (classes, interfaces, arrays and in later Java versions enums and annotations), not just classes.
	 * That the java inventors seemingly didn't understand their own type system and just called everything
	 * "Class" on the API-level,* even interfaces, is just an error that should be repeated as little as possible.<br>
	 *
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 * @param classLoader class loader from which the class must be loaded
	 *
	 * @return the resolved type instance (of type {@link Class})
	 *
	 * @throws LinkageError see {@link Class#forName(String)}
	 * @throws ExceptionInInitializerError see {@link Class#forName(String)}
	 * @throws ClassNotFoundException see {@link Class#forName(String)}
	 */
	public static final Class<?> resolveType(final String typeName, final ClassLoader classLoader)
		throws LinkageError, ExceptionInInitializerError, ClassNotFoundException
	{
		final Class<?> type = tryResolvePrimitiveType(typeName);
		return type != null
			? type
			: Class.forName(typeName, true, classLoader)
		;
	}

	/**
	 * Uses {@link Class#forName(String)} which uses the calling class's {@link ClassLoader}.
	 *
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 *
	 * @return the resolved type instance (of type {@link Class})
	 *
	 * @throws LinkageError see {@link Class#forName(String)}
	 * @throws ExceptionInInitializerError see {@link Class#forName(String)}
	 * @throws ClassNotFoundException see {@link Class#forName(String)}
	 */
	public static final Class<?> resolveTypeForName(final String typeName)
		throws LinkageError, ExceptionInInitializerError, ClassNotFoundException
	{
		final Class<?> type = tryResolvePrimitiveType(typeName);
		return type != null
			? type
			: Class.forName(typeName)
		;
	}

	/**
	 * Calls {@link #resolveType(String, ClassLoader)}, but suppresses any {@link ClassNotFoundException} and returns
	 * {@code null} instead. This is useful if the passed class name is only potentially resolvable
	 * at runtime and is still valid if not. Example: resolving a old type dictionary as far as possible
	 * and marking the not resolvable types as unresolvable.
	 *
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 * @param classLoader class loader from which the class must be loaded
	 * @return the {@link Class} instance representing the passed class name or {@code null} if unresolevable.
	 */
	public static final Class<?> tryResolveType(final String typeName, final ClassLoader classLoader)
	{
		try
		{
			return XReflect.resolveType(typeName, classLoader);
		}
		catch(final ClassNotFoundException e)
		{
			// intentionally return null
			return null;
		}
	}

	/**
	 * Alias for {@link #tryIterativeResolveType(ClassLoader, String...)} with the following difference:<br>
	 * If none of the passed {@literal typeNames} can be resolved, a {@link ClassNotFoundException} listing
	 * all passed {@literal typeNames} is thrown.
	 *
	 * @param classLoader class loader from which the class must be loaded
	 * @param typeNames the full qualified type names to be attempted to be resolved one by one.
	 *
	 * @return the first successfully resolved {@link Class} instance.
	 *
	 * @throws ClassNotFoundException if none of the passed {@literal typeNames} could have been resolved.
	 *
	 * @see #tryIterativeResolveType(ClassLoader, String...)
	 */
	public static final Class<?> iterativeResolveType(
		final ClassLoader classLoader,
		final String...   typeNames
	)
		throws ClassNotFoundException
	{
		final Class<?> type = tryIterativeResolveType(classLoader, typeNames);
		if(type != null)
		{
			return type;
		}

		// if none of the provided type names resulted in a match, a combined ClassNotFoundException is thrown
		throw new ClassNotFoundException(Arrays.toString(typeNames));
	}

	/**
	 * This methods attempts to resolve the passed {@literal typeNames} to {@link Class} instances using
	 * {@link #resolveType(String, ClassLoader)} one by one.
	 * The {@link Class} instance of the first successful attempt is returned.
	 * If none of the passed {@literal typeNames} can be resolved, {@literal null} is returned.
	 * See {@link #iterativeResolveType(ClassLoader, String...)} for an exception-throwing version.
	 * <p>
	 * <b>Note:</b><br>
	 * While it is generally a bad idea to just use a trial and error approach until something works,
	 * a logic like this is required to resolve types whose packages changes across different versions of a library.
	 * If the different full qualified class names are known, they can be used in an iterative attempt to resolve
	 * the class, hence avoiding hard dependencies to certain library versions in the using code by moving
	 * type names from imports at compile time to dynamic class resolving at runtime.<br>
	 * However, this approach has its limits, of course. If too much changes (field names, method names, parameters,
	 * behavior) the dynamic strategy results in chaos as the compiler gets more and more circumvented and more and
	 * more source code is transformed into context-less plain strings.<br>
	 * Therefore, when in doubt, it is preferable to stick to the general notion of this method being a "bad idea"
	 * and finding a more reliable solution.
	 *
	 * @param classLoader class loader from which the class must be loaded
	 * @param typeNames the full qualified type names to be attempted to be resolved one by one.
	 *
	 * @return the first successfully resolved {@link Class} instance or {@literal null}
	 *
	 * @see #resolveType(String, ClassLoader)
	 */
	public static final Class<?> tryIterativeResolveType(
		final ClassLoader classLoader,
		final String...   typeNames
	)
	{
		notNull(typeNames);
		notEmpty(typeNames);

		for(final String typeName : typeNames)
		{
			try
			{
				// just a debug hook
				final Class<?> type = resolveType(typeName, classLoader);
				return type;
			}
			catch(final ClassNotFoundException e)
			{
				// class not found for the current type name, continue with next one.
				continue;
			}
		}

		return null;
	}

	/**
	 * Local alias for {@link ClassLoader#getSystemClassLoader()}.
	 *
	 * @return the system class loader.
	 */
	public static final ClassLoader defaultTypeResolvingClassLoader()
	{
		return ClassLoader.getSystemClassLoader();
	}

	/**
	 * Calls {@link #resolveType(String, ClassLoader)} with {@link #defaultTypeResolvingClassLoader()}.
	 * Make sure this is a suitable {@link ClassLoader} when using this method.
	 *
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 * @return the resolved type instance (of type {@link Class})
	 *
	 * @throws LinkageError see {@link Class#forName(String)}
	 * @throws ExceptionInInitializerError see {@link Class#forName(String)}
	 * @throws ClassNotFoundException see {@link Class#forName(String)}
	 */
	public static final Class<?> resolveType(final String typeName)
		throws LinkageError, ExceptionInInitializerError, ClassNotFoundException
	{
		return resolveType(typeName, defaultTypeResolvingClassLoader());
	}

	/**
	 * Calls {@link #tryResolveType(String, ClassLoader)} with {@link #defaultTypeResolvingClassLoader()}.
	 * Make sure this is a suitable {@link ClassLoader} when using this method.
	 *
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 * @return the {@link Class} instance representing the passed class name or {@code null} if unresolevable.
	 */
	public static final Class<?> tryResolveType(final String typeName)
	{
		return tryResolveType(typeName, defaultTypeResolvingClassLoader());
	}

	/**
	 * Calls {@link #iterativeResolveType(ClassLoader, String...)} with {@link #defaultTypeResolvingClassLoader()}.
	 * Make sure this is a suitable {@link ClassLoader} when using this method.
	 *
	 * @param typeNames the full qualified type names to be attempted to be resolved one by one.
	 *
	 * @return the first successfully resolved {@link Class} instance.
	 *
	 * @throws ClassNotFoundException if none of the passed {@literal typeNames} could have been resolved.
	 */
	public static final Class<?> iterativeResolveType(final String... typeNames)
		throws ClassNotFoundException
	{
		return iterativeResolveType(defaultTypeResolvingClassLoader(), typeNames);
	}

	public static final Class<?> tryIterativeResolveType(final String... typeNames)
	{
		return tryIterativeResolveType(defaultTypeResolvingClassLoader(), typeNames);
	}

	public static final Field tryGetDeclaredField(
		final Class<?> declaringClass,
		final String   fieldName
	)
	{
		if(declaringClass == null)
		{
			return null;
		}

		try
		{
			return declaringClass.getDeclaredField(fieldName);
		}
		catch(final ReflectiveOperationException e)
		{
			// field may be unresolvable
			return null;
		}
	}

	public static final Class<?> tryResolvePrimitiveType(final String className)
	{
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
		return tryResolvePrimitiveType(typeName) != null;
	}

	public static final boolean isOfAnyType(
		final Class<?>    subject   ,
		final Class<?>... supertypes
	)
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

	public static final boolean isOfAnyType(
		final Class<?>           subject   ,
		final Iterable<Class<?>> supertypes
	)
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

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(final T object)
	{
		return (Class<T>)object.getClass();
	}

	public static char fieldIdentifierDelimiter()
	{
		return '#';
	}

	public static String typename_enum()
	{
		return "enum";
	}

	public static char nestedClassNameSeparator()
	{
		return '$';
	}

	public static String toFullQualifiedFieldName(
		final Class<?>                actualClass,
		final java.lang.reflect.Field field
	)
	{
		return actualClass == field.getDeclaringClass()
			? deriveFieldIdentifier(field)
			: toFullQualifiedFieldName(actualClass, deriveFieldIdentifier(field))
		;
	}

	public static String deriveFieldIdentifier(final java.lang.reflect.Field field)
	{
		return toFullQualifiedFieldName(field.getDeclaringClass(), field.getName());
	}

	public static String toFullQualifiedFieldName(final Class<?> c, final String fieldName)
	{
		return c.getName() + fieldIdentifierDelimiter() + fieldName;
	}

	public static int getFieldIdentifierDelimiterIndex(final String identifier)
	{
		final int index = identifier.lastIndexOf(fieldIdentifierDelimiter());
		if(index < 0)
		{
			throw new IllegalArgumentException("No delimiter found in identifier");
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

	public static <T> Instantiator<T> WrapDefaultConstructor(final Class<T> type)
		throws NoSuchMethodRuntimeException
	{
		try
		{
			return Instantiator.WrapDefaultConstructor(
				type.getConstructor()
			);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}
		catch(final SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks if the passed type is equal to or a sub type of {@link Collection} or {@link Map}.
	 * <p>
	 * <i>Sad that such a method is necessary in the first place, but here we are.</i>
	 * <br>(See {@link XMap} for an example on how to do it correctly.)
	 *
	 * @param type the type to be checked.
	 *
	 * @return whether the passed type is a java.util collection "in the broader sense".
	 *
	 * @see Collection
	 * @see Map
	 */
	public static boolean isJavaUtilCollectionType(final Class<?> type)
	{
		return Collection.class.isAssignableFrom(type)
			|| Map.class.isAssignableFrom(type)
		;
	}

	public static boolean hasEnumeratedTypeName(final Class<?> type)
	{
		final String typeName  = type.getName();
		final int    lastIndex = typeName.length() - 1;
		for(int i = 0; i < lastIndex; i++)
		{
			i = typeName.indexOf(nestedClassNameSeparator(), i);
			if(i < 0)
			{
				return false;
			}

			final char c = typeName.charAt(i + 1);
			if(XChars.isDigit(c))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isProxyClass(final Class<?> c)
	{
		/* (20.08.2019 TM)NOTE:
		 * IMHO, the method
		 * Proxy#isProxyClass is nonsensical and unusable given its name. Or maybe it's just badly named.
		 *
		 * To determine if a class is a Proxy class it MUST ONLY be checked for extending Proxy, but NOT
		 * any second condition, like being registered in some cache somewhere or something.
		 * What if (for whatever reason) Proxy's proxyClassCache does not contain the class in question?
		 * Does that make that class no longer a Proxy class all of a sudden? Its instances no proxies anymore?
		 * Surely not. If any arbitrary proxy class is to be identified, it MUST ONLY be checked for extending
		 * Proxy. No second condition that might cause the check to result in a false instead of a true.
		 * Anything else is a bug and therefore unusable.
		 *
		 * Maybe the method's intention is more along the line of testing if a certain class has really
		 * been dynamically created and properly registered, to filter out all classes that might extend
		 * Proxy but were not really validly created (but hacked into existence or whatever).
		 * So maybe what the method does is something like "isValidProxyClass". Identify the "good" proxies,
		 * reject everything else. That is a validation method, not a type identifying method as the name implies.
		 * Wouldn't be the first time that namings in the JDK are horribly off.
		 *
		 * However, for recognizing ANY proxy type, that method would be dangerously wrong: Anything that
		 * extends Proxy is a Proxy. Period. No second condition.
		 * ESPECIALLY including any class that extends Proxy but has been created in another way.
		 *
		 * So the singular check is the correct thing to do for the given name and its implied logic.
		 * And competence of JDK developers can, once more, not be trusted.
		 */
		return Proxy.class.isAssignableFrom(c);
	}

	public static boolean isValidProxyClass(final Class<?> c)
	{
		// horribly wrong name for a validation method
		return Proxy.isProxyClass(c);
	}

	public static Field[] collectPrimitiveFieldsByByteSize(final Field[] fields, final int byteSize)
	{
		if(byteSize != XMemory.byteSize_byte()
		&& byteSize != XMemory.byteSize_short()
		&& byteSize != XMemory.byteSize_int()
		&& byteSize != XMemory.byteSize_long()
		)
		{
			throw new IllegalArgumentException("Invalid Java primitive byte size: " + byteSize);
		}

		final Field[] primFields = new Field[fields.length];
		int primFieldsCount = 0;
		for(int i = 0; i < fields.length; i++)
		{
			if(fields[i].getType().isPrimitive() && XMemory.byteSizePrimitive(fields[i].getType()) == byteSize)
			{
				primFields[primFieldsCount++] = fields[i];
			}
		}
		return Arrays.copyOf(primFields, primFieldsCount);
	}

	public static final Field[] collectInstanceFields(final Class<?> objectClass)
	{
		return collectInstanceFields(objectClass, XFunc.all());
	}

	public static final Field[] collectInstanceFields(final Class<?> objectClass, final Predicate<? super Field> selector)
	{
		final BulkList<Field> objectFields = BulkList.New(20);
		XReflect.iterateDeclaredFieldsUpwards(objectClass, field ->
		{
			// non-instance fields are always discarded
			if(!XReflect.isInstanceField(field))
			{
				return;
			}
			if(selector != null && !selector.test(field))
			{
				return;
			}

			objectFields.add(field);
		});

		final Field[] array = XArrays.reverse(objectFields.toArray(Field.class));

		return array;
	}

	public static int calculatePrimitivesLength(final Field[] primFields)
	{
		int length = 0;
		for(int i = 0; i < primFields.length; i++)
		{
			if(!primFields[i].getType().isPrimitive())
			{
				throw new IllegalArgumentException("Not a primitive field: " + primFields[i]);
			}
			length += XMemory.byteSizePrimitive(primFields[i].getType());
		}
		return length;
	}

	public static <T, S extends T> S copyFields(
		final T source,
		final S target
	)
	{
		return copyFields(source, target, XFunc.all(), CopyPredicate::all);
	}

	public static <T, S extends T> S copyFields(
		final T                        source       ,
		final S                        target       ,
		final Predicate<? super Field> fieldSelector
	)
	{
		return copyFields(source, target, fieldSelector, CopyPredicate::all);
	}


	public static <T, S extends T> S copyFields(
		final T                        source       ,
		final S                        target       ,
		final Predicate<? super Field> fieldSelector,
		final CopyPredicate            copySelector
	)
	{
		validateFamiliarClass(source, target);
		final Field[] copyFields = collectInstanceFields(source.getClass(), fieldSelector);

		for(final Field field : copyFields)
		{
			try
			{
				copyFieldValue(source, target, field, copySelector);
			}
			catch(final Exception e)
			{
				throw new MemoryException(
					"Cannot copy value of field " + field
					+ " from source instance " + XChars.systemString(source)
					+ " to target instance "   + XChars.systemString(target) + ".",
					e
				);
			}
		}

		return target;
	}

	final static <T, S extends T> S copyFields(
		final T             source       ,
		final S             target       ,
		final Field[]       copyFields,
		final CopyPredicate copySelector
	)
	{
		validateFamiliarClass(source, target);
		for(final Field field : copyFields)
		{
			try
			{
				copyFieldValue(source, target, field, copySelector);
			}
			catch(final Exception e)
			{
				throw new MemoryException(
					"Cannot copy value of field " + field
					+ " from source instance " + XChars.systemString(source)
					+ " to target instance "   + XChars.systemString(target) + ".",
					e
				);
			}
		}

		return target;
	}

	private static <T, S extends T> void copyFieldValue(
		final T             source      ,
		final S             target      ,
		final Field         field       ,
		final CopyPredicate copySelector
	)
		throws IllegalArgumentException, IllegalAccessException
	{
		// must circumvent reflection access by low-level access due to warnings about JDK-internal reflection access.
		final long fieldOffset = XMemory.objectFieldOffset(field);

		if(field.getType().isPrimitive())
		{
			copyPrimitiveFieldValue(source, target, field, fieldOffset, copySelector);
			return;
		}

		final Object value = XMemory.getObject(source, fieldOffset);
		if(!copySelector.test(source, target, field, value))
		{
			return;
		}

		XMemory.setObject(target, fieldOffset, value);
	}

	private static <T, S extends T>void copyPrimitiveFieldValue(
		final T             source        ,
		final S             target        ,
		final Field         primitiveField,
		final long          fieldOffset   ,
		final CopyPredicate copySelector
	)
		throws IllegalArgumentException, IllegalAccessException
	{
		if(!copySelector.test(source, target, primitiveField, null))
		{
			return;
		}

		// must circumvent reflection access by low-level access due to warnings about JDK-internal reflection access.
		final Class<?> primitiveType = primitiveField.getType();
		if(primitiveType == int.class)
		{
			XMemory.set_int(target, fieldOffset, XMemory.get_int(source, fieldOffset));
		}
		else if(primitiveType == double.class)
		{
			XMemory.set_double(target, fieldOffset, XMemory.get_double(source, fieldOffset));
		}
		else if(primitiveType == long.class)
		{
			XMemory.set_long(target, fieldOffset, XMemory.get_long(source, fieldOffset));
		}
		else if(primitiveType == boolean.class)
		{
			XMemory.set_boolean(target, fieldOffset, XMemory.get_boolean(source, fieldOffset));
		}
		else if(primitiveType == float.class)
		{
			XMemory.set_float(target, fieldOffset, XMemory.get_float(source, fieldOffset));
		}
		else if(primitiveType == char.class)
		{
			XMemory.set_char(target, fieldOffset, XMemory.get_char(source, fieldOffset));
		}
		else if(primitiveType == short.class)
		{
			XMemory.set_short(target, fieldOffset, XMemory.get_short(source, fieldOffset));
		}
		else if(primitiveType == byte.class)
		{
			XMemory.set_byte(target, fieldOffset, XMemory.get_byte(source, fieldOffset));
		}
		else
		{
			// e.g. void.class, maybe value types in the future or whatever.
			throw new MemoryException("Field with unhandled primitive type: " + primitiveField);
		}
	}

	/**
	 * Checks if {@code superClassInstance.getClass().isAssignableFrom(sameOrSubClassInstance.getClass())}
	 *
	 * @param <T> the super type
	 * @param <S> the same or sub type
	 * @param superClassInstance the super instance
	 * @param sameOrSubClassInstance ths sub or sub instance
	 * @throws IllegalArgumentException if the check fails
	 */
	public static <T, S extends T> void validateFamiliarClass(
		final T superClassInstance    ,
		final S sameOrSubClassInstance
	)
	{
		if(superClassInstance.getClass().isAssignableFrom(sameOrSubClassInstance.getClass()))
		{
			return;
		}

		throw new IllegalArgumentException(
			XChars.systemString(sameOrSubClassInstance)
			+ " is not of the same class or a sub class of "
			+ XChars.systemString(superClassInstance)
		);
	}

	public final static Class<?> getSuperClassNonNull(final Class<?> c)
	{
		return c == Object.class
			? c
			: c.getSuperclass()
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	private XReflect()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
