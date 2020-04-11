import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.SizeGripIcon;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

import java.io.*;
import java.util.*;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import java.nio.file.*;
import java.awt.Robot;


public final class RSTAUIDemoApp extends JFrame implements SearchListener {
	private CollapsibleSectionPanel csp;
	private RSyntaxTextArea textArea;
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private FindToolBar findToolBar;
	private ReplaceToolBar replaceToolBar;
	private StatusBar statusBar;
	private String filename;
	private String filepath;


	private RSTAUIDemoApp() {
		initSearchDialogs();
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		csp = new CollapsibleSectionPanel();
		JPanel jp=new JPanel();
		jp.add(new FileTree(new File(".")));
		jp.add(csp);
		setJMenuBar(createMenuBar());
		contentPane.add(jp);

		textArea = new RSyntaxTextArea(40, 150);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		csp.add(sp);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		contentPane.add(errorStrip, BorderLayout.LINE_END);
		// JPanel jd = new JPanel();
		// jd.add(new TestTerminal());
		statusBar = new StatusBar();
		// contentPane.add(jd);
		contentPane.add(statusBar, BorderLayout.SOUTH);
       try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/lib/styles/dark.xml"));
            theme.apply(textArea);
            } 
            catch (Exception ioe) { // Never happens
                ioe.printStackTrace();
            }
        
	  CompletionProvider provider = createCompletionProvider();
      AutoCompletion ac = new AutoCompletion(provider);
      ac.install(textArea);

