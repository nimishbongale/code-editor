import java.awt.*;
import java.awt.event.*; //importing abstract window toolkit

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo; //swing UI manager, for different styles
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.CollapsibleSectionPanel; //rsta=rsyntaxtextarea, a textarea component which supports code highlighting
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.SizeGripIcon;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
/*imports from external jars*/

import org.fife.ui.autocomplete.*; //for autocomplete features
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

import java.io.*;
import java.util.*;
/*regular imports*/

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
/*JTree for displaying directory structure*/

import java.nio.file.*;
/* for file stream handling*/
import java.awt.Robot;
/*to mimick keypresses*/
import javax.swing.plaf.metal.*;
/*MetalTabbedPaneUI schemes*/

import javax.swing.plaf.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.text.View;

class CustomTabbedPaneUI extends MetalTabbedPaneUI //making a custom tabbed pane for showing multiple files
{
   Rectangle xRect;//tab shape
   protected void installListeners() {
      super.installListeners();
      tabPane.addMouseListener(new MyMouseHandler());//adding a mouse handler to listen to mouse clicks
   }
  
   protected void paintTab(Graphics g, int tabPlacement,
                           Rectangle[] rects, int tabIndex,
                           Rectangle iconRect, Rectangle textRect) {
      super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
      Font f = g.getFont();
      g.setFont(new Font("Consolas", Font.BOLD, 15));//setting font of text inside tab
      FontMetrics fm = g.getFontMetrics(g.getFont());
      int charWidth = fm.charWidth('x');//char inside close button
      int maxAscent = fm.getMaxAscent();
      g.drawString("x", textRect.x + textRect.width - 3, textRect.y + textRect.height - 3);
      g.drawRect(textRect.x+textRect.width-5,
                 textRect.y+textRect.height-maxAscent, charWidth+2, maxAscent-1);
      xRect = new Rectangle(textRect.x+textRect.width-5,
                 textRect.y+textRect.height-maxAscent, charWidth+2, maxAscent-1);//final tab shape
      g.setFont(f);
    }
  
    class MyMouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
			int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());//get the current tab index clicked
			JTabbedPane tabPane = (JTabbedPane)e.getSource();
			String ans=tabPane.getTitleAt(tabIndex);//get the title of the tab, filepath
			if(!ans.equals("blank")){
				RSTAUIDemoApp.filepath=ans.trim(); //set trimmed filepath to current file
			}
            if (xRect.contains(e.getPoint())) {
			   if(tabIndex>0)//don't remove the blank tab, leads to UI issues
               tabPane.remove(tabIndex);//remove tab
            }
        }
    }
}


public final class RSTAUIDemoApp extends JFrame implements SearchListener {
	private JPanel csp; 
	private RSyntaxTextArea textArea;
	private JTabbedPane tabbedPane; 
	private FindDialog findDialog; //Find text in textArea
	private ReplaceDialog replaceDialog; //Find and Replace text in textArea
	private StatusBar statusBar; //Status bar for showing important messages
	private String filename; //current filename
	public static String filepath; //current filepath
	public static String folderpath; //current folderpath

