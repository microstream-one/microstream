package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.collections.XUtilsCollection;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.typing.TypeMappingLookup;


public interface BinaryValueTranslatorProvider
{
	/**
	 * Normal translator to translate a value from binary form to a target instance.
	 * 
	 * @param sourceLegacyType the source legacy type
	 * @param sourceMember the source member
	 * @param targetCurrentType the target current type
	 * @param targetMember the target member
	 * @return the provided value setter
	 */
	public BinaryValueSetter provideTargetValueTranslator(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDefinitionMember   sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDefinitionMember   targetMember
	);
	
	/**
	 * Special translator to translate a value from binary form to an intermediate binary form.
	 * 
	 * @param sourceLegacyType the source legacy type
	 * @param sourceMember the source member
	 * @param targetCurrentType the target current type
	 * @param targetMember the target member
	 * @return the provided value setter
	 */
	public BinaryValueSetter provideBinaryValueTranslator(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDefinitionMember   sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDefinitionMember   targetMember
	);
	
	
	
	public static BinaryValueTranslatorProvider New(
		final XGettingMap<String, BinaryValueSetter>                      customTranslatorLookup  ,
		final XGettingSequence<? extends BinaryValueTranslatorKeyBuilder> translatorKeyBuilders   ,
		final BinaryValueTranslatorLookupProvider                         translatorLookupProvider,
		final boolean                                                     switchByteOrder
	)
	{
		return new BinaryValueTranslatorProvider.Default(
			mayNull(customTranslatorLookup),
			unwrapKeyBuilders(translatorKeyBuilders),
			notNull(translatorLookupProvider),
			switchByteOrder
		);
	}
	
	static BinaryValueTranslatorKeyBuilder[] unwrapKeyBuilders(
		final XGettingSequence<? extends BinaryValueTranslatorKeyBuilder> translatorKeyBuilders
	)
	{
		return translatorKeyBuilders == null || translatorKeyBuilders.isEmpty()
			? null
			: XUtilsCollection.toArray(translatorKeyBuilders, BinaryValueTranslatorKeyBuilder.class)
		;
	}
	
	public final class Default implements BinaryValueTranslatorProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<String, BinaryValueSetter> customTranslatorLookup  ;
		private final BinaryValueTranslatorKeyBuilder[]      translatorKeyBuilders   ;
		private final BinaryValueTranslatorLookupProvider    translatorLookupProvider;
		private final boolean                                switchByteOrder         ;
		
