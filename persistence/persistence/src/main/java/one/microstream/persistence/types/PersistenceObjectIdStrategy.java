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

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.chars.XParsing;
import one.microstream.collections.types.XReference;
import one.microstream.exceptions.ParsingException;

public interface PersistenceObjectIdStrategy
{
	public PersistenceObjectIdProvider createObjectIdProvider();
	
	public String strategyTypeNameObjectId();
	
	
	
	public static PersistenceObjectIdStrategy.Transient Transient()
	{
		return new PersistenceObjectIdStrategy.Transient(Persistence.defaultStartObjectId());
	}
	
	public static PersistenceObjectIdStrategy.Transient Transient(final long startingObjectId)
	{
		return new PersistenceObjectIdStrategy.Transient(Persistence.validateObjectId(startingObjectId));
	}
	
	public final class Transient implements PersistenceObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}
		
		public static char openingCharacter()
		{
			return '(';
		}
		
		public static char closingCharacter()
		{
			return ')';
		}
		
		public static void assemble(final VarString vs, final PersistenceObjectIdStrategy.Transient idStrategy)
		{
			vs
			.add(PersistenceObjectIdStrategy.Transient.typeName())
			.add(openingCharacter()).add(idStrategy.startingObjectId()).add(closingCharacter())
			;
		}
		
		public static PersistenceObjectIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceObjectIdStrategy.Transient.class,
				typeName(),
				typeIdStrategyContent
			);
			
			final char[] input  = XChars.readChars(typeIdStrategyContent);
			final int    iBound = input.length;
			
			final XReference<String> valueString = X.Reference(null);
			
			int i = typeName().length();
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			i = XParsing.checkCharacter(input, i, openingCharacter(), typeName());
			i = XParsing.parseToSimpleTerminator(input, i, iBound, closingCharacter(), valueString);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			if(i != iBound)
			{
				throw new ParsingException("Invalid trailing content at index " + i);
			}
			
			return valueString.get().isEmpty()
				? PersistenceObjectIdStrategy.Transient()
				: PersistenceObjectIdStrategy.Transient(Long.parseLong(valueString.get()))
			;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingObjectId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.startingObjectId = startingObjectId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final long startingObjectId()
		{
			return this.startingObjectId;
		}
		
		@Override
		public String strategyTypeNameObjectId()
		{
			return Transient.typeName();
		}

		@Override
		public final PersistenceObjectIdProvider createObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Transient(this.startingObjectId);
		}
		
	}
	
	public static PersistenceObjectIdStrategy.None None()
	{
		return new PersistenceObjectIdStrategy.None();
	}
	
	public final class None implements PersistenceObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "None";
		}
		
		public static void assemble(final VarString vs, final PersistenceObjectIdStrategy.None idStrategy)
		{
			vs
			.add(PersistenceObjectIdStrategy.None.typeName())
			;
		}
		
		public static PersistenceObjectIdStrategy.None parse(final String typeIdStrategyContent) throws ParsingException
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceObjectIdStrategy.None.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			// the rest of the string is ignored intentionally.
			return PersistenceObjectIdStrategy.None();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		None()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String strategyTypeNameObjectId()
		{
			return None.typeName();
		}

		@Override
		public final PersistenceObjectIdProvider createObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Failing();
		}
		
	}
	
	@FunctionalInterface
	public interface Assembler<S extends PersistenceObjectIdStrategy>
	{
		public void assembleIdStrategy(VarString vs, S idStrategy);
	}
	
	@FunctionalInterface
	public interface Parser<S extends PersistenceObjectIdStrategy>
	{
		public S parse(String typeIdStrategyContent);
	}
	
}
