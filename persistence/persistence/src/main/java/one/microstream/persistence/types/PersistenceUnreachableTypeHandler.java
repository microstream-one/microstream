package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;

import java.util.function.Consumer;


/**
 * This is, of course, not a handler that is unreachable, but a handler for a type whose instances are
 * (decided by the developer) no longer reachable in the entity graph, despite potentially still being present
 * in a live database, but not having been cleaned up, yet. However, this effectively means that the handler
 * itself should be kind of "unreachable", too, in the sense that it may never be necessary to access and use it.
 * Without such a handler, a class could never be removed at the design level without replacement
 * until the last instance of it has been cleaned up the the storage housekeeping. That is an unpleasent dependency.
 * The preferable way is to explicitly tell the typing system to "ignore" those types. That is done by registering
 * a kind of dummy type handler. That dummy handler is this type here.
 * 
 * 
 *
 * @param <D> the data type
 * @param <T> the handled type
 */
public interface PersistenceUnreachableTypeHandler<D, T> extends PersistenceLegacyTypeHandler<D, T>
{
	@Override
	public default Class<D> dataType()
	{
		return null;
	}
	
	@Override
	public default void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op: for all intents and purposes, an unreachable instance's references need not and must not be iterated.
	}

	@Override
	public default void iterateLoadableReferences(final D data, final PersistenceReferenceLoader iterator)
	{
		// no-op: for all intents and purposes, an unreachable instance's references need not and must not be iterated.
	}
	
	@Override
	public default Class<T> type()
	{
		return null;
	}

	@Override
	public default T create(final D data, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException(
			"Cannot create an instance of a type explicitly marked as unreachable: " + this.toTypeIdentifier()
		);
	}

	@Override
	public default void updateState(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		// nothing to do here in either case (null or exception)
	}

	@Override
	public default void complete(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		// nothing to do here in either case (null or exception)
	}
	
	@Override
	default <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// nothing to iterate. No need to throw an exception, either.
		return logic;
	}
	
	
	
	public static <D, T> PersistenceUnreachableTypeHandler<D, T> New(
		final PersistenceTypeDefinition typeDefinition
	)
	{
		return new PersistenceUnreachableTypeHandler.Default<>(
			notNull(typeDefinition)
		);
	}
	
	public class Default<D, T>
	extends PersistenceLegacyTypeHandler.Abstract<D, T>
	implements PersistenceUnreachableTypeHandler<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
				
	}

}
