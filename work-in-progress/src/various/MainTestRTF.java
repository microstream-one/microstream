package various;


import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public class MainTestRTF
{
	static final String INVALIDATOR       = "$$";
	static final String INVALIDATOR_REGEX = INVALIDATOR;
	
	public static void main(final String[] args) throws Exception
	{
//		mergeRtfs(XIO.Path("D:/rtftest/"));
		mergeRtfs(XIO.Path("D:/_HumanLegacy/HuLe Docs/E01 Turning Point/Szenen"));
	}

	// very hacky, ignoring exceptions etc., only for provisional use
	public static void mergeRtfs(final Path dir) throws Exception
	{
		final VarString vs = VarString.New(1_000_000);

		// sort by filename, just in case
		final Path[] files = XIO.listEntries(dir);
		Arrays.sort(files, (f1, f2) -> XIO.getFileName(f1).compareTo(XIO.getFileName(f2)));

		for(final Path f : files)
		{
			if(XIO.unchecked.isDirectory(f) || !XIO.getFileName(f).endsWith(".rtf") || XIO.getFileName(f).contains(INVALIDATOR))
			{
				continue;
			}
//			System.out.println("processing "+f);

			final RTFEditorKit rtfParser = new RTFEditorKit();
			final Document document = rtfParser.createDefaultDocument();
			rtfParser.read(new FileInputStream(f.toFile()), document, 0);
			final String text = document.getText(0, document.getLength());
			
//			vs.lf().add(f.getName().substring(0, f.getName().length() - 4)).lf().lf();
			
			vs.add(text);
		}

		final Path target = XIO.Path(dir.getParent(), XIO.getFileName(dir)+"_merged_rtf.txt");
		String s = vs.toString();
		
		// remove lol-cr
		s = s.replaceAll("\\r","");
		
		// remove marked meta text
		s = Pattern.compile(INVALIDATOR_REGEX + ".*?" + INVALIDATOR_REGEX, Pattern.DOTALL).matcher(s).replaceAll("");
		
		// cut spaces and remove everything beyond one blank line
		s = s.replaceAll("\\n\\s+\\n", "\n\n").replaceAll("(\\n){3,}+", "\n\n");
				
		XIO.write(target, s, XChars.defaultJvmCharset());
	}

}
