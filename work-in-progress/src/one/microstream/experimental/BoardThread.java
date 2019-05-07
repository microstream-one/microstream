package one.microstream.experimental;

public abstract class BoardThread extends Thread
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final boolean[] threadBoard;
	final int threadBoardIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
//		System.out.println("complete: "+this.threadBoardIndex);
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
