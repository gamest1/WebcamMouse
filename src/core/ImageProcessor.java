package core;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;


public class ImageProcessor {
	private static final int minPixels = 121;
	private static final int kernel = 4;		// powers of two
	private static final int numSamples = 4;
	private static final int numSamples2 = numSamples*numSamples;
	private static final int numSamples3 = numSamples2*numSamples;
	private static final int maxGap = 5;
	private static final int motionThresholdHigh = 60;
	private static final int motionThresholdLow = 30;
	BufferedImage image = null;
	BufferedImage debugImage = null;
	BufferedImage background = null;
	private double[][] coord = null;
	private double[] size = null;
	private static Frame lastFrame = new Frame(new double[][]{{0.5,0.5,0.5},{0.5,0.5,0.5},{0.5,0.5,0.5}},new double[]{0,0,0}, 0, Camera.getInstance().getImage(), Camera.getInstance().getImage()); // default frame, all dots centered, no size, timestamp is 0
	
	private float[][] colors = null;
	
	public ImageProcessor() {
		Camera camera = Camera.getInstance();
		image = camera.getImage();
		background = camera.getBackground();
		int w = image.getWidth();
		int h = image.getHeight();
		debugImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		if (image == null) {
			System.out.println("No image in Image Processor, why?");
			System.exit(1);
		}
		filterImage();
		setUpColors();
		Graphics g = debugImage.getGraphics();
		g.drawImage(image, 0, 0, w, h, 0, 0, w, h, null);
		
		FrameBuffer buffer = FrameBuffer.getInstance();
		long timestamp = System.nanoTime();
		coord = new double[3][2];
		size = new double[3];
		getFrameData();
		lastFrame = new Frame(coord, size, timestamp, image, debugImage); // update lastFrame
		buffer.add(lastFrame);
	}
	
