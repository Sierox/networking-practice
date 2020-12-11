package com.network.server.game.entities;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Entity {
	protected String id;
	protected int point;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected Color color;

	public Entity(String id, int x, int y, int width, int height, Color color) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public void givePoint() {
		point++;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getPoint() {
		return point;
	}

	public abstract void render(Graphics2D g2d);

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getSx() {
		return width;
	}

	public void setSx(int sx) {
		this.width = sx;
	}

	public int getSy() {
		return height;
	}

	public void setSy(int sy) {
		this.height = sy;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getPosition() {
		return x + "," + y;
	}

	public void setPositionInteger(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
