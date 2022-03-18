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

import static one.microstream.X.notNull;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.math.XMath;
import one.microstream.persistence.exceptions.PersistenceExceptionParser;
import one.microstream.persistence.exceptions.PersistenceExceptionParserIncompleteInput;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingComplexTypeDefinition;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingEnumName;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingMemberName;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingMemberTerminator;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingMemberType;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingPrimitiveDefinition;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingType;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingTypeBody;
import one.microstream.persistence.exceptions.PersistenceExceptionParserMissingTypeId;
import one.microstream.reflect.XReflect;
import one.microstream.util.Substituter;
import one.microstream.util.UtilStackTrace;

public interface PersistenceTypeDictionaryParser
{
	public XGettingSequence<? extends PersistenceTypeDictionaryEntry> parseTypeDictionaryEntries(String input)
		throws PersistenceExceptionParser
	;

	public static PersistenceTypeDictionaryParser.Default New(
		final PersistenceTypeResolver        typeResolver       ,
		final PersistenceFieldLengthResolver fieldLengthResolver,
		final PersistenceTypeNameMapper      typeNameMapper
	)
	{
		return New(
			typeResolver       ,
			fieldLengthResolver,
			Substituter.New(),
			typeNameMapper
		);
	}
	
	public static PersistenceTypeDictionaryParser.Default New(
		final PersistenceTypeResolver        typeResolver       ,
		final PersistenceFieldLengthResolver fieldLengthResolver,
		final Substituter<String>            stringSubstitutor  ,
		final PersistenceTypeNameMapper      typeNameMapper
	)
	{
		return new PersistenceTypeDictionaryParser.Default(
			notNull(typeResolver)       ,
			notNull(fieldLengthResolver),
			notNull(stringSubstitutor)  ,
			notNull(typeNameMapper)
		);
	}

	public final class Default
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDictionaryParser
	{
		// CHECKSTYLE.OFF: FinalParameters: parameter assignment required for performance reasons

		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		// util methods //
		
		private static void checkForIncompleteInput(final int i, final int iBound)
		{
			if(i < iBound)
			{
				return;
			}
			throw UtilStackTrace.cutStacktraceByOne(
				new PersistenceExceptionParserIncompleteInput(i)
			);
		}
		

		private static int skipWhitespaces(final char[] input, int i, final int iBound)
		{
			while(i < iBound && input[i] <= ' ')
			{
				i++;
			}
			
			return i;
		}
		
		private static int skipTerminated(final char[] input, int i, final int iBound, final char terminator)
		{
			while(i < iBound && input[i] > ' ' && input[i] != terminator)
			{
				i++;
			}
			
			return i;
		}
		
		private static int skipTerminated(
			final char[] input ,
			      int    i     ,
			final int    iBound,
			final char   t1    ,
			final char   t2    ,
			final char   t3
		)
		{
			while(i < iBound && input[i] > ' ' && input[i] != t1 && input[i] != t2 && input[i] != t3)
			{
				i++;
			}
			
			return i;
		}
				
		// keyword methods //

		private static boolean equalsCharSequence(
			final char[] input ,
			final int    iStart,
			final int    iBound,
			final char[] sample
		)
		{
			final int sampleLength;
			if(iStart + (sampleLength = sample.length) > iBound)
			{
				return false;
			}
			
			for(int i = 0; i < sampleLength; i++)
			{
				if(input[iStart + i] != sample[i])
				{
					return false;
				}
			}
			
			return true;
		}

		private static boolean isNonComplexVariableLengthType(
			final char[] input ,
			final int    iStart,
			final int    iBound
		)
		{
			return equalsCharSequence(input, iStart, iBound, ARRAY_TYPE_BYTES)
				|| equalsCharSequence(input, iStart, iBound, ARRAY_TYPE_CHARS)
			;
		}

		// parser methods //

		private static void parseTypes(
			final char[]                                   input              ,
			final int                                      iStart             ,
			final int                                      iBound             ,
			final PersistenceTypeResolver                  typeResolver       ,
			final PersistenceFieldLengthResolver           fieldLengthResolver,
			final Substituter<String>                      stringSubstitutor  ,
			final PersistenceTypeNameMapper                typeNameMapper     ,
			final BulkList<PersistenceTypeDictionaryEntry> entries
		)
		{
			int i = iStart;
			while(i < iBound)
			{
				final TypeEntry typeEntry = new TypeEntry(
					typeResolver       ,
					fieldLengthResolver,
					stringSubstitutor,
					typeNameMapper
				);
				i = parseType(input, i, iBound, typeEntry);
				entries.add(typeEntry);
				
				// skip trailing whitespaces before bound is checked again
				i = skipWhitespaces(input, i, iBound);
			}
			
			// input may actually end here, no incompleteness check!
		}
			
		private static int parseType(
			final char[]    input    ,
			final int       iStart   ,
			final int       iBound   ,
			final TypeEntry typeEntry
		)
		{
			int i = iStart;
			i = skipWhitespaces (input, i, iBound);
			i = parseTypeId     (input, i, iBound, typeEntry);
			i = skipWhitespaces (input, i, iBound);
			i = parseTypeName   (input, i, iBound, typeEntry);
			i = skipWhitespaces (input, i, iBound);
			i = parseTypeMembers(input, i, iBound, typeEntry);
			
			return i;
		}

		private static int parseTypeId(
			final char[]    input    ,
			final int       iStart   ,
			final int       iBound   ,
			final TypeEntry typeEntry
		)
		{
			int i = iStart;
			
			// scroll to end of and validate typeId. Terminator only for "smarter" error messages.
			i = skipTerminated(input, i, iBound, TYPE_START);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingTypeId(iStart);
			}
			
			final long typeId = Long.parseLong(new String(input, iStart, i - iStart));
			typeEntry.setTid(typeId);
			
			return i;
		}

