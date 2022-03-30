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

public interface PersistenceUnreachableTypeHandlerCreator<D>
{
	public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <D> PersistenceUnreachableTypeHandlerCreator<D> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Default<>();
	}
	
	public final class Default<D> implements PersistenceUnreachableTypeHandlerCreator<D>
	{
		@Override
		public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
//			XDebug.println("Creating unreachable type handler for " + typeDefinition.toTypeIdentifier());
			return PersistenceUnreachableTypeHandler.New(typeDefinition);
		}
		
	}
	
}
