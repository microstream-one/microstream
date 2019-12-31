package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.math.XMath;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberCreator;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberAppender;

public interface BinaryField extends PersistenceTypeDefinitionMemberFieldGeneric
{
	// (17.04.2019 TM)FIXME: priv#88: must have a connection to or even itself be a PersistenceTypeDefinitionMember.
	
	@Override
	public Class<?> type();
	
	@Override
	public String name();
	
	public long offset();

	@Override
	public default BinaryField copyForName(final String name)
	{
		return this.copyForName(null, name);
	}
	
	@Override
	public BinaryField copyForName(String qualifier, String name);
	
	
	
	public interface Initializable extends BinaryField
	{
		public long initializeOffset(long offset);
		
		public String initializeName(String name);
		
		public default String initializeNameOptional(final String name)
		{
			final String currentName = this.name();
			if(currentName != null)
			{
				return currentName;
			}
			
			return this.initializeName(name);
		}
	}
	
	
	public interface Defaults
	{
		public static String defaultUninitializedName()
		{
			return BinaryField.Default.NAME_UNINITIALIZED;
		}
		
		public static long defaultUninitializedOffset()
		{
			return -1;
		}
	}
	
	
	public static BinaryField New(
		final Class<?> type
	)
	{
		return New(type, Defaults.defaultUninitializedName());
	}
	
	public static BinaryField New(
		final Class<?> type,
		final String   name
	)
	{
		return new BinaryField.Default(
			AbstractBinaryHandlerCustom.CustomField(type, notNull(name)),
			Defaults.defaultUninitializedOffset()
		);
	}
	
	public static BinaryField Complex(
		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
	)
	{
		return Complex(Defaults.defaultUninitializedName(), nestedFields);
	}
	
	public static BinaryField Complex(
		final String                                         name        ,
		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
	)
	{
		return new BinaryField.Default(
			AbstractBinaryHandlerCustom.Complex(notNull(name), nestedFields),
			Defaults.defaultUninitializedOffset()
		);
	}
	
	public static BinaryField Bytes()
	{
		return Chars(Defaults.defaultUninitializedName());
	}
	
	public static BinaryField Bytes(final String name)
	{
		return new BinaryField.Default(
			AbstractBinaryHandlerCustom.bytes(name),
			Defaults.defaultUninitializedOffset()
		);
	}
	
	public static BinaryField Chars()
	{
		return Chars(Defaults.defaultUninitializedName());
	}
	
	public static BinaryField Chars(final String name)
	{
		return new BinaryField.Default(
			AbstractBinaryHandlerCustom.chars(name),
			Defaults.defaultUninitializedOffset()
		);
	}
	
	public final class Default implements BinaryField.Initializable
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		static final String NAME_UNINITIALIZED = "[Uninitialized " + BinaryField.class.getSimpleName() + "]";
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private PersistenceTypeDefinitionMemberFieldGeneric actual;
		private long offset;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super();
			this.actual = actual;
			this.offset = offset;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long initializeOffset(final long offset)
		{
			if(this.offset >= 0)
			{
				if(this.offset == offset)
				{
					return offset;
				}
				
				// (04.04.2019 TM)EXCP: proper exception
				throw new PersistenceException("Offset already assigned: " + this.offset + ". Passed: " + offset);
			}
			
			return this.offset = XMath.notNegative(offset);
		}
		
		@Override
		public final String initializeName(final String name)
		{
			final String currentName = this.actual.name();
			
			if(!currentName.equals(Defaults.defaultUninitializedName()))
			{
				if(currentName.equals(name))
				{
					return currentName;
				}
				
				// (04.04.2019 TM)EXCP: proper exception
				throw new PersistenceException(
					"Name already initialized: current name \"" + currentName + "\" != \"" + name + "\""
				);
			}
			this.actual = this.actual.copyForName(name);
			
			return this.actual.name();
		}
		
		@Override
		public final long offset()
		{
			return XMath.notNegative(this.offset);
		}
		
		@Override
		public BinaryField copyForName(final String qualifier, final String name)
		{
			return new BinaryField.Default(
				this.actual.copyForName(qualifier, name),
				this.offset
			);
		}
		
		@Override
		public final Class<?> type()
		{
			return this.actual.type();
		}

		@Override
		public String typeName()
		{
			return this.actual.typeName();
		}
		
		@Override
		public final String qualifier()
		{
			return this.actual.qualifier();
		}
		
		@Override
		public final String name()
		{
			return this.actual.name();
		}
		
		@Override
		public final String identifier()
		{
			return this.actual.identifier();
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			this.actual.assembleTypeDescription(assembler);
		}

		@Override
		public boolean isReference()
		{
			return this.actual.isReference();
		}

		@Override
		public boolean isPrimitive()
		{
			return this.actual.isPrimitive();
		}

		@Override
		public boolean isPrimitiveDefinition()
		{
			return this.actual.isPrimitiveDefinition();
		}
		
		@Override
		public boolean isEnumConstant()
		{
			return this.actual.isEnumConstant();
		}

		@Override
		public boolean hasReferences()
		{
			return this.actual.hasReferences();
		}

		@Override
		public long persistentMinimumLength()
		{
			return this.actual.persistentMinimumLength();
		}

		@Override
		public long persistentMaximumLength()
		{
			return this.actual.persistentMaximumLength();
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return this.actual.isValidPersistentLength(persistentLength);
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			this.actual.validatePersistentLength(persistentLength);
		}

		@Override
		public PersistenceTypeDefinitionMember createDefinitionMember(
			final PersistenceTypeDefinitionMemberCreator creator
		)
		{
			return this.actual.createDefinitionMember(creator);
		}
		
	}
	
}
