package com.network.client.game.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public class Food extends Entity {

	public Food(String id, int x, int y, Color color) {
		super(id, x, y, 10, 10, color);
	}

	@Override
	public void render(Graphics2D g2d) {
		g2d.setColor(color);
		g2d.fillRect(x, y, width, height);
	}
}
