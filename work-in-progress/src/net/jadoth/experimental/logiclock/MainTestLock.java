package net.jadoth.experimental.logiclock;

import net.jadoth.functional.Action;



public class MainTestLock
{
	static final Resource<Thread> LEAF_1 = Resource.New();
	static final Resource<Thread> LEAF_2 = Resource.New();
	static final Resource<Thread> LEAF_3 = Resource.New();
	
	static final Resource<Thread> NODE_1_2 = Resource.New(LEAF_1, LEAF_2);
	static final Resource<Thread> NODE_2_3 = Resource.New(LEAF_2, LEAF_3);
	static final Resource<Thread> NODE_1_3 = Resource.New(LEAF_1, LEAF_3);
	static final Resource<Thread> NODE_ALL = Resource.New(LEAF_1, LEAF_2, LEAF_3);
	
	static final Resource<Thread> NODE_NODE_ALL = Resource.New(NODE_1_2, NODE_2_3);
	
	
	
	public static void main(final String[] args)
	{
		new Thread("T1"){
			@Override public void run() {
				LEAF_1.execute(this, work);
			}
		}.start();
		
		new Thread("T2"){
			@Override public void run() {
				NODE_2_3.execute(this, work);
			}
		}.start();

		new Thread("T3"){
			@Override public void run() {
				NODE_ALL.execute(this, work);
			}
		}.start();
	}
	
	static final Action work = new Action(){
		@Override public void execute() {
			System.out.println(Thread.currentThread()+" working");
		}
	};
		
}
