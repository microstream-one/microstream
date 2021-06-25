package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
