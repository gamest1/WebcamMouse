package core;

import gui.GUI;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.*;

import java.awt.Color;


public class WebcamMouse {
	static {
	    //Webcam.setDriver(new IpCamDriver());
	}
	public static void main (String[] args) {
		
		Colors.init();
		CameraHandler handler = CameraHandler.getInstance();
		handler.start();
		GUI gui = GUI.getInstance();

		
		/*int[] dimensions = {32, 128};
		int[] center = {16,64};
		String[][] str = new String[dimensions[0]][dimensions[1]];
		for (int i = 0; i < dimensions[0]; ++i) {
			for (int j = 0; j < dimensions[1]; ++j) {
				str[i][j] = "#";
			}
		}
		int kernel = 4;
		int bound, x, y;
		int maxI = Math.max(dimensions[0], dimensions[1])/(kernel * 2) + (kernel * 2);
		int[] quarterDim = {dimensions[0]/(kernel * 2), dimensions[1]/(kernel * 2)};
		for (int i = 0; i < maxI; ++i) {
			for (int j = -1; j <= 1; j = j + 2) {
				//System.out.println();
				if (i < quarterDim[0] + 1) {
					bound = (Math.min(i, quarterDim[1]));
					for (int k = -bound; k <= bound; ++k) {
						//System.out.println(k);
						x = (center[0] + j*i*kernel + kernel * dimensions[0])%dimensions[0];
						y = (center[1] + k*kernel + kernel * dimensions[1])%dimensions[1];
						str[x][y] = " ";
						print(str);
					}
				}
				if (i < quarterDim[1] + 1) {
					bound = (Math.min(i, quarterDim[0]));
					for (int k = -bound; k <= bound; ++k) {
						x = (center[0] + k*kernel + kernel * dimensions[0])%dimensions[0];
						y = (center[1] + j*i*kernel + kernel * dimensions[1])%dimensions[1];
						str[x][y] = " ";
						print(str);
					}
				}
			}
		}*/
		
	}
	
	public static void print(String[][] str) {
		for (int i = 0; i < str.length; ++i) {
			for (int j = 0; j < str[0].length; ++j) {
				System.out.print(str[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

}