		private static int parseTypeName(
			final char[]    input    ,
			final int       iStart   ,
			final int       iBound   ,
			final TypeEntry typeEntry
		)
		{
			int i = iStart;
			
			// scroll to end of and validate type name (either whitespace or type body start character)
			i = skipTerminated(input, i, iBound, TYPE_START);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingType(iStart);
			}
			
			// set valid type name
			typeEntry.setTypeName(new String(input, iStart, i - iStart));
			
			return i;
		}

		private static int parseTypeMembers(
			final char[]    input    ,
			final int       iStart   ,
			final int       iBound   ,
			final TypeEntry typeEntry
		)
		{
			final TypeMemberBuilder member = typeEntry.createTypeMemberBuilder();
			
			int i = iStart;
			
			// validate and skip type start charater
			if(i >= iBound || input[i] != TYPE_START)
			{
				throw new PersistenceExceptionParserMissingTypeBody(i);
			}
			i++;
			
			// parse member entries until type end character
			while(i < iBound)
			{
				if(input[i] == TYPE_END)
				{
					break;
				}
				
				member.reset();
				i = skipWhitespaces(input, i, iBound);
				i = parseTypeMember(input, i, iBound, member);
				typeEntry.addMember(member.buildTypeMember());
				
				// skip trailing whitespaces before bound is checked again
				i = skipWhitespaces(input, i, iBound);
			}
			checkForIncompleteInput(i, iBound);
			
			// current character must be the type end character at this point, so it is skipped
			return i + 1;
		}

		private static int parseTypeMember(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			int i;
			i = parsePrimitiveDefinition(input, iStart, iBound, member);
			if(i != iStart)
			{
				return i;
			}
			
			i = parseEnumDefinition(input, iStart, iBound, member);
			if(i != iStart)
			{
				return i;
			}
			
			return parseFieldDefinition(input, iStart, iBound, member);
		}

		private static int parseFieldTypeName(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = skipTerminated(input, iStart, iBound, MEMBER_TERMINATOR, TYPE_END, MEMBER_FIELD_QUALIFIER_SEPERATOR);
			if(i == iStart)
			{
				// a type only containing Whitespaces gets to this point
				return iStart;
			}
			
			final int a = skipWhitespaces(input, i, iBound);
			checkForIncompleteInput(a, iBound);
			
			if(input[a] == MEMBER_FIELD_QUALIFIER_SEPERATOR)
			{
				throw new PersistenceExceptionParserMissingMemberType(i);
			}
			
			member.setTypeName(new String(input, iStart, i - iStart));
			
			return a;
		}

		private static int parsePrimitiveDefinition(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			if(!equalsCharSequence(input, iStart, iBound, ARRAY_KEYWORD_PRIMITIVE))
			{
				return iStart;
			}
			member.setTypeName(KEYWORD_PRIMITIVE);

			final int i = skipWhitespaces(input, iStart + ARRAY_KEYWORD_PRIMITIVE.length, iBound);
			return parsePrimitiveDefinitionDetails(input, i, iBound, member);
		}
		
		private static int parsePrimitiveDefinitionDetails(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			int i = iStart;
			while(i < iBound && input[i] != MEMBER_TERMINATOR && input[i] != TYPE_END)
			{
				// whitespaces allowed, hence the trim() below
				i++;
			}
			
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingPrimitiveDefinition(i);
			}
			checkForIncompleteInput(i, iBound);
			
			member.primitiveDefinition = new String(input, iStart, i - iStart).trim();
			
			return parseMemberTermination(input, i, iBound);
		}
		
		private static int parseEnumDefinition(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			if(!equalsCharSequence(input, iStart, iBound, ARRAY_KEYWORD_ENUM))
			{
				return iStart;
			}
			member.setTypeName(KEYWORD_ENUM);

			final int i = skipWhitespaces(input, iStart + ARRAY_KEYWORD_ENUM.length, iBound);
			return parseEnumConstantName(input, i, iBound, member);
		}
		
		private static int parseEnumConstantName(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			// parse enum persisted name
			final int i = skipTerminated(input, iStart, iBound, MEMBER_TERMINATOR, TYPE_END, MEMBER_FIELD_QUALIFIER_SEPERATOR);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingEnumName(i);
			}
			
			final int a = skipWhitespaces(input, i, iBound);
			checkForIncompleteInput(a, iBound);
			
			// the first thing is always the enum name
			setFieldName(input, iStart, i, member);

			// check for terminator
			return parseMemberTermination(input, a, iBound);
		}
				
		// (30.07.2019 TM)NOTE: literal parsing implemented but then not needed. Kept around a while.
