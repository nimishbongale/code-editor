import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Console extends WindowAdapter implements WindowListener,  ActionListener, Runnable
{
    private JFrame frame;
    private JTextArea textArea;    private Thread reader;
    private Thread reader2;
2    private final PipedInputStream pin2=new PipedInputStream(); 
26:
27: Thread errorThrower; // just for testing (Throws an Exception at this Console
28:     
29:     public Console()
30:     {
31:             // create all components and add them
32:             frame=new JFrame("Java Console");
33:             Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
34:             Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
35:             int x=(int)(frameSize.width/2);
36:             int y=(int)(frameSize.height/2);
37:             frame.setBounds(x,y,frameSize.width,frameSize.height);
38:             
39:             textArea=new JTextArea();
40:             textArea.setEditable(false);
41:             JButton button=new JButton("clear");
42:             
43:             frame.getContentPane().setLayout(new BorderLayout());
44:             frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
45:             frame.getContentPane().add(button,BorderLayout.SOUTH);
46:             frame.setVisible(true);         
47:             
48:             frame.addWindowListener(this);          
49:             button.addActionListener(this);
50:             
51:             try
52:             {
53:                     PipedOutputStream pout=new PipedOutputStream(this.pin);
54:                     System.setOut(new PrintStream(pout,true)); 
55:             } 
56:             catch (java.io.IOException io)
57:             {
58:                     textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
59:             }
60:             catch (SecurityException se)
61:             {
62:                     textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
63:             } 
64:             
65:             try 
66:             {
67:                     PipedOutputStream pout2=new PipedOutputStream(this.pin2);
68:                     System.setErr(new PrintStream(pout2,true));
69:             } 
70:             catch (java.io.IOException io)
71:             {
72:                     textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
73:             }
74:             catch (SecurityException se)
75:             {
76:                     textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
77:             }               
78:                     
79:             quit=false; // signals the Threads that they should exit
80:                             
81:             // Starting two separate threads to read from the PipedInputStreams                             
82:             //
83:             reader=new Thread(this);
84:             reader.setDaemon(true); 
85:             reader.start(); 
86:             //
87:             reader2=new Thread(this);       
88:             reader2.setDaemon(true);        
89:             reader2.start();
90:                             
91:             // testing part
92:             // you may omit this part for your application
93:             // 
94:             System.out.println("Hello World 2");
95:             System.out.println("All fonts available to Graphic2D:\n");
96:             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
97:             String[] fontNames=ge.getAvailableFontFamilyNames();
98:             for(int n=0;n<fontNames.length;n++)  System.out.println(fontNames[n]);               
99:             // Testing part: simple an error thrown anywhere in this JVM will be printed on the Console
100:            // We do it with a seperate Thread becasue we don't wan't to break a Thread used by the Console.
101:
102:            System.out.println("\nLets throw an error on this console");  
103:            errorThrower=new Thread(this);
104:            errorThrower.setDaemon(true);
105:            errorThrower.start();
106:    }
107:    
108:    public synchronized void windowClosed(WindowEvent evt)
109:    {
110:            quit=true;
111:            this.notifyAll(); // stop all threads
112:            try { reader.join(1000);pin.close();   } catch (Exception e){}          
113:            try { reader2.join(1000);pin2.close(); } catch (Exception e){}
114:                System.exit(0);
115:    }               
116:    public synchronized void windowClosing(WindowEvent evt)
117:    {
118:            frame.setVisible(false); // default behaviour of JFrame
119:                frame.dispose();
120:    }
121:    
122:    public synchronized void actionPerformed(ActionEvent evt)
123:    {
124:            textArea.setText("");
125:    }
126:
127:    public synchronized void run()
128:    {
129:            try
129:            {                       
130:                    while (Thread.currentThread()==reader)
131:                    {
132:                            try { this.wait(100);}catch(InterruptedException ie) {}
133:                            if (pin.available()!=0)
134:                            {
135:                                    String input=this.readLine(pin);
136:                                    textArea.append(input);
137:                            }
138:                            if (quit) return;
139:                    }
140:            
141:                    while (Thread.currentThread()==reader2)
142:                    {
143:                            try { this.wait(100);}catch(InterruptedException ie) {}
144:                            if (pin2.available()!=0)
145:                            {
146:                                    String input=this.readLine(pin2);
147:                                    textArea.append(input);
148:                            }
149:                            if (quit) return;
150:                    }                       
151:            } catch (Exception e)
152:            {
153:                    textArea.append("\nConsole reports an Internal error.");
154:                    textArea.append("The error is: "+e);                  
155:            }
156:            
157:            // just for testing (Throw a Nullpointer after 1 second)
158:            if (Thread.currentThread()==errorThrower)
159:            {
160:                    try { this.wait(1000); }catch(InterruptedException ie){}
161:                    throw new NullPointerException("Application test: throwing an NullPointerException It should arrive at the console");
162:            }
163:
164:    }
165:    
166:    public synchronized String readLine(PipedInputStream in) throws IOException
167:    {
168:            String input="";
169:            do
170:            {
171:                    int available=in.available();
172:                    if (available==0) break;
173:                    byte b[]=new byte[available];
174:                    in.read(b);
175:                    input=input+new String(b,0,b.length);                                                                                   
176:            }while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !qur>it);
177:            return input;
178:    }       
179:            
180:        public static void main(String[] arg)
181:    {
182:            new Console(); // create console with not reference     
183:    }                        
184:}
