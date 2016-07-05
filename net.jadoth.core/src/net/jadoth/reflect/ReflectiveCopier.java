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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jadoth.collections.old.OldCollections;
import net.jadoth.exceptions.IllegalAccessRuntimeException;


/**
 * The Class ReflectiveCopier.
 *
 * @author Thomas Muenz
 */
public class ReflectiveCopier
{

	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////

	/** The Constant cachedInstanceFields. */
	private static final HashMap<Class<?>, Field[]> CACHED_INSTANCE_FIELDS = new HashMap<>();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Gets the all copyable fields.
	 *
	 * @param c the c
	 * @return the all copyable fields
	 */
	public static final ArrayList<Field> getAllCopyableFields(final Class<?> c)
	{
		return JadothReflect.getAllFields(c, Modifier.FINAL | Modifier.STATIC);
	}

	/**
	 * Gets the cached instance fields.
	 *
	 * @param c the c
	 * @return the cached instance fields
	 */
	private static Field[] getCachedInstanceFields(final Class<?> c)
	{
		Field[] instanceFields = CACHED_INSTANCE_FIELDS.get(c);
		if(instanceFields == null)
		{
			instanceFields = OldCollections.toArray(getAllCopyableFields(c), Field.class);
			CACHED_INSTANCE_FIELDS.put(c, instanceFields);
		}
		return instanceFields;
	}