//		private static int parseLiteral(
//			final char[]    input ,
//			final int       iStart,
//			final int       iBound,
//			final VarString string
//		)
//		{
//			// opening literal delimiter is skipped
//			int i = iStart + 1;
//
//			while(i < iBound)
//			{
//				if(input[i] == LITERAL_DELIMITER)
//				{
//					break;
//				}
//				if(input[i] == LITERAL_ESCAPER)
//				{
//					if(++i == iBound)
//					{
//						break;
//					}
//				}
//				string.add(input[i]);
//				i++;
//			}
//			checkForIncompleteInput(i, iBound);
//
//			// closing literal delimiter is skipped
//			return i + 1;
//		}

		private static int parseMemberName(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = skipTerminated(input, iStart, iBound, MEMBER_TERMINATOR, TYPE_END, MEMBER_COMPLEX_DEF_END);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingMemberName(i);
			}
			setFieldName(input, iStart, i, member);

			return parseMemberTermination(input, i, iBound);
		}

		private static int parseFieldDefinition(
			final char[]            input ,
			final int               iStart,
			final int               iBound,
			final TypeMemberBuilder member
		)
		{
			final int i = parseFieldVariableLength(input, iStart, iBound, member);
			if(i != iStart)
			{
				return i;
			}

			return parseFieldSimple(input, iStart, iBound, member);
		}

		private static int parseFieldSimple(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			// parse member type name
			final int i = parseFieldTypeName(input, iStart, iBound, member);

			// parse member name
			return parseFieldName(input, i, iBound, member);
		}

		private static int parseFieldName(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			// parse next symbol (either field qualifier or field member name)
			final int i = skipTerminated(input, iStart, iBound, MEMBER_TERMINATOR, TYPE_END, MEMBER_FIELD_QUALIFIER_SEPERATOR);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingMemberName(i);
			}
			
			int a = skipWhitespaces(input, i, iBound);
			checkForIncompleteInput(a, iBound);

			// check for qualifier#fieldname pattern, actual field name parsed subsequently.
			if(input[a] == MEMBER_FIELD_QUALIFIER_SEPERATOR)
			{
				member.setQualifier(new String(input, iStart, i - iStart));
				
				// skip separator and whitespaces after it
				a = skipWhitespaces(input, a + 1, iBound);
				return parseMemberName(input, a, iBound, member);
			}

			// otherwise must be qualifierless simple field name, member name is set right away
			setFieldName(input, iStart, i, member);

			// check for terminator
			return parseMemberTermination(input, i, iBound);
		}
		
		private static void setFieldName(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			member.setFieldName(new String(input, iStart, iBound - iStart));
		}

		private static int parseFieldVariableLength(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = parseMemberComplexType(input, iStart, iBound, member);
			if(i != iStart)
			{
				return i;
			}
			
			if(!isNonComplexVariableLengthType(input, iStart, iBound))
			{
				return iStart;
			}

			member.isVariableLength = true;

			// can be parsed as simple type, variable length marker has already been set. Rest is no different.
			return parseFieldSimple(input, iStart, iBound, member);
		}

		private static int parseMemberComplexType(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			if(!equalsCharSequence(input, iStart, iBound, ARRAY_TYPE_COMPLEX))
			{
				return iStart;
			}

			int i = skipWhitespaces(input, iStart + ARRAY_TYPE_COMPLEX.length, iBound);
			member.setTypeName(TYPE_COMPLEX);
			member.isVariableLength = true;
			member.isComplex        = true;

			i = parseComplexMemberName(input, i, iBound, member);
			
			final NestedMemberBuilder nestedMemberBuilder = member.createNestedMemberBuilder();
			while(i < iBound)
			{
				if(input[i] == MEMBER_COMPLEX_DEF_END)
				{
					break;
				}
				
				i = parseNestedMember(input, i, iBound, nestedMemberBuilder);
				member.nestedMembers.add(nestedMemberBuilder.buildGenericFieldMember());

				// (11.05.2020 TM)NOTE: fix for priv#291
				nestedMemberBuilder.reset();
				
				// skip trailing whitespaces before bound is checked again
				i = skipWhitespaces(input, i, iBound);
			}
			checkForIncompleteInput(i, iBound);

			// +1 to skip complex definition end
			return parseMemberTermination(input, i + 1, iBound);
		}

		private static int parseComplexMemberName(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = skipTerminated(input, iStart, iBound, MEMBER_TERMINATOR, TYPE_END, MEMBER_COMPLEX_DEF_START);
			if(i == iStart)
			{
				throw new PersistenceExceptionParserMissingMemberName(i);
			}
			
			final int a = skipWhitespaces(input, i, iBound);
			if(a == iBound)
			{
				throw new PersistenceExceptionParserMissingMemberName(i);
			}
						
			if(input[a] != MEMBER_COMPLEX_DEF_START)
			{
				throw new PersistenceExceptionParserMissingComplexTypeDefinition(i);
			}

			setFieldName(input, iStart, i, member);
			
			return skipWhitespaces(input, a + 1, iBound);
		}

		private static int parseNestedMember(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = parseFieldVariableLength(input, iStart, iBound, member);
			if(i != iStart)
			{
				return i;
			}
			return parseNestedSimpleType(input, iStart, iBound, member);
		}

		private static int parseNestedSimpleType(
			final char[]                input ,
			final int                   iStart,
			final int                   iBound,
			final AbstractMemberBuilder member
		)
		{
			final int i = parseFieldTypeName(input, iStart, iBound, member);
			
			return parseMemberName(input, i, iBound, member);
		}

		private static int parseMemberTermination(
			final char[] input ,
			final int    iStart,
			final int    iBound
		)
		{
			final int i = skipWhitespaces(input, iStart, iBound);
			if(i == iBound || input[i] != MEMBER_TERMINATOR)
			{
				throw new PersistenceExceptionParserMissingMemberTerminator(i);
			}
			
			// +1 to skip terminator
			return i + 1;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeResolver        typeResolver       ;
		final PersistenceFieldLengthResolver fieldLengthResolver;
		final Substituter<String>            stringSubstitutor  ;
		final PersistenceTypeNameMapper      typeNameMapper     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeResolver        typeResolver       ,
			final PersistenceFieldLengthResolver fieldLengthResolver,
			final Substituter<String>            stringSubstitutor  ,
			final PersistenceTypeNameMapper      typeNameMapper
		)
		{
			super();
			this.typeResolver        = typeResolver       ;
			this.fieldLengthResolver = fieldLengthResolver;
			this.stringSubstitutor   = stringSubstitutor  ;
			this.typeNameMapper      = typeNameMapper     ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public XGettingSequence<? extends PersistenceTypeDictionaryEntry> parseTypeDictionaryEntries(
			final String inputString
		)
			throws PersistenceExceptionParser
		{
			if(inputString == null)
			{
				return null;
			}
						
			// no unique TypeId constraint here in order to collect all entries, even if with inconsistencies.
			final BulkList<PersistenceTypeDictionaryEntry> entries = BulkList.New();
			
			try
			{
				final char[] input = XChars.readChars(inputString);
				parseTypes(
					input,
					0,
					input.length,
					this.typeResolver       ,
					this.fieldLengthResolver,
					this.stringSubstitutor,
					this.typeNameMapper,
					entries
				);
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
				throw new PersistenceExceptionParserIncompleteInput(inputString.length(), e);
			}
			catch(final PersistenceExceptionParser e)
			{
				throw e;
			}
			catch(final RuntimeException e)
			{
				throw new PersistenceExceptionParser(-1, e);
			}
			
			return entries;
		}
		
		// CHECKSTYLE.ON: FinalParameters
	}


	final class TypeEntry extends  PersistenceTypeDictionaryEntry.Abstract
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long                                             tid             ;
		private String                                           originalTypeName;
		private String                                           typeName        ;
		private final BulkList<PersistenceTypeDescriptionMember> allMembers      = new BulkList<>();
		private final BulkList<PersistenceTypeDescriptionMember> instanceMembers = new BulkList<>();

		final PersistenceTypeResolver        typeResolver       ;
		final PersistenceFieldLengthResolver fieldLengthResolver;
		final Substituter<String>            stringSubstitutor  ;
		final PersistenceTypeNameMapper      typeNameMapper     ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		TypeEntry(
			final PersistenceTypeResolver        typeResolver       ,
			final PersistenceFieldLengthResolver fieldLengthResolver,
			final Substituter<String>            stringSubstitutor  ,
			final PersistenceTypeNameMapper      typeNameMapper
		)
		{
			super();
			this.typeResolver        = typeResolver       ;
			this.fieldLengthResolver = fieldLengthResolver;
			this.stringSubstitutor   = stringSubstitutor  ;
			this.typeNameMapper      = typeNameMapper     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public synchronized String typeName()
		{
			if(this.typeName == null)
			{
				final String mappedTypeName = this.instanceMembers.isEmpty()
					? this.typeNameMapper.mapInterfaceName(this.originalTypeName)
					: this.typeNameMapper.mapClassName(this.originalTypeName)
				;
				this.typeName = mappedTypeName == null
					? this.originalTypeName
					: this.stringSubstitutor.substitute(mappedTypeName)
				;
			}
			
			return this.typeName;
		}
		
		@Override
		public long typeId()
		{
			return this.tid;
		}
		
		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers()
		{
			return this.instanceMembers;
		}
		
		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers()
		{
			return this.allMembers;
		}
		
		final void addMember(final PersistenceTypeDescriptionMember member)
		{
			this.allMembers.add(member);
			if(member.isInstanceMember())
			{
				this.instanceMembers.add(member);
			}
		}
		
		void setTypeName(final String typeName)
		{
			// typeName gets initialized on demand to ensure the members are set
			this.originalTypeName = this.stringSubstitutor.substitute(typeName);
		}
				
		void setTid(final long tid)
		{
			this.tid = tid;
		}
		
		final TypeMemberBuilder createTypeMemberBuilder()
		{
			return new TypeMemberBuilder(
				this.typeResolver       ,
				this.fieldLengthResolver,
				this.stringSubstitutor  ,
				this.typeNameMapper
			);
		}

	}

	
	
	abstract class AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeResolver        typeResolver       ;
		final PersistenceFieldLengthResolver fieldLengthResolver;
		final Substituter<String>            stringSubstitutor  ;
		final PersistenceTypeNameMapper      typeNameMapper     ;

		final BulkList<PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers = new BulkList<>();
		
		boolean isVariableLength, isComplex;
		String qualifier, originalTypeName, typeName, fieldName;

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractMemberBuilder(
			final PersistenceTypeResolver        typeResolver       ,
			final PersistenceFieldLengthResolver fieldLengthResolver,
			final Substituter<String>            stringSubstitutor  ,
			final PersistenceTypeNameMapper      typeNameMapper
		)
		{
			super();
			this.typeResolver        = typeResolver       ;
			this.fieldLengthResolver = fieldLengthResolver;
			this.stringSubstitutor   = stringSubstitutor  ;
			this.typeNameMapper      = typeNameMapper     ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		final void setQualifier(final String qualifier)
		{
			this.qualifier = this.stringSubstitutor.substitute(qualifier);
		}
		
		final void setTypeName(final String typeName)
		{
			this.originalTypeName = this.stringSubstitutor.substitute(typeName);
		}
		
		final void setFieldName(final String fieldName)
		{
			this.fieldName = this.stringSubstitutor.substitute(fieldName);
		}
		
		final String qualifier()
		{
			return this.qualifier;
		}
		
		final synchronized String typeName()
		{
			if(this.typeName == null)
			{
				/* (15.01.2020 TM)NOTE:
				 * Precedence for #mapInterfaceName, because in this context, it's more about the "top level type":
				 * The type that developers use in their entity design.
				 * Usually / primarily an interface. Only if that does not apply, then class name mapping might apply.
				 * So it should be perfectly safe to use this lookup strategy here.
				 * And if not, the type dictionary can still be fixed manually. This is just a convenience function,
				 * nothing critical.
				 */
				String mappedTypeName = this.typeNameMapper.mapInterfaceName(this.originalTypeName);
				if(mappedTypeName == null)
				{
					mappedTypeName = this.typeNameMapper.mapClassName(this.originalTypeName);
				}
				if(mappedTypeName == null)
				{
					mappedTypeName = this.originalTypeName;
				}
				
				this.typeName = this.stringSubstitutor.substitute(mappedTypeName);
			}
			
			return this.typeName;
		}
		
		final String fieldName()
		{
			return this.fieldName;
		}
		
		AbstractMemberBuilder reset()
		{
			this.qualifier        = null ;
			this.isVariableLength = false;
			this.isComplex        = false;
			this.originalTypeName = null ;
			this.typeName         = null ;
			this.fieldName        = null ;
			this.nestedMembers.clear();
			return this;
		}
		
		final NestedMemberBuilder createNestedMemberBuilder()
		{
			return new NestedMemberBuilder(
				this.typeResolver       ,
				this.fieldLengthResolver,
				this.stringSubstitutor  ,
				this.typeNameMapper
			);
		}
		
		final PersistenceTypeDescriptionMemberFieldGeneric buildGenericFieldMember()
		{
			final PersistenceFieldLengthResolver lengthResolver = this.fieldLengthResolver;
			
			final String typeName  = this.typeName();
			final String fieldName = this.fieldName();
			
			if(this.isVariableLength)
			{
				return this.isComplex
					? PersistenceTypeDescriptionMemberFieldGenericComplex.New(
						this.qualifier,
						fieldName,
						this.nestedMembers,
						lengthResolver.resolveComplexMemberMinimumLength(fieldName, typeName, this.nestedMembers),
						lengthResolver.resolveComplexMemberMaximumLength(fieldName, typeName, this.nestedMembers)
					)
					: PersistenceTypeDescriptionMemberFieldGenericVariableLength.New(
						typeName,
						this.qualifier,
						fieldName,
						lengthResolver.resolveMinimumLengthFromDictionary(null, fieldName, typeName),
						lengthResolver.resolveMaximumLengthFromDictionary(null, fieldName, typeName)
					)
				;
			}

			return PersistenceTypeDescriptionMemberFieldGenericSimple.New(
				typeName,
				this.qualifier,
				fieldName,
				!XReflect.isPrimitiveTypeName(typeName),
				lengthResolver.resolveMinimumLengthFromDictionary(null, fieldName, typeName),
				lengthResolver.resolveMaximumLengthFromDictionary(null, fieldName, typeName)
			);
		}

		abstract long resolveMemberMinimumLength();

		abstract long resolveMemberMaximumLength();

	}

	
	
	final class TypeMemberBuilder extends AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		String primitiveDefinition;
		
		// (30.07.2019 TM)NOTE: literal parsing implemented but then not needed. Kept around a while.
