package com.network.client.game;

import com.network.client.Main;
import com.network.client.gfx.GameCanvas;
import com.network.client.gfx.GameFrame;

import java.util.HashMap;

import com.network.client.network.Client;
import com.network.client.game.entities.Entity;

public class Game {

	public static Client client;
	public static GameCanvas canvas;
	public static GameFrame frame;
	public static HashMap<String, Entity> entities;

	public Game() {
		entities = new HashMap<String, Entity>();
		canvas = new GameCanvas();
		frame = new GameFrame(canvas);
		client = new Client();

		Thread connection = new Thread(client);
		connection.setName("Connection");
		client.connect(Main.HOST, Main.PORT);
		connection.start();

	}
}
