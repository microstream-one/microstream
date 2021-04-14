package one.microstream.functional;

import java.util.function.Consumer;



/**
 * 
 *
 */
public interface Processable<E>
{
	public <P extends Consumer<? super E>> P process(P processor);
}