	public RSyntaxTextArea inittextarea(){
		textArea = new RSyntaxTextArea(38, 150);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); //by default, code editor syntaxes according to java synatxes
		textArea.setCodeFoldingEnabled(true);//code folding enable
		textArea.setMarkOccurrences(true); //mark previous occurences
		new AutoCompletion(createCompletionProvider()).install(textArea); //install completion provider on the rest of the textareas
		return textArea;//return current instance of textarea
	}

	@Override
	public String getSelectedText(){
		return textArea.getText();
	}
	
	private RSTAUIDemoApp(String fop) { //parameterized constructor accepting folderpath
		initSearchDialogs();
		folderpath=fop; //make static variable=current folderpath
		UIManager.put("TabbedPane.selected", Color.white);//selected tab colour=white, unselected gray 
		tabbedPane= new JTabbedPane();
		tabbedPane.setUI(new CustomTabbedPaneUI()); //setUI of tabbedpane
		RTextScrollPane sp = new RTextScrollPane(textArea); //add it to a scrollpane
		tabbedPane.addTab("  blank    ",null,new RTextScrollPane(inittextarea()),
                  "File1");
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		csp = new JPanel();
		JPanel jp=new JPanel();
		csp.setLayout(new BoxLayout(csp, BoxLayout.Y_AXIS));
		setTitle("N45 Editor | "+fop); //title of texteditor + folderpath
		jp.add(new FileTree(new File(fop))); //add the JTree component on the left
		jp.add(csp); //add the textarea + tab combination
		csp.add(tabbedPane); //add tabbedpane to Jpanel
		setJMenuBar(createMenuBar());
		contentPane.add(jp); //add a menubar

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		contentPane.add(errorStrip, BorderLayout.LINE_END);
		statusBar = new StatusBar();
		contentPane.add(statusBar, BorderLayout.SOUTH); //add the statusBar to the bottom
       try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/lib/styles/default.xml"));//initially default theme
            theme.apply(textArea);//apply to textarea
            } 
            catch (Exception ioe) { // Never happens
                ioe.printStackTrace();
            }
        
	  CompletionProvider provider = createCompletionProvider(); //Completion provider, external jar
      AutoCompletion ac = new AutoCompletion(provider);
      ac.install(textArea); //adds basic autocompletion to the textarea

      setContentPane(contentPane);
      setTitle("N45Editor");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
	}

     private CompletionProvider createCompletionProvider() {
      DefaultCompletionProvider provider = new DefaultCompletionProvider();
	  try{
	  File fil = new File("keywords.txt"); //read from file keywords.txt
	  Scanner scan = new Scanner(new FileReader(fil.getPath()));
        while (scan.hasNext()) {
			provider.addCompletion(new BasicCompletion(provider, scan.nextLine()));//completion is added and enabled
        }/*Ctrl + <space> activates completion*/
	  }
	  catch(Exception e){
		  e.printStackTrace();
	  }

      provider.addCompletion(new ShorthandCompletion(provider, "sysout",
            "System.out.println(", "System.out.println("));
			/*Basic shorthand, upon typing sysout, and pressing Ctrl + <space>, System.out.println overwrrites sysout*/
      provider.addCompletion(new ShorthandCompletion(provider, "syserr",
            "System.err.println(", "System.err.println("));
		provider.addCompletion(new ShorthandCompletion(provider, "psvm",
            "public static void main(String args[]){", "public static void main(String args[]){"));
		provider.addCompletion(new ShorthandCompletion(provider, "p",
            "print()", "print()"));/*some basic support for python*/
		provider.addCompletion(new ShorthandCompletion(provider, "d",
            "def()", "def()"));
      return provider;
   }

	/*this function gets called in a loop to add the find and replace dialog menus*/
	private void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}

	/* function called while saving a file*/
	private void saveFile(JMenuItem savefile){
		statusBar.setLabel("Waiting");
		JFileChooser fileChoose = new JFileChooser(); //filechooser dialog
        int option = fileChoose.showSaveDialog(savefile);
        /*
             * ShowSaveDialog instead of showOpenDialog if the user clicked OK
             * (and not cancel)
         */
        if (option == JFileChooser.APPROVE_OPTION) { //if approved
            try {
                File openFile = fileChoose.getSelectedFile();
				filename=openFile.getName();
				filepath=openFile.getPath();
				tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),"    "+filepath+"      "); //change the title
                BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
                out.write(textArea.getText()); //write contents of textarea into the file
                out.close();
				statusBar.setLabel("Ready");
            } catch (Exception ex) { // again, catch any exceptions and...
                // ...write to the debug console
                System.err.println(ex.getMessage());
            }
        }
	}


	private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
		JMenu menu;//menu inside menubar
		JMenuItem openfile=new JMenuItem("Open File");
        openfile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		//set shortcut tooltip
		openfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			statusBar.setLabel("Waiting");
            JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to  browse files to open)
            int option = open.showOpenDialog(openfile); // get the option that the user selected (approve or cancel)
            if (option == JFileChooser.APPROVE_OPTION) {
                // FEdit.clear(textArea); // clear the TextArea before applying the file contents
                try {
                    File openFile = open.getSelectedFile();
					filename=openFile.getName();
					filepath=openFile.getPath();
					textArea=inittextarea();//new textarea object
					tabbedPane.addTab("   "+filepath+"      ", null,new RTextScrollPane(textArea),"blank");
					Scanner scan = new Scanner(new FileReader(filepath));
                    while (scan.hasNext()) {
						textArea.setText(textArea.getText()+"\n"+scan.nextLine());
                    }
					statusBar.setLabel("Ready");
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
			statusBar.setLabel("Waiting");
             Object[] options = {"Save", "Return"};
                int n = JOptionPane.showOptionDialog(newfile, "Do you want to save the file at first ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);//show dialog
                if (n == 0) {
                    saveFile(newfile);//save
                } else if (n == 1) {
                    textArea.setText(""); //clear textarea
                }
				statusBar.setLabel("Ready");
    	}
		});

		JMenuItem savefile=new JMenuItem("Save As");
		 savefile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		savefile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			saveFile(savefile);
		}
		});

		JMenuItem sfile=new JMenuItem("Save"); //simple Ctrl + S
		 sfile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		sfile.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			statusBar.setLabel("Waiting");
			  try {
                    FileWriter myWriter = new FileWriter(filepath);
      				myWriter.write(textArea.getText()); //gettext and write into file
      				myWriter.close();
					statusBar.setLabel("Ready");
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
		}
		});

		JMenuItem xexit=new JMenuItem("Exit"); //quit
		 xexit.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		xexit.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			 System.exit(0); //quit program
		}
		});

		JMenuItem nw=new JMenuItem("New Window"); //for a new window, just run the runnning command again
		nw.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		nw.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			statusBar.setLabel("Waiting");
			try{
			if(System.getProperty("os.name").equals("Linux")){ //for linux and windows
				Process process = Runtime.getRuntime().exec("java -cp '.:lib/jars/*' RSTAUIDemoApp .");//command to run in linux
			}
			else{
				Process process= Runtime.getRuntime().exec("java -cp \".;lib/jars/*\" RSTAUIDemoApp .");//command to run in windows
			}
			statusBar.setLabel("Ready");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		});

		JMenuItem openfolder=new JMenuItem("Open Folder");//open another folder in a new window 
		 openfolder.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		openfolder.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			statusBar.setLabel("Waiting");
			JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(openfolder);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();//selected file from the dialog 
			   String ar[]={file.getPath()};//get its path
			   statusBar.setLabel("Ready");
			   main(ar);//run main with the folderpath
            }
		}
		});


        menu = new JMenu("  File  ");
		/*Add all the above options to a menu*/
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
 	      	Robot robot=new Robot();/* Robot simulates keypress*/
    		robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_Z);
			robot.keyRelease(KeyEvent.VK_Z);/*Ctrl + Z*/
    		robot.keyRelease(KeyEvent.VK_CONTROL);
} catch (Exception e) {
        e.printStackTrace();
}
		}
		});

        menu.add(undo); //Add undo option to menu

		JMenuItem redo=new JMenuItem("Redo");
		 redo.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		redo.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
      		 Robot robot=new Robot();

    robot.keyPress(KeyEvent.VK_CONTROL);
    		robot.keyPress(KeyEvent.VK_Y);
			robot.keyRelease(KeyEvent.VK_Y);/*Ctrl + Y*/
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
    		robot.keyPress(KeyEvent.VK_X);/*Ctrl + X*/
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
    		robot.keyPress(KeyEvent.VK_C);/*Ctrl + C*/
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
    		robot.keyPress(KeyEvent.VK_V);/*Ctrl + V*/
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
		/*Change look and feel depending on different XMl StyleSheets*/
		for (String inf : info) {
			JRadioButtonMenuItem rbMenuItem= new JRadioButtonMenuItem(inf);
			b.add(rbMenuItem);
			rbMenuItem.setMnemonic(KeyEvent.VK_R);
			rbMenuItem.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			try {	
				Theme theme = Theme.load(getClass().getResourceAsStream("/lib/styles/"+inf+".xml"));
				/*load XML*/
            	theme.apply(textArea);//apply theme to textarea
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
		menu.add(new JMenuItem(new ShowFindDialogAction()));//Add default dialogactionbutton
		menu.add(new JMenuItem(new ShowReplaceDialogAction())); //Add default replace dialog
		menu.add(new JMenuItem(new GoToLineAction())); //Add default gotoline option
		mb.add(menu);

        menu = new JMenu("  Run  ");
		JMenuItem t=new JMenuItem("New Terminal"); //Open the terminal in a new JFrame
		 t.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		t.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
			statusBar.setLabel("Waiting");
			try{
			Process process = Runtime.getRuntime().exec("java TestTerminal");
			/*opens Terminal*/
		statusBar.setLabel("Ready");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		});

		
        menu.add(t);
		mb.add(menu);

        menu = new JMenu("  Help  ");
       JMenuItem doc=new JMenuItem("Documentation");
	   //README.md
		doc.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
		 try {
                   if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    Desktop.getDesktop().browse(new URI("https://github.com/nimishbongale/code-editor/blob/master/README.md"));
}
		//opens mentioned URL in default browser
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
		}
		});
        menu.add(doc);

		JMenuItem lic=new JMenuItem("License");
		//LICENSE
		lic.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
		 try {
                   if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    Desktop.getDesktop().browse(new URI("https://github.com/nimishbongale/code-editor/blob/master/LICENSE"));
}
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
		}
		});

        menu.add(lic);
        menu.addSeparator();
       	JMenuItem about=new JMenuItem("About");
		//ABOUT
		about.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent ev) {
		 try {
                   if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    Desktop.getDesktop().browse(new URI("http://n45editor.surge.sh"));
}
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
		}
		});

        menu.add(about);
		mb.add(menu);
		return mb;
	}
	 
	private void initSearchDialogs() {

		findDialog = new FindDialog(this, this);
		replaceDialog = new ReplaceDialog(this, this);

		// This ties the properties of the two dialogs together (match case,
		// regex, etc.).
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);
	}


	/**
	 * Listens for events from our search dialogs
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

	/*Driver Code*/
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

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
		String selectedPath = tree.getSelectionPath().toString();
		String ans[]=selectedPath.split(",", 0); //to get relative path
		String pathy="";
		if(System.getProperty("os.name").equals("Linux"))
		pathy=ans[ans.length-2].toString()+"/"+(ans[ans.length-1].toString().substring(0,ans[ans.length-1].length()-1)).trim();
    else
	pathy=ans[ans.length-2].toString()+"\\"+(ans[ans.length-1].toString().substring(0,ans[ans.length-1].length()-1)).trim();
	//to handle cases in different OS
		 try {
			 		textArea=inittextarea();//inittextarea returns a RSyntaxTextArea object
					 String pathe=pathy.substring(1,pathy.length());
					Scanner scan = new Scanner(new FileReader(pathe));
                    while (scan.hasNext()) {
						textArea.setText(textArea.getText()+"\n"+scan.nextLine());//Read and write
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

  /** 
  Add nodes from under "dir" into curTop. Highly recursive. 
  Carry out DFS to get complete directory structure
  */
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
  }//Adjust according to requirement
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
			int line = dialog.getLineNumber();//defualt searches in textarea
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
			this.info = info;//for getting info from the radio buttons
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
     * The status bar for this application
     */
	private static class StatusBar extends JPanel {
		public JLabel label;
		StatusBar() {
			label = new JLabel("Ready");
			setLayout(new BorderLayout());
			add(label, BorderLayout.LINE_START);
			add(new JLabel(new SizeGripIcon()), BorderLayout.LINE_END);
		}

		void setLabel(String l) {
			label.setText(l);
		}
	}
}