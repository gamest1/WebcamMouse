package core;

import java.awt.image.BufferedImage;

public class Frame {
	private double[][] COORD = new double[3][2];		// normalized to 0 <= x <= 1; dim1: red,green,blue; dim2: x,y
	private double[] SIZE = new double[3];				// area as a fraction of image; dim1: red,green,blue
	private double[] CENTER = {0, 0};			// center of the 3 coordinates
	private long TIMESTAMP = 0;
	private BufferedImage image = null;

	
	private Frame() {
		
	}
	
	public Frame(double[][] coord, double[] size, long timestamp, BufferedImage img) {
		TIMESTAMP = timestamp;
		image = img;
		for (int i = 0; i < 3; ++i) {
			SIZE[i] = size[i];
			for (int j = 0; j < 2; ++j) {
				COORD[i][j] = coord[i][j];
				CENTER[j] += coord[i][j];
			}
		}
		CENTER[0] /= 3;
		CENTER[1] /= 3;
	}
	
	public double[][] getCoords() {
		double[][] out = new double[3][2];
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 2; ++j) {
				out[i][j] = COORD[i][j];
			}
		}
		return out;
	}
	
	public double[] getSizes() {
		double[] out = new double[3];
		for (int i = 0; i < 3; ++i) {
			out[i] = SIZE[i];
		}
		return out;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public double[] getCenter() {
		return CENTER;
	}
}
