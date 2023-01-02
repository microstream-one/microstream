package one.microstream.persistence.binary.java.net;

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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import one.microstream.persistence.binary.internal.CustomBinaryHandler;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryField;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerInetSocketAddress extends CustomBinaryHandler<InetSocketAddress>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInetSocketAddress New()
	{
		return new BinaryHandlerInetSocketAddress();
	}
	
	public static String uninitializedHostName()
	{
		return "[uninitialized]";
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final BinaryField<InetSocketAddress>
		hostname = Field(String.class, InetSocketAddress::getHostName),
		address  = Field(InetAddress.class, InetSocketAddress::getAddress),
		port     = Field_int(InetSocketAddress::getPort)
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BinaryHandlerInetSocketAddress()
	{
		super(InetSocketAddress.class);
	}
	


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public InetSocketAddress create(final Binary data, final PersistenceLoadHandler handler)
	{
		/*
		 * Blank instance with dummy values that gets updated later.
		 * Because the way the class is written, there is no other way of handling it.
		 */
		return InetSocketAddress.createUnresolved(uninitializedHostName(), 0);
	}
	
	@Override
	public void initializeState(
		final Binary                 data    ,
		final InetSocketAddress      instance,
		final PersistenceLoadHandler handler
	)
	{
		final String      hostname = (String)this.hostname.readReference(data, handler);
		final InetAddress address  = (InetAddress)this.address.readReference(data, handler);
		final int         port     = this.port.read_int(data);
		
		// sadly, there is no way of directly copying the weirdly written class.
		final InetSocketAddress copyDummy = address == null
			? new InetSocketAddress(hostname, port)
			: new InetSocketAddress(address, port)
		;
		XReflect.copyFields(copyDummy, instance);
	}
	
	@Override
	public void updateState(
		final Binary                 data    ,
		final InetSocketAddress      instance,
		final PersistenceLoadHandler handler
	)
	{
		// super class does validation generically
		super.updateState(data, instance, handler);
	}
		
}
