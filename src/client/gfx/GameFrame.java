package com.network.client.gfx;

import java.awt.Cursor;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public GameFrame(GameCanvas canvas) {
		addWindowListener(canvas);
		addMouseMotionListener(canvas);
		this.add(canvas);
		this.setResizable(false);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		this.setTitle("Mouse");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(300, 300);
		this.setVisible(true);
	}
}
