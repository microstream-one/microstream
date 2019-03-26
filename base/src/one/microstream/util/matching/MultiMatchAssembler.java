package one.microstream.util.matching;

import static one.microstream.X.notNull;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

import one.microstream.chars.VarString;
import one.microstream.math.XMath;

public interface MultiMatchAssembler<E>
{
	public VarString assembleState(
		VarString                        vs                 ,
		DecimalFormat                    similarityFormatter,
		BiConsumer<VarString, ? super E> elementAssembler
	);
		
	public VarString assembleMappingSchemeHorizontal(
		VarString                        vs                 ,
		DecimalFormat                    similarityFormatter,
		BiConsumer<VarString, ? super E> elementAssembler
	);
	
	public VarString assembleMappingSchemeVertical(
		VarString                        vs                 ,
		DecimalFormat                    similarityFormatter,
		BiConsumer<VarString, ? super E> elementAssembler
	);
	
	public default VarString assemble(
		final VarString vs
	)
	{
		return this.assembleState(
			vs,
			Defaults.defaultSimilarityFormatter(),
			Defaults.defaultElementAssembler()
		);
	}
		
	public default VarString assembleMappingSchemeHorizontal(
		final VarString vs
	)
	{
		return this.assembleMappingSchemeHorizontal(
			vs,
			Defaults.defaultSimilarityFormatter(),
			Defaults.defaultElementAssembler()
		);
	}
	
	public default VarString assembleMappingSchemeVertical(
		final VarString vs
	)
	{
		return this.assembleMappingSchemeVertical(
			vs,
			Defaults.defaultSimilarityFormatter(),
			Defaults.defaultElementAssembler()
		);
	}
	
	public interface Defaults
	{
		public static <E> BiConsumer<VarString, ? super E> defaultElementAssembler()
		{
			return (vs, e) ->
				vs.add(e)
			;
		}
		
		public static DecimalFormat defaultSimilarityFormatter()
		{
			return new DecimalFormat("0.000");
		}
		
	}
	

		
	public class Implementation<E> implements MultiMatchAssembler<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
				
		public static <E> MultiMatchAssembler.Implementation<E> New(
			final MultiMatch.Implementation<E> match
		)
		{
			return new MultiMatchAssembler.Implementation<>(
				notNull(match)
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final MultiMatch.Implementation<E> match;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(final MultiMatch.Implementation<E> match)
		{
			super();
			this.match = match;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public VarString assembleState(
			final VarString                        vs       ,
			final DecimalFormat                    formatter,
			final BiConsumer<VarString, ? super E> assembler
		)
		{
			final MultiMatch.Implementation<E> match = this.match;
			final int candidateCount = Math.min(match.sourceCandidateCount, match.targetCandidateCount);
			
			vs
			.add("Matching state:")
			.lf().add("[candidateCount = ").add(candidateCount).add(']')
			.lf().add("[matchCount = ").add(match.matchCount).add(']')
			.lf().add("[averageSimilarity = ").add(XMath.round3(match.averageSimilarity)).add(']')
			.lf().add("[lowestSimilarity  = ").add(XMath.round3(match.lowestSimilarity )).add(']')
			.lf().add("[highestSimilarity = ").add(XMath.round3(match.highestSimilarity)).add(']')
			.lf()
			;

			vs.add("s\\t").tab();
			final int tLength = match.matrix[0].length;
			for(int t = 0; t < tLength; t++)
			{
				vs.add(t).tab();
			}
			vs.add("s2t").lf();

			for(int s = 0; s < match.matrix.length; s++)
			{
				final double[] sTargets = match.matrix[s];
				vs.add(s).tab();
				for(int t = 0; t < sTargets.length; t++)
				{
					if(sTargets[t] > 0)
					{
						vs.add(formatter.format(sTargets[t]));
					}
					vs.tab();
				}
				if(match.srcToTrgMap[s] >= 0)
				{
					vs.add("#" + match.srcToTrgMap[s]).tab();
					assembler.accept(vs, match.inputSource[s]);
					vs.tab();
					assembler.accept(vs, match.inputTarget[match.srcToTrgMap[s]]);
				}
				else if(match.srcCandCount[s] > 0)
				{
					vs.add('[').add(match.srcCandCount[s]).add(']');
				}
				else
				{
					vs.tab();
					assembler.accept(vs, match.inputSource[s]);
					vs.tab();
				}
				vs.lf();
			}

			vs.add("t2s").tab();
			for(int t = 0; t < tLength; t++)
			{
				if(match.trgToSrcMap[t] >= 0)
				{
					vs.add("#" + match.trgToSrcMap[t]);
				}
				else if(match.trgCandCount[t] > 0)
				{
					vs.add('[').add(match.trgCandCount[t]).add(']');
				}
				vs.tab();
			}
			vs.lf();
			vs.lf();
			vs.lf();
			
			return vs;
		}

		@Override
		public VarString assembleMappingSchemeHorizontal(
			final VarString                        line1Srcs,
			final DecimalFormat                    formatter,
			final BiConsumer<VarString, ? super E> assembler
		)
		{
			final MultiMatch.Implementation<E> match = this.match;
			
			final VarString
				line2SrcSymbol = VarString.New(),
				line3Similaris = VarString.New(),
				line4TrgSymbol = VarString.New(),
				line5Trgs      = VarString.New()
			;
			
			final MultiMatchResult<? extends E> result = match.result();

			for(final E e : result.inputSources())
			{
				assembler.accept(line1Srcs, e);
				line1Srcs.tab();
			}

			for(final MultiMatchResult.Item<? extends E> e : result.matchesInSourceOrder())
			{
				if(e != null)
				{
					line2SrcSymbol.append('|');
					line3Similaris.append(formatter.format(e.similarity()));
					line4TrgSymbol.append('|');
					assembler.accept(line5Trgs, e.targetElement());
				}
				else
				{
					line2SrcSymbol.append('-');
				}
				line2SrcSymbol.tab();
				line3Similaris.tab();
				line4TrgSymbol.tab();
				line5Trgs.tab();
			}

			for(final E e : result.unmatchedTargets())
			{
				line4TrgSymbol.append('+').tab();
				assembler.accept(line5Trgs, e);
				line5Trgs.tab();
			}

			return line1Srcs
				.lf().add(line2SrcSymbol)
				.lf().add(line3Similaris)
				.lf().add(line4TrgSymbol)
				.lf().add(line5Trgs)
			;
		}

		@Override
		public VarString assembleMappingSchemeVertical(
			final VarString                        vs       ,
			final DecimalFormat                    formatter,
			final BiConsumer<VarString, ? super E> assembler
		)
		{
			final MultiMatch.Implementation<E> match = this.match;
			
			for(int s = 0 ; s < match.inputSource.length; s++)
			{
				assembler.accept(vs, match.inputSource[s]);
				if(match.linkedTargets[s] != null)
				{
					vs.add("\t<-");
					vs.add(formatter.format(match.linkedSourceSimilarities[s]));
					vs.add("->\t");
					assembler.accept(vs, match.linkedTargets[s]);
				}
				else
				{
					vs.add("\t[deleted]");
				}
				vs.lf();
			}
			for(int t = 0 ; t < match.target.length; t++)
			{
				if(match.target[t] != null)
				{
					vs.add("\t[new]\t");
					assembler.accept(vs, match.target[t]);
					vs.lf();
				}
			}

			return vs;
		}
			
	}

}
