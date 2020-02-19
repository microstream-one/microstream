package one.microstream.storage.restclient;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class MainTestStorageRestClient
{
	public static void main(
		final String[] args
	)
		throws Throwable
	{
		final StorageView view = StorageView.New(
			StorageViewConfiguration.Default(),
			StorageRestClientJersey.New("http://localhost:4567/microstream")
		);
	
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(view.root());
		root.add(new DummyNode());
		final DefaultTreeModel model = new DefaultTreeModel(root);
		final JTree tree = new JTree(model);
		tree.setShowsRootHandles(true);
		tree.collapsePath(new TreePath(root));
		tree.addTreeWillExpandListener(new TreeWillExpandListener()
		{
			@Override
			public void treeWillExpand(
				final TreeExpansionEvent event
			)
				throws ExpandVetoException
			{
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
				if(node.getChildCount() == 1 && node.getChildAt(0) instanceof DummyNode)
				{
					node.removeAllChildren();
					final StorageViewElement element = (StorageViewElement)node.getUserObject();
					for(final StorageViewElement member : element.members(false))
					{
						final DefaultMutableTreeNode child = new DefaultMutableTreeNode(member);
						if(member.hasMembers())
						{
							child.add(new DummyNode());
						}
						node.add(child);
					}
				}
				model.nodeStructureChanged(node);
			}
			
			@Override
			public void treeWillCollapse(
				final TreeExpansionEvent event
			)
				throws ExpandVetoException
			{
			}
		});
		final JFrame frame = new JFrame("REST Client Test");
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(new JScrollPane(tree));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
	static class DummyNode extends DefaultMutableTreeNode
	{
	}
	
}
