package one.microstream.storage.exceptions;

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

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;

public class StorageExceptionDisruptingExceptions extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final XGettingSequence<Throwable> disruptions;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions
	)
	{
		super();
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final String                      message
	)
	{
		super(message);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final Throwable                   cause
	)
	{
		super(cause);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final String                      message    ,
		final Throwable                   cause
	)
	{
		super(message, cause);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions       ,
		final String                      message           ,
		final Throwable                   cause             ,
		final boolean                     enableSuppression ,
		final boolean                     writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.disruptions = disruptions;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final XGettingSequence<Throwable> disruptions()
	{
		return this.disruptions;
	}
	
	@Override
	public String assembleOutputString()
	{
		final VarString vs = VarString.New("Disruptions: {");
		for(final Throwable d : this.disruptions)
		{
			vs.add(d.getClass().getName()).add(':').add(d.getMessage()).add(',').blank();
		}
		vs.deleteLast().add('}');
		
		return vs.toString();
	}
	
}
