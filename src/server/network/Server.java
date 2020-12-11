package com.network.server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.network.server.Main;
import com.network.server.console.ConsoleFrame;
import com.network.server.game.Game;
import com.network.util.MessageDecoder;

public class Server {

	private static ServerSocket server;
	public static ArrayList<ClientHandler> clients;
	private static boolean running;
	public static ConsoleFrame console;

	public Server() {
		console = new ConsoleFrame();
		clients = new ArrayList<ClientHandler>();
		Thread t = new Thread(new Game());
		t.start();
		try {
			server = new ServerSocket(Main.PORT);
			running = true;
			console.write(getServerInfo());
			waitForClients();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void waitForClients() throws IOException {
		int counter = 1;
		while (running) {
			Socket s = server.accept();
			ClientHandler handler = new ClientHandler(s, "p" + counter);
			counter++;
			clients.add(handler);
			handler.start();
		}
	}

	public static void sendToEveryone(String message) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).isInit())
				clients.get(i).sendToClient(message);
		}
	}

	public static void sendToEveryoneElse(String message) {
		for (int i = 0; i < clients.size(); i++) {
			if (!clients.get(i).getClientId().equals(MessageDecoder.getSenderId(message)))
				clients.get(i).sendToClient(message);
		}
	}

	public static void shutdown() {
		Server.sendToEveryone("0:exit.0");
		System.exit(0);
	}

	public static String getIp() {
		return server.getInetAddress().getHostAddress();
	}

	public static String getExternalIp() {
		URL whatismyip;
		String ip = "?.?.?.?";
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			ip = in.readLine();
		} catch (MalformedURLException e) {
			System.err.println("Error while fetching external ip.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ip;
	}

	public static String getLocalIp() {
		try {
			return Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "?.?.?.?";
	}

	public static String getServerInfo() {
		return "********************************************************************************************************"
				+ "\n" + "Server online on:" + "\n" + "Host IP: " + getIp() + "\n" + "Local IP: " + getLocalIp() + "\n"
				+ "External IP: " + getExternalIp() + "\n" + "Port: " + getPort() + "\n"
				+ "********************************************************************************************************";
	}

	public static int getPort() {
		return Main.PORT;
	}
}
