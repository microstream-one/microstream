package one.microstream.test.collections;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import one.microstream.chars.XChars;
import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;

public class MainTestStorageJDKConstants
{
	static final Reference<Object[]>    ROOT    = Reference.New(null)        ;
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		final Object[] createdConstants = createConstants();
		
		if(ROOT.get() == null)
		{
			System.out.println("Storing collections ...");
			ROOT.set(createdConstants);
			STORAGE.storeRoot();
			System.out.println("Stored collections.");
		}
		else
		{
			System.out.println("Loaded collections.");
			final Object[] loadedRoot = ROOT.get();
			for(int i = 0; i < createdConstants.length; i++)
			{
				System.out.println(
					XChars.systemString(createdConstants[i])
					+ "\t<-"
					+ (createdConstants[i] == loadedRoot[i])
					+ "-> "
					+ XChars.systemString(loadedRoot[i])
				);
			}
			Test.print("Exporting collection data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testConstants"));
		}
		
		System.exit(0);
	}
	
	
	private static Object[] createConstants()
	{
		return new Object[]{
			Collections.emptyList()        ,
			Collections.emptySet()         ,
			Collections.emptyMap()         ,
			Collections.emptyNavigableSet(),
			Collections.emptyNavigableMap(),
			Collections.reverseOrder()     ,
			Comparator.naturalOrder()      ,
			BigDecimal.ZERO                ,
			BigDecimal.ONE                 ,
			BigDecimal.TEN                 ,
			BigInteger.ZERO                ,
			BigInteger.ONE                 ,
			BigInteger.TEN                 ,
			Optional.empty()               ,
			OptionalInt.empty()            ,
			OptionalLong.empty()           ,
			OptionalDouble.empty()
		};
	}
		
}

