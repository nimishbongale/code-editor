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
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
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
import javax.swing.plaf.metal.*;
import javax.swing.plaf.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.text.View;

class CustomTabbedPaneUI extends MetalTabbedPaneUI
{
   Rectangle xRect;
   protected void installListeners() {
      super.installListeners();
      tabPane.addMouseListener(new MyMouseHandler());
   }
  
   protected void paintTab(Graphics g, int tabPlacement,
                           Rectangle[] rects, int tabIndex,
                           Rectangle iconRect, Rectangle textRect) {
      super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
      Font f = g.getFont();
      g.setFont(new Font("Consolas", Font.BOLD, 15));
      FontMetrics fm = g.getFontMetrics(g.getFont());
      int charWidth = fm.charWidth('x');
      int maxAscent = fm.getMaxAscent();
      g.drawString("x", textRect.x + textRect.width - 3, textRect.y + textRect.height - 3);
      g.drawRect(textRect.x+textRect.width-5,
                 textRect.y+textRect.height-maxAscent, charWidth+2, maxAscent-1);
      xRect = new Rectangle(textRect.x+textRect.width-5,
                 textRect.y+textRect.height-maxAscent, charWidth+2, maxAscent-1);
      g.setFont(f);
    }
  
    class MyMouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
			int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
			JTabbedPane tabPane = (JTabbedPane)e.getSource();
			String ans=tabPane.getTitleAt(tabIndex);
			if(!ans.equals("blank")){
				RSTAUIDemoApp.filepath=ans.trim();
			}
            if (xRect.contains(e.getPoint())) {
			   if(tabIndex>0)
               tabPane.remove(tabIndex);
            }
        }
    }
}


public final class RSTAUIDemoApp extends JFrame implements SearchListener {
	private JPanel csp;
	private RSyntaxTextArea textArea;
	private JTabbedPane tabbedPane; 
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private StatusBar statusBar;
	private String filename;
	public static String filepath;
	public static String folderpath;

	public RSyntaxTextArea inittextarea(){
		textArea = new RSyntaxTextArea(38, 150);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		return textArea;
	}
	
