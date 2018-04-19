package net.jadoth.persistence.types;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import net.jadoth.collections.JadothArrays;
import net.jadoth.collections.interfaces.ChainStorage;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.util.Composition;


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

	// types that may never be encountered by persistancelayer at all (not yet complete)
	private static final Class<?>[] NOT_ID_MAPPABLE_TYPES =
	{
		// system stuff
		ClassLoader.class,
		Thread.class,

		// IO stuff
		InputStream.class,
		OutputStream.class,
		FileChannel.class,
		Socket.class,

		// unshared composition types
		ChainStorage.class,
		ChainStorage.Entry.class
	};

	// types that may never need to be analyzed generically (custom handler must be present)
	private static final Class<?>[] UNANALYZABLE_TYPES = JadothArrays.add(
		NOT_ID_MAPPABLE_TYPES,
		Composition.class,
		Collection.class
	);

	private static final String DEFAULT_FILENAME_TYPE_DICTIONARY = "PersistenceTypeDictionary.ptd";
	private static final String DEFAULT_FILENAME_TYPE_ID         = "TypeId.tid"                   ;
	private static final String DEFAULT_FILENAME_OBJECT_ID       = "ObjectId.oid"                 ;


	public static String defaultFilenameTypeDictionary()
	{
		return DEFAULT_FILENAME_TYPE_DICTIONARY;
	}

	public static String defaultFilenameTypeId()
	{
		return DEFAULT_FILENAME_TYPE_ID;
	}

	public static String defaultFilenameObjectId()
	{
		return DEFAULT_FILENAME_OBJECT_ID;
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
		return JadothReflect.isOfAnyType(type, UNANALYZABLE_TYPES);
	}

	public static boolean isNotTypeIdMappable(final Class<?> type)
	{
		return JadothReflect.isOfAnyType(type, NOT_ID_MAPPABLE_TYPES);
	}

	public static final PersistenceTypeEvaluator defaultTypeEvaluatorTypeIdMappable()
	{
		return type ->
			!isNotTypeIdMappable(type)
		;
	}

	public static final PersistenceTypeEvaluator defaultTypeEvaluatorPersistable()
	{
		return type ->
			!isNotPersistable(type)
		;
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluator()
	{
		return field ->
			!JadothReflect.isTransient(field)
		;
	}

	protected Persistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
