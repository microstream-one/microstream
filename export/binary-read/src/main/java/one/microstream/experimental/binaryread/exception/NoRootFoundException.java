package one.microstream.experimental.binaryread.exception;

public class NoRootFoundException extends BinaryReadException
{
    public NoRootFoundException(final Long objectId)
    {
        super(String.format("Root Object with ObjectId %s is not found", objectId));
    }
}
