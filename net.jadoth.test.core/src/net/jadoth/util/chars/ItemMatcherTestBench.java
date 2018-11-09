package net.jadoth.util.chars;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class ItemMatcherTestBench extends JFrame
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final int TEXTFIELD_COLUMNS      = 20;
	static final int TEXTFIELD_COUNT        = 16;
	static final int TEXTFIELD_OFFSET_X     = 25;
	static final int TEXTFIELD_OFFSET_Y     = 50;
	static final int TEXTFIELD_MULTIPLIER_Y = 20;

	static final int FRAME_WIDTH  = 1000;
	static final int FRAME_HEIGHT =  750;


	static final Font TEXTFIELD_FONT = new Font("Courier New", Font.PLAIN, 12);

	static {
		UIManager.put("TextArea.border", UIManager.get("TextField.border")); // omg
	}



	///////////////////////////////////////////////////////////////////////////
	//  static methods  //
	/////////////////////

	static JTextField createTextField()
	{
		final JTextField tf = new JTextField(TEXTFIELD_COLUMNS);
		tf.setFont(TEXTFIELD_FONT);
		return tf;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final JTextField[] targetFields;
	final JTextField[] linkedFields;
	final JTextField[] sourceFields;
	final JTextArea    txtOutput;




	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ItemMatcherTestBench(final String title, final int fieldCount)
	{
		super(title);
		this.sourceFields = new JTextField[fieldCount];
		this.targetFields = new JTextField[fieldCount];
		this.linkedFields = new JTextField[fieldCount];
		this.txtOutput = new JTextArea(fieldCount, 2*TEXTFIELD_COLUMNS + 10);
		this.createComponents();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void createTextFields(final JTextField[] textfields, final int xOffset, final int yOffset)
	{
		final Container pane = this.getContentPane();
		final Insets insets = pane.getInsets();

		for(int i = 0; i < textfields.length; i++)
		{
			final Dimension size = (textfields[i] = createTextField()).getPreferredSize();
			textfields[i].setBounds(
				insets.left + xOffset,
				insets.top  + yOffset + i * TEXTFIELD_MULTIPLIER_Y,
				size.width,
				size.height
			);
			pane.add(textfields[i]);

		}
	}

	private void createComponents()
	{
		final Container pane = this.getContentPane();
		pane.setLayout(null);
		final Insets insets = pane.getInsets();
		this.setSize(insets.left + insets.right + FRAME_WIDTH, insets.top + insets.bottom + FRAME_HEIGHT);

		this.createTextFields(this.sourceFields, TEXTFIELD_OFFSET_X, TEXTFIELD_OFFSET_Y);
		this.createTextFields(this.targetFields, TEXTFIELD_OFFSET_X + 250, TEXTFIELD_OFFSET_Y);
		this.createTextFields(this.linkedFields, TEXTFIELD_OFFSET_X + 500, TEXTFIELD_OFFSET_Y);


		pane.add(this.txtOutput);
		this.txtOutput.setFont(TEXTFIELD_FONT);
		final Dimension size = this.txtOutput.getPreferredSize();
		this.txtOutput.setBounds(
			TEXTFIELD_OFFSET_X,
			TEXTFIELD_OFFSET_Y + TEXTFIELD_MULTIPLIER_Y * this.sourceFields.length + 25,
			645,
			size.height
		);
	}





	static void createAndShowGUI()
	{
		final JFrame frame = new ItemMatcherTestBench(ItemMatcherTestBench.class.getSimpleName(), TEXTFIELD_COUNT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void main(final String[] args)
	{
		// for thread safety, this method should be invoked from the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				createAndShowGUI();
			}
		});
	}



}
