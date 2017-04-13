package net.jadoth.persistence.types;

import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XList;
import net.jadoth.persistence.exceptions.PersistenceExceptionParser;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserIncompleteInput;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingComplexTypeDefinition;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingMemberTerminator;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingType;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingTypeBody;
import net.jadoth.persistence.exceptions.PersistenceExceptionParserMissingTypeId;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionaryParser
{
	public PersistenceTypeDictionary parse(final String input) throws PersistenceExceptionParser;



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
			final BulkList<PersistenceTypeDescription<?>> types                 ,
			final char[]                                  input                 ,
			final PersistenceFieldLengthResolver          lengthResolver        ,
			final PersistenceTypeDescriptionBuilder      typeDescriptionBuilder
		)
		{
			final EqHashTable<String, XList<TypeDescriptionEntry>> typeEntries = EqHashTable.New();
			final Function<String, XList<TypeDescriptionEntry>>    supplier    = typeName -> BulkList.New();
			
			for(int i = 0; (i = skipWhiteSpacesEoFSafe(input, i)) < input.length;)
			{
				final TypeDescriptionEntry typeEntry = new TypeDescriptionEntry();
				i = parseType(input, i, typeEntry, lengthResolver);
				typeEntries.ensure(typeEntry.typeName, supplier).add(typeEntry);
			}
			
			for(final KeyValue<String, XList<TypeDescriptionEntry>> te : typeEntries)
			{
				// (13.04.2017 TM)TODO: OGS-3: evaluate typeBuilder instances for obsolete types.
				
				// (13.04.2017 TM)NOTE: reconstructed old logic: only consider one replace by TO-DO's logic
				final TypeDescriptionEntry typeEntry = te.value().last();
				
				// (13.04.2017 TM)TODO: Create "TypeDescriptionFamily" meta type to keep old versions
				
				final PersistenceTypeDescription<?> typeDescription = typeDescriptionBuilder.build(
					typeEntry.tid             ,
					typeEntry.typeName        ,
					null                      ,
					typeEntry.isObsolete      ,
					typeEntry.members.immure()
				);
				types.add(typeDescription);
			}
			
		}

		private static int parseType(
			final char[]                         input         ,
			final int                            i             ,
			final TypeDescriptionEntry                      typeBuilder   ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			int p = i;
			p = parseTypeId     (input, p, typeBuilder);
			p = skipWhiteSpaces (input, p);
			p = parseTypeName   (input, p, typeBuilder);
			p = skipWhiteSpaces (input, p);
			p = parseTypeMembers(input, p, typeBuilder, lengthResolver);
			return p;
		}

		private static int parseTypeId(final char[] input, final int i, final TypeDescriptionEntry typeBuilder)
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
			typeBuilder.tid = Long.parseLong(new String(input, i, p - i));
			return p;
		}

		private static int parseTypeName(final char[] input, final int i, final TypeDescriptionEntry typeBuilder)
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
			typeBuilder.typeName = new String(input, i, p - i);
			if(input[p = skipWhiteSpaces(input, p)] != TYPE_START)
			{
				throw new PersistenceExceptionParserMissingTypeBody(p);
			}
			return p + 1;
		}

		private static int parseTypeMembers(
			final char[]                         input         ,
			      int                            i             ,
			final TypeDescriptionEntry                      typeBuilder   ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			final TypeMemberBuilder member = new TypeMemberBuilder(lengthResolver);
			while(input[i = skipWhiteSpaces(input, i)] != TYPE_END)
			{
				i = parseTypeMember(input, i, member.reset());
				typeBuilder.members.add(member.buildTypeMember());
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
			member.type = new String(input, i, p - i);
			return p;
		}

		private static int parsePrimitiveDefinition(final char[] input, final int i, final TypeMemberBuilder member)
		{
			if(!isPrimitive(input, i))
			{
				return i;
			}

			final int p = skipWhiteSpaces(input, i + 9);
			member.type = KEYWORD_PRIMITIVE;

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

			member.name = new String(input, i, p - i);

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
				member.declrTypeName = new String(input, i, p - i);
				p = skipWhiteSpaces(input, skipWhiteSpaces(input, p) + 1); // skip white spaces, separator, whitespaces
				return parseMemberName(input, p, member);
			}

			// otherwise must be pseudo field name, set member name
			member.name = new String(input, i, p - i);


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

			member.type = TYPE_COMPLEX;
			member.isComplex = true;

			int p;
			p = skipWhiteSpaces       (input, i + LITERAL_LENGTH_TYPE_COMPLEX);
			p = parseComplexMemberName(input, p, member);

			final NestedMemberBuilder nestedMemberBuilder = new NestedMemberBuilder(member.lengthResolver);

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
			member.name = new String(input, i, p - i);
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

		final PersistenceFieldLengthResolver     lengthResolver        ;
		final PersistenceTypeDescriptionBuilder typeDescriptionBuilder;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceFieldLengthResolver     lengthResolver        ,
			final PersistenceTypeDescriptionBuilder typeDescriptionBuilder
		)
		{
			super();
			this.lengthResolver         = lengthResolver        ;
			this.typeDescriptionBuilder = typeDescriptionBuilder;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceTypeDictionary parse(final String input) throws PersistenceExceptionParser
		{
			final BulkList<PersistenceTypeDescription<?>> types = BulkList.New();
			
			try
			{
				parseTypes(types, input.toCharArray(), this.lengthResolver, this.typeDescriptionBuilder);
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
			
			return PersistenceTypeDictionary.New(types);
		}

		// CHECKSTYLE.ON: FinalParameters
	}


	final class TypeDescriptionEntry
	{
		      long                                       tid       ;
		      String                                     typeName  ;
		      boolean                                    isObsolete;
		final BulkList<PersistenceTypeDescriptionMember> members  = new BulkList<>();

		TypeDescriptionEntry()
		{
			super();
		}
		
		void reset()
		{
			this.tid        =     0;
			this.typeName   =  null;
			this.isObsolete = false;
			this.members.clear();
		}

		@Override
		public String toString()
		{
			final VarString vc = VarString.New();
			vc
			.add(this.tid)
			.blank()
			.add(this.typeName)
			.blank()
			.add(this.isObsolete ? " (obsolete) " : "")
			.add('{');
			if(!this.members.isEmpty())
			{
				vc.lf();
				for(int i = 0; i < Jadoth.to_int(this.members.size()); i++)
				{
					vc.tab().add(this.members.at(i)).add(';').lf();
				}
			}
			vc.add('}');
			return vc.toString();
		}
	}

	abstract class AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		boolean isVariableLength, isComplex;
		String declrTypeName, type, name;
		final BulkList<PersistenceTypeDescriptionMemberPseudoField> nestedMembers = new BulkList<>();
		final PersistenceFieldLengthResolver                        lengthResolver;


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public AbstractMemberBuilder(final PersistenceFieldLengthResolver lengthResolver)
		{
			super();
			this.lengthResolver = lengthResolver;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		AbstractMemberBuilder reset()
		{
			this.declrTypeName    = null ;
			this.isVariableLength = false;
			this.isComplex        = false;
			this.type             = null ;
			this.name             = null ;
			this.nestedMembers.clear();
			return this;
		}

		final PersistenceTypeDescriptionMemberPseudoField buildNestedMember()
		{
			if(this.isVariableLength)
			{
				return this.isComplex
					? new PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation(
						this.name,
						this.nestedMembers,
						this.lengthResolver.resolveComplexMemberMinimumLength(this.name, this.type, this.nestedMembers),
						this.lengthResolver.resolveComplexMemberMaximumLength(this.name, this.type, this.nestedMembers)
					)
					: PersistenceTypeDescriptionMemberPseudoFieldVariableLength.New(
						this.type,
						this.name,
						this.lengthResolver.resolveMinimumLengthFromDictionary(null, this.name, this.type),
						this.lengthResolver.resolveMaximumLengthFromDictionary(null, this.name, this.type)
					)
				;
			}

			return PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation.New(
				this.type,
				this.name,
				!JadothReflect.isPrimitiveTypeName(this.type),
				this.lengthResolver.resolveMinimumLengthFromDictionary(null, this.name, this.type),
				this.lengthResolver.resolveMaximumLengthFromDictionary(null, this.name, this.type)
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

		public TypeMemberBuilder(final PersistenceFieldLengthResolver lengthResolver)
		{
			super(lengthResolver);
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

			if(this.declrTypeName != null)
			{
				return new PersistenceTypeDescriptionMemberField.Implementation(
					this.type,
					this.name,
					this.declrTypeName,
					!JadothReflect.isPrimitiveTypeName(this.type),
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
			this.declrTypeName         = null;
			return this;
		}

		@Override
		final long resolveMemberMinimumLength()
		{
			if(this.primitiveDefinition != null)
			{
				return this.resolveMinimumPrimitiveLength();
			}
			return this.lengthResolver.resolveMinimumLengthFromDictionary(this.declrTypeName, this.name, this.type);
		}

		@Override
		final long resolveMemberMaximumLength()
		{
			if(this.primitiveDefinition != null)
			{
				return this.resolveMaximumPrimitiveLength();
			}
			return this.lengthResolver.resolveMaximumLengthFromDictionary(this.declrTypeName, this.name, this.type);
		}

	}

	final class NestedMemberBuilder extends AbstractMemberBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public NestedMemberBuilder(final PersistenceFieldLengthResolver lengthResolver)
		{
			super(lengthResolver);
		}

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		final NestedMemberBuilder reset()
		{
			super.reset();
			return this;
		}

		@Override
		final long resolveMemberMinimumLength()
		{
			return this.lengthResolver.resolveMinimumLengthFromDictionary(null, this.name, this.type);
		}

		@Override
		final long resolveMemberMaximumLength()
		{
			return this.lengthResolver.resolveMaximumLengthFromDictionary(null, this.name, this.type);
		}

	}


}
