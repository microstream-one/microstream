package net.jadoth.persistence.internal;

import java.io.File;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.exceptions.PersistenceException;

public class UtilPersistenceIo
{
	public static void move(final File sourceFile, final File targetFile)
	{
		try
		{
			XFiles.move(sourceFile, targetFile);
		}
		catch(final Exception e)
		{
			// (07.03.2019 TM)EXCP: proper exception
			throw new PersistenceException("Could not move file: " + sourceFile + " -> " + targetFile, e);
		}
	}
	
}
