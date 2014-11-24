package core;

import java.awt.image.BufferedImage;

public class Frame {
	private double[][] COORD = new double[3][2];		// normalized to 0 <= x <= 1; dim1: red,green,blue; dim2: x,y
	private double[] SIZE = new double[3];				// area as a fraction of image; dim1: red,green,blue
	private double[] CENTER = {0, 0};					// center of the 3 coordinates
	private boolean[] found = new boolean[3];			// whether finger[i] was found
	private boolean valid = false;						// if none of the fingers was found, then the frame is invalid
	private long TIMESTAMP = 0;
	private BufferedImage image = null;
	private BufferedImage debugImage = null;

	
	private Frame() {
		
	}
	
	public Frame(double[][] coord, double[] size, long timestamp, BufferedImage img, BufferedImage debug) {
		TIMESTAMP = timestamp;
		image = img;
		debugImage = debug;
		int counter = 0;
		for (int i = 0; i < 3; ++i) {
			SIZE[i] = size[i];
			for (int j = 0; j < 2; ++j) {
				if (coord[i][j] < 0) {
					found[i] = false;
				}
				else {
					found[i] = true;
					CENTER[j] += coord[i][j];
				}
				COORD[i][j] = coord[i][j];
			}
			if (found[i]) {
				++counter;
			}
		}
		if (counter > 0) {
			valid = true;
			CENTER[0] /= counter;
			CENTER[1] /= counter;
		}
		else {
			CENTER[0] = 0.5;
			CENTER[1] = 0.5;
		}
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
	
	public BufferedImage getDebugImage() {
		return debugImage;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public double[] getCenter() {
		return CENTER;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public boolean found(int i) {
		return found[i];
	}
}
