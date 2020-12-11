package com.network.server.console;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.network.server.game.Game;
import com.network.server.game.entities.Entity;
import com.network.server.network.Server;

@SuppressWarnings("serial")
public class SpectateFrame extends JFrame implements WindowListener{
	private SpectateCanvas canvas = new SpectateCanvas();
	public SpectateFrame() {
		this.addWindowListener(this);
		this.add(canvas);
		this.setTitle("Spectate");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(300, 300);
		this.setVisible(true);
	}
	
	public void render(){
		canvas.repaint();
	}
	
	public void windowClosed(WindowEvent e) {
		Server.console.disposeSpectatorFrame();
		Server.console.writeln("] Spectation window closed.");
	}
	
	public void windowActivated(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}

@SuppressWarnings("serial")
class SpectateCanvas extends JPanel{
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		for (Entity entity : Game.getEntities().values()) {
			entity.render(g2d);
		}
	}
}
