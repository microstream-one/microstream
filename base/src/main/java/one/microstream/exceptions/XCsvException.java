
package one.microstream.exceptions;

public class XCsvException extends BaseException
{
	public XCsvException()
	{
		super();
	}
	
	public XCsvException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public XCsvException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	
	public XCsvException(final String message)
	{
		super(message);
	}
	
	public XCsvException(final Throwable cause)
	{
		super(cause);
	}
	
}
