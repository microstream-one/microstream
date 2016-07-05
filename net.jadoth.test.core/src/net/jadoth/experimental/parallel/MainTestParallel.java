package net.jadoth.experimental.parallel;

import java.util.Arrays;

import net.jadoth.concurrent.JadothThreads;
import net.jadoth.math.JadothMath;

public class MainTestParallel
{
	static final int THREAD_COUNT = 10;



	public static void main(final String[] args)
	{
		final boolean[] threadBoard = new boolean[THREAD_COUNT];

		for(int i = 0; i < THREAD_COUNT; i++)
		{
			new BoardThread(threadBoard, i){ @Override public void run() {
				System.out.println("Preparing... "+this.threadBoardIndex);
				JadothThreads.sleep(JadothMath.random(1000)+1000); // do a lot of work
//				JaThreads.sleep(JaMath.random(50) + 1000 + this.number*100); // do a lot of work
				System.out.println("Starting "+this.threadBoardIndex);
				this.complete();
			}}.start();
		}

		synchronized(threadBoard){
			try
			{
				System.out.println("Started waiting for threads: "+Arrays.toString(threadBoard));
				tb: for(int currentWaitIndex = 0;; threadBoard.wait()){ // crazy for :D
					while(threadBoard[currentWaitIndex])
					{
						if(++currentWaitIndex >= THREAD_COUNT)
						{
							break tb; // all threads have finished, drop out.
						}
					}
					System.out.println("Waiting on ("+currentWaitIndex+")"+Arrays.toString(threadBoard));
					 // wait for next notification to check thread status
				}
				// reaching this point means all threads have completed their work.
			}
			catch(final InterruptedException e)
			{
				e.printStackTrace(); // (29.05.2011)FIXME: what to do here...
			}
		}

		System.out.println(THREAD_COUNT+" threads finished:");
		System.out.println(Arrays.toString(threadBoard));


	}

}



abstract class BoardThread extends Thread
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final boolean[] threadBoard;
	final int threadBoardIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BoardThread(final boolean[] board, final int threadBoardIndex)
	{
		super();
		this.threadBoard = board;
		this.threadBoardIndex = threadBoardIndex;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected void complete()
	{
		System.out.println("complete: "+this.threadBoardIndex);
		this.threadBoard[this.threadBoardIndex] = true; // thread safe as each thread only accesses his own array slot
		synchronized(this.threadBoard){
			this.threadBoard.notify(); // master thread is the only one waiting for board
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public abstract void run();

}
