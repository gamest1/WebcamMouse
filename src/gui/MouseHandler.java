package gui;


import core.Frame;
import core.FrameBuffer;

import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class MouseHandler implements Runnable {
	private static MouseHandler instance = null;
	private static double minDistance = 0.01;
	private static int WIDTH;
	private static int HEIGHT;
	private Robot robot = null;
	FrameBuffer buffer = null;
	Frame frame = null;
	private boolean active = false;
	
	private static int million = 1000000;
	private static int doubleThreshold = 1000;
	private long leftPressed = -1;
	private long rightPressed = -1;
	private long doubleClicked = -1;
	
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
			double zoom = 1.15;
			double[] center = frame.getCenter();
			double[][] coords = frame.getCoords();
			double[] sizes = frame.getSizes();
			robot.mouseMove((int)((zoom*(center[0]-0.5)+zoom*0.5)*WIDTH), (int)((zoom*(center[1]-0.5)+zoom*0.5)*WIDTH));
			
			double[] distances = new double[3];		// 0: thumb - index, 1: index - middle, 2: middle - thumb
			boolean[] touching = new boolean[3];
			double distx, disty;
			for (int i = 0; i < 3; ++i) {
				distx = coords[i][0] - coords[(i+1)%3][0];
				disty = coords[i][1] - coords[(i+1)%3][1];
				distances[i] = distx * distx + disty * disty - (sizes[i] + sizes[(i+1)%3]) / Math.PI;
				if (distances[i] < minDistance) {
					touching[i] = true;
				}
				else {
					touching[i] = false;
				}
			}
			
			leftClick(touching);
			leftDoubleClick(touching);
			leftRelease(touching);

			rightClick(touching);
			rightRelease(touching);
			
		}
		catch (Exception e) {
			System.out.println("Couldn't initialize Robot");
			System.out.println(e.getMessage());
		}
	}
	
	private void leftClick(boolean[] touching) {
		if (leftPressed < 0 && touching[0] && !touching[1] && !touching[2]) {
			leftPressed = System.nanoTime() / million;
			robot.mousePress(InputEvent.BUTTON1_MASK);
			System.out.println("leftClick");
		}
	}
	
	private void leftDoubleClick(boolean[] touching) {
		if (doubleClicked < 0 && touching[0] && touching[1] && !touching[2]) {
			doubleClicked = System.nanoTime() / million;
			robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(50);
			robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			System.out.println("doubleClick");
		}
	}
	
	private void leftRelease(boolean[] touching) {
		if (leftPressed > 0 && !touching[0]) {
			leftPressed = -1;
			doubleClicked = -1;
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			System.out.println("leftRelease");
		}
	}
	
	private void rightClick(boolean[] touching) {
		if (rightPressed < 0 && touching[2] && !touching[1] && !touching[0]) {
			rightPressed = System.nanoTime() / million;
			robot.mousePress(InputEvent.BUTTON3_MASK);
			System.out.println("rightClick");
		}
	}
	
	private void rightRelease(boolean[] touching) {
		if (rightPressed > 0 && !touching[2]) {
			rightPressed = -1;
			robot.mouseRelease(InputEvent.BUTTON3_MASK);
			System.out.println("rightRelease");
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