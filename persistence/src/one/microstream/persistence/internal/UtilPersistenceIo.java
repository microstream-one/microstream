package one.microstream.persistence.internal;

import java.io.File;

import one.microstream.files.XFiles;
import one.microstream.persistence.exceptions.PersistenceException;

/*
 * Prefix "Util" to be excluded from the usual "Persistence~" type suggestions
 * but be easily findable when searching for "Util~".
 * It makes so much sense that way. Really weird that noone else does it that way.
 */
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
