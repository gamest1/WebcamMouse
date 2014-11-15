package core;


import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.ipcam.*;


import java.awt.Dimension;


public class Camera {
	private static Camera instance = null;
	private Webcam webcam = null;
	
	
	private Camera() {
		webcam = Webcam.getDefault();
		if (webcam == null) {
		    try {
		    	Webcam.setDriver(new IpCamDriver());
		    	IpCamDeviceRegistry.register(new IpCamDevice("Find5", "http://192.168.178.51:8080/video", IpCamMode.PUSH));
		    }
		    catch (Exception e) {System.out.println("Ooops!"); System.exit(1);}
			webcam = Webcam.getDefault();
		}
		if (webcam != null) {
			System.out.println("Webcam: " + webcam.getName());
		}
		else {
			System.out.println("No webcam detected");
			System.exit(1);
		}
		Dimension[] dim = webcam.getViewSizes();
		for (int i = 0; i < dim.length; ++i) {
			if ((dim[i].getHeight()*dim[i].getWidth()) > dim[0].getHeight()*dim[0].getWidth()) {
				dim[0] = dim[i];
			}
		}
		webcam.setViewSize(dim[0]);
		webcam.open();
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
		return out;
	}
	
}
