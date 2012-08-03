/*
SSMV (Super Simple MPO Viewer) - Copyright (c) 2012 Hendrik Iben - hendrik [dot] iben <at> googlemail [dot] com

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package ssmv;

import java.awt.Dimension;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

public class SSMV implements Runnable {

	private static Preferences prefs = Preferences.userNodeForPackage(SSMV.class);
	
	public static enum StereoMode { Cross, Anaglyph, Wiggle };
	
	public static final String SCross = "Cross";
	public static final String SWiggle = "Wiggle";
	public static final String SAnaglyph = "Anaglyph";
	
	public static String smToString(StereoMode sm) {
		switch(sm) {
		case Anaglyph:
			return SAnaglyph;
		case Wiggle:
			return SWiggle;
		default:
			return SCross;
		}
	}
	
	public static StereoMode stringToSM(String s) {
		if(SAnaglyph.toLowerCase().equals(s.toLowerCase()))
			return StereoMode.Anaglyph;
		if(SWiggle.toLowerCase().equals(s.toLowerCase()))
			return StereoMode.Wiggle;
		
		return StereoMode.Cross;
	}
	
	public static final String prefHGap = "hgap";
	public static final String prefHBorder = "hborder";
	public static final String prefVBorder = "vborder";

	public static final String prefSwap = "swap";
	public static final String prefHelpPoints = "helppoints";

	public static final String prefMode = "mode";
	
	public static final String prefWiggleDelay = "wiggledelay";
	
	public static final String prefAnaglyphMask = "anaglyphmask";
	
	public static final int prefHGapDefault = 10;
	public static final int prefHBorderDefault = 10;
	public static final int prefVBorderDefault = 10;

	public static final int prefWiggleDelayDefault = 80;

	public static final int prefAnaglyphMaskDefault = 0;
	
	public static final boolean prefSwapDefault = false;
	public static final boolean prefHelpPointsDefault = true;
	
	public static final String prefModeDefault = SCross; 
	
	private static final String acAbout = "about";
	private static final String acOpen = "open";
	private static final String acExit = "exit";
	private static final String acSwap = "swap";
	private static final String acHGap = "hgap";
	private static final String acHelpPoints = "helppoints";
	private static final String acVBorder = "vborder";
	private static final String acHBorder = "hborder";
	private static final String acSaveLeft = "saveleft";
	private static final String acSaveRight = "saveright";
	private static final String acSaveAnaglyph = "saveanaglyph";
	private static final String acCross = "modecross";
	private static final String acAnaglyph = "modeanaglyph";
	private static final String acAnaglyphMask = "modeanaglyphmask";
	private static final String acWiggle = "modewiggle";
	private static final String acWiggleDelay = "wiggledelay";
	
	private static final int [] anaglyphMasks = {
		0xFF0000, // red
		0x00FF00, // green
		0x0000FF // blue
	};
	
	private static final String [] anaglyphMaskNames = {
		"red/cyan",
		"green/magenta",
		"blue/yellow"
	};
	
	private static final String dAMIndex = "amindex";
	
	private static final String about =
			"SSMV - Super Simple MPO Viewer v1.0\n" +
			"Copyright (c) 2012 Hendrik Iben - hendrik [dot] iben <at> googlemail [dot] com\n" +
			"\n" +
			"This simple tool can be used to view stereographic images in the MPO format that is used " +
			"by several cameras and Nintendo's 3DS system\n" +
			"I wrote it because I could not quicky find a tool to view these images in Linux (at least none that worked)\n" +
			"\n" +
			"BACKGROUND\n" +
			"MPO files are really just two JPEG images that have been saved into one file. The first one is for the left eye " +
			"while the second one is meant to be viewed with the right eye to perceive a three-dimensional effect.\n" +
			"The most cost effective way of viewing these images is by applying the cross-eyed or wall-eyed technique on the two " +
			"images displayed next to each other. I personally find cross-eyed viewing easier but this application supports also " +
			"wall-eyed viewing if you select 'Swap' from the 'Image' menu.\n" +
			"\n" +
			"If you need help aligning your eyes, helper points can be displayed above the image (default setting). Sometimes it is easier " +
			"to concentrate just on the dots instead of the image.\n" +
			"\n" +
			"In addition two viewing the images side by side, the application also supports 'wiggle' stereo, where the two images " +
			"are quickly swapped. The effect depends strongly on the image...\n" +
			"Your last option is viewing the images in anaglyph form where the left and right images are color-filtered. If you happen " +
			"to have on of these red/cyan glasses lying around this might be worth a try (but this mode implies bad colors...).\n" +
			"You can also choose between the three possible filter combinations or us swap to switch sides...\n" +
			"\n" +
			"LICENSE (MIT)\n" +
			"\n" +
			"Permission is hereby granted, free of charge, to any person obtaining a copy of " +
			"this software and associated documentation files (the \"Software\"), to deal in " +
			"the Software without restriction, including without limitation the rights to " +
			"use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies " +
			"of the Software, and to permit persons to whom the Software is furnished to do " +
			"so, subject to the following conditions:\n" +
			"\n " +
			"The above copyright notice and this permission notice shall be included in all " +
			"copies or substantial portions of the Software.\n" +
			"\n" +
			"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR " +
			"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
			"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE " +
			"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER " +
			"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, " +
			"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE " +
			"SOFTWARE."
			;
	
	private JFrame frame;
	private JMenuBar menuBar;
	private StereoPanel stereoPanel;
	
	private BufferedImage leftEyeImage = null;
	private BufferedImage rightEyeImage = null;
	
	private boolean swap = prefs.getBoolean(prefSwap, prefSwapDefault);
	
	private JFileChooser openChooser;
	private JFileChooser saveChooser;
	
	private boolean isSwap() {
		return swap;
	}
	
	private void setSwap(boolean swap) {
		this.swap = swap;
	}
	
	private BufferedImage getLeft() {
		return swap ? rightEyeImage : leftEyeImage;
	}

	private BufferedImage getRight() {
		return swap ? leftEyeImage : rightEyeImage;
	}
	
	private boolean validImage() {
		return leftEyeImage != null;
	}
	
	private WindowAdapter windowListener = new WindowAdapter() {

		@Override
		public void windowClosing(WindowEvent e) {
			if(aboutWindow!=null)
				aboutWindow.dispose();
			frame.dispose();
		}
	};
	
	private static boolean isJPEGHeader(byte [] data, int offset) {
		if(offset < 0)
			return false;
		
		if( (offset+3) >= data.length)
			return false;
		
		return (data[offset] == (byte)0xFF) && (data[offset+1] == (byte)0xD8);
	}
	
	private void loadMPO(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte [] buffer = new byte [1024];
		int r;
		
		while( (r = is.read(buffer)) > 0) {
			baos.write(buffer, 0, r);
		}
		
		byte [] imageData = baos.toByteArray();
		
		if(!isJPEGHeader(imageData, 0))
			throw new IOException("File does not contain JPEG data!");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
		int size = imageData.length;
		
		BufferedImage fi = ImageIO.read(bais);
		if(fi == null)
			throw new IOException("No image found!");
		int firstSize = size - bais.available();
		
		int secondOffset = firstSize;
		
		while( (secondOffset > 0) && !isJPEGHeader(imageData, secondOffset) )
			secondOffset--;

		if(secondOffset==0) {
			secondOffset = firstSize+1;
			while((secondOffset < imageData.length) && !isJPEGHeader(imageData, secondOffset))
				secondOffset++;
		}
		
		if(secondOffset >= imageData.length)
			throw new IOException("No second image found!");
		
		bais.reset();
		bais.skip(secondOffset);
		
		BufferedImage si = ImageIO.read(bais);
		if(si == null)
			throw new IOException("File only contains one image...");
		
		if(fi.getWidth() != si.getWidth() || fi.getHeight() != si.getHeight())
			throw new IOException("The two images differ in size!");
		
		leftEyeImage = fi;
		rightEyeImage = si;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				adjustImageAreaInFrame();
			}
		});
	}
	
	private void saveImage(BufferedImage bi, String dialogTitle) {
		if(saveChooser == null) {
			if(openChooser == null) {
				saveChooser = new JFileChooser();
			} else {
				saveChooser = new JFileChooser(openChooser.getCurrentDirectory());
			}
		}
		saveChooser.setDialogTitle(dialogTitle);
		boolean canLeave = false;
		while(!canLeave) {
			canLeave = true;
			if(saveChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File f = saveChooser.getSelectedFile();

				String ext = getExt(f.getName());

				if(ext.length()==0) {
					canLeave = false;
					JOptionPane.showMessageDialog(frame, "You need to specify a file extension (jpg, png, bmp) to save the image!", "No Extension!", JOptionPane.ERROR_MESSAGE);
					continue;
				}

				if(f.exists()) {
					if(JOptionPane.showConfirmDialog(frame, "File '" + f.getName() + "' already exists. Overwrite ?") != JOptionPane.OK_OPTION) {
						canLeave = false;
						continue;
					}
				}

				try {
					FileOutputStream fos = new FileOutputStream(f);
					if(!ImageIO.write(bi, ext, fos)) {
						canLeave = false;
						JOptionPane.showMessageDialog(frame, "Extension '" + ext + "' is not supported!", "Unknown format!", JOptionPane.ERROR_MESSAGE);
						continue;
					}
				} catch(Exception e) {
					JOptionPane.showMessageDialog(frame, "There was an error while saving the image....", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private AbstractAction fileAction = new AbstractAction("File") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(acOpen.equals(e.getActionCommand())) {
				if(openChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						FileInputStream fis = new FileInputStream(openChooser.getSelectedFile());
						loadMPO(fis);
						frame.setTitle("SSMV - " + openChooser.getSelectedFile().getName());
					} catch (FileNotFoundException fnfe) {
						JOptionPane.showMessageDialog(frame, "The file you selected was not found...", "File not found!", JOptionPane.ERROR_MESSAGE);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(frame, "There was an error while reading the file...\n" + ioe.getMessage(), "Read-Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if(acExit.equals(e.getActionCommand())) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
			if(acSaveLeft.equals(e.getActionCommand())) {
				if(validImage()) {
					saveImage(getRight(), "Save image on the left as..."); // right eye image is on the left
				}
			}
			if(acSaveRight.equals(e.getActionCommand())) {
				if(validImage()) {
					saveImage(getLeft(), "Save image on the right as...");  // left eye image is on the right
				}
			}
			if(acSaveAnaglyph.equals(e.getActionCommand())) {
				if(validImage()) {
					saveImage(stereoPanel.getAnaglyphImage(), "Save anaglyph image..."); 
				}
			}
		}
	};
	
	private AbstractAction aboutAction = new AbstractAction("About") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			showAboutWindow();
		}
	};
	
	private Integer getNumber(String message, int initial, String error) {
		String newgaps;
		while( (newgaps = JOptionPane.showInputDialog(frame, message, ""+initial) ) != null ) {
			try {
				int newgap = Integer.parseInt(newgaps.trim());
				if(newgap < 0)
					throw new NumberFormatException();

				return newgap;
			} catch(NumberFormatException nfe) {
				JOptionPane.showMessageDialog(frame, error + "\nPlease enter a natural number >= 0", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}
	
	private AbstractAction imageAction = new AbstractAction("Image") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(acSwap.equals(e.getActionCommand())) {
				JCheckBoxMenuItem jcbi = (JCheckBoxMenuItem)e.getSource();
				setSwap(jcbi.isSelected());
				prefs.putBoolean(prefSwap, isSwap());
				stereoPanel.repaint();
			}
			if(acCross.equals(e.getActionCommand())) {
				stereoPanel.setStereoMode(StereoMode.Cross);
				prefs.put(prefMode, smToString(stereoPanel.getStereoMode()));
				stereoPanel.repaint();
			}
			if(acWiggle.equals(e.getActionCommand())) {
				stereoPanel.setStereoMode(StereoMode.Wiggle);
				prefs.put(prefMode, smToString(stereoPanel.getStereoMode()));
				stereoPanel.repaint();
			}
			if(acAnaglyph.equals(e.getActionCommand())) {
				stereoPanel.setStereoMode(StereoMode.Anaglyph);
				prefs.put(prefMode, smToString(stereoPanel.getStereoMode()));
				stereoPanel.repaint();
			}
			if(acAnaglyphMask.equals(e.getActionCommand())) {
				JMenuItem jmi = (JMenuItem)e.getSource();
				Integer amindex = (Integer)jmi.getClientProperty(dAMIndex);
				if(amindex==null)
					amindex = 0;
				
				stereoPanel.setAnanglyphMaskIndex(amindex);
				prefs.putInt(prefAnaglyphMask, stereoPanel.getAnaglyphyMaskIndex());
				stereoPanel.repaint();
			}
			if(acHelpPoints.equals(e.getActionCommand())) {
				JCheckBoxMenuItem jcbi = (JCheckBoxMenuItem)e.getSource();
				stereoPanel.setHelpPoints(jcbi.isSelected());
				prefs.putBoolean(prefHelpPoints, stereoPanel.isHelpPoints());
			}
			if(acHGap.equals(e.getActionCommand())) {
				int hgap = stereoPanel.getHGap();
				
				Integer newgap = getNumber("New value for horizontal gap (pixel)", hgap, "Invalid horizontal gap!");
				
				if(newgap!=null) {
						stereoPanel.setHGap(newgap);
						prefs.putInt(prefHGap, newgap);
				}
			}
			if(acHBorder.equals(e.getActionCommand())) {
				int hborder = stereoPanel.getHBorder();
				
				Integer newborder = getNumber("New value for horizontal border (pixel)", hborder, "Invalid horizontal border!");
				
				if(newborder!=null) {
					stereoPanel.setHBorder(newborder);
					prefs.putInt(prefHBorder, newborder);
				}
			}
			if(acVBorder.equals(e.getActionCommand())) {
				int vborder = stereoPanel.getVBorder();
				
				Integer newborder = getNumber("New value for vertical border (pixel)", vborder, "Invalid vertical border!");
				
				if(newborder!=null) {
					stereoPanel.setVBorder(newborder);
					prefs.putInt(prefVBorder, newborder);
				}
			}
			if(acWiggleDelay.equals(e.getActionCommand())) {
				int wiggledelay = stereoPanel.getWiggleDelay();
				
				Integer newdelay = getNumber("New value for wiggle delay (ms)", wiggledelay, "Invalid delay!");
				
				if(newdelay!=null) {
					stereoPanel.setWiggleDelay(newdelay);
					prefs.putInt(prefWiggleDelay, newdelay);
				}
			}
		}		
	};
	
	public static JMenuItem setACAndText(JMenuItem jmi, String actionCommand, String text, Character mnemonic) {
		jmi.setActionCommand(actionCommand);
		jmi.setText(text);
		if(mnemonic!=null)
			jmi.setMnemonic(mnemonic);
		return jmi;
	}
	
	public static JMenuItem setSelected(JMenuItem jmi, boolean selected) {
		jmi.setSelected(selected);
		return jmi;
	}
	
	public static JMenuItem withKeyStroke(JMenuItem jmi, KeyStroke ks) {
		jmi.setAccelerator(ks);
		return jmi;
	}
	
	private void adjustImageAreaInFrame() {
		if(stereoPanel!=null)
			stereoPanel.invalidate();
		if( (frame.getExtendedState()&Frame.MAXIMIZED_BOTH) == 0 ) {
			frame.pack();
			if(stereoPanel!=null)
				stereoPanel.repaint();
		} else {
			// and here comes the joy of spaghetti code... (happens in constructor)
			if(stereoPanel!=null)
				stereoPanel.repaint();
		}
	}
	
	private static boolean hasTransparency(BufferedImage bi) {
		return bi.getTransparency() != BufferedImage.OPAQUE;
	}
	
	public static BufferedImage createAnaglyphImage(BufferedImage left, BufferedImage right, int leftMask, BufferedImage dst) {
		int w = left.getWidth();
		int h = left.getHeight();

		if(dst == null || dst.getWidth() != w || dst.getHeight() != h || hasTransparency(left) != hasTransparency(dst)) {
			dst = new BufferedImage(w, h, hasTransparency(left) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		}
		
		int [] rgbdatal = new int [w * h];
		left.getRGB(0, 0, w, h, rgbdatal, 0, w);
		int [] rgbdatar = new int [w * h];
		right.getRGB(0, 0, w, h, rgbdatar, 0, w);
		
		int lmask = ((leftMask & 0x00FFFFFF) | 0xFF000000);
		int rmask = ((~leftMask) & 0x00FFFFFF);
		
		for(int i=0; i<rgbdatal.length; i++) {
			rgbdatal[i] = (rgbdatal[i] & lmask) | (rgbdatar[i] & rmask);
		}
		
		dst.setRGB(0, 0, w, h, rgbdatal, 0, w);
		
		return dst;
	}
	
	public class StereoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private StereoMode mode = null;

		private int wiggleDelay = prefs.getInt(prefWiggleDelay, prefWiggleDelayDefault);
		private boolean wiggleTurn = false;

		private int hborder = prefs.getInt(prefHBorder, prefHBorderDefault);
		private int vborder = prefs.getInt(prefVBorder, prefVBorderDefault);
		
		private int hgap = prefs.getInt(prefHGap, prefHGapDefault);
		
		private boolean helpPoints = prefs.getBoolean(prefHelpPoints, prefHelpPointsDefault);
		
		private BufferedImage anaglyphSourceLeft = null;
		private BufferedImage anaglyphSourceRight = null;
		private BufferedImage anaglyph = null;
		
		private int usedIndex = -1;
		private int anaglyphMaskIndex;
		
		public StereoPanel() {
			setStereoMode(stringToSM(prefs.get(prefMode, prefModeDefault)));
			setAnanglyphMaskIndex(prefs.getInt(prefAnaglyphMask, prefAnaglyphMaskDefault));
		}
		
		public BufferedImage getAnaglyphImage() {
			if(!validImage())
				return null;
			
			if(anaglyph == null || anaglyphSourceLeft != getLeft() || usedIndex != anaglyphMaskIndex) {
				anaglyphSourceLeft = getLeft();
				anaglyphSourceRight = getRight();
				anaglyph = createAnaglyphImage(anaglyphSourceLeft, anaglyphSourceRight, anaglyphMasks[anaglyphMaskIndex], anaglyph);
			}
			return anaglyph;
		}
		
		public BufferedImage getWiggleImage() {
			if(!validImage())
				return null;
			
			return wiggleTurn ? getRight() : getLeft();
		}
		
		private class WiggleThread extends Thread {
			private boolean keepRunning = true;
			
			public WiggleThread() {
				setDaemon(true);
				start();
			}
			
			public void endWiggle() {
				keepRunning = false;
			}
			
			public void run() {
				while(keepRunning) {
					try {
						sleep(wiggleDelay);
					} catch (InterruptedException e) {
					}
					
					if(mode==StereoMode.Wiggle) {
						if(validImage()) {
							wiggleTurn = !wiggleTurn;
							repaint();
						}
					} else {
						break;
					}
				}
			}
		};
		
		private WiggleThread wiggleThread = null;
		
		public void setHGap(int hgap) {
			if(this.hgap == hgap)
				return;
			
			if(hgap < 0)
				hgap = 0;
			
			this.hgap = hgap;
			adjustImageAreaInFrame();
		}
		
		public void setHBorder(int hborder) {
			if(this.hborder == hborder)
				return;
			
			if(hborder < 0)
				hborder = 0;
			
			this.hborder = hborder;
			adjustImageAreaInFrame();
		}

		public void setVBorder(int vborder) {
			if(this.vborder == vborder)
				return;
			
			if(vborder < 0)
				vborder = 0;
			
			this.vborder = vborder;
			adjustImageAreaInFrame();
		}
	
		public int getHGap() {
			return hgap;
		}
		
		public int getVBorder() {
			return vborder;
		}
		
		public int getHBorder() {
			return hborder;
		}
		
		public boolean isHelpPoints() {
			return helpPoints;
		}
		
		public StereoMode getStereoMode() {
			return mode;
		}
		
		public void setStereoMode(StereoMode sm) {
			if(mode == sm)
				return;
			
			mode = sm;
			
			if(mode == StereoMode.Wiggle) {
				if(wiggleThread!=null) {
					wiggleThread.endWiggle();
				}

				wiggleThread = new WiggleThread();
			} else {
				if(wiggleThread!=null) {
					wiggleThread.endWiggle();
					wiggleThread = null;
				}
			}
			
			adjustImageAreaInFrame();
		}
		
		public void setAnanglyphMaskIndex(int ami) {
			if(ami == anaglyphMaskIndex)
				return;
			
			if(ami < 0 || ami >= anaglyphMasks.length)
				ami = 0;
			anaglyphMaskIndex = ami;
			
			repaint();
		}
		
		public int getAnaglyphyMaskIndex() {
			return anaglyphMaskIndex;
		}
		
		public void setHelpPoints(boolean helpPoints) {
			if(this.helpPoints == helpPoints)
				return;
			
			this.helpPoints = helpPoints;
			repaint();
		}
		
		public int getWiggleDelay() {
			return wiggleDelay;
		}
		
		public void setWiggleDelay(int wd) {
			wiggleDelay = wd;
		}
		
		public Dimension getMinimumSize() {
			if(!validImage())
				return super.getMinimumSize();

			switch(mode) {
			case Anaglyph:
			case Wiggle:
				return new Dimension(getLeft().getWidth() + 2 * hborder, getLeft().getHeight() + 2 * vborder);
			default:
				return new Dimension(getLeft().getWidth()*2 + hgap + 2 * hborder, getLeft().getHeight() + 2 * vborder);
			}
		}
		
		public Dimension getPreferredSize() {
			if(!validImage())
				return super.getPreferredSize();
			
			return getMinimumSize();
		}
		
		public Dimension getMaximumSize() {
			if(!validImage())
				return super.getMaximumSize();
			
			return getMinimumSize();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			if(!validImage()) {
				super.paintComponent(g);
				return;
			}
			
			Graphics2D g2d = (Graphics2D)g;
			int w = getWidth();
			int h = getHeight();
			
			int iw = getLeft().getWidth();
			int ih = getLeft().getHeight();
			
			g2d.setColor(getBackground());
			g2d.fillRect(0,0,w,h);
			
			int delta_h;
			int delta_v = (h - (ih + 2 * vborder)) / 2;;
			
			switch(mode) {
			case Anaglyph:
				delta_h = (w - (iw + 2 * hborder)) / 2;
				
				g2d.drawImage(getAnaglyphImage(), null, delta_h + hborder, delta_v + vborder);
				break;
			case Wiggle:
				delta_h = (w - (iw + 2 * hborder)) / 2;
				g2d.drawImage(getWiggleImage(), null, delta_h + hborder, delta_v + vborder);
				break;
			default:
				delta_h = (w - (iw*2 + hgap + 2 * hborder)) / 2;

				if(helpPoints) {
					int hpsize = vborder - 4;
					g2d.setColor(getForeground());
					int hpx = delta_h + (iw - hpsize) / 2;
					g2d.fillArc(hpx+1, delta_v+1, hpsize, hpsize, 0, 360);
					hpx = delta_h + iw + hgap + (iw - hpsize) / 2;
					g2d.fillArc(hpx+1, delta_v+1, hpsize, hpsize, 0, 360);

				}

				g2d.drawImage(getRight(), null, delta_h + hborder, delta_v + vborder);
				g2d.drawImage(getLeft(), null, delta_h + hborder + iw + hgap, delta_v + vborder);
			}
		}
	}
	
	public static String getExt(String filename) {
		int idx = filename.lastIndexOf('.');
		if(idx<=0)
			return "";
		return filename.substring(idx+1);
	}
	
	public void run() {
		openChooser = new JFileChooser();
		
		openChooser.setAcceptAllFileFilterUsed(true);
		openChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "MPO Stereo JPEG Container (.MPO)";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
					return true;
				return getExt(f.getName()).toLowerCase().equals("mpo");
			}
		});
		
		GraphicsDevice gd = MouseInfo.getPointerInfo().getDevice();
		frame = new JFrame("SSMV", gd.getDefaultConfiguration());
		
		frame.setLocationByPlatform(true);
		frame.setJMenuBar(menuBar = new JMenuBar());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(windowListener);
		
		frame.add(stereoPanel = new StereoPanel());
		
		JMenu fileMenu = new JMenu("File");
		
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(fileAction), acOpen, "Open...", 'O'), KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK)));
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(fileAction), acSaveLeft, "Save left image...", 'L'), KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK)));
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(fileAction), acSaveRight, "Save right image...", 'R'), KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK)));
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(fileAction), acSaveAnaglyph, "Save anaglyph image...", 'Y'), KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK)));
		fileMenu.addSeparator();
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(aboutAction), acAbout, "About...", 'A'), KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)));
		fileMenu.addSeparator();
		fileMenu.add(withKeyStroke(setACAndText(new JMenuItem(fileAction), acExit, "Exit", 'X'), KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK)));
		
		JMenu imageMenu = new JMenu("Image");
		
		imageMenu.add(withKeyStroke(setSelected(setACAndText(new JCheckBoxMenuItem(imageAction), acSwap, "Swap", 'S'), prefs.getBoolean(prefSwap, prefSwapDefault)), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK)));
		imageMenu.add(withKeyStroke(setSelected(setACAndText(new JCheckBoxMenuItem(imageAction), acHelpPoints, "Help-Points", 'P'), prefs.getBoolean(prefHelpPoints, prefHelpPointsDefault)), KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK)));
		imageMenu.add(withKeyStroke(setACAndText(new JMenuItem(imageAction), acWiggleDelay, "Wiggle Delay...", 'D'), KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK)));

		JMenuItem modeSub = new JMenu("Mode");
		ButtonGroup bg = new ButtonGroup();
		
		JMenuItem miCross = withKeyStroke(setACAndText(new JRadioButtonMenuItem(imageAction), acCross, "Cross-Eyed", 'C'), KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));;
		JMenuItem miAnaglyph = withKeyStroke(setACAndText(new JRadioButtonMenuItem(imageAction), acAnaglyph, "Anaglyph", 'A'), KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		JMenuItem miWiggle = withKeyStroke(setACAndText(new JRadioButtonMenuItem(imageAction), acWiggle, "Wiggle", 'W'), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
		
		bg.add(miCross);
		bg.add(miAnaglyph);
		bg.add(miWiggle);
		
		switch(stereoPanel.getStereoMode()) {
		case Cross:
			miCross.setSelected(true);
			break;
		case Anaglyph:
			miAnaglyph.setSelected(true);
			break;
		case Wiggle:
			miWiggle.setSelected(true);
			break;
		}
		
		modeSub.add(miCross);
		modeSub.add(miAnaglyph);
		modeSub.add(miWiggle);
		
		imageMenu.add(modeSub);
		
		JMenuItem anaglyphMaskSub = new JMenu("Anaglyph Mask");
		ButtonGroup ambg = new ButtonGroup();
		
		int amset = stereoPanel.getAnaglyphyMaskIndex();
		for(int i=0; i<anaglyphMasks.length; i++) {
			JMenuItem mimask = setACAndText(new JRadioButtonMenuItem(imageAction), acAnaglyphMask, anaglyphMaskNames[i], null);
			mimask.putClientProperty(dAMIndex, Integer.valueOf(i));
			ambg.add(mimask);
			if(amset == i)
				mimask.setSelected(true);
			anaglyphMaskSub.add(mimask);
		}

		imageMenu.add(anaglyphMaskSub);
		
		imageMenu.add(setACAndText(new JMenuItem(imageAction), acHGap, "Horz. Gap...", 'G'));
		imageMenu.add(setACAndText(new JMenuItem(imageAction), acHBorder, "Horz. Border...", 'H'));
		imageMenu.add(setACAndText(new JMenuItem(imageAction), acVBorder, "Vert. Border...", 'V'));
		
		menuBar.add(fileMenu);
		menuBar.add(imageMenu);
		
		frame.setSize(640, 480);
		frame.setVisible(true);
	}
	
	private JFrame aboutWindow;
	public void showAboutWindow() {
		if(aboutWindow == null) {
			aboutWindow = new JFrame("About SSMV...", frame.getGraphicsConfiguration());
			aboutWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			aboutWindow.setLocationByPlatform(true);
			
			JTextArea aboutText = new JTextArea(15, 64);
			aboutText.setLineWrap(true);
			aboutText.setText(about);
			aboutText.setCaretPosition(0);
			aboutText.setEditable(false);
			aboutWindow.add(new JScrollPane(aboutText));
			aboutWindow.pack();
		}
		
		aboutWindow.setVisible(true);
	}
	
	public static void main(String...args) {
		EventQueue.invokeLater(new SSMV());
	}
}
