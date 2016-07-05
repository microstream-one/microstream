package net.jadoth.cql;

import java.util.function.Consumer;

import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;

public final class CqlWrapperCollectorLinkingFinalizing<O, R> implements Aggregator<O, R>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final R                   target   ;
	final BiProcedure<O, R>   linker   ;
	final Consumer<? super R> finalizer;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	CqlWrapperCollectorLinkingFinalizing(
		final R                   target   ,
		final BiProcedure<O, R>   linker   ,
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
