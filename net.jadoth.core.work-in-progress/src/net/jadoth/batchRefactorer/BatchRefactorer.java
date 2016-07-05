package net.jadoth.batchRefactorer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Predicate;

public final class BatchRefactorer
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final LineBreakNormalizer     lineBreakNormalizer;
	private final LineBreakStrategy       lineBreakStrategy  ;
	private final Charset                 charset            ;
	private final PatternNormalizer       patternNormalizer  ;
	private final Predicate<? super File> fileFilter         ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BatchRefactorer(
		final LineBreakNormalizer     lineBreakNormalizer,
		final LineBreakStrategy       lineBreakStrategy  ,
		final Charset                 charset            ,
		final PatternNormalizer       patternNormalizer  ,
		final Predicate<? super File> fileFilter
	)
	{
		super();
		this.lineBreakNormalizer = lineBreakNormalizer;
		this.lineBreakStrategy   = lineBreakStrategy  ;
		this.charset             = charset            ;
		this.patternNormalizer   = patternNormalizer  ;
		this.fileFilter          = fileFilter         ;
	}
	
	
	public void batchRefactorFiles(final File... directories)
	{
		
	}
	
	
}
