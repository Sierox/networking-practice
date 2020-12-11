package com.network.client.gfx;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;

import com.network.client.game.entities.Entity;
import com.network.client.game.Game;

@SuppressWarnings("serial")
public class GameCanvas extends JPanel implements MouseMotionListener, WindowListener {

	public int mouseX;
	public int mouseY;

	public GameCanvas() {
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		for (Entity entity : Game.entities.values()) {
			entity.render(g2d);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		updateMouseLocation(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateMouseLocation(e);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		Game.client.sendLeave();
	}

	private void updateMouseLocation(MouseEvent e) {
		mouseX = e.getX() - 2;
		mouseY = e.getY() - 25;
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