	private RSTAUIDemoApp(String fop) {
		initSearchDialogs();
		folderpath=fop;
		UIManager.put("TabbedPane.selected", Color.white);
		tabbedPane= new JTabbedPane();
		tabbedPane.setUI(new CustomTabbedPaneUI());
		RTextScrollPane sp = new RTextScrollPane(textArea);
		tabbedPane.addTab("  blank    ",null,new RTextScrollPane(inittextarea()),
                  "File1");
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		csp = new JPanel();
		JPanel jp=new JPanel();
		csp.setLayout(new BoxLayout(csp, BoxLayout.Y_AXIS));
		setTitle("N45 Editor | "+fop);
		jp.add(new FileTree(new File(fop)));
		jp.add(csp);
		csp.add(tabbedPane);
		setJMenuBar(createMenuBar());
		contentPane.add(jp);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		contentPane.add(errorStrip, BorderLayout.LINE_END);
		statusBar = new StatusBar();
		// contentPane.add(jd);
		contentPane.add(statusBar, BorderLayout.SOUTH);
       try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/lib/styles/default.xml"));
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

	protected JComponent makeTextPanel(String text) {
    JPanel panel = new JPanel(false);
    JLabel filler = new JLabel(text);
    filler.setHorizontalAlignment(JLabel.CENTER);
    panel.setLayout(new GridLayout(1, 1));
    panel.add(filler);
    return panel;
}

     private CompletionProvider createCompletionProvider() {
      DefaultCompletionProvider provider = new DefaultCompletionProvider();
	  try{
	  File fil = new File("keywords.txt");
	  Scanner scan = new Scanner(new FileReader(fil.getPath()));
        while (scan.hasNext()) {
			provider.addCompletion(new BasicCompletion(provider, scan.nextLine()));
        }
	  }
	  catch(Exception e){
		  e.printStackTrace();
	  }

      provider.addCompletion(new ShorthandCompletion(provider, "sysout",
            "System.out.println(", "System.out.println("));
      provider.addCompletion(new ShorthandCompletion(provider, "syserr",
            "System.err.println(", "System.err.println("));
		provider.addCompletion(new ShorthandCompletion(provider, "psvm",
            "public static void main(String args[]){", "public static void main(String args[]){"));
		provider.addCompletion(new ShorthandCompletion(provider, "p",
            "print()", "print()"));

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
				filepath=openFile.getPath();
				tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),"    "+filepath+"      ");
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
        openfile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));

		openfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
            JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to  browse files to open)
            int option = open.showOpenDialog(openfile); // get the option that the user selected (approve or cancel)
            if (option == JFileChooser.APPROVE_OPTION) {
                // FEdit.clear(textArea); // clear the TextArea before applying the file contents
                try {
                    File openFile = open.getSelectedFile();
					filename=openFile.getName();
					filepath=openFile.getPath();
					textArea=inittextarea();
					tabbedPane.addTab("   "+filepath+"      ", null,new RTextScrollPane(textArea),"Does nothing at all");
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
		 newfile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
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
		 savefile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		savefile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			saveFile(savefile);
		}
		});

		JMenuItem sfile=new JMenuItem("Save");
		 sfile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		sfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			  try {
                    FileWriter myWriter = new FileWriter(filepath);
      				myWriter.write(textArea.getText());
      				myWriter.close();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
		}
		});

		JMenuItem xexit=new JMenuItem("Exit");
		 xexit.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		xexit.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			 System.exit(0);
		}
		});

		JMenuItem nw=new JMenuItem("New Window");
		 nw.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		nw.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try{
			Process process = Runtime.getRuntime().exec("java -cp '.;lib/jars/*' RSTAUIDemoApp");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		});

		JMenuItem openfolder=new JMenuItem("Open Folder");
		 openfolder.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		openfolder.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(openfolder);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
			   String ar[]={file.getPath()};
			   main(ar);
            }
		}
		});


        menu = new JMenu("  File  ");

		menu.add(newfile);
        menu.add(nw);
		menu.add(openfile);
        menu.add(openfolder);
		menu.add(sfile);
        menu.add(savefile);
        menu.add(xexit);
		mb.add(menu);

        menu = new JMenu("  Edit  ");
		JMenuItem undo=new JMenuItem("Undo");
		 undo.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
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
		 redo.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
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

		JMenuItem cut=new JMenuItem("Cut");
		 cut.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		cut.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
 	      	Robot robot=new Robot();
    		robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_X);
			robot.keyRelease(KeyEvent.VK_X);
    		robot.keyRelease(KeyEvent.VK_CONTROL);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});

        menu.add(cut);

		JMenuItem copy=new JMenuItem("Copy");
		 copy.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		copy.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
 	      	Robot robot=new Robot();
    		robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_C);
			robot.keyRelease(KeyEvent.VK_C);
    		robot.keyRelease(KeyEvent.VK_CONTROL);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});

        menu.add(copy);

		JMenuItem paste=new JMenuItem("Paste");
		 paste.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		paste.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
 	      	Robot robot=new Robot();
    		robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
    		robot.keyRelease(KeyEvent.VK_CONTROL);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});

        menu.add(paste);
		mb.add(menu);

        menu = new JMenu("  View  ");
		JMenu submenu1=new JMenu("Theme");
		ButtonGroup bg = new ButtonGroup();
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo info : infos) {
			addItem(new LookAndFeelAction(info), bg, submenu1);
		}
		menu.add(submenu1);
		JMenu submenu2=new JMenu("Style");
		ButtonGroup b = new ButtonGroup();
		String[] info = {"dark","darkii","default-alt","default","eclipse","idea","idle","jce","vs"};
		for (String inf : info) {
			JRadioButtonMenuItem rbMenuItem= new JRadioButtonMenuItem(inf);
			b.add(rbMenuItem);
			rbMenuItem.setMnemonic(KeyEvent.VK_R);
			rbMenuItem.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
				Theme theme = Theme.load(getClass().getResourceAsStream("/lib/styles/"+inf+".xml"));
            	theme.apply(textArea);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});
		submenu2.add(rbMenuItem);
		}
		menu.add(submenu2);
		mb.add(menu);

        menu= new JMenu("  Search  ");
		menu.add(new JMenuItem(new ShowFindDialogAction()));
		menu.add(new JMenuItem(new ShowReplaceDialogAction()));
		menu.add(new JMenuItem(new GoToLineAction()));
		mb.add(menu);

        menu = new JMenu("  Run  ");
		JMenuItem t=new JMenuItem("New Terminal");
		 t.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
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

	 
	private void initSearchDialogs() {

		findDialog = new FindDialog(this, this);
		replaceDialog = new ReplaceDialog(this, this);

		// This ties the properties of the two dialogs together (match case,
		// regex, etc.).
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);

		// Create tool bars and tie their search contexts together also.
		// findToolBar = new FindToolBar(this);
		// findToolBar.setSearchContext(context);
		// replaceToolBar = new ReplaceToolBar(this);
		// replaceToolBar.setSearchContext(context);

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

	public static void main(String args[]) {
		SwingUtilities.invokeLater(() -> {
			try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			RSTAUIDemoApp jj = new RSTAUIDemoApp(args[0]);
			jj.setVisible(true);
			ImageIcon img = new ImageIcon("lib/logo.png");
			jj.setIconImage(img.getImage());
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
			 		textArea=inittextarea();
					 String pathe=pathy.substring(1,pathy.length());
					Scanner scan = new Scanner(new FileReader(pathe));
                    while (scan.hasNext()) {
						textArea.setText(textArea.getText()+"\n"+scan.nextLine());
                    }
			 		tabbedPane.addTab("    "+pathe+"      ",null,new RTextScrollPane(textArea),
                  "File");
                   
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