		private transient TypeMappingLookup<BinaryValueSetter> translatorLookup;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XGettingMap<String, BinaryValueSetter> customTranslatorLookup  ,
			final BinaryValueTranslatorKeyBuilder[]      translatorKeyBuilders   ,
			final BinaryValueTranslatorLookupProvider    translatorLookupProvider,
			final boolean                                switchByteOrder
		)
		{
			super();
			this.customTranslatorLookup   = customTranslatorLookup  ;
			this.translatorKeyBuilders    = translatorKeyBuilders   ;
			this.translatorLookupProvider = translatorLookupProvider;
			this.switchByteOrder          = switchByteOrder         ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private TypeMappingLookup<BinaryValueSetter> translatorLookup()
		{
			if(this.translatorLookup == null)
			{
				this.translatorLookup = this.translatorLookupProvider.mapping(this.switchByteOrder);
			}
			
			return this.translatorLookup;
		}
		
		private BinaryValueSetter provideValueSkipper(final PersistenceTypeDefinitionMember sourceMember)
		{
			if(sourceMember.isReference())
			{
				// skip the long-typed OID value
				return BinaryValueTranslators::skip_long;
			}

			/* (27.09.2018 TM)TODO: Legacy Type Mapping: implement skipping a variable length type.
			 * This even already exists in BinaryReferenceTraverser
			 */

			return resolvePrimitiveSkipper(sourceMember);
		}
		
		private static void validateIsPrimitiveType(final PersistenceTypeDefinitionMember member)
		{
			final Class<?> memberType = member.type();
			if(memberType == null || !memberType.isPrimitive())
			{
				throw new BinaryPersistenceException("Unhandled type \"" + toTypedIdentifier(member) + ".");
			}
		}
		
		private static BinaryValueSetter resolvePrimitiveSkipper(
			final PersistenceTypeDefinitionMember sourceMember
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
			throw new BinaryPersistenceException(
				"Unhandled primitive type \"" + toTypedIdentifier(sourceMember) + "."
			);
		}
		
		private BinaryValueSetter provideValueTranslator(
			final Class<?> sourceType,
			final Class<?> targetType
		)
		{
			final BinaryValueSetter translator = this.translatorLookup().lookup(sourceType, targetType);
			if(translator != null)
			{
				return translator;
			}
			
			validateIsReferenceType(sourceType);
			validateIsReferenceType(targetType);
			
			/*
			 * In case none of the other mapping tools (explicit mapping, member matching and translator registration)
			 * covered the current case, it is essential to check the target type compatibility, since it is
			 * too dangerous to arbitrarily copy references to instances of one type into fields of another type.
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
			
			throw new BinaryPersistenceException(
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
			return BinaryValueFunctions.getObjectValueSetter(Object.class, this.switchByteOrder);
		}
		
		private static void validateIsReferenceType(final PersistenceTypeDescriptionMember member)
		{
			if(member.isReference())
			{
				return;
			}
			
			throw new BinaryPersistenceException(
				"Non-reference type \"" + toTypedIdentifier(member) + "\" cannot be handled generically."
			);
		}
		
		private static void validateIsReferenceType(final Class<?> type)
		{
			if(!type.isPrimitive())
			{
				return;
			}

			throw new BinaryPersistenceException("Unhandled primitive type: \"" + type.getName() + ".");
		}
		
		private static String toTypedIdentifier(final PersistenceTypeDescriptionMember member)
		{
			return member.typeName() + "\" of "
				+ PersistenceTypeDescriptionMember.class.getSimpleName() + " " + member.identifier()
			;
		}
		
		private BinaryValueSetter lookupCustomValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDescriptionMember  sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDescriptionMember  targetMember
		)
		{
			if(this.translatorKeyBuilders == null || this.customTranslatorLookup == null)
			{
				return null;
			}
			
			final XGettingMap<String, BinaryValueSetter> customTranslatorLookup = this.customTranslatorLookup;
			final BinaryValueTranslatorKeyBuilder[]      translatorKeyBuilders  = this.translatorKeyBuilders ;
			
			for(final BinaryValueTranslatorKeyBuilder keyBuilder : translatorKeyBuilders)
			{
				final String key = keyBuilder.buildTranslatorLookupKey(
					sourceLegacyType ,
					sourceMember     ,
					targetCurrentType,
					targetMember
				);
				
				final BinaryValueSetter customValueSetter = customTranslatorLookup.get(key);
				if(customValueSetter != null)
				{
					return customValueSetter;
				}
			}
			
			return null;
		}
		
		@Override
		public BinaryValueSetter provideTargetValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDefinitionMember   sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDefinitionMember   targetMember
		)
		{
			if(targetMember == null)
			{
				return this.provideValueSkipper(sourceMember);
			}
			
			// check for potential custom value translator
			final BinaryValueSetter customValueSetter = this.lookupCustomValueTranslator(
				sourceLegacyType ,
				sourceMember     ,
				targetCurrentType,
				targetMember
			);
			if(customValueSetter != null)
			{
				return customValueSetter;
			}
			
			// note: see #validateCompatibleTargetType for target field type compatability validation.
			
			// check for generically handleable types on both sides
			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			if(sourceType != null && targetType != null)
			{
				return this.provideValueTranslator(sourceType, targetType);
			}
						
			// generic fallback: for two reference fields, simply resolve the OID to a reference/instance.
			return this.provideReferenceResolver(sourceMember, targetMember);
		}
		
		@Override
		public final BinaryValueSetter provideBinaryValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDefinitionMember   sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDefinitionMember   targetMember
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
			return this.provideTargetValueTranslator(sourceLegacyType, sourceMember, targetCurrentType, targetMember);
		}
		
	}
	
}
