package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionParser;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserIncompleteInput;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingComplexTypeDefinition;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingMemberTerminator;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingType;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingTypeBody;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingTypeId;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.Substituter;

public interface PersistenceTypeDictionaryParser
{
	public XGettingSequence<? extends PersistenceTypeDictionaryEntry> parseTypeDictionary(String input)
		throws PersistenceExceptionParser
	;


	
	public static PersistenceTypeDictionaryParser.Implementation New(
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		return new PersistenceTypeDictionaryParser.Implementation(
			notNull(lengthResolver)
		);
	}

	public final class Implementation
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDictionaryParser
	{
		// CHECKSTYLE.OFF: FinalParameters: parameter assignment required for performance reasons

		///////////////////////////////////////////////////////////////////////////
		// static methods    //
		/////////////////////

		// util methods //

		// (16.04.2013 TM)XXX: improve all parsing methods with iStart, iEnd pattern like in CsvParser implementation

		private static int skipWhiteSpacesEoFSafe(final char[] input, int i)
		{
			while(i < input.length && input[i] <= ' ')
			{
				i++;
			}
			return i;
		}

		private static int skipWhiteSpaces(final char[] input, int i)
		{
			while(input[i] <= ' ')
			{
				i++;
			}
			return i;
		}

		// keyword methods //

		private static boolean equalsCharSequence(final char[] input, final int i, final char[] sample)
		{
			for(int s = 0; s < sample.length; s++)
			{
				if(input[i + s] != sample[s])
				{
					return false;
				}
			}
			return true;
		}

		private static boolean isPrimitive(final char[] input, final int i)
		{
			return equalsCharSequence(input, i, ARRAY_KEYWORD_PRIMITIVE);
		}

		private static boolean isVariableLengthType(final char[] input, final int i)
		{
			return equalsCharSequence(input, i, ARRAY_TYPE_BYTES  )
				|| equalsCharSequence(input, i, ARRAY_TYPE_CHARS  )
				|| equalsCharSequence(input, i, ARRAY_TYPE_COMPLEX)
			;
		}

		private static boolean isComplexType(final char[] input, final int i)
		{
			return equalsCharSequence(input, i, ARRAY_TYPE_COMPLEX);
		}

		// parser methods //

		private static void parseTypes(
			final char[]                                   input         ,
			final PersistenceFieldLengthResolver           lengthResolver,
			final BulkList<PersistenceTypeDictionaryEntry> entries
		)
		{
			final Substituter<String> stringSubstitutor = Substituter.New();
			
			for(int i = 0; (i = skipWhiteSpacesEoFSafe(input, i)) < input.length;)
			{
				final Entry typeEntry = new Entry(stringSubstitutor);
				i = parseType(input, i, typeEntry, lengthResolver);
				entries.add(typeEntry);
			}
		}
			
		private static int parseType(
			final char[]                         input         ,
			final int                            i             ,
			final Entry                          typeEntry     ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			int p = i;
			p = parseTypeId     (input, p, typeEntry);
			p = skipWhiteSpaces (input, p);
			p = parseTypeName   (input, p, typeEntry);
			p = skipWhiteSpaces (input, p);
			p = parseTypeMembers(input, p, typeEntry, lengthResolver);
			return p;
		}

		private static int parseTypeId(final char[] input, final int i, final Entry typeBuilder)
		{
			int p = i;
			while(input[p] >= '0' && input[p] <= '9')
			{
				p++;
			}
			if(p == i)
			{
				throw new PersistenceExceptionParserMissingTypeId(i);
			}
			typeBuilder.setTid(Long.parseLong(new String(input, i, p - i)));
			return p;
		}

		private static int parseTypeName(final char[] input, final int i, final Entry typeBuilder)
		{
			int p = i;
			while(input[p] > ' ' && input[p] != TYPE_START)
			{
				p++;
			}
			if(p == i)
			{
				throw new PersistenceExceptionParserMissingType(i);
			}
			typeBuilder.setTypeName(new String(input, i, p - i));
			if(input[p = skipWhiteSpaces(input, p)] != TYPE_START)
			{
				throw new PersistenceExceptionParserMissingTypeBody(p);
			}
			return p + 1;
		}

		private static int parseTypeMembers(
			final char[]                         input         ,
			      int                            i             ,
			final Entry           typeEntry     ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			final TypeMemberBuilder member = new TypeMemberBuilder(lengthResolver, typeEntry.stringSubstitutor);
			while(input[i = skipWhiteSpaces(input, i)] != TYPE_END)
			{
				i = parseTypeMember(input, i, member.reset());
				typeEntry.members.add(member.buildTypeMember());
			}
			return i + 1;
		}

		private static int parseTypeMember(final char[] input, final int i, final TypeMemberBuilder member)
		{
			int p;
			p = parsePrimitiveDefinition(input, i, member);
			if(p != i)
			{
				return p;
			}
			return parseInstanceMember(input, i, member);
		}

		private static int parseMemberTypeName(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			int p = i;
			while(input[p] > ' '
			   && input[p] != MEMBER_TERMINATOR
			   && input[p] != MEMBER_FIELD_DECL_TYPE_SEPERATOR
			   && input[p] != TYPE_END
			)
			{
				p++;
			}
			member.setTypeName(new String(input, i, p - i));
			return p;
		}

		private static int parsePrimitiveDefinition(final char[] input, final int i, final TypeMemberBuilder member)
		{
			if(!isPrimitive(input, i))
			{
				return i;
			}

			final int p = skipWhiteSpaces(input, i + 9);
			member.setTypeName(KEYWORD_PRIMITIVE);

			int p2 = p;
			while(input[p2] != MEMBER_TERMINATOR && input[p2] != TYPE_END)
			{
				p2++;
			}
			member.primitiveDefinition = new String(input, p, p2 - p).trim();
			return parseMemberTermination(input, p2);
		}

		private static int parseMemberName(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			int p = i;
			while(input[p] > ' '
			   && input[p] != MEMBER_TERMINATOR
			   && input[p] != TYPE_END
			   && input[p] != MEMBER_COMPLEX_DEF_END
			)
			{
				p++;
			}

			member.setFieldName(new String(input, i, p - i));

			return parseMemberTermination(input, p);
		}

		private static int parseInstanceMember(final char[] input, final int i, final TypeMemberBuilder member)
		{
			final int p = parseMemberVariableLength(input, i, member);
			if(p != i)
			{
				return p;
			}

			return parseInstanceMemberSimpleType(input, i, member);
		}

		private static int parseInstanceMemberSimpleType(
			final char[]                input ,
			final int                   i     ,
			final AbstractMemberBuilder member
		)
		{
			// parse member type name
			int p = i;
			p = parseMemberTypeName(input, p, member);
			p = skipWhiteSpaces    (input, p);

			// parse member name
			return parseInstanceMemberName(input, p, member);
		}

		private static int parseInstanceMemberName(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			// parse next symbol (either declaring type name or pseudo field member name)
			int p = i;
			while(input[p] > ' '
			   && input[p] != MEMBER_TERMINATOR
			   && input[p] != MEMBER_FIELD_DECL_TYPE_SEPERATOR
			   && input[p] != TYPE_END
			)
			{
				p++;
			}

			// check for declaring type name, parse actual field name externally
			if(input[skipWhiteSpaces(input, p)] == MEMBER_FIELD_DECL_TYPE_SEPERATOR)
			{
				member.setDeclaringTypeName(new String(input, i, p - i));
				p = skipWhiteSpaces(input, skipWhiteSpaces(input, p) + 1); // skip white spaces, separator, whitespaces
				return parseMemberName(input, p, member);
			}

			// otherwise must be pseudo field name, set member name
			member.setFieldName(new String(input, i, p - i));


			// check for terminator
			return parseMemberTermination(input, p);
		}

		private static int parseMemberVariableLength(
			final char[]                input ,
			final int                   i     ,
			final AbstractMemberBuilder member
		)
		{
			if(!isVariableLengthType(input, i))
			{
				return i;
			}

			member.isVariableLength = true;

			final int p = parseMemberComplexType(input, i, member);
			if(p != i)
			{
				return p;
			}

			// can be parsed as simple type, variable length marker has already been set. Rest is no different.
			return parseInstanceMemberSimpleType(input, i, member);
		}

		private static int parseMemberComplexType(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			if(!isComplexType(input, i))
			{
				return i;
			}

			member.setTypeName(TYPE_COMPLEX);
			member.isComplex = true;

			int p;
			p = skipWhiteSpaces       (input, i + LITERAL_LENGTH_TYPE_COMPLEX);
			p = parseComplexMemberName(input, p, member);

			final NestedMemberBuilder nestedMemberBuilder = new NestedMemberBuilder(
				member.lengthResolver   ,
				member.stringSubstitutor
			);

			while(input[p = skipWhiteSpaces(input, p)] != MEMBER_COMPLEX_DEF_END)
			{
				p = parseNestedMember(input, p, nestedMemberBuilder);
				member.nestedMembers.add(nestedMemberBuilder.buildNestedMember());
			}

			return parseMemberTermination(input, p + 1); // +1 to skip complex definition end
		}

		private static int parseComplexMemberName(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			int p = i;
			while(input[p] > ' '
			   && input[p] != MEMBER_TERMINATOR
			   && input[p] != MEMBER_COMPLEX_DEF_START
			   && input[p] != TYPE_END
			)
			{
				p++;
			}
			member.setFieldName(new String(input, i, p - i));
			if(input[p = skipWhiteSpaces(input, p)] != MEMBER_COMPLEX_DEF_START)
			{
				throw new PersistenceExceptionParserMissingComplexTypeDefinition(p);
			}
			return skipWhiteSpaces(input, p + 1);
		}

		private static int parseNestedMember(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			final int p = parseMemberVariableLength(input, i, member);
			if(p != i)
			{
				return p;
			}
			return parseNestedSimpleType(input, i, member);
		}

		private static int parseNestedSimpleType(final char[] input, final int i, final AbstractMemberBuilder member)
		{
			int p;
			p = parseMemberTypeName(input, i, member);
			p = skipWhiteSpaces(input, p);
			return parseMemberName(input, p, member);
		}

		private static int parseMemberTermination(final char[] input, final int i)
		{
			int p = i;
			if(input[p = skipWhiteSpaces(input, p)] != MEMBER_TERMINATOR)
			{
				throw new PersistenceExceptionParserMissingMemberTerminator(p);
			}
			return p + 1; // +1 to skip complex definition end
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final PersistenceFieldLengthResolver lengthResolver;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final PersistenceFieldLengthResolver lengthResolver)
		{
			super();
			this.lengthResolver = lengthResolver;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public XGettingSequence<? extends PersistenceTypeDictionaryEntry> parseTypeDictionary(final String input) throws PersistenceExceptionParser
		{
			if(input == null)
			{
				return null;
			}
			
			// no unique TypeId constraint here in order to collect all entries, even if with inconsistencies.
			final BulkList<PersistenceTypeDictionaryEntry> entries = BulkList.New();
			
			try
			{
				parseTypes(input.toCharArray(), this.lengthResolver, entries);
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
				throw new PersistenceExceptionParserIncompleteInput(input.length(), e);
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


	final class Entry extends  PersistenceTypeDictionaryEntry.AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long                                     tid     ;
		private String                                   typeName;
		final BulkList<PersistenceTypeDescriptionMember> members  = new BulkList<>();
		
		final Substituter<String>                        stringSubstitutor;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(final Substituter<String> stringSubstitutor)
		{
			super();
			this.stringSubstitutor = stringSubstitutor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public long typeId()
		{
			return this.tid;
		}
		
		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.members;
		}
		
		
		void setTypeName(final String typeName)
		{
			this.typeName = this.stringSubstitutor.substitute(typeName);
		}
				
		void setTid(final long tid)
		{
			this.tid = tid;
		}

	}

	
	
	abstract class AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		boolean isVariableLength, isComplex;
		private String declrTypeName, typeName, fieldName;
		final BulkList<PersistenceTypeDescriptionMemberPseudoField> nestedMembers     = new BulkList<>();
		final PersistenceFieldLengthResolver                        lengthResolver   ;
		final Substituter<String>                                   stringSubstitutor;


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public AbstractMemberBuilder(
			final PersistenceFieldLengthResolver lengthResolver   ,
			final Substituter<String>            stringSubstitutor
		)
		{
			super();
			this.lengthResolver    = lengthResolver   ;
			this.stringSubstitutor = stringSubstitutor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		final void setDeclaringTypeName(final String declrTypeName)
		{
			this.declrTypeName = this.stringSubstitutor.substitute(declrTypeName);
		}
		
		final void setTypeName(final String typeName)
		{
			this.typeName = this.stringSubstitutor.substitute(typeName);
		}
		
		final void setFieldName(final String fieldName)
		{
			this.fieldName = this.stringSubstitutor.substitute(fieldName);
		}
		
		final String declaringTypeName()
		{
			return this.declrTypeName;
		}
		
		final String typeName()
		{
			return this.typeName;
		}
		
		final String fieldName()
		{
			return this.fieldName;
		}
		
		AbstractMemberBuilder reset()
		{
			this.declrTypeName    = null ;
			this.isVariableLength = false;
			this.isComplex        = false;
			this.typeName         = null ;
			this.fieldName        = null ;
			this.nestedMembers.clear();
			return this;
		}

		final PersistenceTypeDescriptionMemberPseudoField buildNestedMember()
		{
			if(this.isVariableLength)
			{
				return this.isComplex
					? new PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation(
						this.fieldName,
						this.nestedMembers,
						this.lengthResolver.resolveComplexMemberMinimumLength(this.fieldName, this.typeName, this.nestedMembers),
						this.lengthResolver.resolveComplexMemberMaximumLength(this.fieldName, this.typeName, this.nestedMembers)
					)
					: PersistenceTypeDescriptionMemberPseudoFieldVariableLength.New(
						this.typeName,
						this.fieldName,
						this.lengthResolver.resolveMinimumLengthFromDictionary(null, this.fieldName, this.typeName),
						this.lengthResolver.resolveMaximumLengthFromDictionary(null, this.fieldName, this.typeName)
					)
				;
			}

			return PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation.New(
				this.typeName,
				this.fieldName,
				!JadothReflect.isPrimitiveTypeName(this.typeName),
				this.lengthResolver.resolveMinimumLengthFromDictionary(null, this.fieldName, this.typeName),
				this.lengthResolver.resolveMaximumLengthFromDictionary(null, this.fieldName, this.typeName)
			);
		}

		abstract long resolveMemberMinimumLength();

		abstract long resolveMemberMaximumLength();

	}

	
	
	final class TypeMemberBuilder extends AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		String primitiveDefinition;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public TypeMemberBuilder(
			final PersistenceFieldLengthResolver lengthResolver   ,
			final Substituter<String>            stringSubstitutor
		)
		{
			super(lengthResolver, stringSubstitutor);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final long resolveMinimumPrimitiveLength()
		{
			return this.lengthResolver.resolveMinimumLengthFromPrimitiveType(
				PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation.resolvePrimitiveDefinition(
					this.primitiveDefinition
				)
			);
		}

		final long resolveMaximumPrimitiveLength()
		{
			return this.lengthResolver.resolveMaximumLengthFromPrimitiveType(
				PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation.resolvePrimitiveDefinition(
					this.primitiveDefinition
				)
			);
		}

		final PersistenceTypeDescriptionMember buildTypeMember()
		{
			if(this.primitiveDefinition != null)
			{
				return new PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation(
					this.primitiveDefinition,
					this.resolveMinimumPrimitiveLength(),
					this.resolveMaximumPrimitiveLength()
				);
			}

			if(this.declaringTypeName() != null)
			{
				return new PersistenceTypeDescriptionMemberField.Implementation(
					this.typeName(),
					this.fieldName(),
					this.declaringTypeName(),
					!JadothReflect.isPrimitiveTypeName(this.typeName()),
					this.resolveMemberMinimumLength(),
					this.resolveMemberMaximumLength()
				);
			}

			return this.buildNestedMember();
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		final TypeMemberBuilder reset()
		{
			super.reset();
			this.primitiveDefinition   = null;
			return this;
		}

		@Override
		final long resolveMemberMinimumLength()
		{
			if(this.primitiveDefinition != null)
			{
				return this.resolveMinimumPrimitiveLength();
			}
			
			return this.lengthResolver.resolveMinimumLengthFromDictionary(
				this.declaringTypeName(),
				this.fieldName()        ,
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
			
			return this.lengthResolver.resolveMaximumLengthFromDictionary(
				this.declaringTypeName(),
				this.fieldName()        ,
				this.typeName()
			);
		}

	}

	final class NestedMemberBuilder extends AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public NestedMemberBuilder(
			final PersistenceFieldLengthResolver lengthResolver   ,
			final Substituter<String>            stringSubstitutor
		)
		{
			super(lengthResolver, stringSubstitutor);
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
			return this.lengthResolver.resolveMinimumLengthFromDictionary(
				null            ,
				this.fieldName(),
				this.typeName()
			);
		}

		@Override
		final long resolveMemberMaximumLength()
		{
			return this.lengthResolver.resolveMaximumLengthFromDictionary(
				null            ,
				this.fieldName(),
				this.typeName()
			);
		}

	}

}
