package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStoreEnumLoading
{

	public static void main(final String[] args)
	{
		final Data root = new Data();
		final EmbeddedStorageManager storage = EmbeddedStorage.start(root);
		
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(root.gebaeude == null)
		{
			root.gebaeude = Gebaeude.HAUS;
			
			Test.print("Storing ...");
			storage.store(root);
			Test.print("Storing completed.");
			TestImportExport.testExport(storage, Test.provideTimestampedDirectory("testExport"));
		}
		else
		{
			// Ausgabe HAUS - Hier ist noch alles okay.
			System.out.println("Gebäude: " + root.gebaeude);

			// Ausgabe HAUS - Hier wird es seltsam.
			System.out.println("Farbe:   " + root.gebaeude.farbe);

			// Ausgabe null - Das ist absolut unverständlich.
			System.out.println("Farbe:   " + root.gebaeude.farbe.name());
		}
		
		System.exit(0);
	}
	
}

class Data
{
	Gebaeude gebaeude;
}

enum Farbe
{
	ROT, BLAU;
}

enum Gebaeude
{
	HAUS  (Farbe.ROT),
	KIRCHE(Farbe.BLAU);

	public Farbe farbe;

	private Gebaeude(final Farbe farbe)
	{
		this.farbe = farbe;
	}
	
}
