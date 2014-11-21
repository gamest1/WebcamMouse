package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;

import core.Colors;

public class ColorPanel extends JPanel {
	private static ColorPanel instance = null;
	private static int DIAMETER = 26;
	
	private ColorPanel() {
		
	}
	
	public static ColorPanel getInstance() {
		if (instance == null) {
			instance = new ColorPanel();
		}
		return instance;
	}
	
	protected void paintComponent(Graphics g) {
		int x, y;
		g.setColor(new Color(220,220,220));
		g.fillRect(0, 0, DIAMETER*5, DIAMETER);
		for (int i = 0; i < 3; ++i) {
			g.setColor(new Color(Colors.getRGB(i)));
			x = i*2*DIAMETER;
			y = 0;
			g.fillOval(x, y, DIAMETER, DIAMETER);
		}
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(DIAMETER*5, DIAMETER);
    }
}
