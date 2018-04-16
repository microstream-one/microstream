package net.jadoth.persistence.internal;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.io.IOException;

import net.jadoth.collections.EqHashTable;
import net.jadoth.persistence.types.PersistenceRefactoringMapping;
import net.jadoth.persistence.types.PersistenceRefactoringMappingProvider;
import net.jadoth.util.chars.StringTable;


public class FileRefactoringMappingProvider
extends AbstractProviderByFile
implements PersistenceRefactoringMappingProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static FileRefactoringMappingProvider New(final File file)
	{
		return new FileRefactoringMappingProvider(
			notNull(file)
		);
	}
	
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected FileRefactoringMappingProvider(final File file)
	{
		super(file);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected String readFile()
	{
		try
		{
			return this.read();
		}
		catch(final IOException e)
		{
			throw new RuntimeException(); // (16.04.2018 TM)EXCP: proper exception
		}
	}


	@Override
	public PersistenceRefactoringMapping provideRefactoringMapping()
	{
		if(!this.canRead())
		{
			return PersistenceRefactoringMapping.New();
		}
		
		final String fileContent = this.readFile();
		final StringTable st = StringTable.Static.parse(fileContent);
		
		final EqHashTable<String, String> mapping = EqHashTable.New();
		
		for(final String[] row : st.rows())
		{
			final String oldIdentifier = normalize(row[0]);
			if(oldIdentifier == null)
			{
				throw new NullPointerException(); // (16.04.2018 TM)EXCP: proper exception
			}
			mapping.add(oldIdentifier, normalize(row[1]));
		}
		
		return PersistenceRefactoringMapping.New(mapping);
	}
	
	private static String normalize(final String s)
	{
		final String normalized = s.trim();
		
		return normalized.isEmpty()
			? null
			: normalized
		;
	}
	
}
