package gui;


import core.Camera;
import core.Frame;
import core.FrameBuffer;

import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class MouseHandler implements Runnable {
	private static MouseHandler instance = null;
	private static double minDistance = 0.015;
	private static double centerRadius = 0.2;
	private static double centerRadius2 = centerRadius * centerRadius;
	private static int WIDTH;
	private static int HEIGHT;
	private static int[] DIM = new int[2];
	private Robot robot = null;
	private FrameBuffer buffer = null;
	private Frame frame = new Frame(new double[][]{{0.5,0.5,0.5},{0.5,0.5,0.5},{0.5,0.5,0.5}},new double[]{0,0,0}, 0, Camera.getInstance().getImage(), Camera.getInstance().getImage());
	private Frame lastFrame = new Frame(new double[][]{{0.5,0.5,0.5},{0.5,0.5,0.5},{0.5,0.5,0.5}},new double[]{0,0,0}, 0, Camera.getInstance().getImage(), Camera.getInstance().getImage());
	private boolean active = false;
	
	private static int million = 1000000;
	private static int doubleThreshold = 1000;
	private long leftPressed = -1;
	private long rightPressed = -1;
	private long doubleClicked = -1;
	
	private double deltaT = 0.02;
	private double[] deltaX = {0, 0};
	private double[] pos = {0.5, 0.5};
	
	private boolean[] lastTouching = {false, false, false};
	
	private MouseHandler() {
		//numPixels = WIDTH * HEIGHT;
		buffer = FrameBuffer.getInstance();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		WIDTH = (int)screenSize.getWidth();
		HEIGHT = (int)screenSize.getHeight();
		DIM[0] = WIDTH;
		DIM[1] = HEIGHT;
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
				calculatePos();
				try {
					Thread.sleep(20);
				}
				catch (InterruptedException e) {
					continue;
				}
			}
			frame = buffer.popNext();
			doStuff();
			lastFrame = frame;
		}
	}
	
	private void calculatePos() {
		for (int i = 0; i < 2; ++i) {
			pos[i] += deltaX[i] * deltaT;
			if (pos[i] < 0) {
				pos[i] = 0;
			}
			else if (pos[i] >= 1) {
				pos[i] = 1;
			}
			//System.out.println(pos[0] + " | " + pos[1] + " | " + deltaX[0] + " | " + deltaX[1]);
		}
		robot.mouseMove((int)(pos[0] * WIDTH), (int)(pos[1] * HEIGHT));
	}
	
	private void calculateDeltaX() {
		double[][] coords = frame.getCoords();
		double[][] lastCoords = lastFrame.getCoords();
		if (coords[0][0] < 0) {
			deltaX[0] = 0;
			deltaX[1] = 0;
			return;
		}
		if (lastCoords[0][0] < 0) {
			lastCoords[0][0] = coords[0][0];
		}
		if (lastCoords[0][1] < 0) {
			lastCoords[0][1] = coords[0][1];
		}
		double x = (coords[0][0] + lastCoords[0][0]) / 2;
		double y = (coords[0][1] + lastCoords[0][1]) / 2;
		double dist = Math.sqrt((x - 0.5) * (x - 0.5) + (y - 0.5) * (y - 0.5));
		if (dist < centerRadius + 0.01) {
			deltaX[0] = 0;
			deltaX[1] = 0;
		}
		else {
			double coef = (dist - centerRadius) / dist;
			deltaX[0] = (x - 0.5) * coef;
			deltaX[1] = (y - 0.5) * coef;
			//System.out.println(dist + " | " + centerRadius + " | " + coef + " | " + deltaX[0] + " | " + deltaX[1]);
		}
		
	}
	
	private void doStuff() {
		try {
			calculateDeltaX();
			double[][] coords = frame.getCoords();
			double[] sizes = frame.getSizes();
			/*double zoom = 1.15;
			double[] center = frame.getCenter();
			
			double[][] lastCoords = lastFrame.getCoords();
			double x = (coords[0][0] + lastCoords[0][0]) / 2;
			double y = (coords[0][1] + lastCoords[0][1]) / 2;
			robot.mouseMove((int)((zoom*(x-0.5)+zoom*0.5)*WIDTH), (int)((zoom*(y-0.5)+zoom*0.5)*WIDTH));*/
			
			double[] distances = new double[3];		// 0: thumb - index, 1: index - middle, 2: middle - thumb
			boolean[] touching = new boolean[3];
			double distx, disty;
			for (int i = 0; i < 3; ++i) {
				if (coords[i][0] < 0 || coords[(i+1)%3][0] < 0) {
					touching[i] = lastTouching[i];
					continue;
				}
				distx = coords[i][0] - coords[(i+1)%3][0];
				disty = coords[i][1] - coords[(i+1)%3][1];
				distances[i] = distx * distx + disty * disty - (sizes[i] + sizes[(i+1)%3]) / Math.PI;
				if (distances[i] < minDistance) {
					touching[i] = true;
					lastTouching[i] = true;
				}
				else {
					touching[i] = false;
					lastTouching[i] = false;
				}
			}
			
			leftClick(touching);
			leftRelease(touching);
			
			leftDoubleClick(touching);
			leftDoubleRelease(touching);

			rightClick(touching);
			rightRelease(touching);
			
			scrolling(touching, coords);
			
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
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			System.out.println("leftClick");
		}
		long leftDown = System.nanoTime() / million - leftPressed;
		if (leftDown < 100000 && leftDown > 1000 && touching[0] && !touching[1] && !touching[2]) {
			leftPressed = 1000000;
			robot.mousePress(InputEvent.BUTTON1_MASK);
			System.out.println("leftDown");
		}
	}
	
	private void leftRelease(boolean[] touching) {
		if (leftPressed > 0 && !touching[0]) {
			leftPressed = -1;
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			System.out.println("leftRelease");
		}
	}
	
	private void leftDoubleClick(boolean[] touching) {
		if (doubleClicked < 0 && touching[0] && (touching[2] || touching[1])) {
			doubleClicked = System.nanoTime() / million;
			robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			System.out.println("doubleClick");
		}
	}
	
	private void leftDoubleRelease(boolean[] touching) {
		if (doubleClicked > 0 && (!touching[0] || (!touching[2] && !touching[1]))) {
			doubleClicked = -1;
			System.out.println("doubleRelease");
		}
	}
	
	private void rightClick(boolean[] touching) {
		if (rightPressed < 0 && touching[2] && !touching[1] && !touching[0]) {
			rightPressed = System.nanoTime() / million;
			robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON3_MASK);
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
	
	private void scrolling(boolean[] touching, double[][] coords) {
		if (coords[0][1] < 0 || coords[1][1] < 0 || coords[2][1] < 0) {
			return;
		}
		if (coords[1][1] > coords[0][1] && coords[2][1] > coords[0][1]) {
			if (touching[1]) {
				robot.mouseWheel(-1);
			}
			else {
				robot.mouseWheel(1);
			}
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