package one.microstream.util;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.BulkList;


/**
 * An instance that collects (buffers) elements and gets notified once the collecting process is completed.
 *
 * 
 * @param <E>
 */
public interface BufferingCollector<E>
{
	public void accept(E element);

	public void resetElements();

	public void finalizeElements();

	public long size();

	public default boolean isEmpty()
	{
		return this.size() == 0;
	}



	public static <E> BufferingCollector<E> New(final Consumer<? super E> finalizingLogic)
	{
		return new BufferingCollector.Default<>(notNull(finalizingLogic), null);
	}

	public static <E> BufferingCollector<E> New(
		final Consumer<? super E> finalizingLogic   ,
		final Consumer<? super E> collectingListener
	)
	{
		return new BufferingCollector.Default<>(notNull(finalizingLogic), collectingListener);
	}

	public final class Default<E> implements BufferingCollector<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final BulkList<E>          buffer          = BulkList.New();
		private final Consumer<? super E> finalizingLogic   ;
		private final Consumer<? super E> collectingListener;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Consumer<? super E> finalizingLogic, final Consumer<? super E> collectingListener)
		{
			super();
			this.finalizingLogic    = finalizingLogic   ;
			this.collectingListener = collectingListener;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final E element)
		{
			if(this.collectingListener != null)
			{
				this.collectingListener.accept(element);
			}
			this.buffer.accept(element);
		}

		@Override
		public void resetElements()
		{
			this.buffer.clear();
		}

		@Override
		public void finalizeElements()
		{
			this.buffer.iterate(this.finalizingLogic);
		}

		@Override
		public long size()
		{
			return this.buffer.size();
		}

	}

}
