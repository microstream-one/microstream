package one.microstream.persistence.binary.java.util;

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

import java.util.Properties;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


/**
 * This type handler cannot handle a {@link Properties}' defaults values.
 * They simply left no way to query the defaults of a certain {@link Properties}
 * instance.<p>
 * For a type handler that provides this functionality, albeit specific to JDK 8 (and higher but still compatible JDKs),
 * see {@literal one.microstream.jdk8.java.util.BinaryHandlerProperties}.
 *
 */
public final class BinaryHandlerProperties extends AbstractBinaryHandlerMap<Properties>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerProperties New()
	{
		return new BinaryHandlerProperties();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerProperties()
	{
		super(Properties.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Properties create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Properties();
	}
	
}
