

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import net.jadoth.chars.VarString;
import net.jadoth.files.XFiles;

public class MainTestRTF
{
	static final String INVALIDATOR       = "$$";
	static final String INVALIDATOR_REGEX = INVALIDATOR;
	
	public static void main(final String[] args) throws Exception
	{
//		mergeRtfs(new File("D:/rtftest/"));
		mergeRtfs(new File("D:/_HumanLegacy/HuLe Docs/E01 Turning Point/Szenen"));
	}

	// very hacky, ignoring exceptions etc., only for provisional use
	public static void mergeRtfs(final File dir) throws Exception
	{
		final VarString vs = VarString.New(1_000_000);

		// sort by filename, just in case
		final File[] files = dir.listFiles();
		Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));

		for(final File f : files)
		{
			if(f.isDirectory() || !f.getName().endsWith(".rtf") || f.getName().contains(INVALIDATOR))
			{
				continue;
			}
//			System.out.println("processing "+f);

			final RTFEditorKit rtfParser = new RTFEditorKit();
			final Document document = rtfParser.createDefaultDocument();
			rtfParser.read(new FileInputStream(f), document, 0);
			final String text = document.getText(0, document.getLength());
			
//			vs.lf().add(f.getName().substring(0, f.getName().length() - 4)).lf().lf();
			
			vs.add(text);
		}

		final File target = new File(dir.getParent(), dir.getName()+"_merged_rtf.txt");
		String s = vs.toString();
		
		// remove lol-cr
		s = s.replaceAll("\\r","");
		
		// remove marked meta text
		s = Pattern.compile(INVALIDATOR_REGEX+".*?"+INVALIDATOR_REGEX, Pattern.DOTALL).matcher(s).replaceAll("");
		
		// cut spaces and remove everything beyond one blank line
		s = s.replaceAll("\\n\\s+\\n", "\n\n").replaceAll("(\\n){3,}+", "\n\n");
				
		XFiles.writeStringToFile(target, s);
	}

}
