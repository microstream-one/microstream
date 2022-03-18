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

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeMismatchValidator<D>
{
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
	);
	
	
	
	public static <D> PersistenceTypeMismatchValidator.Failing<D> Failing()
	{
		return new PersistenceTypeMismatchValidator.Failing<>();
	}
	
	public static <D> PersistenceTypeMismatchValidator.NoOp<D> NoOp()
	{
		return new PersistenceTypeMismatchValidator.NoOp<>();
	}
	
	public final class Failing<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			if(unmatchableTypeHandlers.isEmpty())
			{
				return;
			}
			
			final VarString vs = VarString.New("[");
			unmatchableTypeHandlers.iterate(th -> vs.add(',').add(th.type().getName()));
			vs.deleteLast().setLast(']');
			
			throw new PersistenceException("Persistence type definition mismatch for the following types: " + vs);
			
		}
	}
	
	public final class NoOp<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			// no-op
		}
	}
	
}
