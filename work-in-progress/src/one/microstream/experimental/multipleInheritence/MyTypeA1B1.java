package one.microstream.experimental.multipleInheritence;

import one.microstream.experimental.basic.TypeA;
import one.microstream.experimental.basic.TypeB;

/* read:
public class MyTypeA1B1 extends TypeB.Implementation, TypeA.Implementation */
public class MyTypeA1B1 extends TypeA.Implementation implements TypeB
{
	protected MyTypeB1 thisB1 = new MyTypeB1();

	protected Object myValue = null;



	///////////////////////////////////////////////////////////////////////////
	// Methods inherited from TypeA //
	/////////////////////////////////
	@Override
	public Object getAValue() {
		return super.getAValue();
	}

	@Override
	public void setAValue(final Object aValue) {
		super.setAValue(aValue);
	}

	@Override
	protected String getInternWhoAmI() {
		return "I'm MyTypeA1B1";
	}


	boolean nothingIsNull()
	{
		//protected members of superclass #1 are accessible of course
		if(this.aValue == null)
		{
			return false;
		}

		//protected members of superclass #2 are accessible now as well
		if(this.thisB1.getProtectedBValue() == null)
		{
			return false;
		}

		return true;
	}

	@Override
	public Object getBValue() {
		return this.thisB1.getBValue();
	}

	@Override
	public void setBValue(final Object value) {
		this.thisB1.setBValue(value);
	}



	@Override
	public String whoAmI() {
		/*
		 * Call either of the two superclasses' whoAmI().
		 * Both will eventually call MyTypeA1B1.getInternWhoAmI().
		 *
		 * This means true multiple inheritence (with a little inner class detour)
		 * because both superclasses use A1B1 as their subclass
		 */

		//call superclass#1 method TypeB.whoAmI() which in turn will call this.getInternWhoAmI()
		System.out.print("MyTypeA1B1 calling superclass #1 whoAmI(): ");
		System.out.println(super.whoAmI());
		System.out.println("");

		//call superclass#2 method TypeA.Body.whoAmI() which in turn will call this.getInternWhoAmI()
		System.out.print("MyTypeA1B1 calling superclass #2 whoAmI(): ");
		System.out.println(this.thisB1.whoAmI());
		System.out.println("");

		//decide which one to take. Let's take superclass#2's method
		System.out.print("Picked one: ");
		return this.thisB1.whoAmI();
	}

	@Override
	public String toString() {
		return this.whoAmI();
	}


	///////////////////////////////////////////////////////////////////////////
	// Class part inherited from TypeB //
	////////////////////////////////////
	protected class MyTypeB1 extends TypeB.Implementation
	{
		@Override
		public Object getBValue() {
			return super.getBValue();
		}

		@Override
		public void setBValue(final Object value) {
			if(MyTypeA1B1.this.nothingIsNull() && MyTypeA1B1.this.myValue != null)
			{
				//subclass #2 can access members of subclass #1
			}
			super.setBValue(value);
			/* superclass logic will use overridden methods of MyTypeA1
			 * which can use MyTypeA1B1.this subsequently
			 * so A1B1 logic can influence TypeA.Body
			 * like it would be normal "first class" inheritence
			 */
		}

		//make protected members of superclass #2 accessible for enclosing class
		protected Object getProtectedBValue(){
			return this.protectedBValue;
		}
		protected void getProtectedBValue(final Object protectedBValue){
			this.protectedBValue = protectedBValue;
		}

		@Override
		protected String getInternWhoAmI() {
			return MyTypeA1B1.this.getInternWhoAmI();
		}

	}

}
