package com.network.client.network;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

import com.network.util.MessageDecoder;
import com.network.client.game.Game;
import com.network.client.game.entities.Food;
import com.network.client.game.entities.Player;

public class Client implements Runnable {

	public String id;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Couldn't connect to the server.");
			System.exit(0);
		}
	}

	@Override
	public void run() {
		try {
			waitForMessages();
		} catch (SocketException e){
			JOptionPane.showMessageDialog(null, "Connection to server lost!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitForMessages() throws IOException {
		String message;
		while ((message = reader.readLine()) != null) {
			handleMessage(message);
		}
	}

	public void handleMessage(String message) {
		// Print the message (for debugging).
		System.out.println("in<<< " + message);

		// If the message comes from the server.
		if (MessageDecoder.getSenderId(message).equals("0")) {
			// Receive ID.
			if (MessageDecoder.getType(message).equals("id")) {
				id = MessageDecoder.getStrValue(message, 0);
				addPlayer(id, Color.RED);
			}
			// Teleport
			else if (MessageDecoder.getType(message).equals("tp")) {
				Game.canvas.mouseX = MessageDecoder.getIntValue(message, 0);
				Game.canvas.mouseY = MessageDecoder.getIntValue(message, 1);
			}
			// Leave
			else if (MessageDecoder.getType(message).equals("leave")) {
				// If this client is leaving.
				if (MessageDecoder.getStrValue(message, 0).equals(id)) {
					closeSocket();
					// If this client is kicked.
					if (MessageDecoder.getIntValue(message, 1) == 1)
						JOptionPane.showMessageDialog(null, "You were kicked!");
					System.exit(0);
				}
				// If another client is leaving.
				Game.entities.remove(MessageDecoder.getStrValue(message, 0));
			} else if (MessageDecoder.getType(message).equals("point")) {
				Game.entities.get(MessageDecoder.getStrValue(message, 0)).givePoint();
			} else if (MessageDecoder.getType(message).equals("points")) {
				Game.entities.get(MessageDecoder.getStrValue(message, 0))
						.setPoint(MessageDecoder.getIntValue(message, 1));
			} else if (MessageDecoder.getType(message).equals("exit")){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, "Server shutdown.");
				System.exit(0);
			}
			// Render
			else if (MessageDecoder.getType(message).equals("render")) {
				render();
			}
			// Position Request
			else if (MessageDecoder.getType(message).equals("posreq")) {
				sendPosition();
			}

			// If the message comes from a player (client).
		} else if (MessageDecoder.getSenderId(message).startsWith("p")) {
			// Join Notice
			if (MessageDecoder.getType(message).equals("join")) {
				addPlayer(MessageDecoder.getStrValue(message, 0), Color.BLACK);
			}
			// Position Notice
			else if (MessageDecoder.getType(message).equals("pos")) {
				updatePosition(MessageDecoder.getSenderId(message), MessageDecoder.getIntValue(message, 0),
						MessageDecoder.getIntValue(message, 1));
			}
			// If the message comes from a food.
		} else if (MessageDecoder.getSenderId(message).startsWith("f")) {
			// Spawn Notice
			if (MessageDecoder.getType(message).equals("spawn")) {
				addFood(MessageDecoder.getSenderId(message), MessageDecoder.getIntValue(message, 0),
						MessageDecoder.getIntValue(message, 1));
			}
			// Eaten Notice
			else if (MessageDecoder.getType(message).equals("eaten")) {
				Game.entities.remove(MessageDecoder.getSenderId(message));
			}
		}
	}

	public void render() {
		Game.canvas.repaint();
	}

	public void sendPosition() {
		sendToServer(id + ":pos." + Game.canvas.mouseX + "," + Game.canvas.mouseY + ";");
	}

	public void sendLeave() {
		sendToServer(id + ":leave.0");
	}

	public void updatePosition(String id, int x, int y) {
		Game.entities.get(id).setPositionInteger(x, y);
	}

	public void addPlayer(String id, Color color) {
		Game.entities.put(id, new Player(id, 0, 0, color));
	}

	public void addFood(String id, int x, int y) {
		Game.entities.put(id, new Food(id, x, y, Color.green));
	}

	public void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendToServer(String message) {
		System.out.println("out>>> " + message);
		writer.println(message);
		writer.flush();
	}
}
