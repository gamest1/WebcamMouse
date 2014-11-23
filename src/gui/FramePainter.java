package gui;

import core.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class FramePainter extends JPanel implements Runnable {
	private static FramePainter instance = null;
	private static int WIDTH;
	private static int HEIGHT;
	//private static int numPixels;
	FrameBuffer buffer = null;
	JPanel panel = null;
	Frame frame = null;
	private boolean active = false;
	private int mode = 1;			// 1 = frame painter; 2 = calibration painter;
	
	private FramePainter() {
		//numPixels = WIDTH * HEIGHT;
		buffer = FrameBuffer.getInstance();
		panel = new JPanel();
	}
	
	public static FramePainter getInstance() {
		if (instance == null) {
			instance = new FramePainter();
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
			repaint();
		}
	}
	
	protected void paintComponent(Graphics g) {
		if (frame == null) {
			return;
		}
		super.paintComponent(g);
		if (mode == 1) {
			paintFrames(g);
		}
		else if (mode == 2) {
			paintCalibration(g);
		}
	}
	
	private void paintFrames(Graphics g) {
		BufferedImage img = frame.getImage();
		WIDTH = img.getWidth();
		HEIGHT = img.getHeight();
		int numPixels = WIDTH*HEIGHT;
		g.drawImage(img,0,0,WIDTH,HEIGHT,null);
		if (!frame.isValid()) {
			return;
		}
		double[][] coord = frame.getCoords();
		double[] size = frame.getSizes();
		int r;
		int x, y;
		//g.setColor(Color.WHITE);
		//g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.RED);
		for (int i = 0; i < 3; ++i) {
			if (!frame.found(i)) {
				continue;
			}
			r = (int)Math.sqrt((size[i] * numPixels)/Math.PI);
			x = (int)(coord[i][0] * WIDTH);
			y = (int)(coord[i][1] * HEIGHT);
			g.drawOval(x - r, y - r, r*2, r*2);
		}
	}
	
	private void paintCalibration(Graphics g) {
		BufferedImage img = frame.getImage();
		WIDTH = img.getWidth();
		HEIGHT = img.getHeight();
		g.drawImage(img,0,0,WIDTH,HEIGHT,null);
		int r = 20;
		int a = (int)(r*(0.5*Math.sqrt(2)));
		int x = WIDTH/2;
		int y = HEIGHT/2;
		//g.setColor(Color.WHITE);
		//g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.RED);
		g.drawOval(x - r, y - r, r*2, r*2);
		g.drawLine(x - a, y - a, x + a, y + a);
		g.drawLine(x - a, y + a, x + a, y - a);
	}
	
	public Dimension getPreferredSize() {
        return Camera.getInstance().getWebcam().getViewSize();
    }
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void setActive(boolean set) {
		active = set;
	}
	
	public void setMode(int var) {
		mode = var;
	}
}
