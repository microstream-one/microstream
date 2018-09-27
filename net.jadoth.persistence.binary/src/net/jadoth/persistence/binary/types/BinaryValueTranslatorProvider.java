package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.typing.TypeMappingLookup;


public interface BinaryValueTranslatorProvider
{
	/**
	 * Normal translator to translate a value from binary form to a target instance.
	 * 
	 * @param sourceMember
	 * @param targetMember
	 * @return
	 */
	public BinaryValueSetter provideValueTranslator(
		PersistenceTypeDescriptionMember sourceMember,
		PersistenceTypeDescriptionMember targetMember
	);
	
	/**
	 * Special translator to translate a value from binary form to an intermediate binary form.
	 * 
	 * @param sourceMember
	 * @param targetMember
	 * @return
	 */
	public BinaryValueSetter provideBinaryValueTranslator(
		PersistenceTypeDescriptionMember sourceMember,
		PersistenceTypeDescriptionMember targetMember
	);
	
	
	
	public static BinaryValueTranslatorProvider New(final TypeMappingLookup<BinaryValueSetter> translatorLookup)
	{
		return new BinaryValueTranslatorProvider.Implementation(
			notNull(translatorLookup)
		);
	}
	
	public final class Implementation implements BinaryValueTranslatorProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TypeMappingLookup<BinaryValueSetter> translatorLookup;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final TypeMappingLookup<BinaryValueSetter> translatorLookup)
		{
			super();
			this.translatorLookup = translatorLookup;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private BinaryValueSetter provideValueSkipper(final PersistenceTypeDescriptionMember sourceMember)
		{
			if(sourceMember.isReference())
			{
				// skip the long-typed OID value
				return BinaryValueTranslators::skip_long;
			}

			/* (27.09.2018 TM)TODO: implemented skipping a variable length type
			 * This even already exists in BinaryReferenceTraverser
			 */

			return resolvePrimitiveSkipper(sourceMember);
		}
		
		private static void validateIsPrimitiveType(final PersistenceTypeDescriptionMember member)
		{
			final Class<?> memberType = member.type();
			if(memberType == null || !memberType.isPrimitive())
			{
				// (27.09.2018 TM)EXCP: proper exception
				throw new RuntimeException("Unhandled type \"" + toTypedIdentifier(member) + ".");
			}
		}
		
		private static BinaryValueSetter resolvePrimitiveSkipper(
			final PersistenceTypeDescriptionMember sourceMember
		)
		{
			validateIsPrimitiveType(sourceMember);
			
			final Class<?> sourceType = sourceMember.type();
			return sourceType == byte.class
				? BinaryValueTranslators::skip_byte
				: sourceType == boolean.class
				? BinaryValueTranslators::skip_boolean
				: sourceType == short.class
				? BinaryValueTranslators::skip_short
				: sourceType == char.class
				? BinaryValueTranslators::skip_char
				: sourceType == int.class
				? BinaryValueTranslators::skip_int
				: sourceType == float.class
				? BinaryValueTranslators::skip_float
				: sourceType == long.class
				? BinaryValueTranslators::skip_long
				: sourceType == double.class
				? BinaryValueTranslators::skip_double
				: throwUnhandledPrimitiveException(sourceMember)
			;
		}
		
		private static BinaryValueSetter throwUnhandledPrimitiveException(
			final PersistenceTypeDescriptionMember sourceMember
		)
		{
			// (27.09.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Unhandled primitive type \"" + toTypedIdentifier(sourceMember) + "."
			);
		}
		
		private BinaryValueSetter provideValueTranslator(final Class<?> sourceType, final Class<?> targetType)
		{
			final BinaryValueSetter translator = this.translatorLookup.lookup(sourceType, targetType);
			if(translator != null)
			{
				return translator;
			}
			
			validateIsReferenceType(sourceType);
			validateIsReferenceType(targetType);
			
			/*
			 * In case non of the other mapping tools (explicit mapping and member matching and translator registration)
			 * Covered the current case, it is essential to check the target type compatibility, since it is
			 * too dangerous to arbitrarily copy references to instances with one type into fields with another type.
			 */
			validateCompatibleTargetType(sourceType, targetType);
			
			return this.provideReferenceResolver();
		}
		
		private static void validateCompatibleTargetType(final Class<?> sourceType, final Class<?> targetType)
		{
			if(targetType.isAssignableFrom(sourceType))
			{
				return;
			}
			
			// (27.09.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Incompatible types: " + sourceType.getName() + " -> " + targetType.getName()
			);
		}
		
		private BinaryValueSetter provideReferenceResolver(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			validateIsReferenceType(sourceMember);
			validateIsReferenceType(targetMember);
			
			return this.provideReferenceResolver();
		}
		
		private BinaryValueSetter provideReferenceResolver()
		{
			return BinaryPersistence.getSetterReference();
		}
		
		private static void validateIsReferenceType(final PersistenceTypeDescriptionMember member)
		{
			if(member.isReference())
			{
				return;
			}
			
			// (27.09.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Non-reference type \"" + toTypedIdentifier(member) + "\" cannot be handled generically."
			);
		}
		
		private static void validateIsReferenceType(final Class<?> type)
		{
			if(!type.isPrimitive())
			{
				return;
			}
			
			// (27.09.2018 TM)EXCP: proper exception
			throw new RuntimeException("Unhandled primitive type: \"" + type.getName() + ".");
		}
		
		private static String toTypedIdentifier(final PersistenceTypeDescriptionMember member)
		{
			return member.typeName() + "\" of "
				+ PersistenceTypeDescriptionMember.class.getSimpleName() + " " + member.uniqueName()
			;
		}
		
		@Override
		public BinaryValueSetter provideValueTranslator(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			if(targetMember == null)
			{
				return this.provideValueSkipper(sourceMember);
			}

			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			if(sourceType != null && targetType != null)
			{
				return this.provideValueTranslator(sourceType, targetType);
			}
			
			/* (27.09.2018 TM)TODO: Specific value translator registration options
			 * Like registration per:
			 * - TID
			 * - source type name
			 * - member name
			 * 
			 * Maybe all combined in a map with String keys and a custom key generator logic so that
			 * developers can define their own registration strategy and specificity.
			 */
			
			// generic fallback: for two reference fields, simply resolve the OID to a reference/instance.
			return provideReferenceResolver(sourceMember, targetMember);
		}
		
		@Override
		public final BinaryValueSetter provideBinaryValueTranslator(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			if(sourceMember.isReference())
			{
				return BinaryValueTranslators.provideReferenceValueBinaryTranslator(sourceMember, targetMember);
			}
			
			validateIsPrimitiveType(sourceMember);
			
			// target may be null (meaning the source member/field value shall be skipped)
			if(targetMember != null)
			{
				validateIsPrimitiveType(targetMember);
			}

			// primitives can be handled the normal way: copy/translate the bytes from source to target.
			return this.provideValueTranslator(sourceMember, targetMember);
		}
		
	}
	
}
