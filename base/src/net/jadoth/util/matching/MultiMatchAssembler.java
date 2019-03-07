package net.jadoth.util.matching;

import java.util.function.BiConsumer;

import net.jadoth.chars.VarString;
import net.jadoth.math.XMath;
import net.jadoth.typing.KeyValue;

public interface MultiMatchAssembler<E>
{
	public VarString assembleState(
		VarString                        vs      ,
		String                           title   ,
		BiConsumer<VarString, ? super E> appender
	);
		
	public VarString assembleMappingSchemeHorizontal(
		VarString                        vs      ,
		BiConsumer<VarString, ? super E> appender
	);
		
	public VarString assembleMappingSchemeVertical(VarString vs, BiConsumer<VarString, ? super E> appender);
	
	
		
	public class Implementation<E> implements MultiMatchAssembler<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final MultiMatch.Implementation<E> match;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(final MultiMatch.Implementation<E> im)
		{
			super();
			this.match = im;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public VarString assembleState(
			final VarString                        vs      ,
			final String                           title   ,
			final BiConsumer<VarString, ? super E> appender
		)
		{
			final int mc = MultiMatcher.calculateMatchCount(this.match.srcToTrgMap);

			vs
			.add(title).lf()
			.add("[candidateCount = " + Math.min(this.match.sourceCandidateCount, this.match.targetCandidateCount) + "]")
			.add("[totalSimilarity = " + XMath.round3(this.match.totalSimilarity()) + "]")
			.lf()
			.add("[matchCount = " + mc + "]")
			.add("[averageSimilarity = " + (mc == 0 ? 0 : XMath.round3(this.match.totalSimilarity() / mc)) + "]")
			.lf()
			;

			vs.add("s\\t").tab();
			final int tLength = this.match.quantifiers[0].length;
			for(int t = 0; t < tLength; t++)
			{
				vs.add(t).tab();
			}
			vs.add("s2t").lf();

			for(int s = 0; s < this.match.quantifiers.length; s++)
			{
				final int[] sTargets = this.match.quantifiers[s];
				vs.add(s).tab();
				for(int t = 0; t < sTargets.length; t++)
				{
					if(sTargets[t] > 0)
					{
						vs.add(MultiMatch.Implementation.similarity(sTargets[t]));
					}
					vs.tab();
				}
				if(this.match.srcToTrgMap[s] >= 0)
				{
					vs.add("#" + this.match.srcToTrgMap[s]).tab();
					appender.accept(vs, this.match.inputSource[s]);
					vs.tab();
					appender.accept(vs, this.match.inputTarget[this.match.srcToTrgMap[s]]);
				}
				else if(this.match.srcCandCount[s] > 0)
				{
					vs.add("[" + this.match.srcCandCount[s] + "]");
				}
				else
				{
					vs.tab();
					appender.accept(vs, this.match.inputSource[s]);
					vs.tab();
				}
				vs.lf();
			}

			vs.add("t2s").tab();
			for(int t = 0; t < tLength; t++)
			{
				if(this.match.trgToSrcMap[t] >= 0)
				{
					vs.add("#" + this.match.trgToSrcMap[t]);
				}
				else if(this.match.trgCandCount[t] > 0)
				{
					vs.add("[" + this.match.trgCandCount[t] + "]");
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
			final VarString vs,
			final BiConsumer<VarString, ? super E> appender
		)
		{
			final VarString
				line2 = VarString.New(),
				line3 = VarString.New(),
				line4 = VarString.New()
			;
			
			final MultiMatchResult<E> result = this.match.result();

			for(final E e : result.inputSources())
			{
				appender.accept(vs, e);
				vs.tab();
			}

			for(final KeyValue<E, E> e : result.matchesInSourceOrder())
			{
				if(e != null)
				{
					line2.append('|');
					line3.append('|');
					appender.accept(line4, e.value());
				}
				else
				{
					line2.append('-');
				}
				line2.tab();
				line3.tab();
				line4.tab();
			}

			for(final E e : result.unmatchedTargets())
			{
				line3.append('+').tab();
				appender.accept(line4, e);
				line4.tab();
			}

			return vs.lf().add(line2).lf().add(line3).lf().add(line4);
		}

		@Override
		public VarString assembleMappingSchemeVertical(final VarString vs, final BiConsumer<VarString, ? super E> appender)
		{
			for(int s = 0 ; s < this.match.inputSource.length; s++)
			{
				appender.accept(vs, this.match.inputSource[s]);
				if(this.match.linkedTargets[s] != null)
				{
					vs.add("\t<-");
					vs.add(MultiMatch.Implementation.similarity(this.match.linkedSrcQuantifiers[s]));
					vs.add("->\t");
					appender.accept(vs, this.match.linkedTargets[s]);
				}
				else
				{
					vs.add("\t[deleted]");
				}
				vs.lf();
			}
			for(int t = 0 ; t < this.match.target.length; t++)
			{
				if(this.match.target[t] != null)
				{
					vs.add("\t[new]\t");
					appender.accept(vs, this.match.target[t]);
					vs.lf();
				}
			}

			return vs;
		}
			
	}

}
