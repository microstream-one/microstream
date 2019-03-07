package one.microstream.network.types;

import static one.microstream.X.notNull;
import static one.microstream.bytes.XBytes.parseByteOrder;

import java.nio.ByteOrder;

import one.microstream.chars.VarString;
import one.microstream.math.XMath;

public interface NetworkClientGreeting
{
	public String    host();
	public int       port();
	public ByteOrder byteOrder();



	public interface Creator<P>
	{
		public NetworkClientGreeting createGreeting(P parameter);
	}



	public class Implementation implements NetworkClientGreeting
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final String SEPERATOR       = ":";
		private static final String TERMINATOR      = "\n";
		private static final String TAG_HOST        = "host";
		private static final String TAG_PORT        = "port";
		private static final String TAG_BYTEORDER   = "byteorder";
		private static final String TAG_SESSIONID   = "sessionid";
		private static final String TAG_PROTOCOL    = "protocol";
		private static final char[] CHARS_HOST      =             ( TAG_HOST      + SEPERATOR).toCharArray();
		private static final char[] CHARS_PORT      = (TERMINATOR + TAG_PORT      + SEPERATOR).toCharArray();
		private static final char[] CHARS_BYTEORDER = (TERMINATOR + TAG_BYTEORDER + SEPERATOR).toCharArray();
		private static final char[] CHARS_SESSIONID = (TERMINATOR + TAG_SESSIONID + SEPERATOR).toCharArray();
		private static final char[] CHARS_PROTOCOL  = (TERMINATOR + TAG_PROTOCOL  + SEPERATOR).toCharArray();


		// (30.10.2012 TM)XXX: horrible prototype code, overhaul
		public static NetworkClientGreeting.Implementation parseGreeting(final String greeting)
		{
			final int protocolIndex = greeting.indexOf(TAG_PROTOCOL);
			if(protocolIndex < 0)
			{
				throw new IllegalArgumentException(); // (30.10.2012)EXCP: proper exception
			}
			final String protocolPart = greeting.substring(protocolIndex + CHARS_PROTOCOL.length - 1);
			final String[] standardParts = greeting.substring(0, protocolIndex).split("[\\:\\n*]");

			String    host      = null;
			Integer   port      = null;
			ByteOrder byteOrder = null;
			Long      sessionId = null;

			try
			{
				for(int i = 0; i < standardParts.length; i++)
				{
					final String identifier = standardParts[i].trim();
					if(identifier.length() == 0)
					{
						continue;
					}
					if(TAG_HOST.equals(identifier))
					{
						host = standardParts[++i].trim();
					}
					else if(TAG_PORT.equals(identifier))
					{
						port = Integer.parseInt(standardParts[++i].trim());
					}
					else if(TAG_BYTEORDER.equals(identifier))
					{
						byteOrder = parseByteOrder(standardParts[++i].trim());
					}
					else if(TAG_SESSIONID.equals(identifier))
					{
						sessionId = Long.parseLong(standardParts[++i].trim());
					}
				}
			}
			catch(final Exception e)
			{
				throw new IllegalArgumentException(e);
			}
			if(host == null)
			{
				throw new IllegalArgumentException("Unknown host");
			}
			if(port == null)
			{
				throw new IllegalArgumentException("Unknown port");
			}
			if(byteOrder == null)
			{
				throw new IllegalArgumentException("Unknown byte order");
			}
			if(sessionId == null)
			{
				throw new IllegalArgumentException("Unknown session id");
			}

			return new Implementation(host, port, byteOrder, sessionId, protocolPart);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String    host;
		private final int       port;
		private final ByteOrder byteOrder;
		private final long      sessionId; // (31.10.2012 TM)FIXME: move session id to subclass
		private final String    protocol;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final String    host     ,
			final int       port     ,
			final ByteOrder byteOrder,
			final long      sessionId,
			final String    protocol
		)
		{
			super();
			this.host      = notNull(host)        ;
			this.port      = XMath.positive(port);
			this.byteOrder = notNull(byteOrder)   ;
			this.sessionId = sessionId            ;
			this.protocol  = notNull(protocol)    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public String host()
		{
			return this.host;
		}

		@Override
		public int port()
		{
			return this.port;
		}

		@Override
		public ByteOrder byteOrder()
		{
			return this.byteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		public VarString assemble(final VarString vc)
		{
			vc
			.add(CHARS_HOST).add(this.host)
			.add(CHARS_PORT).add(this.port)
			.add(CHARS_BYTEORDER).add(this.byteOrder.toString())
			.add(CHARS_SESSIONID).add(this.sessionId)
			.add(CHARS_PROTOCOL).add(this.protocol)
			;
			return vc;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return this.assemble(VarString.New()).toString();
		}

	}

}
