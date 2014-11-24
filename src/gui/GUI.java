package gui;

import javax.swing.*;

import core.Camera;
import core.Colors;
import core.ImageProcessor;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;


public class GUI extends JFrame implements ActionListener {
	private static GUI instance = null;
	private JPanel mainPane = null;
	private JPanel buttonPane = null;
	private JPanel colorsPane = null;
	private JPanel debugPane = null;
	private FramePainter painter = null;
	private MouseHandler mouse = null;
	private ColorPanel colorPane = null;
	private Thread t1 = null;
    
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
            	// key pressed
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                // key released
            	//System.out.println(e.getKeyCode());
            	if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                	removePainter();
                	removeMouseHandler();
            	}
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
                // key typed
            }
            return false;
        }
    }
    
	private GUI() {
		super("WebcamMouse");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPaneSetup();
		buttonPane.add(Box.createRigidArea(new Dimension(0,0)));
		this.add(mainPane);
		this.pack();
		this.setVisible(true);
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
	}
	
	private void mainPaneSetup() {
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		
		JPanel upperPane = new JPanel();
		upperPane.setLayout(new BoxLayout(upperPane, BoxLayout.X_AXIS));
		
		buttonPaneSetup();
		upperPane.add(buttonPane);
		colorPane = ColorPanel.getInstance();
		upperPane.add(colorPane);
		
		mainPane.add(upperPane);
		
	}
	
	private void addPainter() {
		painter = FramePainter.getInstance();
		painter.setActive(true);
		painter.setMode(1);
		Thread t1 = new Thread(painter);
		t1.start();
		
		this.remove(mainPane);
		mainPane.add(painter);
		this.add(mainPane);
		this.pack();
		this.setVisible(true);
	}
	
	private void removePainter() {
		if (t1 != null) {
			t1 = null;;
		}
		if (painter == null) {
			return;
		}
		painter.setActive(false);
		this.remove(mainPane);
		mainPane.remove(painter);
		this.add(mainPane);
		this.pack();
		this.setVisible(true);
		painter = null;
	}
	
	private void addMouseHandler() {
		mouse = MouseHandler.getInstance();
		mouse.setActive(true);
		Thread t1 = new Thread(mouse);
		t1.start();
	}
	
	private void removeMouseHandler() {
		if (t1 != null) {
			t1 = null;;
		}
		if (mouse == null) {
			return;
		}
		mouse.setActive(false);
		mouse = null;
	}
	
	private void calibrate() {
		painter = FramePainter.getInstance();
		painter.setActive(true);
		painter.setMode(2);
		Thread t1 = new Thread(painter);
		t1.start();
		
		calibrationDialog("Please move the color of the Thumb into the center of the circle");
		calibrateColor(0);
		calibrationDialog("Please move the color of the Index Finger into the center of the circle");
		calibrateColor(1);
		calibrationDialog("Please move the color of the Middle Finger into the center of the circle");
		calibrateColor(2);
		
		colorPane.repaint();
		painter.setActive(false);
		painter = null;
	}
	
	private void calibrationDialog(String str) {
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(str);
        panel.add(label);
        panel.add(painter);
		JOptionPane.showMessageDialog(null, panel);
	}
	
	private void calibrateColor(int k) {
		BufferedImage image = Camera.getInstance().getImage();
		int[] rgb = {0, 0, 0};
		int color;
		int x = image.getWidth() / 2;
		int y = image.getHeight() / 2;
		for (int i = -2; i <= 2; ++i) {
			for (int j = -2; j <= 2; ++j) {
				color = image.getRGB(x + i, y + j);
				rgb[0] += (color&0x00FF0000)>>16;
				rgb[1] += (color&0x0000FF00)>>8;
				rgb[2] += (color&0x000000FF);
			}
		}
		rgb[0] /=25;
		rgb[1] /=25;
		rgb[2] /=25;
		Colors.setRGB(rgb[0], rgb[1], rgb[2], k);
	}
	
	private void buttonPaneSetup() {
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		
		JButton button1 = new JButton("Calibrate");
		button1.setVerticalTextPosition(AbstractButton.CENTER);
		button1.setHorizontalTextPosition(AbstractButton.CENTER);
		button1.setActionCommand("calibrate");
		button1.addActionListener(this);
		button1.setToolTipText("Calibrate colors");
		buttonPane.add(button1);
		
		JButton button2 = new JButton("Debug Mode");
		button2.setVerticalTextPosition(AbstractButton.CENTER);
		button2.setHorizontalTextPosition(AbstractButton.CENTER);
		button2.setActionCommand("debug");
		button2.addActionListener(this);
		button2.setToolTipText("Shows the images and indicates where it detects the fingers.");
		buttonPane.add(button2);
		
		JButton button3 = new JButton("Normal Mode");
		button3.setVerticalTextPosition(AbstractButton.CENTER);
		button3.setHorizontalTextPosition(AbstractButton.CENTER);
		button3.setActionCommand("normal");
		button3.addActionListener(this);
		button3.setToolTipText("Normal Mode");
		buttonPane.add(button3);
		
		JButton button4 = new JButton("Flip");
		button4.setVerticalTextPosition(AbstractButton.CENTER);
		button4.setHorizontalTextPosition(AbstractButton.CENTER);
		button4.setActionCommand("flip");
		button4.addActionListener(this);
		button4.setToolTipText("Flip Image Horizontally");
		buttonPane.add(button4);
		
		buttonPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("calibrate".equals(e.getActionCommand())) {
			removePainter();
			removeMouseHandler();
			calibrate();
		}
		else if ("debug".equals(e.getActionCommand())) {
			removeMouseHandler();
			addPainter();
		}
		else if ("normal".equals(e.getActionCommand())) {
			removePainter();
			addMouseHandler();
		}
		else if ("flip".equals(e.getActionCommand())) {
			flipImage();
		}
	}
	
	private void flipImage() {
		Camera.getInstance().toggleFlip();
	}
	
	public static GUI getInstance() {
		if (instance==null) {
			instance = new GUI();
		}
		return instance;
	}
}
