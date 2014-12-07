package core;


import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.ipcam.*;


import java.awt.Dimension;
import java.awt.Graphics;


public class Camera {
	private static Camera instance = null;
	private Webcam webcam = null;
	private boolean flip = true;
	
	private BufferedImage background = null;
	
	
	private Camera() {
		webcam = Webcam.getDefault();
		if (webcam == null) {
		    try {
		    	Webcam.setDriver(new IpCamDriver());
		    	IpCamDeviceRegistry.register(new IpCamDevice("Find5", "http://192.168.178.50:8080/video", IpCamMode.PUSH));
		    }
		    catch (Exception e) {System.out.println("Ooops!"); System.exit(1);}
			webcam = Webcam.getDefault();
		}
		try {
			webcam.getViewSize();
		}
		catch(Exception e) {
			webcam = null;
		}
		if (webcam != null) {
			System.out.println("Webcam: " + webcam.getName());
			Dimension[] dim = webcam.getViewSizes();
			for (int i = 0; i < dim.length; ++i) {
				if ((dim[i].getHeight()*dim[i].getWidth()) > dim[0].getHeight()*dim[0].getWidth()) {
					dim[0] = dim[i];
				}
			}
			webcam.setViewSize(dim[0]);
			webcam.open();
			background = getImage();
		}
		else {
			System.out.println("No webcam detected");
			//webcam = null;
			//System.exit(1);
		}
	}
	
	public static Camera getInstance() {
		if (instance == null) {
			instance = new Camera();
		}
		return instance;
	}
	
	public Webcam getWebcam() {
		return webcam;
	}
	
	public BufferedImage getImage() {
		BufferedImage out = webcam.getImage();
		int w = out.getWidth();
		int h = out.getHeight();
		if (flip) {
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
			
			g.drawImage(out, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			out = copy;
		}
		return out;
	}
	
	public void setFlip(boolean value) {
		flip = value;
	}
	
	public boolean getFlip() {
		return flip;
	}
	
	public void toggleFlip() {
		flip = !flip;
	}
	
	public void setBackground() {
		background = getImage();
	}
	
	public BufferedImage getBackground() {
		return background;
	}
	
}
