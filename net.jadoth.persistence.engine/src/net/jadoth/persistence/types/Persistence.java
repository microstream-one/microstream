package net.jadoth.persistence.types;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Supplier;

import net.jadoth.chars.StringTable;
import net.jadoth.collections.XArrays;
import net.jadoth.collections.interfaces.ChainStorage;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.typing.Composition;


public class Persistence extends Swizzle
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/**
	 * Reasons for choosing UTF8 as the standard charset:
	 * 1.) It is independent from endianess.
	 * 2.) It is massively smaller due to most content containing almost only single-byte ASCII characters
	 * 3.) It is overall more commonly and widespread used and compatible than any specific format.
	 */
	public static final Charset standardCharset()
	{
		return StandardCharsets.UTF_8;
	}
	
	public static String defaultFilenameTypeDictionary()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "PersistenceTypeDictionary.ptd";
	}

	public static String defaultFilenameTypeId()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "TypeId.tid";
	}

	public static String defaultFilenameObjectId()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "ObjectId.oid";
	}
	
	/**
	 * types that may never be encountered by the persistance layer at all (not yet complete)
	 * 
	 * @return
	 */
	public static Class<?>[] notIdMappableTypes()
	{
		// (20.04.2018 TM)TODO: add NOT_ID_MAPPABLE_TYPES list
		// why permanently occupy additional memory with fields and instances for constant values?
		return new Class<?>[]
		{
			// types that are explicitely marked as unpersistable. E.g. the persistence logic itself!
			Unpersistable.class,
			
			// system stuff (cannot be restored intrinsically due to ties to JVM internals)
			ClassLoader.class,
			Thread.class,

			// IO stuff (cannot be restored intrinsically due to ties to external resources like files, etc.)
			InputStream.class,
			OutputStream.class,
			FileChannel.class,
			Socket.class,

			// unshared composition types (those are internal helper class instances, not entities)
			ChainStorage.class,
			ChainStorage.Entry.class
		};
	}

	/**
	 * Types that may never need to be analyzed generically (custom handler must be present)
	 * 
	 * @return
	 */
	public static Class<?>[] unanalyzableTypes()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return XArrays.add(
			notIdMappableTypes(),
			Composition.class,
			Collection.class
		);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static boolean isPersistable(final Class<?> type)
	{
		return !isNotPersistable(type);
	}

	public static boolean isTypeIdMappable(final Class<?> type)
	{
		return !isNotTypeIdMappable(type);
	}

	public static boolean isNotPersistable(final Class<?> type)
	{
		return XReflect.isOfAnyType(type, unanalyzableTypes());
	}

	public static boolean isNotTypeIdMappable(final Class<?> type)
	{
		return XReflect.isOfAnyType(type, notIdMappableTypes());
	}

	public static final PersistenceTypeEvaluator defaultTypeEvaluatorTypeIdMappable()
	{
		return type ->
			!isNotTypeIdMappable(type)
		;
	}
	
	public static final <M> PersistenceTypeMismatchValidator<M> typeMismatchValidatorFailing()
	{
		return PersistenceTypeMismatchValidator.Failing();
	}
	
	public static final <M> PersistenceTypeMismatchValidator<M> typeMismatchValidatorNoOp()
	{
		return PersistenceTypeMismatchValidator.NoOp();
	}

	public static final PersistenceTypeEvaluator defaultTypeEvaluatorPersistable()
	{
		return type ->
			!isNotPersistable(type)
		;
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluator()
	{
		return (entityType, field) ->
			!XReflect.isTransient(field)
		;
	}
	
	public static final PersistenceEagerStoringFieldEvaluator defaultReferenceFieldMandatoryEvaluator()
	{
		// by default, no field is mandatory
		return (entityType, field) ->
			false
		;
	}

	@SuppressWarnings("unchecked") // type safety guaranteed by the passed typename. The typename String "is" the T.
	public static <T> Class<T> resolveType(final String typeName)
	{
		try
		{
			return (Class<T>)XReflect.classForName(typeName);
		}
		catch(final ClassNotFoundException e)
		{
			throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName, e);
		}
	}
	
	@SuppressWarnings("unchecked") // type safety guaranteed by the passed typename. The typename String "is" the T.
	public static <T> Class<T> resolveTypeOptional(final String typeName)
	{
		try
		{
			return (Class<T>)XReflect.classForName(typeName);
		}
		catch (final ClassNotFoundException e)
		{
			/*
			 * class not found for an optional type dictionary entry is not necessarily an error.
			 * It might just have been renamed or deleted in the runtime code.
			 * This has to be validated at a later point.
			 */
			return null;
		}
	}
	

	public static final PersistenceRootResolver RootResolver(final String rootIdentifier, final Object rootInstance)
	{
		return RootResolver(rootIdentifier, () -> rootInstance);
	}

	public static final PersistenceRootResolver RootResolver(final Object rootInstance)
	{
		return RootResolver(() -> rootInstance);
	}

	public static final PersistenceRootResolver RootResolver(final Supplier<?> rootInstanceSupplier)
	{
		return RootResolver("root", rootInstanceSupplier);
	}
	
	public static final PersistenceRootResolver RootResolver(
		final String      rootIdentifier      ,
		final Supplier<?> rootInstanceSupplier
	)
	{
		return PersistenceRootResolver.New(rootIdentifier, rootInstanceSupplier);
	}
	
	public static final PersistenceRootResolver RootResolver(
		final String                                rootIdentifier      ,
		final Supplier<?>                           rootInstanceSupplier,
		final PersistenceRefactoringResolverProvider refactoringMapping
	)
	{
		return PersistenceRootResolver.Wrap(
			RootResolver(rootIdentifier, rootInstanceSupplier),
			refactoringMapping
		);
	}
	
	public static final PersistenceRootResolver RootResolver(
		final Supplier<?>                           rootInstanceSupplier,
		final PersistenceRefactoringResolverProvider refactoringMapping
	)
	{
		return PersistenceRootResolver.Wrap(
			RootResolver(rootInstanceSupplier),
			refactoringMapping
		);
	}
	
	public static final PersistenceRootResolver.Builder RootResolverBuilder()
	{
		return PersistenceRootResolver.Builder();
	}
	
	public static final PersistenceRefactoringMappingProvider RefactoringMapping(final File refactoringsFile)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringsFile)
		);
	}
	
	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final XGettingTable<String, String> refactoringMappings
	)
	{
		return PersistenceRefactoringMappingProvider.New(refactoringMappings);
	}
	
	public static XGettingTable<String, String> readRefactoringMappings(final File file)
	{
		// (19.04.2018 TM)EXCP: proper exception
		final String fileContent = XFiles.readStringFromFile(
			file,
			Persistence.standardCharset(),
			RuntimeException::new
		);
		final StringTable stringTable                     = StringTable.Static.parse(fileContent);
		final XGettingTable<String, String> keyValueTable = stringTable.toKeyValueTable(
			row ->
				row[0], // debuggability linebreak, do not reformat!
			row ->
				row[1] // debuggability linebreak, do not reformat!
		);
		
		return keyValueTable;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
		
	protected Persistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
