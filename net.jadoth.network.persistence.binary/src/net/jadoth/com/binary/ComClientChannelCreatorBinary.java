package net.jadoth.com.binary;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.com.ComClientChannelCreator;
import net.jadoth.com.ComProtocol;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceManager;

public interface ComClientChannelCreatorBinary<C> extends ComClientChannelCreator<C>
{
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
			
			/* FIXME ComClientChannelCreatorBinary.Default#createPersistenceManager()
			 * - create TypeDictionaryIoHandler:
			 *   read-only implementation that throws exceptions for writing operations.
			 *   or maybe even at the TypeDictionaryManager level.
			 *   loader implementation that feeds from the received TypeDictionary string.
			 *   Only problem with that is: the nicely typed protocol parameter would have to degrade into a string.
			 *   Or maybe an alterantive provider logic that builds a runtime TypeDictionary from the received view.
			 * 
			 * - set byte order
			 * - set channel to foundation
			 */
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
	
}
