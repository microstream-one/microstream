package one.microstream.experimental.inheritence;

import one.microstream.experimental.basic.TypeA;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public interface TypeA1 extends TypeA
{
	public Object getA1Value();
	public void setA1Value(Object a1Value);


	/*
	 * Body of TypeA1 simple extends the body of TypeA parallel to the extending the interface
	 * So the Body classes inherit alongside the interfaces
	 */
	public class Implementation extends TypeA.Implementation implements TypeA1
	{
		protected Object a1Value = null;



		@Override
		public Object getA1Value()
		{
			return this.a1Value;
		}

		@Override
		public void setA1Value(final Object a1Value)
		{
			this.a1Value = a1Value;
		}
	}

}
