package net.jadoth.persistence.test;

final class TestBytie
{
	final byte b1, b2, b3, b4;

	public TestBytie(final byte b1, final byte b2, final byte b3, final byte b4)
	{
		super();
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.b4 = b4;
	}

	public TestBytie(final int b1, final int b2, final int b3, final int b4)
	{
		super();
		this.b1 = (byte)b1;
		this.b2 = (byte)b2;
		this.b3 = (byte)b3;
		this.b4 = (byte)b4;
	}

	@Override
	public String toString()
	{
		return "Byty [b1=" + this.b1 + ", b2=" + this.b2 + ", b3=" + this.b3 + ", b4=" + this.b4 + "]";
	}

}