package one.microstream.test.corp.main;

import one.microstream.X;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStoreEnum
{
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation()
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("Refactorings.csv"))
//		)
		.start()
	;

	public static void main(final String[] args)
	{
//		if(System.currentTimeMillis() > 0)
//		{
//			printEnums();
//			System.exit(0);
//		}
		
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(
				createGraph()
				);
				
				Test.print("Storing ...");
				STORAGE.storeRoot();
				Test.print("Storing completed.");
			}
			else
			{
				// subsequent executions enter here (database reading)

				Test.print("Model data loaded.");
				Test.print("Root instance: " + STORAGE.root());
				
				Test.print("Exporting data ...");
				TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
				Test.print("Data export completed.");
			}
			
			// no shutdown required, the storage concept is inherently crash-safe
			System.exit(0);
		}
		
		
		static Object[] createGraph()
		{
			return X.array(
				SimpleEnum.TypeA,
				SimpleEnum.TypeB,
				SimpleEnum.TypeC,
				PersistableCrazyEnum.ShouldWorkNormal,
				PersistableCrazyEnum.ShouldWorkSpecial,
//				UnpersistableCrazyEnum.MustCrash,
				StatefulEnum.Type1,
				StatefulEnum.Type2,
				StatefulEnum.Type3
			);
		}
		
		static void printEnums()
		{
			StatefulEnum.Type1.state();
			System.out.println(StatefulEnum.Type1);
			System.out.println(StatefulEnum.Type1.getClass());
			System.out.println(StatefulEnum.Type1.getDeclaringClass());
			
			UnpersistableCrazyEnum.MustCrash.crazyState();
			System.out.println(UnpersistableCrazyEnum.MustCrash);
			System.out.println(UnpersistableCrazyEnum.MustCrash.getClass());
			
			final Class<?> c = UnpersistableCrazyEnum.MustCrash.getDeclaringClass();
			System.out.println(c);
			System.out.println(XReflect.isEnum(UnpersistableCrazyEnum.MustCrash.getClass()));
		}
		
	}


	enum SimpleEnum
	{
		TypeA,
		TypeB,
		TypeC;
	}

	enum StatefulEnum
	{
		Type1(100),
		Type2(200),
		Type3(300);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private int state;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private StatefulEnum(final int state)
		{
			this.state = state;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public int state()
		{
			return this.state;
		}
		
		@Override
		public String toString()
		{
			return this.name() + "-" + this.state;
		}
		
	}
	
	enum PersistableCrazyEnum
	{
		ShouldWorkNormal(0),
		ShouldWorkSpecial(1)
		{
			transient short mustBeDiscarded;
			
			@Override
			public long handleableState()
			{
				this.mustBeDiscarded++;
				return super.handleableState();
			}
			
			@Override
			public String toString()
			{
				return super.toString() + " (state queried " + this.mustBeDiscarded + " times)";
			}
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long handleableState;
			
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private PersistableCrazyEnum(final long handleableState)
		{
			this.handleableState = handleableState;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public long handleableState()
		{
			return this.handleableState;
		}
		
		@Override
		public String toString()
		{
			return this.name() + "-" + this.handleableState;
		}
		
	}

	enum UnpersistableCrazyEnum
	{
		MustCrash(1111)
		{
			long crazySpecialHelperState;
			
			@Override
			public long crazyState()
			{
				this.crazySpecialHelperState++;
				return super.crazyState();
			}
			
			@Override
			public String toString()
			{
				return super.toString() + " (state queried " + this.crazySpecialHelperState + " times)";
			}
		},
		WouldWork2(2222),
		WouldWork3(3333);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long crazyState;
			
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private UnpersistableCrazyEnum(final long crazyState)
		{
			this.crazyState = crazyState;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public long crazyState()
		{
			return this.crazyState;
		}
		
		@Override
		public String toString()
		{
			return this.name() + "-" + this.crazyState;
		}
		
	}
