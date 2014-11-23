package gui;


import core.Frame;
import core.FrameBuffer;

import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Toolkit;

public class MouseHandler implements Runnable {
	private static MouseHandler instance = null;
	private static int WIDTH;
	private static int HEIGHT;
	private Robot robot = null;
	FrameBuffer buffer = null;
	Frame frame = null;
	private boolean active = false;
	
	private MouseHandler() {
		//numPixels = WIDTH * HEIGHT;
		buffer = FrameBuffer.getInstance();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		WIDTH = (int)screenSize.getWidth();
		HEIGHT = (int)screenSize.getHeight();
	}
	
	public static MouseHandler getInstance() {
		if (instance == null) {
			instance = new MouseHandler();
		}
		return instance;
	}
	
	public void run() {
		while (active) {
			while (!buffer.hasNext()) {
				try {
					Thread.sleep(20);
				}
				catch (InterruptedException e) {
					continue;
				}
			}
			frame = buffer.popNext();
			doStuff();
		}
	}
	
	private void doStuff() {
		try {
			double[] center = frame.getCenter();
			robot.mouseMove((int)(center[0]*WIDTH), (int)(center[1]*HEIGHT));
		}
		catch (Exception e) {
			System.out.println("Couldn't initialize Robot");
			System.out.println(e.getMessage());
		}
	}
	
	public void setActive(boolean set) {
		active = set;
		if (active) {
			try {
				robot = new Robot();
			}
			catch (Exception e) {
				System.out.println("Couldn't initialize Robot");
				System.out.println(e.getMessage());
			}
		}
		else {
			robot = null;
		}
	}
}