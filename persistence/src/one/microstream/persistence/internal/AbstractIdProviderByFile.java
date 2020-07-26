package one.microstream.persistence.internal;

import static one.microstream.math.XMath.notNegative;
import static one.microstream.math.XMath.positive;

import one.microstream.afs.AFile;
import one.microstream.io.AbstractProviderByFile;

public abstract class AbstractIdProviderByFile extends AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void writeId(final AFile file, final long value)
	{
		write(file, Long.toString(value));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long increase ;
	private       long id       ;
	private       long threshold;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractIdProviderByFile(final AFile file, final long increase, final long id)
	{
		super(file);
		this.id        = notNegative(id      );
		this.increase  =    positive(increase);
		this.threshold =        id + increase ;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void writeId(final long value)
	{
		this.write(Long.toString(value));
	}

	protected void internalInitialize()
	{
		// either read existing id or write and provide default value
		this.threshold = this.id = this.canRead()
			? this.readId()
			: this.provideDefaultId()
		;
	}
	
	protected long readId()
	{
		return Long.parseLong(this.read());
	}
	
	protected long provideDefaultId()
	{
		// write id's default value and return it
		this.writeId(this.id);
		return this.id;
	}

	private void enlargeThreshold()
	{
		this.writeId(this.threshold += this.increase);
	}

	protected final long next()
	{
		if(++this.id >= this.threshold)
		{
			this.enlargeThreshold();
		}
		return this.id;
	}

	protected final long current()
	{
		return this.id;
	}

	protected final void internalUpdateId(final long value)
	{
		if(value < this.id)
		{
			throw new IllegalArgumentException(
				"Desired update id value of " + value + " has already been passed by current id value of " + this.id
			);
		}
		// check for no-op and prevent write
		if(value == this.id)
		{
			return;
		}
		/*
		 * set and write new higher value as id and threshold.
		 * This means the passed value is stored as the current highest registered value but no further id range
		 * is reserved for now. This happens only as soon as the next id is actually required.
		 * The rationale behind this behavior is that in a stable developed system, no more type ids are
		 * required, so reserving some in advance is just a waste of id range.
		 */
		this.writeId(this.threshold = this.id = value);
	}

}