      setContentPane(contentPane);
      setTitle("N45Editor");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
	}

     private CompletionProvider createCompletionProvider() {
      DefaultCompletionProvider provider = new DefaultCompletionProvider();
      provider.addCompletion(new BasicCompletion(provider, "abstract"));
      provider.addCompletion(new BasicCompletion(provider, "assert"));
      provider.addCompletion(new BasicCompletion(provider, "break"));
      provider.addCompletion(new BasicCompletion(provider, "case"));
      provider.addCompletion(new BasicCompletion(provider, "transient"));
      provider.addCompletion(new BasicCompletion(provider, "try"));
      provider.addCompletion(new BasicCompletion(provider, "void"));
      provider.addCompletion(new BasicCompletion(provider, "volatile"));
      provider.addCompletion(new BasicCompletion(provider, "while"));

      provider.addCompletion(new ShorthandCompletion(provider, "sysout",
            "System.out.println(", "System.out.println("));
      provider.addCompletion(new ShorthandCompletion(provider, "syserr",
            "System.err.println(", "System.err.println("));

      return provider;
   }


	private void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}

	private void saveFile(JMenuItem savefile){
		JFileChooser fileChoose = new JFileChooser();
        int option = fileChoose.showSaveDialog(savefile);

        /*
             * ShowSaveDialog instead of showOpenDialog if the user clicked OK
             * (and not cancel)
         */
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                File openFile = fileChoose.getSelectedFile();
				filename=openFile.getName();
                setTitle("N45Editor"+" | "+filename);
				filepath=openFile.getPath();
                BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
                out.write(textArea.getText());
                out.close();
                // edit = false;
            } catch (Exception ex) { // again, catch any exceptions and...
                // ...write to the debug console
                System.err.println(ex.getMessage());
            }
        }
	}


	private JMenuBar createMenuBar() {

        JMenuBar mb = new JMenuBar();
		JMenu menu; 

		JMenuItem openfile=new JMenuItem("Open File");
		openfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
            JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to  browse files to open)
            int option = open.showOpenDialog(openfile); // get the option that the user selected (approve or cancel)

            /*
             * NOTE: because we are OPENing a file, we call showOpenDialog~ if
             * the user clicked OK, we have "APPROVE_OPTION" so we want to open
             * the file
             */
            if (option == JFileChooser.APPROVE_OPTION) {
                // FEdit.clear(textArea); // clear the TextArea before applying the file contents
                try {
                    File openFile = open.getSelectedFile();
					filename=openFile.getName();
					setTitle("N45Editor"+" | "+filename);
					filepath=openFile.getPath();
                    Scanner scan = new Scanner(new FileReader(filepath));
                    while (scan.hasNext()) {
						textArea.setText(textArea.getText()+"\n"+scan.nextLine());
                    }
                } catch (Exception ex) { // catch any exceptions, and...
                    // ...write to the debug console
                    System.err.println(ex.getMessage());
                }
            }
    	}
		});

		JMenuItem newfile=new JMenuItem("New File");
		newfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
             Object[] options = {"Save", "Return"};
                int n = JOptionPane.showOptionDialog(newfile, "Do you want to save the file at first ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (n == 0) {
                    saveFile(newfile);
                } else if (n == 1) {
                    textArea.setText("");
                }
    	}
		});

		JMenuItem savefile=new JMenuItem("Save As");
		savefile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			saveFile(savefile);
		}
		});

		JMenuItem sfile=new JMenuItem("Save");
		sfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			  try {
                    FileWriter myWriter = new FileWriter(filepath);
      				myWriter.write(textArea.getText());
      				myWriter.close();
                } catch (Exception ex) { // catch any exceptions, and...
                    // ...write to the debug console
                    System.err.println(ex.getMessage());
                }
		}
		});

		JMenuItem xexit=new JMenuItem("Exit");
		xexit.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			 System.exit(0);
		}
		});

		JMenuItem nw=new JMenuItem("New Window");
		nw.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try{
			Process process = Runtime.getRuntime().exec("java -cp \".;lib/jars/*\" RSTAUIDemoApp");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		});


        menu = new JMenu("  File  ");

		menu.add(newfile);
        menu.add(nw);
		menu.add(openfile);
        menu.add(new JMenuItem("Open Folder"));
		menu.add(sfile);
        menu.add(savefile);
        menu.add(xexit);
		mb.add(menu);

        menu = new JMenu("  Edit  ");
		JMenuItem undo=new JMenuItem("Undo");
		undo.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
 	      	Robot robot=new Robot();
    		robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_Z);
			robot.keyRelease(KeyEvent.VK_Z);
    		robot.keyRelease(KeyEvent.VK_CONTROL);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});

        menu.add(undo);

		JMenuItem redo=new JMenuItem("Redo");
		redo.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
      		 Robot robot=new Robot();

    robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_Y);
			robot.keyRelease(KeyEvent.VK_Y);
    		robot.keyRelease(KeyEvent.VK_CONTROL);
		} catch (Exception e) {
        e.printStackTrace();
}
		}
		});
		menu.add(redo);
		
        menu.add(new JMenuItem("Cut"));
        menu.add(new JMenuItem("Copy"));
        menu.add(new JMenuItem("Paste"));
		mb.add(menu);

        menu = new JMenu("  View  ");
		ButtonGroup bg = new ButtonGroup();
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo info : infos) {
			addItem(new LookAndFeelAction(info), bg, menu);
		}
		mb.add(menu);

        menu= new JMenu("  Search  ");
		menu.add(new JMenuItem(new ShowFindDialogAction()));
		menu.add(new JMenuItem(new ShowReplaceDialogAction()));
		menu.add(new JMenuItem(new GoToLineAction()));
		menu.addSeparator();

		int ctrl = getToolkit().getMenuShortcutKeyMask();
		int shift = InputEvent.SHIFT_MASK;
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl|shift);
		Action a = csp.addBottomComponent(ks, findToolBar);
		a.putValue(Action.NAME, "Show Find Search Bar");
		menu.add(new JMenuItem(a));
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_H, ctrl|shift);
		a = csp.addBottomComponent(ks, replaceToolBar);
		a.putValue(Action.NAME, "Show Replace Search Bar");
		menu.add(new JMenuItem(a));
		mb.add(menu);

        menu = new JMenu("  Run  ");
		JMenuItem t=new JMenuItem("New Terminal");
		t.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try{
			Process process = Runtime.getRuntime().exec("java TestTerminal");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		});
        menu.add(t);
		menu.add(new JMenuItem("Default run current file"));
		mb.add(menu);

        menu = new JMenu("  Help  ");
        menu.add(new JMenuItem("Documentation"));
		menu.add(new JMenuItem("License"));
        menu.addSeparator();
        menu.add(new JMenuItem("About"));
		mb.add(menu);

		return mb;
	}


	@Override
	public String getSelectedText() {
		return textArea.getSelectedText();
	}

	//  public void actionPerformed(ActionEvent e) {
        // If the source of the event was our "close" option

    // }

	/**
	 * Creates our Find and Replace dialogs.
	 */

	 
	private void initSearchDialogs() {

		findDialog = new FindDialog(this, this);
		replaceDialog = new ReplaceDialog(this, this);

		// This ties the properties of the two dialogs together (match case,
		// regex, etc.).
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);

		// Create tool bars and tie their search contexts together also.
		findToolBar = new FindToolBar(this);
		findToolBar.setSearchContext(context);
		replaceToolBar = new ReplaceToolBar(this);
		replaceToolBar.setSearchContext(context);

	}


	/**
	 * Listens for events from our search dialogs and actually does the dirty
	 * work.
	 */
	@Override
	public void searchEvent(SearchEvent e) {

		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result;

		switch (type) {
			default: // Prevent FindBugs warning later
			case MARK_ALL:
				result = SearchEngine.markAll(textArea, context);
				break;
			case FIND:
				result = SearchEngine.find(textArea, context);
				if (!result.wasFound() || result.isWrapped()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE:
				result = SearchEngine.replace(textArea, context);
				if (!result.wasFound() || result.isWrapped()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE_ALL:
				result = SearchEngine.replaceAll(textArea, context);
				JOptionPane.showMessageDialog(null, result.getCount() +
						" occurrences replaced.");
				break;
		}

		String text;
		if (result.wasFound()) {
			text = "Text found; occurrences marked: " + result.getMarkedCount();
		}
		else if (type==SearchEvent.Type.MARK_ALL) {
			if (result.getMarkedCount()>0) {
				text = "Occurrences marked: " + result.getMarkedCount();
			}
			else {
				text = "";
			}
		}
		else {
			text = "Text not found";
		}
		statusBar.setLabel(text);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new RSTAUIDemoApp().setVisible(true);
		});
	}

	 class FileTree extends JPanel {
  /** Construct a FileTree */
  public FileTree(File dir) {
    setLayout(new BorderLayout());

    // Make a tree list with all the nodes, and make it a JTree
    JTree tree = new JTree(addNodes(null, dir));

    // Add a listener
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
		// System.out.println(tree.getLastSelectedPathComponent().getUserObject().toString());
		// File f = new File(node.toString());  
        //     String abso = f.getPath(); 
		String selectedPath = tree.getSelectionPath().toString();
		String ans[]=selectedPath.split(",", 0);
		String pathy=ans[ans.length-2].toString()+"\\"+(ans[ans.length-1].toString().substring(0,ans[ans.length-1].length()-1)).trim();
		 try {
					setTitle("N45Editor"+" | "+pathy);
					textArea.setText("");
                    Scanner scan = new Scanner(new FileReader(pathy.substring(1,pathy.length())));
                    while (scan.hasNext()) {
						textArea.setText(textArea.getText()+"\n"+scan.nextLine());
                    }
                } catch (Exception ex) { // catch any exceptions, and...
                    // ...write to the debug console
                    System.err.println(ex.getMessage());
                }

     }
    });

    // Lastly, put the JTree into a JScrollPane.
    JScrollPane scrollpane = new JScrollPane();
    scrollpane.getViewport().add(tree);
	// scrollpane.getViewport().setPreferredSize(new Dimension(10,200));
    add(BorderLayout.CENTER, scrollpane);
  }

  /** Add nodes from under "dir" into curTop. Highly recursive. */
  DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
    String curPath = dir.getPath();
    DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
    if (curTop != null) { // should only be null at root
      curTop.add(curDir);
    }
    Vector ol = new Vector();
    String[] tmp = dir.list();
    for (int i = 0; i < tmp.length; i++)
      ol.addElement(tmp[i]);
    Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
    File f;
    Vector files = new Vector();
    // Make two passes, one for Dirs and one for Files. This is #1.
    for (int i = 0; i < ol.size(); i++) {
      String thisObject = (String) ol.elementAt(i);
      String newPath;
      if (curPath.equals("."))
        newPath = thisObject;
      else
        newPath = curPath + File.separator + thisObject;
      if ((f = new File(newPath)).isDirectory())
        addNodes(curDir, f);
      else
        files.addElement(thisObject);
    }
    // Pass two: for files.
    for (int fnum = 0; fnum < files.size(); fnum++)
      curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
    return curDir;
  }

  public Dimension getMinimumSize() {
    return new Dimension(200, 400);
  }

  public Dimension getPreferredSize() {
    return new Dimension(200, 643);
  }
	 }
	private class GoToLineAction extends AbstractAction {

		GoToLineAction() {
			super("Go To Line...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			GoToDialog dialog = new GoToDialog(RSTAUIDemoApp.this);
			dialog.setMaxLineNumberAllowed(textArea.getLineCount());
			dialog.setVisible(true);
			int line = dialog.getLineNumber();
			if (line>0) {
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(line-1));
				} catch (BadLocationException ble) { // Never happens
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
					ble.printStackTrace();
				}
			}
		}

	}

    /**
     * Changes the Look and Feel.
     */
	private class LookAndFeelAction extends AbstractAction {

		private LookAndFeelInfo info;

		LookAndFeelAction(LookAndFeelInfo info) {
			putValue(NAME, info.getName());
			this.info = info;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(RSTAUIDemoApp.this);
				if (findDialog!=null) {
					findDialog.updateUI();
					replaceDialog.updateUI();
				}
				pack();
			} catch (RuntimeException re) {
				throw re; // FindBugs
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

    /**
     * Shows the Find dialog.
     */
	private class ShowFindDialogAction extends AbstractAction {

		ShowFindDialogAction() {
			super("Find...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			findDialog.setVisible(true);
		}
	}
    /**
     * Shows the Replace dialog.
     */
	private class ShowReplaceDialogAction extends AbstractAction {

		ShowReplaceDialogAction() {
			super("Replace...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			replaceDialog.setVisible(true);
		}
	}
    /**
     * The status bar for this application.
     */
	private static class StatusBar extends JPanel {
		private JLabel label;
		StatusBar() {
			label = new JLabel("Ready");
			setLayout(new BorderLayout());
			add(label, BorderLayout.LINE_START);
			add(new JLabel(new SizeGripIcon()), BorderLayout.LINE_END);
		}

		void setLabel(String label) {
			this.label.setText(label);
		}
	}
}