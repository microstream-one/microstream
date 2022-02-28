package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

public class ComHandlerSendReceiveMessageData implements ComHandlerSend<ComMessageData>, ComHandlerReceive<ComMessageData>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendReceiveMessageData(
		final ComChannelDynamic<?> channel
	)
	{
		super();
		this.comChannel = channel;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object processMessage(final ComMessageData message)
	{
		return message.getData();
	}

	@Override
	public Object processMessage(final Object received)
	{
		final ComMessageData message = (ComMessageData)received;
		return this.processMessage(message);
	}

	@Override
	public Object sendMessage(final ComMessageData message)
	{
		this.comChannel.send(message);
		return null;
	}

	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageData message = (ComMessageData)messageObject;
		return this.sendMessage(message);
	}
}
