package gui;

import javax.swing.*;

import core.ImageProcessor;
import debug.FramePainter;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class GUI extends JFrame implements ActionListener {
	private static GUI instance = null;
	private JPanel mainPane = null;
	private JPanel buttonPane = null;
	private JPanel colorsPane = null;
	private JPanel debugPane = null;
	private FramePainter painter = null;
	private Thread t1 = null;
	
	private GUI() {
		super("WebcamMouse");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPaneSetup();
		buttonPane.add(Box.createRigidArea(new Dimension(0,0)));
		this.add(mainPane);
		this.pack();
		this.setVisible(true);
	}
	
	private void mainPaneSetup() {
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		buttonPaneSetup();
		mainPane.add(buttonPane);
		
	}
	
	private void addPainter() {
		painter = FramePainter.getInstance();
		painter.setActive(true);
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
		painter.setActive(false);
		this.remove(mainPane);
		mainPane.remove(painter);
		this.add(mainPane);
		this.pack();
		this.setVisible(true);
		painter = null;
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
		
		buttonPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("calibrate".equals(e.getActionCommand())) {
		}
		else if ("debug".equals(e.getActionCommand())) {
			addPainter();
		}
		else if ("normal".equals(e.getActionCommand())) {
			removePainter();
		}
	}
	
	public static GUI getInstance() {
		if (instance==null) {
			instance = new GUI();
		}
		return instance;
	}
}
