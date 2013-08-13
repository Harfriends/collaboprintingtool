import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;


public class Main {

	/**
	 * @param args
	 */
	
	JTextArea jTextArea;
	JScrollPane jScrollPane;
	JScrollBar vertical;
	JTextField jTextField;
	JFrame mainFrame;
	PageFormat pageFormat;
	ArrayList<File> availableFiles = new ArrayList<File>();
	JButton printButton;
	Boolean printingNow;
	
	String DATA_LOCATION = "DataFolder";
	
	public Main() {
		printingNow = false;
		pageFormat = new PageFormat();
		refreshFiles(new File(DATA_LOCATION));
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			    	loadMainFrame();
					addListeners();
			    }
			  });
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(mainFrame, e.getMessage());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(mainFrame, e.getMessage());
		}
	}

	private void addListeners() {
		mainFrame.addWindowListener( new WindowAdapter() {
		    public void windowOpened( WindowEvent e ){
		        jTextField.requestFocus();
		    }
		    public void windowActivated( WindowEvent e ){
		        jTextField.requestFocus();
		    }
		}); 

		
	}

	public static void main(String[] args) {
		new Main();
	}


	private void loadMainFrame() {
		mainFrame = new JFrame("コラボスクール　プリント印刷ツール");
	   	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
       
        ImageIcon image = new ImageIcon("logo.png");
        JLabel imageLabel = new JLabel(image);        
        
        jTextField = new JTextField(10);
        jTextField.setHorizontalAlignment(JTextField.CENTER);
        Font font = new Font("sansserif", Font.BOLD, 40);
        jTextField.setFont(font);
        jTextField.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		attemptPrint();
        	}
        });
        
        printButton = new JButton("印刷する");
        printButton.setFont(new Font("sansserif",Font.BOLD,25));
        
        printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				attemptPrint();
			}
        });     
        
        JPanel jPanel = new JPanel();
        jPanel.setBackground(new Color(221, 216, 202));
        jPanel.setLayout(new GridLayout(3,1));
        
        mainFrame.getContentPane().add( jPanel, BorderLayout.CENTER );
        
        jPanel.add(imageLabel);
        jPanel.add(jTextField);
        jPanel.add(printButton);
        mainFrame.pack();
        
        jTextField.requestFocus();
		
	}
	
	public void refreshFiles(File folder) {
		availableFiles = new ArrayList<File>();
		loadFiles(folder);
	}
	
	private void loadFiles(File folder) {
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
                loadFiles(fileEntry);
	        } else {
	            if (fileEntry.getName().indexOf(".pdf") != -1){
	            	availableFiles.add(fileEntry);
	            }
	        }
	    }
	}

	public void attemptPrint(){
		
		if (printingNow == false){
			refreshFiles(new File(DATA_LOCATION));
			String fileName = jTextField.getText() + ".pdf";
			Boolean foundBool = false;
			for (int i = 0; i < availableFiles.size(); i++){
				if(availableFiles.get(i).getName().equals(fileName)){
					executePrint(availableFiles.get(i));
					foundBool = true;
					break;
				}
			}
			if (foundBool == false){
				JOptionPane.showMessageDialog(mainFrame, "File not found!");
				jTextField.requestFocus();
				jTextField.selectAll();
			}
		}
	}
	

	protected void executePrint(final File file) {
			 jTextField.setText(file.getName().substring(0,4) + "を印刷中...");
			 printButton.setEnabled(false);
			 
			 SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					System.out.println("execute print thread");
					printingNow = true;
					FileInputStream fis = new FileInputStream(file);
					PrintPdf printPDFFile = new PrintPdf(fis, file.getPath(), pageFormat);
					printPDFFile.print();
					return null;
				}
				@Override
			    public void done() {
					jTextField.setText("");
					printButton.setEnabled(true);
					printingNow = false;
			    }
			 };
				
			 worker.execute();
	}
}