//		VarString literalCollector = VarString.New(64);



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public TypeMemberBuilder(
			final PersistenceTypeResolver        typeResolver       ,
			final PersistenceFieldLengthResolver fieldLengthResolver,
			final Substituter<String>            stringSubstitutor,
			final PersistenceTypeNameMapper      typeNameMapper
		)
		{
			super(typeResolver, fieldLengthResolver, stringSubstitutor, typeNameMapper);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final long resolveMinimumPrimitiveLength()
		{
			return this.fieldLengthResolver.resolveMinimumLengthFromPrimitiveType(
				PersistenceTypeDefinitionMemberPrimitiveDefinition.Default.resolvePrimitiveDefinition(
					this.primitiveDefinition
				)
			);
		}

		final long resolveMaximumPrimitiveLength()
		{
			return this.fieldLengthResolver.resolveMaximumLengthFromPrimitiveType(
				PersistenceTypeDefinitionMemberPrimitiveDefinition.Default.resolvePrimitiveDefinition(
					this.primitiveDefinition
				)
			);
		}

		final PersistenceTypeDescriptionMember buildTypeMember()
		{
			if(this.primitiveDefinition != null)
			{
				return this.buildMemberPrimitiveDefinition();
			}
			
			// identity comparison would suffice, here, but false positives in code analysis tools are annoying ...
			if(PersistenceTypeDictionary.Symbols.KEYWORD_ENUM.equals(this.typeName()))
			{
				return this.buildMemberEnumConstant();
			}

			if(!this.isVariableLength)
			{
				return this.buildMemberField();
			}

			return this.buildGenericFieldMember();
		}
		
		final PersistenceTypeDescriptionMemberPrimitiveDefinition buildMemberPrimitiveDefinition()
		{
			final long persistentLength = XMath.equal(
				this.resolveMinimumPrimitiveLength(),
				this.resolveMaximumPrimitiveLength()
			);
			
			return PersistenceTypeDescriptionMemberPrimitiveDefinition.New(
				this.primitiveDefinition,
				persistentLength
			);
		}
		
		final PersistenceTypeDescriptionMemberEnumConstant buildMemberEnumConstant()
		{
			return PersistenceTypeDescriptionMemberEnumConstant.New(
				this.fieldName()
			);
		}
		
		final PersistenceTypeDescriptionMemberField buildMemberField()
		{
			final String   qualifier               = this.qualifier();
			final Class<?> resolvableDeclaringType = qualifier == null
				? null
				: this.typeResolver.tryResolveType(qualifier)
			;
		
			/* If the qualifier is resolvable to a class, the field is reflectivly derivable.
			 * If not, it must be a generic simple field. Even if that field was reflectively derived in the past.
			 * It is not (or no longer), it cannot be handled reflectively, but only generically.
			 * And, of course, should a type formerly being a class have changed to being an interface,
			 * the field cannot be handled reflectively, either. Whatever future improvements might come to interfaces
			 * (like protected methods, since they already can have private ones, now), it should be pretty much
			 * out of the question that they could possibly ever have instance fields. That would make them identical
			 * to classes and defeat their purpose of implementing multiple inheritance.
			 */
			
			final String  tName  = this.typeName();
			final boolean isPrim = XReflect.isPrimitiveTypeName(tName);
			final String  fName  = this.fieldName();
			final long    minLen = this.resolveMemberMinimumLength();
			final long    maxLen = this.resolveMemberMaximumLength();
			
			return resolvableDeclaringType != null && !resolvableDeclaringType.isInterface()
				? PersistenceTypeDescriptionMemberFieldReflective.New(tName, qualifier, fName, !isPrim, minLen, maxLen)
				: PersistenceTypeDescriptionMemberFieldGenericSimple.New(tName, qualifier, fName, !isPrim, minLen, maxLen)
			;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		final TypeMemberBuilder reset()
		{
			super.reset();
			this.primitiveDefinition = null;
			return this;
		}

		@Override
		final long resolveMemberMinimumLength()
		{
			if(this.primitiveDefinition != null)
			{
				return this.resolveMinimumPrimitiveLength();
			}
			
			return this.fieldLengthResolver.resolveMinimumLengthFromDictionary(
				this.qualifier(),
				this.fieldName(),
				this.typeName()
			);
		}

		@Override
		final long resolveMemberMaximumLength()
		{
			if(this.primitiveDefinition != null)
			{
				return this.resolveMaximumPrimitiveLength();
			}
			
			return this.fieldLengthResolver.resolveMaximumLengthFromDictionary(
				this.qualifier(),
				this.fieldName(),
				this.typeName()
			);
		}

	}

	final class NestedMemberBuilder extends AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public NestedMemberBuilder(
			final PersistenceTypeResolver        typeResolver       ,
			final PersistenceFieldLengthResolver fieldLengthResolver,
			final Substituter<String>            stringSubstitutor  ,
			final PersistenceTypeNameMapper      typeNameMapper
		)
		{
			super(typeResolver, fieldLengthResolver, stringSubstitutor, typeNameMapper);
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		final NestedMemberBuilder reset()
		{
			super.reset();
			
			return this;
		}

		@Override
		final long resolveMemberMinimumLength()
		{
			return this.fieldLengthResolver.resolveMinimumLengthFromDictionary(
				null            ,
				this.fieldName(),
				this.typeName()
			);
		}

		@Override
		final long resolveMemberMaximumLength()
		{
			return this.fieldLengthResolver.resolveMaximumLengthFromDictionary(
				null            ,
				this.fieldName(),
				this.typeName()
			);
		}

	}

}
