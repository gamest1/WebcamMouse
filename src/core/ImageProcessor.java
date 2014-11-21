package core;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;


public class ImageProcessor {
	private static final int minPixels = 144;
	BufferedImage image = null;
	private double[][] coord = null;
	private double[] size = null;
	static Frame lastFrame = new Frame(new double[][]{{0.5,0.5,0.5},{0.5,0.5,0.5},{0.5,0.5,0.5}},new double[]{0,0,0},0, new BufferedImage(50,50,BufferedImage.TYPE_INT_RGB)); // default frame, all dots centered, no size, timestamp is 0
	
	private float[][] colors = null;
	
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
		filterImage();
		setUpColors();
		
		FrameBuffer buffer = FrameBuffer.getInstance();
		long timestamp = System.nanoTime();
		coord = new double[3][2];
		size = new double[3];
		getFrameData();
		lastFrame = new Frame(coord, size, timestamp, image); // update lastFrame
		buffer.add(lastFrame);
	}
	
	private void filterImage() {
		Kernel kernel = new Kernel(3, 3,
		new float[] {
		1f/9f, 1f/9f, 1f/9f,
		1f/9f, 1f/9f, 1f/9f,
		1f/9f, 1f/9f, 1f/9f});
		BufferedImageOp op = new ConvolveOp(kernel);
		image = op.filter(image, null);
	}
	
	private void setUpColors() {
		//colors = Colors.getHSB();
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
		
		int[] oldCenter = new int[2];
		double[] tmpCenter = lastFrame.getCenter();
		oldCenter[0] = (int)(tmpCenter[0] * dimensions[0]);
		oldCenter[1] = (int)(tmpCenter[1] * dimensions[1]);
		Graphics g = image.getGraphics();
		g.setColor(Color.cyan);
		g.fillOval(oldCenter[0] - 10, oldCenter[1] - 10, 20 , 20);
		
		boolean[] found = {false, false, false};
		boolean done = false;
		int rgb;
		int bound, x, y;
		int maxI = Math.max(dimensions[0], dimensions[1])/4 + 4;
		int[] quarterDim = {dimensions[0]/4, dimensions[1]/4};
		for (int i = 0; i < maxI && !done; ++i) {
			for (int j = -1; j <= 1; j = j + 2) {
				//System.out.println();
				if (i < quarterDim[0] + 1) {
					bound = (Math.min(i, quarterDim[1]));
					for (int k = -bound; k <= bound; ++k) {
						//System.out.println(k);
						x = (oldCenter[0] + j*i*2 + dimensions[0])%dimensions[0];
						y = (oldCenter[1] + k*2 + dimensions[1])%dimensions[1];
						checkPixel(x, y, found, intCoord);
					}
				}
				if (i < quarterDim[1] + 4) {
					bound = (Math.min(i, quarterDim[0]));
					for (int k = -bound; k <= bound; ++k) {
						x = (oldCenter[0] + k*2 + dimensions[0])%dimensions[0];
						y = (oldCenter[1] + j*i*2 + dimensions[1])%dimensions[1];
						checkPixel(x, y, found, intCoord);
					}
				}
			}
			done = found[0] && found[1] && found[2];
		}
		
		// end of algorithm, return values
		
		return intCoord;
	}
	
	private void checkPixel(int x, int y, boolean[] found, int[][] coord) {
		int rgb = image.getRGB(x, y);
		float[] hsb = new float[3];
		Colors.calculateHSB(rgb, hsb);
		if (hsb[2] < 0.05 || hsb[2] > 0.95 || hsb[1] < 0.1) return;
		int[] limits = new int[4];
		int tmpCount;
		for (int i = 0; i < 3; ++i) {
			if (found[i]) continue;
			if (Colors.verifyColor(hsb, i)) {
				tmpCount = searchArea(x, y, i, limits);
				if (tmpCount < minPixels) continue;
				//System.out.println(tmpCount + " | " + limits[0][0] + " | " + limits[0][1] + " | " + limits[1][0] + " | " + limits[1][1]);
				coord[i][0] = (limits[0] + limits[1])/2;
				coord[i][1] = (limits[2] + limits[3])/2;
				coord[i][2] = tmpCount;
				found[i] = true;
			}
		}
	}
	
	private int searchArea(int m, int n, int h, int[] limits) {
		int count = 0;
		int tmpCount;

		int rgb;
		float[] hsb = new float[3];
		//int[] oldCenter = {m, n};
		//int[] dimensions = {1, 1};
		limits[0] = m;		// left
		limits[1] = m;		// right
		limits[2] = n;		// up
		limits[3] = n;		// down
		int[] reachedLimit = {0, 0, 0, 0}; // left right up down
		boolean done = false;
		int[] borders = new int[2];
		int x, y;
		while (!done) {
			/*for (int i = 0; i < 2; ++i) {
				dimensions[i] = limits[2*i + 1] - limits[2*i];
			}*/
			if (limits[0] <= 0) {
				reachedLimit[0] = 10;
			}
			if (limits[1] >= image.getWidth() - 1) {
				reachedLimit[1] = 10;
			}
			if (limits[2] <= 0) {
				reachedLimit[2] = 10;
			}
			if (limits[3] >= image.getHeight() - 1) {
				reachedLimit[3] = 10;
			}
			for (int i = 0; i < 4; ++i) {
				if (reachedLimit[i] >= 5) {
					continue;
				}
				if (i%2 == 0) {
					--limits[i];
				}
				else {
					++limits[i];
				}
				if (i < 2) {
					borders[0] = limits[2];
					borders[1] = limits[3];
				}
				else {
					borders[0] = limits[0];
					borders[1] = limits[1];
				}
				tmpCount = 0;
				for (int j = borders[0]; j <= borders[1]; ++j) {
					if (i < 2) {
						x = limits[i];
						y = j;
					}
					else {
						x = j;
						y = limits[i];
					}
					//System.out.println(x + " | " + y);
					rgb = image.getRGB(x, y);
					Colors.calculateHSB(rgb, hsb);
					if (hsb[2] < 0.05 || hsb[2] > 0.95 || hsb[1] < 0.1) continue;
					if (Colors.verifyColor(hsb, h)) {
						++tmpCount;
						Graphics g = image.getGraphics();
						g.setColor(Color.yellow);
						g.drawLine(x,y,x,y);
					}
				}
				if (tmpCount > 0 && tmpCount > (borders[1] - borders[0])*0.05) {
					reachedLimit[i] = 0;
				}
				if (tmpCount == 0) {
					reachedLimit[i] += 1;
				}
				count += tmpCount;
			}
			done = reachedLimit[0] >= 5 && reachedLimit[1] >= 5 && reachedLimit[2] >= 5 && reachedLimit[3] >= 5;
		}
		return count;
	}
	
	private int[][] extractCoordsOld() {
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
		float[] tmp = Colors.getHSB(0);
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
	}
}