	private void filterImage() {
		Kernel kernel = new Kernel(5, 5,
		new float[] {
		1f/25f, 1f/25f, 1f/25f, 1f/25f, 1f/25f,
		1f/25f, 1f/25f, 1f/25f, 1f/25f, 1f/25f,
		1f/25f, 1f/25f, 1f/25f, 1f/25f, 1f/25f,
		1f/25f, 1f/25f, 1f/25f, 1f/25f, 1f/25f,
		1f/25f, 1f/25f, 1f/25f, 1f/25f, 1f/25f});
		/*new float[] {
		1f/9f, 1f/9f, 1f/9f,
		1f/9f, 1f/9f, 1f/9f,
		1f/9f, 1f/9f, 1f/9f});*/
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
		int[][] intCoord = new int[3][3];						// [1..3] -> red,green,blue | [][0] - x, [][1] - y, [][2] - size in total number of pixels of the blob
		int[][][] intCoordArr = new int[3][4][numSamples];		// array of coordinates, for first numSamples blobs found.
		/*for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				intCoord[i][j] = -1;
			}
		}*/
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				for (int k = 0; k < numSamples; ++k) {
					intCoordArr[i][j][k] = -1000;
				}
			}
			for (int k = 0; k < numSamples; ++k) {
				intCoordArr[i][3][k] = 1000000;
			}
		}
		
		// algorithm to figure out values for intCoord and sizePixels below
		// can use lastFrame to draw upon information of the last Frame to make calculations more efficient
		// for example start searching for dots outward beginning at the center of gravity of the dots in the last frame, ect...
		// also it's possible that the image might have to mirrored, although it might be inconsistent between webcams
		// will likely have to add a button in the final product, not something to worry about just now
		
		int[] oldCenter = new int[2];
		double[] tmpCenter = lastFrame.getCenter();
		oldCenter[0] = (int)(tmpCenter[0] * dimensions[0]);
		oldCenter[1] = (int)(tmpCenter[1] * dimensions[1]);
		// draw previous center of gravity
		Graphics g = debugImage.getGraphics();
		g.setColor(Color.cyan);
		g.fillOval(oldCenter[0] - 10, oldCenter[1] - 10, 20 , 20);
		
		boolean[] found = {false, false, false};
		boolean done = false;
		int bound, x, y;
		int maxI = Math.max(dimensions[0], dimensions[1])/(kernel * 2) + (kernel * 2);
		int[] quarterDim = {dimensions[0]/(kernel * 2), dimensions[1]/(kernel * 2)};
		for (int i = 0; i < maxI && !done; ++i) {
			for (int j = -1; j <= 1; j = j + 2) {
				//System.out.println();
				if (i < quarterDim[0] + 1) {
					bound = (Math.min(i, quarterDim[1]));
					for (int k = -bound; k <= bound; ++k) {
						//System.out.println(k);
						x = (oldCenter[0] + j*i*kernel + dimensions[0])%dimensions[0];
						y = (oldCenter[1] + k*kernel + dimensions[1])%dimensions[1];
						checkPixel(x, y, found, intCoordArr);
					}
				}
				if (i < quarterDim[1] + 1) {
					bound = (Math.min(i, quarterDim[0]));
					for (int k = -bound; k <= bound; ++k) {
						x = (oldCenter[0] + k*kernel + dimensions[0])%dimensions[0];
						y = (oldCenter[1] + j*i*kernel + dimensions[1])%dimensions[1];
						checkPixel(x, y, found, intCoordArr);
					}
				}
			}
			done = found[0] && found[1] && found[2];
		}
		
		/*int[] tmpDist = new int[3];
		int[][] dist = new int[numSamples3][4];
		int[] index = new int[3];
		int distx, disty, tmp;
		int[] center = new int[2];
		for (int i = 0; i < numSamples; ++i) {
			for (int j = 0; j < numSamples; ++j) {
				for (int k = 0; k < numSamples; ++k) {
					index[0] = i;
					index[1] = j;
					index[2] = k;
					if (intCoordArr[0][0][index[0]] < 0 || intCoordArr[1][0][index[1]] < 0|| intCoordArr[2][0][index[2]] < 0) {
						dist[i*numSamples2+j*numSamples+k][0] = i;
						dist[i*numSamples2+j*numSamples+k][1] = j;
						dist[i*numSamples2+j*numSamples+k][2] = k;
						dist[i*numSamples2+j*numSamples+k][3] = 100000000;
						continue;
					}
					center[0] = 0;
					center[1] = 0;
					for (int m = 0; m < 3; ++m) {
						center[0] += intCoordArr[m][0][index[m]];
						center[1] += intCoordArr[m][1][index[m]];
					}
					center[0] /= 3;
					center[1] /= 3;
					for (int m = 0; m < 3; ++m) {
						distx = intCoordArr[m][0][index[m]] - center[0];
						disty = intCoordArr[m][1][index[m]] - center[1];
						tmpDist[m] = distx * distx + disty * disty;
					}
					// check if one of the circles is inside another and if yes, increase distances by a lot so they don't get considered
					for (int a = 0; a < 3; ++a) { 
						for (int b = 1; b < 3; ++b) {
							distx = intCoordArr[a][0][index[a]] - intCoordArr[b][0][index[b]];
							disty = intCoordArr[a][1][index[a]] - intCoordArr[b][0][index[b]];
							tmp = distx * distx + disty * disty;
							if (tmp < intCoordArr[a][2][index[a]]/Math.PI || tmp < intCoordArr[b][2][index[b]]/Math.PI) {
								tmpDist[0] = 100000;
								tmpDist[1] = 100000;
								tmpDist[2] = 100000;
							}
						}
					}
					dist[i*numSamples2+j*numSamples+k][0] = i;
					dist[i*numSamples2+j*numSamples+k][1] = j;
					dist[i*numSamples2+j*numSamples+k][2] = k;
					dist[i*numSamples2+j*numSamples+k][3] = tmpDist[0] + tmpDist[1] + tmpDist[2];
				}
			}
		}
		
		int min = 0;
		for (int i = 1; i < numSamples3; ++i) {
			if (dist[i][3] < dist[min][3]) {
				min = i;
			}
		}
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				intCoord[i][j] = intCoordArr[i][j][dist[min][i]];
			}
		}*/
		
		
		int[] min = {0, 0, 0};
		for (int i = 0; i < 3; ++i) {
			for (int j = 1; j < numSamples; ++j) {
				if (intCoordArr[i][3][min[i]] > intCoordArr[i][3][j]) {
					min[i] = j;
				}
			}
		}
		//System.out.println(min[0] + " | " + min[1] + " | " + min[2]);
		//System.out.println(intCoordArr[0][3][min[0]] + " | " + intCoordArr[1][3][min[1]] + " | " + intCoordArr[2][3][min[2]]);
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				intCoord[i][j] = intCoordArr[i][j][min[i]];
			}
		}
		
		// end of algorithm, return values
		
		return intCoord;
	}
	
	private void checkPixel(int x, int y, boolean[] found, int[][][] coord) {
		//int oldrgb = lastFrame.getImage().getRGB(x, y);
		int oldrgb = background.getRGB(x, y);
		int rgb = image.getRGB(x, y);
		if (!Colors.verifyDifference(rgb, oldrgb, motionThresholdHigh)) {
			// draw black dot on background pixel
			Graphics g = debugImage.getGraphics();
			g.setColor(Color.black);
			g.drawLine(x,y,x,y);
			return;
		}
		
		/*if (oldrgb != Color.white.getRGB()) {
			Graphics gl = lastFrame.getImage().getGraphics();
			gl.setColor(Color.white);
			gl.fillOval(x-10, y-10, 20, 20);
		}*/
		
		int index = -1;
		//float[] hsb = new float[3];
		//Colors.calculateHSB(rgb, hsb);
		//if (hsb[2] < 0.05 || hsb[2] > 0.95 || hsb[1] < 0.1) return;
		int[] limits = new int[4];
		int[] tmpCount;
		int dist;
		//int i = Colors.getClosestColor(hsb);
		int i = checkColorNarrow(x, y);
		if (i < 0 || found[i]) return;
		boolean tooClose = false;
		for (int j = 0; j < numSamples; ++j) {
			if (coord[i][0][j] < 0) {
				index = j;
				break;
			}
			else {
				dist = (x - coord[i][0][j]) * (x - coord[i][0][j]) + (y - coord[i][1][j]) * (y - coord[i][1][j]) - coord[i][2][j]*3;
				if (dist < 0) {
					tooClose = true;
					Graphics g = debugImage.getGraphics();
					g.setColor(Color.yellow);
					g.drawLine(x,y,x,y);
				}
			}
		}
		if (tooClose) {
			return;
		}
		if (index < 0) {
			found[i] = true;
			return;
		}
		tmpCount = searchArea(x, y, i, limits);
		if (tmpCount[0] < minPixels) return;
		// draw magenta square for starting pixel of valid blob
		Graphics g = debugImage.getGraphics();
		g.setColor(Color.magenta);
		g.fillRect(x-2,y-2,4,4);
		//g.fillRect(x-(int)(Math.sqrt(tmpCount)*0.5),y-(int)(Math.sqrt(tmpCount)*0.5),(int)Math.sqrt(tmpCount),(int)Math.sqrt(tmpCount));
		
		//System.out.println(tmpCount + " | " + limits[0][0] + " | " + limits[0][1] + " | " + limits[1][0] + " | " + limits[1][1]);
		coord[i][0][index] = (limits[0] + limits[1])/2;
		coord[i][1][index] = (limits[2] + limits[3])/2;
		coord[i][2][index] = tmpCount[0];
		coord[i][3][index] = tmpCount[1];
		//found[i] = true;
	}
	
	private int[] searchArea(int m, int n, int h, int[] limits) {
		int count = 0;
		int tmpCount;
		double colorDifference = 0;
		double tmpDifference;
		int rgb;
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
				if (reachedLimit[i] >= maxGap) {
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
				tmpDifference = 0;
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
					//rgb = image.getRGB(x, y);
					//if (Colors.verifyColorWide(hsb, h)) {
					if (checkColorWide(x, y, h)) {
						rgb = image.getRGB(x, y);
						++tmpCount;
						tmpDifference += Colors.getColorDifference(rgb, h);
					}
				}
				if (tmpCount > 0 && tmpCount > (borders[1] - borders[0])*0.05) {
					reachedLimit[i] = 0;
				}
				if (tmpCount == 0) {
					reachedLimit[i] += 1;
				}
				count += tmpCount;
				colorDifference += tmpDifference;
			}
			done = reachedLimit[0] >= maxGap && reachedLimit[1] >= maxGap && reachedLimit[2] >= maxGap && reachedLimit[3] >= maxGap;
		}
		
		colorDifference = (colorDifference / count) * 10000;
		return new int[]{count, (int)colorDifference};
	}
	
	private int checkColorNarrow(int x, int y) {
		int rgb = image.getRGB(x, y);
		float[] hsb = new float[3];
		Colors.calculateHSB(rgb, hsb);
		if (hsb[2] < 0.05 || hsb[2] > 0.95 || hsb[1] < 0.1) return -1;
		/*rgb = 0;
		for (int i = -averagingRange; i <= averagingRange; ++i) {
			for (int j = -averagingRange; j <= averagingRange; ++j) {
				rgb += image.getRGB(x, y);
			}
		}
		rgb /= (averagingRange*2+1)*(averagingRange*2+1);*/
		return Colors.getClosestColor(hsb);
	}
	
	private boolean checkColorWide(int x, int y, int h) {
		int rgb = image.getRGB(x, y);
		float[] hsb = new float[3];
		Colors.calculateHSB(rgb, hsb);
		if (hsb[2] < 0.05 || hsb[2] > 0.95 || hsb[1] < 0.1) return false;
		if (Colors.getClosestColorWide(rgb) == h) {
			int oldrgb = background.getRGB(x, y);
			if (Colors.verifyDifference(rgb, oldrgb, motionThresholdLow)) {
				Graphics g = debugImage.getGraphics();
				//g.setColor(new Color(Colors.getRGB(h)));
				g.setColor(Color.white);
				g.drawLine(x,y,x,y);
				return true;
			}
			else {
				Graphics g = debugImage.getGraphics();
				g.setColor(Color.yellow);
				g.drawLine(x,y,x,y);
				return false;
			}
		}
		return false;
	}
	
	public float[] RGBtoHSL(int rgb) {
		float[] hsb = new float[3];
		Colors.calculateHSB(rgb, hsb);
		return hsb;
	}
}
