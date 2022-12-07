package one.microstream.integrations.lucene.types;

import java.util.function.Function;

import org.apache.lucene.document.Document;

@FunctionalInterface
public interface EntityMatcher<E> extends Function<Document, E>
{
	@Override
	public E apply(Document document);
}
