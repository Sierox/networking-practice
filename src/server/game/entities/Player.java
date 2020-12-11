package com.network.server.game.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Player extends Entity {

	public Player(String id, int x, int y, Color color) {
		super(id, x, y, 20, 20, color);
		point = 0;
	}

	@Override
	public void render(Graphics2D g2d) {
		g2d.setColor(color);
		g2d.fillRect(x, y, width, height);
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font(g2d.getFont().getFontName(), Font.PLAIN, 10));
		g2d.drawString(id, x + 1, y + 8);
		g2d.setColor(Color.WHITE);
		g2d.drawString("" + point, x + 1, y + 18);
	}
}
