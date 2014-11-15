package core;

import debug.*;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.*;


public class WebcamMouse {
	static {
	    //Webcam.setDriver(new IpCamDriver());
	}
	public static void main (String[] args) {
		
		CameraHandler handler = CameraHandler.getInstance();
		FramePainter painter = FramePainter.getInstance();
		handler.start();
		Thread t1 = new Thread(painter);
		t1.start();
		/*Camera camera = Camera.getInstance();
		Webcam webcam = camera.getWebcam();
		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);*/
		
		
		JFrame window = new JFrame("Test webcam panel");
		window.add(painter);
		//window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

}
