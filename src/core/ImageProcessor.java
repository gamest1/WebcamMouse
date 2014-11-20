package core;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.Graphics;


public class ImageProcessor {
	BufferedImage image = null;
	double[][] coord = null;
	double[] size = null;
	static Frame lastFrame = new Frame(new double[][]{{0.5,0.5,0.5},{0.5,0.5,0.5},{0.5,0.5,0.5}},new double[]{0,0,0},0, new BufferedImage(50,50,BufferedImage.TYPE_INT_RGB)); // default frame, all dots centered, no size, timestamp is 0
	
	public ImageProcessor() {
		image = Camera.getInstance().getImage();
		if (image == null) {
			System.out.println("No image in Image Processor, why?");
			System.exit(1);
		}
		if (Camera.getInstance().getFlip()) {
			int w = image.getWidth();
			int h = image.getHeight();
			BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics g = copy.getGraphics(); 
			int sx1, sx2, sy1, sy2; // source rectangle coordinates
			int dx1, dx2, dy1, dy2; // destination rectangle coordinates
			dx1 = 0;
			dy1 = 0;
			dx2 = w;
			dy2 = h;

			sx1 = w;
			sy1 = 0;
			sx2 = 0;
			sy2 = h;
			
			g.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			image = copy;
		}
		FrameBuffer buffer = FrameBuffer.getInstance();
		long timestamp = System.nanoTime();
		coord = new double[3][2];
		size = new double[3];
		getFrameData();
		lastFrame = new Frame(coord, size, timestamp, image); // update lastFrame
		buffer.add(lastFrame);
	}
	
	private void getFrameData() {
		int[] dimensions = {image.getWidth(),image.getHeight()};
		int pixels = dimensions[0]*dimensions[1];
		
		//calling algorithm here
		int[][] intCoord = extractCoords();

		
		for (int i = 0; i < 3; ++i) {
			size[i] = (double)intCoord[i][2] / (double)pixels;
			for (int j = 0; j < 2; ++j) {
				coord[i][j] = (double)intCoord[i][j] / (double)dimensions[j];
			}
		}
	}
	
	private int[][] extractCoords() {
		int[] dimensions = {image.getWidth(),image.getHeight()};
		int[][] intCoord = new int[3][3];			//[1..3] -> red,green,blue | [][0] - x, [][1] - y, [][2] - size in total number of pixels of the blob
		
		// algorithm to figure out values for intCoord and sizePixels below
		// can use lastFrame to draw upon information of the last Frame to make calculations more efficient
		// for example start searching for dots outward beginning at the center of gravity of the dots in the last frame, ect...
		// also it's possible that the image might have to mirrored, although it might be inconsistent between webcams
		// will likely have to add a button in the final product, not something to worry about just now
		
		
		// following is just random values:
		Random rnd = new Random();
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 2; ++j) {
				//intCoord[i][j] = rnd.nextInt(dimensions[j]);
				intCoord[i][j] = 0;
			}
			intCoord[i][2] = 100;
		}
		
		// end of random values
		
		// sample algorithm to track a certain shade of blue
		int rgb, hit=0;
		float[] hsl;
		float[] tmp = Colors.getInstance().getHSB(0);
		double upper = tmp[0] * 1.1;
		double lower = tmp[0] * 0.9;
		int minHit = 8;
		for (int i = 1; i < dimensions[0] - 1; i = i + 2) {
			for (int j = 1; j < dimensions[1] - 1; j = j + 2) {
				rgb = image.getRGB(i,j);
				hsl = RGBtoHSL(rgb);
				hit = 0;
				//System.out.println(hsl[0]);
				if (hsl[0] < upper && hsl[0] > lower && hsl[1] > 0.8 && hsl[2] > 0.2) {
					for (int a = 0; a < 3; ++a) {
						for (int b = 0; b < 3; ++b) {
							rgb = image.getRGB(i+a-1,j+b-1);
							hsl = RGBtoHSL(rgb);
							if (hsl[0] < upper && hsl[0] > lower && hsl[1] > 0.8) {
								++hit;
							}
						}
					}
					//System.out.println(hit);
				}
				if (hit > minHit) {
					intCoord[2][0] = i;
					intCoord[2][1] = j;
					intCoord[1][0] = i;
					intCoord[1][1] = j;
					intCoord[0][0] = i;
					intCoord[0][1] = j;
					break;
				}
			}
			if (hit > minHit) { 
				break;
			}
		}
		
		// end of algorithm, return values
		
		return intCoord;
	}
	
	public float[] RGBtoHSL(int rgb) {
		float[] hsb = new float[3];
		Colors.calculateHSB(rgb, hsb);
		return hsb;
		/*int max,min;
		double r,g,b, h,s,l;
		double[] RGB = new double[3];
		r = ((rgb&0x00FF0000)>>16) / 255.0;
		g = ((rgb&0x0000FF00)>>8) / 255.0;
		b = ((rgb&0x000000FF)) / 255.0;
		RGB[0] = r;
		RGB[1] = g;
		RGB[2] = b;
		if (r > g && r > b) {
			max = 0;
			if (g > b) {
				min = 2;
			}
			else {
				min = 1;
			}
		}
		else if (g > r && g > b) {
			max = 1;
			if (r > b) {
				min = 2;
			}
			else {
				min = 0;
			}
		}
		else if (b > r && b > g) {
			max = 2;
			if (r > g) {
				min = 1;
			}
			else {
				min = 0;
			}
		}
		else {
			max = 1;
			min = 1;
			l = (RGB[max] + RGB[min]) / 2;
			s = 0;
			h = 0;
			return new double[] {h,s,l};
		}
		l = (RGB[max] + RGB[min]) / 2;
		if (l < 0.5) {
			s = (RGB[max] - RGB[min]) / (RGB[max] + RGB[min]);
		}
		else {
			s = (RGB[max] - RGB[min]) / (2.0 - RGB[max] - RGB[min]);
		}
		
		if (max == 0) {
			h = (RGB[1] - RGB[2]) / (RGB[max] - RGB[min]);
		}
		else if (max == 1) {
			h = 2.0 + (RGB[2] - RGB[0]) / (RGB[max] - RGB[min]);
		}
		else {
			h = 4.0 + (RGB[0] - RGB[1]) / (RGB[max] - RGB[min]);
		}
		return new double[] {h,s,l};*/
	}
}
