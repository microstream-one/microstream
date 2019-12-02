package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.collections.types.XCollection;
import one.microstream.functional.ThrowingProcedure;

public interface DisruptionCollectorExecuting<E> extends DisruptionCollector
{
	public void executeOn(final E element);
	
	
	
	public static <E> DisruptionCollectorExecuting<E> New(final ThrowingProcedure<? super E, ?> logic)
	{
		return new DisruptionCollectorExecuting.WrapperThrowingProcedure<>(
			notNull(logic)                                 ,
			DisruptionCollector.defaultCollectionSupplier(),
			null
		);
	}
	
	public static <E> DisruptionCollectorExecuting<E> New(
		final ThrowingProcedure<? super E, ?>            logic             ,
		final Supplier<? extends XCollection<Throwable>> collectionSupplier
	)
	{
		return new DisruptionCollectorExecuting.WrapperThrowingProcedure<>(
			notNull(logic)    ,
			collectionSupplier,
			null
		);
	}
	
	public static <E> DisruptionCollectorExecuting<E> New(
		final ThrowingProcedure<? super E, ?> logic     ,
		final XCollection<Throwable>          collection
	)
	{
		return new DisruptionCollectorExecuting.WrapperThrowingProcedure<>(
			notNull(logic),
			null          ,
			collection
		);
	}
		
	public class WrapperThrowingProcedure<E> extends DisruptionCollector.Default implements DisruptionCollectorExecuting<E>
	{
		private final ThrowingProcedure<? super E, ?> logic;

		public WrapperThrowingProcedure(
			final ThrowingProcedure<? super E, ?>            logic             ,
			final Supplier<? extends XCollection<Throwable>> collectionSupplier,
			final XCollection<Throwable>                     disruptions
		)
		{
			super(collectionSupplier, disruptions);
			this.logic = logic;
		}
		
		@Override
		public void executeOn(final E element)
		{
			this.execute(this.logic, element);
		}
		
		
	}
}