	public static final Object fieldUntypedCopy(
		final Object      source     ,
		final Object      target     ,
		final Field       field      ,
		final CopyHandler copyHandler
	)
	{
		if(copyHandler != null)
		{
			copyHandler.copy(field, source, target);
		}
		else
		{
			//default (shallow) copy if no handler is given
			JadothReflect.setFieldValue(field, target, JadothReflect.getFieldValue(field, source));
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	public static final <O extends Object, S extends O, T extends O> T fieldCopy(
			final S source, final T target, final Field f, final CopyHandler copyHandler
	)
	{
		return (T)fieldUntypedCopy(source, target, f, copyHandler);
	}

	public static final Object untypedCopy(final Object source, final Object target, final Class<?> commonClass)
		throws IllegalAccessRuntimeException
	{
		final Field[] instanceFields = getCachedInstanceFields(commonClass);
		for(final Field f : instanceFields)
		{
			JadothReflect.setFieldValue(f, target, JadothReflect.getFieldValue(f, source));
		}
		return target;
	}

	/**
	 * Untyped copy.
	 *
	 * @param source the source
	 * @param target the target
	 * @param commonClass the common class
	 * @param fieldsToExclude the fields to exclude
	 * @return the object
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public static final Object untypedCopy(
		final Object     source         ,
		final Object     target         ,
		final Class<?>   commonClass    ,
		final Set<Field> fieldsToExclude
	)
		throws IllegalAccessRuntimeException
	{
		if(fieldsToExclude == null)
		{
			return untypedCopy(source, target, commonClass);
		}

		final Field[] instanceFields = getCachedInstanceFields(commonClass);
		for(final Field f : instanceFields)
		{
			if(fieldsToExclude.contains(f))
			{
				continue;
			}
			JadothReflect.setFieldValue(f, target, JadothReflect.getFieldValue(f, source));
		}
		return target;
	}

	/**
	 * Untyped copy.
	 *
	 * @param source the source
	 * @param target the target
	 * @param commonClass the common class
	 * @param fieldsToExclude the fields to exclude
	 * @param targetFieldCopyHandlers the target field copy handlers
	 * @param targetAnnotationHandlers the target annotation handlers
	 * @param targetClassCopyHandlers the target class copy handlers
	 * @param genericCopyHandler the generic copy handler
	 * @return the object
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public static final Object untypedCopy(
			final Object                                        source                  ,
			final Object                                        target                  ,
			final Class<?>                                      commonClass             ,
			final Set<Field>                                    fieldsToExclude         ,
			final Map<Field, CopyHandler>                       targetFieldCopyHandlers ,
			final Map<Class<? extends Annotation>, CopyHandler> targetAnnotationHandlers,
			final Map<Class<?>, CopyHandler>                    targetClassCopyHandlers ,
			final CopyHandler                                   genericCopyHandler
	)
		throws IllegalAccessRuntimeException
	{
		final Set<Field> nonNullFieldsToExclude = fieldsToExclude != null ? fieldsToExclude : new HashSet<>(0);
		final Field[]    instanceFields         = getCachedInstanceFields(commonClass);

		// simple case without copyhandlers
		if(targetClassCopyHandlers == null && targetFieldCopyHandlers == null && targetAnnotationHandlers == null)
		{
			for(final Field f : instanceFields)
			{
				if(nonNullFieldsToExclude.contains(f))
				{
					continue;
				}
				if(genericCopyHandler != null)
				{
					genericCopyHandler.copy(f, source, target);
				}
				else
				{
					JadothReflect.setFieldValue(f, target, JadothReflect.getFieldValue(f, source));
				}
			}
			return target;
		}

		// special treatment with copyhandlers

		final Map<Field, CopyHandler>                       nonNullTargetFieldCopyHandlers  =
			targetFieldCopyHandlers != null ? targetFieldCopyHandlers : new HashMap<>(0)
		;
		final Map<Class<? extends Annotation>, CopyHandler> nonNullTargetAnnotationHandlers =
			targetAnnotationHandlers != null ? targetAnnotationHandlers : new HashMap<>(0)
		;
		final Map<Class<?>, CopyHandler>                    nonNullTargetClassCopyHandlers  =
			targetClassCopyHandlers != null ? targetClassCopyHandlers : new HashMap<>(0)
		;

		for(final Field f : instanceFields)
		{
			if(nonNullFieldsToExclude.contains(f))
			{
				continue;
			}

			//1.) fieldCopyHandler has highest priority
			CopyHandler handler = nonNullTargetFieldCopyHandlers.get(f);

			//2.) if no fieldCopyHandler then look for annotationCopyHandler
			if(handler == null)
			{
				final Annotation[] annotations = f.getAnnotations();
				for(final Annotation a : annotations)
				{
					handler = nonNullTargetAnnotationHandlers.get(a.annotationType());
					if(handler != null)
					{
						break;
					}
				}
			}
			//3.) if neither fieldCopyHandler nor annotationCopyHandler then look for classCopyHandler
			if(handler == null)
			{
				handler = nonNullTargetClassCopyHandlers.get(f.getType());
			}
			//4.) finally, try using genericCopyHandler
			if(handler == null)
			{
				handler = genericCopyHandler;
			}

			fieldCopy(source, target, f, handler);
		}

		return target;
	}

	/**
	 * This method is a convenience version for <code>copy(source, target, commonClass, null)</code> which means
	 * that no fields are excluded from copiing.
	 * <p>
	 * See {@link #execute(Object, Object, Class, Set)} for further details.
	 *
	 * @param <O> the common type of <code>source</code> and <code>target</code>.
	 *        Can be the same class or a common super class.
	 * @param <S> the type of the source object, which must be of Type <code>O</code>
	 * @param <T> the type of the source object, which must be of Type <code>O</code>
	 * @param source the source object from which the values are read.
	 * @param target the target object to which the values from <code>source</code> are written
	 * @param commonClass the Class that determines the level, at field values are copied from <code>source</code>
	 *        and <code>target</code>.
	 * Can be the same class or a common super class.
	 * @return
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 * @see {@link #execute(Object, Object, Class, Set)}
	 */
	@SuppressWarnings("unchecked")
	public static final <O extends Object, S extends O, T extends O> T copy(
		final S        source     ,
		final T        target     ,
		final Class<O> commonClass
	)
		throws IllegalAccessRuntimeException
	{
		return (T)untypedCopy(source, target, commonClass);
	}

	/**
	 * Copies the values of all copiable instance fields (non static, non final) from source object <code>source</code>
	 * to target object <code>target</code>.
	 * <p>
	 * The parameter <code>commonClass</code> ensures the type compatibility of <code>source</code>
	 * and <code>target</code>.
	 * It also determines the class level, on which the copiing shall be done.
	 * <p>
	 * Examples:<br>
	 * 1.)<br>
	 * Both <code>sourceObj</code> and <code>targetObj</code> are objects of Class MyClassA extends MyCommonClass.
	 * <p>
	 * The call <code>copy(sourceObj, targetObj, MyClassA.class)</code> enables the compiler to check
	 * if both objects are really of the same Type and instructs the copy method to copy all (non static, non final)
	 * fields from Classes MyClassA and MyCommonClass as well.
	 * <p>
	 * The call <code>copy(sourceObj, targetObj, MyCommonClass.class)</code> causes the method only to copy
	 * fields of Class
	 * MyCommonClass.
	 * <p>
	 * 2.)<br>
	 * <code>sourceObj</code> is of Class MyClassA extends MyCommonClass.<br>
	 * <code>targetObj</code> is of Class MyClassB extends MyCommonClass.<br>
	 * <p>
	 * The call <code>copy(sourceObj, targetObj, MyCommonClass.class)</code> would be valid.<br>
	 * The call <code>copy(sourceObj, targetObj, MyClassA.class)</code> would cause a compiler error<br>
	 *
	 * <p>
	 * Notes:<ul>
	 * <li><code>copy</code> does not create any new instances
	 * <li>Copiable fields are cached after the first call to improve performance.
	 * <li>The <code>source</code> object will not be modified in any kind.
	 * <li>All field values of <code>target</code> that are not overwritten by values from <code>source</code>
	 * keep their values.
	 * <li>The Java <code>IllegalAccessException</code> that can occur by reflection access should be prevented by
	 * internal mechanisms.
	 * In case it might still occur, it is nested into a RuntimeException of type IllegalAccessRuntimeException
	 * </ul>
	 *
	 * @param <O> the common type of <code>source</code> and <code>target</code>. Can be the same class or a common
	 *        super class.
	 * @param <S> the type of the source object, which must be of Type <code>O</code>
	 * @param <T> the type of the source object, which must be of Type <code>O</code>
	 * @param source the source object from which the values are read.
	 * @param target the target object to which the values from <code>source</code> are written
	 * @param commonClass the Class that determines the level, at field values are copied from <code>source</code>
	 *        and <code>target</code>.
	 * Can be the same class or a common super class.
	 * @param fieldsToExclude fields that shall explicitly excluded from copiing.
	 * @return
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 * @see {@link #execute(Object, Object, Class)}
	 */
	@SuppressWarnings("unchecked")
	public static final <O extends Object, S extends O, T extends O> T copy(
		final S          source         ,
		final T          target         ,
		final Class<O>   commonClass    ,
		final Set<Field> fieldsToExclude
	)
		throws IllegalAccessRuntimeException
	{
		if(fieldsToExclude == null)
		{
			return (T)untypedCopy(source, target, commonClass);
		}
		return (T)untypedCopy(source, target, commonClass, fieldsToExclude);
	}

	/**
	 * Copy.
	 *
	 * @param <O> the generic type
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param source the source
	 * @param target the target
	 * @param commonClass the common class
	 * @param fieldsToExclude the fields to exclude
	 * @param targetFieldCopyHandlers the target field copy handlers
	 * @param targetAnnotationHandlers the target annotation handlers
	 * @param targetClassCopyHandlers the target class copy handlers
	 * @param genericCopyHandler the generic copy handler
	 * @return the t
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	@SuppressWarnings("unchecked")
	public static final <O extends Object, S extends O, T extends O> T copy(
		final S                                             source                  ,
		final T                                             target                  ,
		final Class<O>                                      commonClass             ,
		final Set<Field>                                    fieldsToExclude         ,
		final Map<Field, CopyHandler>                       targetFieldCopyHandlers ,
		final Map<Class<? extends Annotation>, CopyHandler> targetAnnotationHandlers,
		final Map<Class<?>, CopyHandler>                    targetClassCopyHandlers ,
		final CopyHandler                                   genericCopyHandler
	)
		throws IllegalAccessRuntimeException
	{
		return (T)untypedCopy(
			source                  ,
			target                  ,
			commonClass             ,
			fieldsToExclude         ,
			targetFieldCopyHandlers ,
			targetAnnotationHandlers,
			targetClassCopyHandlers ,
			genericCopyHandler
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/** The location class annotation class handlers. */
	private final HashMap<Class<?>, HashMap<Class<? extends Annotation>, CopyHandler>> locationClassAnnotationClassHandlers =
		new HashMap<>();

	/** The location class copy target class handlers. */
	private final HashMap<Class<?>, HashMap<Class<?>, CopyHandler>> locationClassCopyTargetClassHandlers =
		new HashMap<>();

	/** The location class copy target field handlers. */
	private final HashMap<Class<?>, HashMap<Field, CopyHandler>> locationClassCopyTargetFieldHandlers =
		new HashMap<>();
	/*
	 * only one GenericCopyHandler per locationClass makes sense
	 */
	/** The location class generic handlers. */
	private final HashMap<Class<?>, CopyHandler> locationClassGenericHandlers =
		new HashMap<>();

	/** The general annotation handlers. */
	private final HashMap<Class<? extends Annotation>, CopyHandler> generalAnnotationHandlers =
		new HashMap<>();

	/** The general copy target class handlers. */
	private final HashMap<Class<?>, CopyHandler> generalCopyTargetClassHandlers =
		new HashMap<>();

	/** The general copy target field handlers. */
	private final HashMap<Field, CopyHandler> generalCopyTargetFieldHandlers =
		new HashMap<>();

	/** The generic handler. */
	private CopyHandler genericHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	/**
	 * Trivial default constructor.
	 */
	public ReflectiveCopier()
	{
		super();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Sets the generic copy handler.
	 *
	 * @param handler the handler
	 * @return the reflective copier
	 */
	public ReflectiveCopier setGenericCopyHandler(final CopyHandler handler)
	{
		this.genericHandler = handler;
		return this;
	}

	/**
	 * Adds the copy handler by location class.
	 *
	 * @param handler the handler
	 * @param locationClass the location class
	 * @return the reflective copier
	 */
	public ReflectiveCopier addCopyHandlerByLocationClass(final CopyHandler handler, final Class<?> locationClass)
	{
		return this.addCopyHandler(handler, locationClass, null, null, null);
	}

	/**
	 * Adds the copy handler by copy class.
	 *
	 * @param handler the handler
	 * @param copyTargetClass the copy target class
	 * @return the reflective copier
	 */
	public ReflectiveCopier addCopyHandlerByCopyClass(final CopyHandler handler, final Class<?> copyTargetClass)
	{
		return this.addCopyHandler(handler, null, copyTargetClass, null, null);
	}

	/**
	 * Adds the copy handler by annotation.
	 *
	 * @param handler the handler
	 * @param copyTargetAnnotation the copy target annotation
	 * @return the reflective copier
	 */
	public ReflectiveCopier addCopyHandlerByAnnotation(
		final CopyHandler                 handler             ,
		final Class<? extends Annotation> copyTargetAnnotation
	)
	{
		return this.addCopyHandler(handler, null, null, copyTargetAnnotation, null);
	}

	/**
	 * Adds the copy handler by copy field.
	 *
	 * @param handler the handler
	 * @param copyTargetField the copy target field
	 * @return the reflective copier
	 */
	public ReflectiveCopier addCopyHandlerByCopyField(final CopyHandler handler, final Field copyTargetField)
	{
		return this.addCopyHandler(handler, null, null, null, copyTargetField);
	}

	/**
	 * Registers <code>handler</code> to be used by this DeepCopier.
	 *
	 * @param handler the handler
	 * @param locationClass the location class
	 * @param copyTargetClass the copy target class
	 * @param copyTargetAnnotation the copy target annotation
	 * @param copyTargetField the copy target field
	 * @return the reflective copier
	 * @return
	 */
	public ReflectiveCopier addCopyHandler(
			final CopyHandler handler,
			final Class<?> locationClass,
			final Class<?> copyTargetClass,
			final Class<? extends Annotation> copyTargetAnnotation,
			final Field copyTargetField
	)
	{
		if(locationClass != null)
		{
			// register locationClass-specific handlers

			if(copyTargetField != null)
			{
				HashMap<Field, CopyHandler> targetFieldHandlers =
					this.locationClassCopyTargetFieldHandlers.get(locationClass)
				;
				if(targetFieldHandlers == null)
				{
					targetFieldHandlers = new HashMap<>();
					this.locationClassCopyTargetFieldHandlers.put(locationClass, targetFieldHandlers);
				}
				targetFieldHandlers.put(copyTargetField, handler);
			}
			if(copyTargetAnnotation != null)
			{
				HashMap<Class<? extends Annotation>, CopyHandler> targetAnnotationHandlers =
					this.locationClassAnnotationClassHandlers.get(locationClass)
				;
				if(targetAnnotationHandlers == null)
				{
					targetAnnotationHandlers = new HashMap<>();
					this.locationClassAnnotationClassHandlers.put(locationClass, targetAnnotationHandlers);
				}
				targetAnnotationHandlers.put(copyTargetAnnotation, handler);
			}
			else if(copyTargetClass != null)
			{
				HashMap<Class<?>, CopyHandler> targetClassHandlers =
					this.locationClassCopyTargetClassHandlers.get(locationClass)
				;
				if(targetClassHandlers == null)
				{
					targetClassHandlers = new HashMap<>();
					this.locationClassCopyTargetClassHandlers.put(locationClass, targetClassHandlers);
				}
				targetClassHandlers.put(copyTargetClass, handler);
			}
			else
			{
				this.locationClassGenericHandlers.put(locationClass, handler);
			}
		}
		else if(copyTargetField != null)
		{
			this.generalCopyTargetFieldHandlers.put(copyTargetField, handler);
		}
		else if(copyTargetAnnotation != null)
		{
			this.generalAnnotationHandlers.put(copyTargetAnnotation, handler);
		}
		else if(copyTargetClass != null)
		{
			this.generalCopyTargetClassHandlers.put(copyTargetClass, handler);
		}
		else
		{
			this.genericHandler = handler;
		}

		return this;
	}

	/**
	 * Gets the all copy target class handlers for.
	 *
	 * @param locationClass the location class
	 * @return the all copy target class handlers for
	 */
	private HashMap<Class<?>, CopyHandler> getAllCopyTargetClassHandlersFor(final Class<?> locationClass)
	{
		final HashMap<Class<?> , CopyHandler> locationClassMap;
		locationClassMap = this.locationClassCopyTargetClassHandlers.get(locationClass);

		if(locationClassMap == null)
		{
			return this.generalCopyTargetClassHandlers;
		}

		final HashMap<Class<?>, CopyHandler> returnMap = new HashMap<>();
		returnMap.putAll(this.generalCopyTargetClassHandlers);
		returnMap.putAll(locationClassMap);
		return returnMap;
	}

	/**
	 * Gets the all copy target field handlers for.
	 *
	 * @param locationClass the location class
	 * @return the all copy target field handlers for
	 */
	private HashMap<Field, CopyHandler> getAllCopyTargetFieldHandlersFor(final Class<?> locationClass)
	{
		final HashMap<Field , CopyHandler> locationClassMap;
		locationClassMap = this.locationClassCopyTargetFieldHandlers.get(locationClass);

		if(locationClassMap == null)
		{
			return this.generalCopyTargetFieldHandlers;
		}

		final HashMap<Field , CopyHandler> returnMap = new HashMap<>();
		returnMap.putAll(this.generalCopyTargetFieldHandlers);
		returnMap.putAll(locationClassMap);
		return returnMap;
	}

	/**
	 * Gets the all copy target annotation handlers for.
	 *
	 * @param locationClass the location class
	 * @return the all copy target annotation handlers for
	 */
	private HashMap<Class<? extends Annotation>, CopyHandler> getAllCopyTargetAnnotationHandlersFor(
		final Class<?> locationClass
	)
	{
		final HashMap<Class<? extends Annotation>, CopyHandler> locationClassMap;
		locationClassMap = this.locationClassAnnotationClassHandlers.get(locationClass);

		if(locationClassMap == null)
		{
			return this.generalAnnotationHandlers;
		}

		final HashMap<Class<? extends Annotation>, CopyHandler> returnMap = new HashMap<>();
		returnMap.putAll(this.generalAnnotationHandlers);
		returnMap.putAll(locationClassMap);
		return returnMap;
	}

	/**
	 * Execute.
	 *
	 * @param <O> the generic type
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param source the source
	 * @param target the target
	 * @param commonClass the common class
	 * @return the t
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public <O extends Object, S extends O, T extends O> T execute(
		final S        source     ,
		final T        target     ,
		final Class<O> commonClass
	)
		throws IllegalAccessRuntimeException
	{
		return this.execute(source, target, commonClass, null);
	}

	/**
	 * Execute.
	 *
	 * @param <O> the generic type
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param source the source
	 * @param target the target
	 * @param commonClass the common class
	 * @param fieldsToExclude the fields to exclude
	 * @return the t
	 * @throws IllegalAccessRuntimeException the illegal access runtime exception
	 */
	public <O extends Object, S extends O, T extends O> T execute(
		final S          source         ,
		final T          target         ,
		final Class<O>   commonClass    ,
		final Set<Field> fieldsToExclude
	)
		throws IllegalAccessRuntimeException
	{
		final Class<?> locationClass = source.getClass();

		HashMap<Class<?>, CopyHandler> copyTargetClassHandlers =
			this.getAllCopyTargetClassHandlersFor(locationClass)
		;
		HashMap<Class<? extends Annotation>, CopyHandler> annotationHandlers =
			this.getAllCopyTargetAnnotationHandlersFor(locationClass)
		;
		HashMap<Field, CopyHandler> copyTargetFieldHandlers =
			this.getAllCopyTargetFieldHandlersFor(locationClass)
		;

		//all 3 null will cause the copy method to take a more performant code branch
		if(copyTargetClassHandlers.size() + copyTargetFieldHandlers.size() + annotationHandlers.size() == 0)
		{
			copyTargetClassHandlers = null;
			copyTargetFieldHandlers = null;
			annotationHandlers = null;
		}

		CopyHandler genericHandler = this.locationClassGenericHandlers.get(locationClass);
		if(genericHandler == null)
		{
			genericHandler = this.genericHandler;
		}

		return copy(
			source                 ,
			target                 ,
			commonClass            ,
			fieldsToExclude        ,
			copyTargetFieldHandlers,
			annotationHandlers     ,
			copyTargetClassHandlers,
			genericHandler
		);
	}

}
