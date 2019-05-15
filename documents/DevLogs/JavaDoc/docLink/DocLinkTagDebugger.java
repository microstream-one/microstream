package doclink;

public final class DocLinkTagDebugger implements DocLinkTagProcessor
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final DocLinkTagDebugger New()
		{
			return new DocLinkTagDebugger();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StringBuilder sb = new StringBuilder();
		
		private int currentIndex = 0;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		DocLinkTagDebugger()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void signalTagStart(final char[] chars, final int index)
		{
			this.checkForCatchUp(chars, index);
		}

		@Override
		public void processDocLinkContent(final char[] input, final int start, final int bound)
		{
			this.handleContent(
				input,
				UtilsDocLink.skipWhiteSpaces(input, start, bound),
				UtilsDocLink.trimWhiteSpaces(input, start, bound)
			);
		}
		
		@Override
		public void signalTagEnd(final char[] chars, final int index)
		{
			this.currentIndex = index;
		}
		
		@Override
		public void signalInputEnd(final char[] chars, final int bound)
		{
			this.checkForCatchUp(chars, bound);
		}
		
		private void checkForCatchUp(final char[] input, final int index)
		{
			if(this.currentIndex < index)
			{
				this.sb.append(input, this.currentIndex, index - this.currentIndex);
			}
		}
		
		private void handleContent(final char[] input, final int start, final int bound)
		{
//			this.DEBUG_passThrough(input, start, bound);
			this.DEBUG_printAndBlacken(input, start, bound);
		}
		
		@Deprecated
		final void DEBUG_passThrough(final char[] input, final int start, final int bound)
		{
			this.sb.append(input, start, bound - start);
		}

		@Deprecated
		final void DEBUG_printAndBlacken(final char[] chars, final int offset, final int bound)
		{
			System.out.println("Parsed content: " + String.valueOf(chars, offset, bound - offset));
			this.DEBUG_fillWith(bound - offset, 'x');
		}
		
		private void DEBUG_fillWith(final int amount, final char c)
		{
			for(int i = amount; i --> 0;)
			{
				this.sb.append(c);
			}
		}
		
		public final DocLinkTagDebugger reset()
		{
			// because a clear() or equivalent was too hard to implement for them ...
			this.sb.setLength(0);
			this.currentIndex = 0;
			
			return this;
		}
		
		public final String yield()
		{
			final String result = this.sb.toString();
			this.reset();
			
			return result;
		}
		
	}