package net.jadoth.com.binary;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.com.ComClientChannelCreator;
import net.jadoth.com.ComProtocol;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;

public interface ComClientChannelCreatorBinary<C> extends ComClientChannelCreator<C>
{
	/* (16.11.2018 TM)FIXME: JET-43: integrate ComClientChannelCreatorBinary
	 * Also, but tricky: Maybe the "Default" concept here could be replaced by the ComConnectionHandler concept.
	 * That, in turn, might require to consolidate the Binary type to act as an implicit self-collection.
	 */
	// (16.11.2018 TM)TODO: set Persistence.typeMismatchValidatorFailing()?
	public static ComClientChannelCreatorBinary.Default New(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComClientChannelCreatorBinary.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}

	public final class Default
	extends ComClientChannelCreator.Abstract<SocketChannel>
	implements ComClientChannelCreatorBinary<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryPersistenceFoundation<?> foundation        ;
		private final BufferSizeProvider             bufferSizeProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider
		)
		{
			super();
			this.foundation         = foundation        ;
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected PersistenceManager<?> createPersistenceManager(
			final SocketChannel connection,
			final ComProtocol   protocol
		)
		{
			final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider
			);
			this.foundation.setPersistenceChannel(channel);
			
			final PersistenceTypeDictionaryManager typeDictionaryManager =
				PersistenceTypeDictionaryManager.Immutable(protocol)
			;
			this.foundation.setTypeDictionaryManager(typeDictionaryManager);
			
			// (16.11.2018 TM)TODO: JET-49: divergent target ByteOrder not supported yet in BinaryPersistence.
			this.foundation.setTargetByteOrder(protocol.byteOrder());
						
			// including TypeHandlerManager initialization
			return this.foundation.createPersistenceManager();
		}
		
	}
	
}
