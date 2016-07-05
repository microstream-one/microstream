package net.jadoth.persistence.test;

public interface TestInterfaceD extends TestInterfaceB, TestInterfaceC // intentionally extends A twice!
{
	public static final int CONST_D1 = 41;
	public static final int CONST_D2 = 42;
}
