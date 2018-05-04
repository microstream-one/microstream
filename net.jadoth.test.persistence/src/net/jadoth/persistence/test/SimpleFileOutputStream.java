package net.jadoth.persistence.test;

import static net.jadoth.X.notNull;
import static net.jadoth.file.JadothFiles.ensureWriteableFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SimpleFileOutputStream extends OutputStream
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final File file;
	private FileOutputStream fos = null;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SimpleFileOutputStream(final File file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private FileOutputStream fos()
	{
		if(this.fos == null)
		{
			try
			{
				this.fos = new FileOutputStream(ensureWriteableFile(this.file));
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return this.fos;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void write(final int b) throws IOException
	{
		this.fos().write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException
	{
		this.fos().write(b, off, len);
	}

	@Override
	public void write(final byte[] b) throws IOException
	{
		this.fos().write(b);
	}

	@Override
	public void close() throws IOException
	{
		if(this.fos != null)
		{
			this.fos.close();
			this.fos = null;
		}
	}

}
