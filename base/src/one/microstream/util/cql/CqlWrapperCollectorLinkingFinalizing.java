package one.microstream.util.cql;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.functional.Aggregator;

public final class CqlWrapperCollectorLinkingFinalizing<O, R> implements Aggregator<O, R>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final R                   target   ;
	final BiConsumer<O, R>   linker   ;
	final Consumer<? super R> finalizer;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	CqlWrapperCollectorLinkingFinalizing(
		final R                   target   ,
		final BiConsumer<O, R>   linker   ,
		final Consumer<? super R> finalizer
	)
	{
		super();
		this.target    = target   ;
		this.linker    = linker   ;
		this.finalizer = finalizer;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final O element)
	{
		this.linker.accept(element, this.target);
	}

	@Override
	public final R yield()
	{
		this.finalizer.accept(this.target);
		return this.target;
	}

}
