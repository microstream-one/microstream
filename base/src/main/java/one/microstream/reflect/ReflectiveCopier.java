package one.microstream.reflect;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.XArrays;
import one.microstream.functional.XFunc;


/**
 * 
 * 
 *
 * @param <S> Does not necessarily have to be a class.
 */
public interface ReflectiveCopier<S>
{
	public default <T extends S> T copyTo(final T targetInstance)
	{
		return this.copy(this.sourceInstance(), targetInstance);
	}
	
	public <T extends S> T copy(S sourceInstance, T targetInstance);
	
	/**
	 * Does not necessarily have to be S. S could be an interface.
	 * @return
	 */
	public Class<?> sourceClass();
	
	public S sourceInstance();
	
	public Predicate<? super Field> fieldSelector();
	
	public CopyPredicate copySelector();
	
	public <I extends Consumer<? super Field>> I iterateFields(I iterator);
	
	
	
	public static <S> ReflectiveCopier<S> New(final S sourceInstance)
	{
		return New(XReflect.getClass(sourceInstance), sourceInstance);
	}
	
	public static <S, C extends S> ReflectiveCopier<S> New(final Class<C> sourceClass)
	{
		return New(sourceClass, null);
	}
	
	public static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C> sourceClass   ,
		final S        sourceInstance
	)
	{
		return New(sourceClass, sourceInstance, XFunc.all());
	}
	
	public static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C>                 sourceClass   ,
		final S                        sourceInstance,
		final Predicate<? super Field> fieldSelector
	)
	{
		return New(sourceClass, sourceInstance, fieldSelector, CopyPredicate::all);
	}
	
	public static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C>                 sourceClass   ,
		final S                        sourceInstance,
		final Predicate<? super Field> fieldSelector ,
		final CopyPredicate            copySelector
	)
	{
		final Field[] copyFields = XReflect.collectInstanceFields(sourceClass, fieldSelector);
		
		return new ReflectiveCopier.Default<>(
			sourceClass,
			sourceInstance,
			fieldSelector,
			copyFields,
			copySelector
		);
	}
	
	public final class Default<S> implements ReflectiveCopier<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?>                 sourceClass   ;
		private final S                        sourceInstance;
		private final Predicate<? super Field> fieldSelector ;
		private final Field[]                  fields        ;
		private final CopyPredicate            copySelector  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final Class<?>                 sourceClass   ,
			final S                        sourceInstance,
			final Predicate<? super Field> fieldSelector ,
			final Field[]                  fields        ,
			final CopyPredicate            copySelector
		)
		{
			super();
			this.sourceClass    = sourceClass   ;
			this.sourceInstance = sourceInstance;
			this.fieldSelector  = fieldSelector ;
			this.fields         = fields        ;
			this.copySelector   = copySelector  ;
		}
		

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final Class<?> sourceClass()
		{
			return this.sourceClass;
		}
		
		@Override
		public final S sourceInstance()
		{
			return this.sourceInstance;
		}

		@Override
		public final Predicate<? super Field> fieldSelector()
		{
			return this.fieldSelector;
		}

		@Override
		public final CopyPredicate copySelector()
		{
			return this.copySelector;
		}

		@Override
		public final <I extends Consumer<? super Field>> I iterateFields(final I iterator)
		{
			return XArrays.iterate(this.fields, iterator);
		}

		@Override
		public final <T extends S> T copy(final S sourceInstance, final T targetInstance)
		{
			return XReflect.copyFields(sourceInstance, targetInstance, this.fields, this.copySelector);
		}
		
	}
	
}
