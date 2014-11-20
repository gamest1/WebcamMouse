package core;

import java.awt.Color;

public class Colors {
	private static Colors instance = null;
	private int rgb[] = new int[3];
	private float hsb[][] = new float[3][3];
	
	private Colors() {
		rgb[0] = Color.RED.getRGB();
		rgb[1] = Color.GREEN.getRGB();
		rgb[2] = Color.BLUE.getRGB();
		calculateHSB(rgb, hsb);
	}
	
	public static Colors getInstance() {
		if (instance == null) {
			instance = new Colors();
		}
		return instance;
	}
	
	private static void calculateHSB(int[] rgb, float[][] hsb) {
		for (int i = 0; i < 3; ++i) {
			calculateHSB(rgb[i], hsb[i]);
		}
	}
	
	public static void calculateHSB(int rgb, float[] hsb) {
		Color.RGBtoHSB((rgb&0x00FF0000)>>16, (rgb&0x0000FF00)>>8, (rgb&0x000000FF), hsb);
	}
	
	private void calculateRGB() {
		for (int i = 0; i < 3; ++i) {
			rgb[i] = Color.getHSBColor(hsb[i][0], hsb[i][1], hsb[i][2]).getRGB();
		}
	}
	
	public int[] getRGB() {
		int[] out = new int[3];
		for (int i = 0; i < 3; ++i) {
			out[i] = rgb[i];
		}
		return out;
	}
	
	public int getRGB(int i) {
		return rgb[i];
	}
	
	public float[][] getHSB() {
		float[][] out = new float[3][3];
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				out[i][j] = hsb[i][j];
			}
		}
		return out;
	}
	
	public float[] getHSB(int i) {
		float[] out = new float[3];
		for (int j = 0; j < 3; ++j) {
			out[j] = hsb[i][j];
		}
		return out;
	}
	
	public void setRGB(int newrgb, int i) {
		rgb[i] = newrgb;
		calculateHSB(rgb[i], hsb[i]);
	}
	
	public void setRGB(int r, int g, int b, int i) {
		rgb[i] = (new Color(r,g,b)).getRGB();
		calculateHSB(rgb[i], hsb[i]);
	}
	
	public void setRGB(int[] newrgb) {
		for (int i = 0; i < 3; ++i) {
			rgb[i] = newrgb[i];
		}
		calculateHSB(rgb, hsb);
	}
	
}
