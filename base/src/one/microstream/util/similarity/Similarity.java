package one.microstream.util.similarity;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;

public interface Similarity<E>
{
	public E sourceElement();
	
	public double similarity();
	
	public E targetElement();
	
	
	
	public static <E> E sourceElement(final Similarity<E> instance)
	{
		return instance != null
			? instance.sourceElement()
			: null
		;
	}
	
	public static Double similarity(final Similarity<?> instance)
	{
		return instance != null
			? instance.similarity()
			: null
		;
	}
	
	public static double _similarity(final Similarity<?> instance)
	{
		return instance != null
			? instance.similarity()
			: Double.NaN
		;
	}
	
	public static <E> E targetElement(final Similarity<E> instance)
	{
		return instance != null
			? instance.targetElement()
			: null
		;
	}
	
	public static <E> Similarity<E> New(
		final E      sourceElement,
		final double similarity   ,
		final E      targetElement
	)
	{
		/*
		 * No restrictions on the arguments. In the general case,
		 * anything can be compared to anything with any arbitrary similarity.
		 */
		return new Similarity.Default<>(
			sourceElement,
			similarity   ,
			targetElement
		);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> Similarity<E>[] Array(final int length)
	{
		return new Similarity[length];
	}
	
	public class Default<E> implements Similarity<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final E      sourceElement;
		private final double similarity   ;
		private final E      targetElement;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final E      sourceElement,
			final double similarity   ,
			final E      targetElement
		)
		{
			super();
			this.sourceElement = sourceElement;
			this.similarity    = similarity   ;
			this.targetElement = targetElement;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final E sourceElement()
		{
			return this.sourceElement;
		}
		
		@Override
		public final double similarity()
		{
			return this.similarity;
		}
		
		@Override
		public final E targetElement()
		{
			return this.targetElement;
		}
		
		@Override
		public String toString()
		{
			final VarString vs = VarString.New()
				.add(this.sourceElement, XChars::assembleCautiously)
				.add('<', '-')
				.add(MultiMatchAssembler.Defaults.defaultSimilarityFormatter().format(this.similarity))
				.add('-', '>')
				.add(this.targetElement, XChars::assembleCautiously)
			;
			
			return vs.toString();
		}
		
	}
	
}
