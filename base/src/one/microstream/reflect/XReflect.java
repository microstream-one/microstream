package one.microstream.reflect;


import static one.microstream.X.notEmpty;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XReference;
import one.microstream.exceptions.IllegalAccessRuntimeException;
import one.microstream.exceptions.NoSuchFieldRuntimeException;
import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.functional.Instantiator;
import one.microstream.typing.XTypes;
import one.microstream.util.UtilStackTrace;


/**
 * Provides additional generic util methods for working with java reflection.
 *
 * @author Thomas Muenz
 *
 */
public final class XReflect
{
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
	 * Utility method fixing the WRONGLY implemented {@link Class#isEnum()}.
	 * <p>
	 * Their description is weird (<i>"if this class was declared as an enum in the source code"</i>) and the
	 * implemented behavior is dangerous and useless to identify all classes of instances that are enums.
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
		/*
		 * Slowly feels like EVERYTHING in the JDK has to be replaced by custom code doing the job properly...
		 * If they really want their weird version, then two cleanly named methods are required:
		 * #isEnum()
		 * #isDeclaredEnum()
		 * The latter ("theirs") is probably useful, too, but the primary meaning of "isEnum" is THIS logic here.
		 * 
		 * Since they made their ENUM bit mask unusable (as usual in their reflection code), it can't be used here.
		 * It has to be assumed that only proper enum types extend Enum. If not, blame it on the incompetent
		 * JDK reflection code lacking proper separation of concerns concepts. Amateurs.
		 */
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
			// (28.10.2013 TM)EXCP: proper exception (OMG).
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
			// (28.10.2013 TM)EXCP: proper exception (OMG).
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

	/**
	 * Resolves the passed type name to a runtime type (instance of type {@link Class}).
	 * In contrary to {@link Class#forName(String)}, this method can resolve primitive type names, as well.
	 * <p>
	 * Note on naming:<br>
	 * 1.) Looking up a runtime type instance for a type name string is best described as "resolving" the type.<br>
	 * 2.) The things that are resolved are TYPES
	 * (classes, interfaces, arrays and in later Java versions enums and annotations), not just classes.
	 * That the java inventors seemingly didn't understand their own type system and just called everything
	 * "Class" on the API-level,* even interfaces, is just an error that should be repeated as less as possible.<br>
	 * In conclusion, the proper naming for the action executed by this method (meaning a verb)
	 * is "resolveType" and not a dilettantish "forName" as in {@link Class#forName(String)}.
	 * 
	 * @param typeName the type name to be resolved, primitive name or full qualified type name.
	 * @return the resolved type instance (of type {@link Class})
	 * @throws LinkageError see {@link Class#forName(String)}
	 * @throws ExceptionInInitializerError see {@link Class#forName(String)}
	 * @throws ClassNotFoundException see {@link Class#forName(String)}
	 */
	public static final Class<?> resolveType(final String typeName)
		throws LinkageError, ExceptionInInitializerError, ClassNotFoundException
	{
		final Class<?> type = tryResolvePrimitiveType(typeName);
		return type != null
			? type
			: Class.forName(typeName)
		;
	}
	
	/**
	 * Calls {@link #resolveType(String)}, but suppresses any {@link ClassNotFoundException} and returns
	 * <code>null</code> instead. This is useful if the passed class name is only potentially resolvable
	 * at runtime and is still valid if not. Example: resolving a old type dictionary as far as possible
	 * and marking the not resolvable types as unresolvable.
	 * 
	 * @param className
	 * @return the {@link Class} instance representing the passed class name or <code>null</code> if unresolevable.
	 */
	public static final Class<?> tryResolveType(final String className)
	{
		try
		{
			return XReflect.resolveType(className);
		}
		catch(final ClassNotFoundException e)
		{
			// intentionally return null
			return null;
		}
	}
	
	/**
	 * Alias for {@link #tryIterativeResolveType(String...)} with the following difference:<br>
	 * If none of the passed {@literal typeNames} can be resolved, a {@link ClassNotFoundException} listing
	 * all passed {@literal typeNames} is thrown.
	 * 
	 * @param typeNames the full qualified type names to be attempted to be resolved one by one.
	 * 
	 * @return the first successfully resolved {@link Class} instance.
	 * 
	 * @throws ClassNotFoundException if none of the passed {@literal typeNames} could have been resolved.
	 * 
	 * @see Class#forName(String)
	 */
	public static final Class<?> iterativeResolveType(final String... typeNames)
		throws ClassNotFoundException
	{
		final Class<?> type = tryIterativeResolveType(typeNames);
		if(type != null)
		{
			return type;
		}
		
		// if none of the provided type names resulted in a match, a combined ClassNotFoundException is thrown
		throw new ClassNotFoundException(Arrays.toString(typeNames));
	}
	
	/**
	 * This methods attempts to resolve the passed {@literal typeNames} to {@link Class} instances using
	 * {@link Class#forName(String)} one by one.
	 * The {@link Class} instance of the first successful attempt is returned.
	 * If none of the passed {@literal typeNames} can be resolved, {@literal null} is returned.
	 * See {@link #iterativeResolveType(String...)} for an exception-throwing version.
	 * <p>
	 * <b>Note:</b><br>
	 * While it is generally a bad idea to just use a trial and error approach until something works,
	 * a logic like this is required to resolve types whose packages changes accross different versions of a library.
	 * If the different full qualified class names are known, they can be used in an iterative attempt to resolve
	 * the class, hence avoiding hard dependencies to certain library versions in the using code by moving
	 * type names from imports at compile time to dynamic class resolving at runtime.<br>
	 * However, this approach has its limits, of course. If too much changes (field names, method names, parameters,
	 * behavior) the dynamic strategy results in chaos as the compiler gets more and more circumvented and more and
	 * more source code is transformed into contextless plain strings.<br>
	 * Therefore, when in doubt, it is preferable to stick to the general notion of this method being a "bad idea"
	 * and finding a more reliable solution.
	 * 
	 * @param typeNames the full qualified type names to be attempted to be resolved one by one.
	 * 
	 * @return the first successfully resolved {@link Class} instance or {@literal null}
	 * 
	 * @see Class#forName(String)
	 */
	public static final Class<?> tryIterativeResolveType(final String... typeNames)
	{
		notNull(typeNames);
		notEmpty(typeNames);
		
		for(final String typeName : typeNames)
		{
			try
			{
				// just a debug hook
				final Class<?> type = Class.forName(typeName);
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
		
	public static final Field tryGetDeclaredField(final Class<?> declaringClass, final String fieldName)
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
	
	/**
	 * *sigh*
	 *
	 * @param object
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
	
	public static String typename_enum()
	{
		return "enum";
	}
	
	public static char nestedClassNameSeparator()
	{
		return '$';
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
	 * @return whether or not the passed type is a java.util collection "in the broader sense".
	 * 
	 * @see Collection
	 * @see Map
	 */
	public static boolean isJavaUtilCollectionType(final Class<?> type)
	{
		// because the geniuses failed to make Map a collection. Hilarious.
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
			if(c >= '0' && c <= '9')
			{
				return true;
			}
		}
		
		return false;
	}
		


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XReflect()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
