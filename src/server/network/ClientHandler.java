package com.network.server.network;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;

import javax.swing.JOptionPane;

import com.network.server.game.Game;
import com.network.server.game.entities.Entity;
import com.network.server.game.entities.Food;
import com.network.server.game.entities.Player;
import com.network.util.MessageDecoder;

public class ClientHandler extends Thread {

	private String clientId;
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;
	private boolean init;

	public ClientHandler(Socket socket, String id) {
		init = false;
		this.clientId = id;
		this.socket = socket;
		try {
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void run() {
		try {
			// Initialize the client.
			initClient();
			// Wait for messages as usual.
			waitForMessages();
		} catch (SocketException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ConcurrentModificationException e) {
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Server crashed!");
			Server.shutdown();
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void initClient() {
		// Send the client it's ID.
		sendToClient("0:id." + clientId + ";");
		// Send the client messages telling it about the other clients'
		// existence.
		for (int i = 0; i < Server.clients.size(); i++) {
			if (!Server.clients.get(i).clientId.equals(clientId))
				sendToClient(clientId + ":join." + Server.clients.get(i).clientId + ";");
		}
		// Tell the client about the existence of the existing foods.
		for (Entity f : Game.getEntities().values()) {
			if (f instanceof Food) {
				sendToClient(f.getId() + ":spawn." + f.getX() + "," + f.getY() + ";");
			}
		}
		// Send the client other players' points.
		for (Entity p : Game.getEntities().values()) {
			if (p instanceof Player) {
				sendToClient("0:points." + p.getId() + "," + p.getPoint() + ";");
			}
		}
		// Send messages to all other clients to ask their locations.
		sendToEveryoneElse("0:posreq.0;");
		// Send messages to all other clients to tell them about this client's
		// existence.
		sendToEveryoneElse(clientId + ":join." + clientId + ";");
		Game.getEntities().put(clientId, new Player(clientId, 0, 0, Color.BLACK));
		init = true;
		Server.console.writeln("< Client with id: " + clientId + " joined from "
				+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "!");
	}

	private void waitForMessages() throws IOException {
		String message;
		while ((message = reader.readLine()) != null) {
			handleMessage(message);
		}
	}

	public void handleMessage(String message) {
		// Leave
		if (MessageDecoder.getType(message).equals("leave")) {
			// If the client is leaving tell it to everyone and remove it from
			// the list of clients.
			sendToEveryone("0:leave." + MessageDecoder.getSenderId(message) + ",0;");
			Server.console.writeln("< Client with id: " + clientId + " left from "
					+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "!");
			Server.clients.remove(this);
			Game.getEntities().remove(clientId);
		}
		if (MessageDecoder.getType(message).equals("pos")) {
			Game.getEntities().get(MessageDecoder.getSenderId(message))
					.setPositionInteger(MessageDecoder.getIntValue(message, 0), MessageDecoder.getIntValue(message, 1));
			sendToEveryone(message);
		} else {
			// If none of the above is true, send the message to everyone.
			sendToEveryoneElse(message);
		}
	}

	public void sendToClient(String message) {
		writer.println(message);
		writer.flush();
	}

	public void sendToEveryone(String message) {
		Server.sendToEveryone(message);
	}

	public void sendToEveryoneElse(String message) {
		Server.sendToEveryoneElse(message);
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public boolean isInit() {
		return init;
	}
}
