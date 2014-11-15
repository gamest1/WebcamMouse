package debug;

import core.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class FramePainter extends JPanel implements Runnable {
	private static FramePainter instance = null;
	private static final int WIDTH = 512;
	private static final int HEIGHT = 512;
	private static int numPixels;
	FrameBuffer buffer = null;
	JPanel panel = null;
	Frame frame = null;
	
	private FramePainter() {
		numPixels = WIDTH * HEIGHT;
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
		while (true) {
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
		double[][] coord = frame.getCoords();
		double[] size = frame.getSizes();
		int r;
		int x, y;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.BLACK);
		for (int i = 0; i < 3; ++i) {
			r = (int)Math.sqrt((size[i] * numPixels)/Math.PI);
			x = (int)(coord[i][0] * WIDTH);
			y = (int)(coord[i][1] * HEIGHT);
			g.drawOval(x - r, y - r, r*2, r*2);
		}
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }
	
	public JPanel getPanel() {
		return panel;
	}
}