package core;

import java.awt.Color;

public class Colors {
	private static int rgb[] = new int[3];
	private static float hsb[][] = new float[3][3];
	
	private static final float HueRange = 0.06f;
	private static final float SatRange = 0.15f;
	private static final float BrightnessRange = 0.2f;
	
	private Colors() {
	}
	
	public static void init() {
		rgb[0] = Color.RED.getRGB();
		rgb[1] = Color.GREEN.getRGB();
		rgb[2] = Color.BLUE.getRGB();
		calculateHSB(rgb, hsb);
	}
	
	public static boolean verifyColor(int color, int i) {
		float[] hsbColor = new float[3];
		calculateHSB(color, hsbColor);
		return verifyColor(hsbColor, i);
	}
	
	public static boolean verifyColor(float[] color, int i) {
		if (Math.abs(color[2] - hsb[i][2]) > BrightnessRange) {
			return false;
		}
		if (Math.abs(color[1] - hsb[i][1]) > SatRange) {
			return false;
		}
		float hueDiff = (color[0] - hsb[i][0] + 0.5f);
		if (hueDiff < 0.0f) {
			hueDiff += 0.5f;
		}
		else if (hueDiff > 1.0f) {
			hueDiff -= 1.5f;
		}
		else {
			hueDiff -= 0.5f;
		}
		if (Math.abs(hueDiff) > HueRange) {
			return false;
		}
		return true;
	}
	
	private static void calculateHSB(int[] rgb, float[][] hsb) {
		for (int i = 0; i < 3; ++i) {
			calculateHSB(rgb[i], hsb[i]);
		}
	}
	
	public static void calculateHSB(int rgb, float[] hsb) {
		Color.RGBtoHSB((rgb&0x00FF0000)>>16, (rgb&0x0000FF00)>>8, (rgb&0x000000FF), hsb);
	}
	
	private static void calculateRGB() {
		for (int i = 0; i < 3; ++i) {
			rgb[i] = Color.getHSBColor(hsb[i][0], hsb[i][1], hsb[i][2]).getRGB();
		}
	}
	
	public static int[] getRGB() {
		int[] out = new int[3];
		for (int i = 0; i < 3; ++i) {
			out[i] = rgb[i];
		}
		return out;
	}
	
	public static int getRGB(int i) {
		return rgb[i];
	}
	
	public static float[][] getHSB() {
		float[][] out = new float[3][3];
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				out[i][j] = hsb[i][j];
			}
		}
		return out;
	}
	
	public static float[] getHSB(int i) {
		float[] out = new float[3];
		for (int j = 0; j < 3; ++j) {
			out[j] = hsb[i][j];
		}
		return out;
	}
	
	public static void setRGB(int newrgb, int i) {
		rgb[i] = newrgb;
		calculateHSB(rgb[i], hsb[i]);
	}
	
	public static void setRGB(int r, int g, int b, int i) {
		rgb[i] = (new Color(r,g,b)).getRGB();
		calculateHSB(rgb[i], hsb[i]);
	}
	
	public static void setRGB(int[] newrgb) {
		for (int i = 0; i < 3; ++i) {
			rgb[i] = newrgb[i];
		}
		calculateHSB(rgb, hsb);
	}
	
}
