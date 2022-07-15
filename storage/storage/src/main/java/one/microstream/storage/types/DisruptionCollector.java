package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.exceptions.MultiCauseException;
import one.microstream.functional.ThrowingProcedure;
import one.microstream.util.UtilStackTrace;

public interface DisruptionCollector
{
	public default void execute(final Runnable r)
	{
		try
		{
			r.run();
		}
		catch(final Throwable d)
		{
			this.collectDisruption(d);
		}
	}
	
	public default <E> void execute(final Consumer<? super E> logic, final E element)
	{
		try
		{
			logic.accept(element);
		}
		catch(final Throwable d)
		{
			this.collectDisruption(d);
		}
	}
	
	public default <E> void execute(final ThrowingProcedure<? super E, ?> logic, final E element)
	{
		try
		{
			logic.accept(element);
		}
		catch(final Throwable d)
		{
			this.collectDisruption(d);
		}
	}
	
	public default <O> O execute(final Supplier<O> logic)
	{
		try
		{
			return logic.get();
		}
		catch(final Throwable d)
		{
			this.collectDisruption(d);
			
			/*
			 * Since swallowing (collecting) disruptions is the whole purpose of this thing,
			 * there is no other option than to return null in this case.
			 */
			return null;
		}
	}
	
	public default <I, O> O execute(final Function<? super I, O> logic, final I input)
	{
		try
		{
			return logic.apply(input);
		}
		catch(final Throwable d)
		{
			this.collectDisruption(d);
			
			/*
			 * Since swallowing (collecting) disruptions is the whole purpose of this thing,
			 * there is no other option than to return null in this case.
			 */
			return null;
		}
	}
	
	public boolean hasDisruptions();
	
	public DisruptionCollector reset();
	
	public DisruptionCollector collectDisruption(Throwable disruption);
	
	public default XGettingCollection<Throwable> yield()
	{
		return this.yield(BulkList.New());
	}
	
	public <C extends Consumer<? super Throwable>> C yield(C target);
	
	public MultiCauseException toMultiCauseException(int stackTraceCutDepth);
	
	public Throwable[] toArray();
	
	public default MultiCauseException toMultiCauseException()
	{
		// actually 0, but this method itself shall but cut (hehe).
		return this.toMultiCauseException(1);
	}
	
	
	public static Supplier<? extends XCollection<Throwable>> defaultCollectionSupplier()
	{
		return BulkList::New;
	}
	
	
	public static DisruptionCollector New()
	{
		return new DisruptionCollector.Default(defaultCollectionSupplier(), null);
	}
	
	public static DisruptionCollector New(final Supplier<? extends XCollection<Throwable>> collectionSupplier)
	{
		return new DisruptionCollector.Default(collectionSupplier, null);
	}
	
	public static DisruptionCollector New(final XCollection<Throwable> collection)
	{
		return new DisruptionCollector.Default(null, collection);
	}
	
	public class Default implements DisruptionCollector
	{
		private final Supplier<? extends XCollection<Throwable>> collectionSupplier;
		
		private XCollection<Throwable> disruptions;

		Default(
			final Supplier<? extends XCollection<Throwable>> collectionSupplier,
			final XCollection<Throwable>                     disruptions
		)
		{
			super();
			this.collectionSupplier = collectionSupplier;
			this.disruptions        = disruptions       ;
		}

		@Override
		public final synchronized DisruptionCollector collectDisruption(final Throwable disruption)
		{
			if(this.disruptions == null)
			{
				this.disruptions = this.collectionSupplier.get();
			}
			
			this.disruptions.add(disruption);
			
			return this;
		}
		
		@Override
		public final synchronized <C extends Consumer<? super Throwable>> C yield(final C target)
		{
			if(this.disruptions != null)
			{
				this.disruptions.iterate(target);
			}
			
			return target;
		}
		
		@Override
		public final synchronized boolean hasDisruptions()
		{
			return this.disruptions != null && !this.disruptions.isEmpty();
		}
		
		@Override
		public DisruptionCollector reset()
		{
			if(this.collectionSupplier != null)
			{
				this.disruptions = null;
			}
			else
			{
				this.disruptions.clear();
			}
			
			return this;
		}
		

		@Override
		public Throwable[] toArray()
		{
			final Throwable[] array = this.disruptions == null
				? null
				: this.disruptions.toArray(Throwable.class)
			;
			
			return array;
		}
		
		@Override
		public MultiCauseException toMultiCauseException(final int stackTraceCutDepth)
		{
			final MultiCauseException exception = new MultiCauseException(this.toArray());
			
			if(stackTraceCutDepth > 0)
			{
				UtilStackTrace.cutStacktraceByN(exception, stackTraceCutDepth + 1);
			}
			
			return exception;
		}
		
	}
	
}
