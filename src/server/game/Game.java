package com.network.server.game;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.network.server.game.entities.Entity;
import com.network.server.game.entities.Food;
import com.network.server.game.entities.Player;
import com.network.server.network.Server;

public class Game implements Runnable {

	private static HashMap<String, Entity> entities;
	private static int counter = 1;
	private static boolean autoSpawn;

	public Game() {
		entities = new HashMap<String, Entity>();
		autoSpawn = true;
	}

	@Override
	public void run() {
		while (true) {
			Server.sendToEveryone("0:posreq.0");
			Server.sendToEveryone("0:render.0");
			if(Server.console.hasSpectator())
				Server.console.getSpectator().render();
			if (autoSpawn) {
				if (new Random().nextInt(100) == 1) {
					createFood();
				}
			}
			checkCollectedFood();
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void createFood() {
		int x = new Random().nextInt(280);
		int y = new Random().nextInt(280);
		entities.put("f" + counter, new Food("f" + counter, x, y, Color.GREEN));
		Server.sendToEveryone("f" + counter + ":spawn." + x + "," + y + ";");
		count();
	}

	public static void createFood(int x, int y) {
		entities.put("f" + counter, new Food("f" + counter, x, y, Color.GREEN));
		Server.sendToEveryone("f" + counter + ":spawn." + x + "," + y + ";");
		count();
	}

	private static void checkCollectedFood() {
		try {
			Iterator<Map.Entry<String, Entity>> iterP = getEntities().entrySet().iterator();
			while (iterP.hasNext()) {
				Entry<String, Entity> p = iterP.next();
				if (p.getValue() instanceof Player) {
					Iterator<Map.Entry<String, Entity>> iterF = getEntities().entrySet().iterator();
					while (iterF.hasNext()) {
						Entry<String, Entity> f = iterF.next();
						if (f.getValue() instanceof Food) {
							if (new Rectangle(p.getValue().getX(), p.getValue().getY(), 20, 20)
									.intersects(new Rectangle(f.getValue().getX(), f.getValue().getY(), 10, 10))) {
								Server.sendToEveryone(f.getValue().getId() + ":eaten.0;");
								Server.sendToEveryone("0:point." + p.getValue().getId() + ";");
								p.getValue().givePoint();
								iterF.remove();
								getEntities().remove(f.getValue().getId());
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public static HashMap<String, Entity> getEntities() {
		return entities;
	}

	public static void count() {
		counter++;
	}

	public static boolean isAutoSpawn() {
		return autoSpawn;
	}

	public static void setAutoSpawn(boolean isAutoSpawn) {
		autoSpawn = isAutoSpawn;
	}
}
