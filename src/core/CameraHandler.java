package core;

public class CameraHandler extends Thread {
	private Camera camera = null;
	private static int fps = 15;
	private static int interval;		// in milliseconds
	private static CameraHandler instance = null;
	private static final int million = 1000000;
	
	private CameraHandler() {
		interval = 1000 / fps;
	}
	
	public static CameraHandler getInstance() {
		if (instance == null) {
			instance = new CameraHandler();
		}
		return instance;
	}
	
	public void run() {
		ImageProcessor ipc = null;
		int start;
		int sleepTime;
		while(true) {
			start = (int)(System.nanoTime()/million);
			ipc = new ImageProcessor();
			sleepTime = interval - (int)((int)(System.nanoTime()/million) - start);
			if (sleepTime < -10) {
				System.out.println("Unable to process images at "+fps+" frames per second!");
			}
			else if (sleepTime > 0) {
				try {
					this.sleep(sleepTime);
				}
				catch (InterruptedException e) {
					System.out.println("Uh oh!");
				}
			}
		}
	}
	
	public static void changeFps(int newFps) {
		fps = newFps;
		interval = 1000 / fps;
	}
	
}
