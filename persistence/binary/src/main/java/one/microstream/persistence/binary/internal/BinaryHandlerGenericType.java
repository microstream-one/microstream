package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
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

import java.lang.reflect.Field;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeInstantiator;

public final class BinaryHandlerGenericType<T> extends AbstractBinaryHandlerReflective<T>
{
	public static <T> BinaryHandlerGenericType<T> New(
		final Class<T>                               type                      ,
		final String                                 typeName                  ,
		final XGettingEnum<Field>                    persistableFields         ,
		final XGettingEnum<Field>                    persisterFields           ,
		final PersistenceFieldLengthResolver         lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator  eagerStoringFieldEvaluator,
		final PersistenceTypeInstantiator<Binary, T> instantiator              ,
		final boolean                                switchByteOrder
	)
	{
		return new BinaryHandlerGenericType<>(
			type                      ,
			typeName                  ,
			persistableFields         ,
			persisterFields           ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			instantiator              ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeInstantiator<Binary, T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericType(
		final Class<T>                               type                      ,
		final String                                 typeName                  ,
		final XGettingEnum<Field>                    persistableFields         ,
		final XGettingEnum<Field>                    persisterFields           ,
		final PersistenceFieldLengthResolver         lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator  eagerStoringFieldEvaluator,
		final PersistenceTypeInstantiator<Binary, T> instantiator              ,
		final boolean                                switchByteOrder
	)
	{
		super(type, typeName, persistableFields, persisterFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.instantiator = notNull(instantiator);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiate(data);
	}

}
