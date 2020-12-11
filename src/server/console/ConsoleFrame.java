package com.network.server.console;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.network.server.game.Game;
import com.network.server.game.entities.Entity;
import com.network.server.game.entities.Food;
import com.network.server.game.entities.Player;
import com.network.server.network.Server;
import com.network.util.MessageDecoder;

public class ConsoleFrame extends JFrame implements KeyListener, WindowListener {
	private static final long serialVersionUID = 1L;

	private JButton sendButton;
	private JTextField sendField;
	private JPanel sendPanel;

	private JScrollPane readAreaScroll;
	private JTextArea readArea;

	private String lastMessage;
	
	private SpectateFrame spectator = null;

	public ConsoleFrame() {

		this.setLayout(new BorderLayout());

		readArea = new JTextArea();
		readArea.setFont(new Font("Consolas", Font.PLAIN, 10));
		readArea.setEditable(false);
		readAreaScroll = new JScrollPane(readArea);

		sendPanel = new JPanel();
		sendPanel.setLayout(new GridLayout(1, 2));

		sendField = new JTextField();
		sendField.setFont(new Font("Consolas", Font.PLAIN, 10));
		sendPanel.add(sendField);

		sendButton = new JButton();
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enterCommand();
				sendField.requestFocusInWindow();
			}
		});
		sendButton.setText("Enter");
		sendPanel.add(sendButton);
		readAreaScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(sendPanel, BorderLayout.SOUTH);
		add(readAreaScroll, BorderLayout.CENTER);
		sendField.addKeyListener(this);
		lastMessage = "";

		this.addWindowListener(this);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Server Control Panel");
		this.setSize(650, 400);
		this.setResizable(false);
		this.setFocusable(true);
		this.setVisible(true);
		sendField.requestFocusInWindow();
	}

	private void handleCommand(String command) {
		try {
			// Info
			if (command.equals("info")) {
				writeln(Server.getServerInfo());
				return;
			}
			// Exit
			else if (command.equals("exit")) {
				Server.sendToEveryone("0:exit.0");
				System.exit(0);
			}
			// Help
			else if (command.equals("help")) {
				writeln(getHelpMessage());
				return;
			}
			// Clear
			else if (command.equals("clear")) {
				readArea.setText("");
				return;
			}
			else if (command.equals("spectate")) {
				if(spectator != null){
					writeln("] Spectation window is already running.");
					return;
				}
				spectator = new SpectateFrame();
				writeln("] Spectation window created.");
				return;
			}
			// Sudo
			else if (!MessageDecoder.getSenderId(command).equals("0")) {
				if(!command.endsWith(";")){
					writeln("] " + "Error: Unknown command or incorrect syntax!");
					return;
				}
				// If an illegal sudo command is entered.
				if (MessageDecoder.getType(command).equals("join")) {
					writeln("] " + "Error: Unknown command or incorrect syntax!");
					return;
				}
				// If the command is entered for all clients.
				if (MessageDecoder.getSenderId(command).equals("-1")) {
					// All leave is special so if it is the case it is handled.
					if (MessageDecoder.getType(command).equals("leave")) {
						int i2 = Server.clients.size();
						for (int i = 0; i < i2; i++) {
							Server.clients.get(0)
									.handleMessage(Server.clients.get(0).getClientId() + command.substring(2));
						}
						writeln("] Sudo: \"" + command + "\" successful!");
						return;
					}
					// If the command isn't special, direct the command to all
					// of the clients.
					else {
						for (int i = 0; i < Server.clients.size(); i++) {
							Server.clients.get(i)
									.handleMessage(Server.clients.get(i).getClientId() + command.substring(2));
						}
						writeln("] Sudo: \"" + command + "\" successful!");
						return;
					}
				}
				// Singular sudo command.
				else {
					for (int i = 0; i < Server.clients.size(); i++) {
						if (Server.clients.get(i).getClientId().equals(MessageDecoder.getSenderId(command))) {
							Server.clients.get(i).handleMessage(command);
							writeln("] Sudo: \"" + command + "\" successful!");
							return;
						}
					}
					writeln("] Couldn't find client with id: " + MessageDecoder.getSenderId(command) + "!");
					return;
				}
			}
			// Server commands.
			else if (MessageDecoder.getSenderId(command).equals("0")) {
				if(!command.endsWith(";")){
					writeln("] " + "Error: Unknown command or incorrect syntax!");
					return;
				}
				// KICK ("kick") (-1 for KICKALL)
				if (MessageDecoder.getType(command).equals("kick")) {
					// KICKALL
					if (MessageDecoder.getStrValue(command, 0).equals("" + -1)) {
						int counter = Server.clients.size();
						for (int i = 0; i < counter; i++) {
							Server.sendToEveryone("0:leave." + Server.clients.get(0).getClientId() + ",1;");
							Game.getEntities().remove(Server.clients.get(0).getClientId());
							writeln("] Kicked client with id: " + Server.clients.get(0).getClientId() + "!");
							Server.clients.remove(0);
						}
						writeln("] The server is empty!");
						return;
					}
					// SINGULAR KICK
					for (int i = 0; i < Server.clients.size(); i++) {
						if (Server.clients.get(i).getClientId().equals(MessageDecoder.getStrValue(command, 0))) {
							Server.sendToEveryone("0:leave." + MessageDecoder.getStrValue(command, 0) + ",1;");
							Game.getEntities().remove(Server.clients.get(i).getClientId());
							Server.clients.remove(i);
							writeln("] " + "Kicked client with id: " + MessageDecoder.getStrValue(command, 0) + "!");
							return;
						}
					}
					writeln("] " + "Couldn't find (and kick) client with id: " + MessageDecoder.getStrValue(command, 0)
							+ "!");
					return;
				}
				// TELEPORT ("tp") (-1 for TPALL)
				else if (MessageDecoder.getType(command).equals("tp")) {
					// TP ALL
					if (MessageDecoder.getStrValue(command, 0).equals("-1")) {
						for (int i = 0; i < Server.clients.size(); i++) {
							Server.clients.get(i).sendToClient("0:tp." + MessageDecoder.getStrValue(command, 1) + ","
									+ MessageDecoder.getStrValue(command, 2) + ";");
						}
						writeln("] Teleported every client to X:" + MessageDecoder.getStrValue(command, 1) + ", Y:"
								+ MessageDecoder.getStrValue(command, 2) + "!");
						return;
					}
					// Singular TP
					else {
						for (int i = 0; i < Server.clients.size(); i++) {
							if (Server.clients.get(i).getClientId().equals(MessageDecoder.getStrValue(command, 0))) {
								Server.clients.get(i).sendToClient("0:tp." + MessageDecoder.getStrValue(command, 1)
										+ "," + MessageDecoder.getStrValue(command, 2) + ";");
								writeln("] " + "Teleported client with id: " + MessageDecoder.getStrValue(command, 0)
										+ " to X:" + MessageDecoder.getStrValue(command, 1) + ", Y:"
										+ MessageDecoder.getStrValue(command, 2) + "!");
								return;
							}
						}
						writeln("] " + "Couldn't find (and teleport) client with id: "
								+ MessageDecoder.getStrValue(command, 0) + "!");
						return;
					}
				}
				// SPAWN FOOD ("spawnfood")
				else if (MessageDecoder.getType(command).equals("spawnfood")) {
					for (int i = 0; i < MessageDecoder.getIntValue(command, 0); i++) {
						Game.createFood(MessageDecoder.getIntValue(command, 1), MessageDecoder.getIntValue(command, 2));
					}
					writeln("] Spawned " + MessageDecoder.getIntValue(command, 0) + " food(s) on X:"
							+ MessageDecoder.getStrValue(command, 1) + ", Y:" + MessageDecoder.getStrValue(command, 2)
							+ "!");
					return;
				}
				// SET AUTO FOOD SPAWNING ("autofoodspawn")
				else if (MessageDecoder.getType(command).equals("autofoodspawn")) {
					if (MessageDecoder.getStrValue(command, 0).equals("true")) {
						if (Game.isAutoSpawn()) {
							writeln("] Food already spawns automatically.");
							return;
						}
						Game.setAutoSpawn(true);
						writeln("] Food will spawn automatically from now on.");
						return;
					} else if (MessageDecoder.getStrValue(command, 0).equals("false")) {
						if (!Game.isAutoSpawn()) {
							writeln("] Food already doesn't spawn automatically.");
							return;
						}
						Game.setAutoSpawn(false);
						writeln("] Food will not spawn automatically anymore.");
						return;
					}
				}
				// SET POINTS OF A PLAYER ("setpoints")
				else if (MessageDecoder.getType(command).equals("setpoints")) {
					if (MessageDecoder.getStrValue(command, 0).equals("-1")) {
						Iterator<Map.Entry<String, Entity>> iter = Game.getEntities().entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, Entity> p = iter.next();
							if (p.getValue() instanceof Player) {
								p.getValue().setPoint(MessageDecoder.getIntValue(command, 1));
								Server.sendToEveryone(
										"0:points." + p.getKey() + "," + MessageDecoder.getIntValue(command, 1) + ";");
							}
						}
						writeln("] Set the points of all clients to " + MessageDecoder.getStrValue(command, 1) + "!");
						return;
					}
					if (Game.getEntities().containsKey(MessageDecoder.getStrValue(command, 0))) {
						Game.getEntities().get(MessageDecoder.getStrValue(command, 0))
								.setPoint(MessageDecoder.getIntValue(command, 1));
						Server.sendToEveryone("0:points." + MessageDecoder.getStrValue(command, 0) + ","
								+ MessageDecoder.getIntValue(command, 1) + ";");
						writeln("] Set the points of client with id: " + MessageDecoder.getStrValue(command, 0) + " to "
								+ MessageDecoder.getStrValue(command, 1) + "!");
						return;
					}
					writeln("] Couldn't find (and set the points of) a client with id: "
							+ MessageDecoder.getStrValue(command, 0) + "!");
					return;
				}
				// DESTROY FOODS ("despawnfood")
				else if (MessageDecoder.getType(command).equals("despawnfood")) {
					Iterator<Map.Entry<String, Entity>> iter = Game.getEntities().entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry<String, Entity> entry = iter.next();
						if (entry.getValue() instanceof Food) {
							Server.sendToEveryone(entry.getKey() + ":eaten.0");
							iter.remove();
						}
					}
					writeln("] Despawned all food!");
					return;
				}
			}
			// If the command doesn't fit any of the above, it is incorrect.
			writeln("] " + "Error: Unknown command or incorrect syntax!");
		} catch (Exception e) {
			writeln("] " + "Error: Unknown command or incorrect syntax!");
			throw new InvalidSyntaxException("Console tried to execute an invalid command!");
		}
	}

	private void enterCommand() {
		String command = sendField.getText();
		lastMessage = command;
		sendField.setText("");
		writeln("> " + command);
		handleCommand(command);
	}

	private String getHelpMessage() {
		return "********************************************************************************************************\n"
				+ "Console Commands:\n"
				+ "Form: <keyword>\n"
				+ "   info     | Shows the server information.\n"
				+ "   spectate | Creates a spectator window.\n"
				+ "   clear    | Clears the console.\n"
				+ "   help     | Shows this.\n"
				+ "   exit     | Shuts the server down.\n"
				+ "\n"
				+ "Server Commands:\n"
				+ "Form: <id>:<keyword>.<value1>,<value2>;\n"
				+ "   kick          | 0:kick.<clientID>; (clientID: -1 all)               | Kicks players.\n"
				+ "   teleport      | 0:tp.<clientID>,<X>,<Y>; (clientID: -1 all)         | Teleports players.\n"
				+ "   spawnfood     | 0:spawnfood.<amount>,<X>,<Y>;                       | Spawns food.\n"
				+ "   despawnfood   | 0:despawnfood.<*>;                                  | Despawns all food.\n"
				+ "   autofoodspawn | 0:autofoodspawn.<boolean>;                          | Sets automatic food spawning.\n"
				+ "   setpoints     | 0:setpoints.<clientID>,<amount>; (clientID: -1 all) | Sets a players score.\n"
				+ "   sudo          | <clientID>:<keyword>.<values...>;                   | Sends message from the client.\n"
				+ "   Available sudo keywords:\n"
				+ "      pos   | <clientID>:pos.<X>,<Y>; | Tells the server that the client is in this position.\n"
				+ "      leave | <clientID>:leave.<*>;   | Tells the server that the client is leaving.\n"
				+ "********************************************************************************************************";
	}

	public void writeln(String message) {
		if (!readArea.getText().equals("")) {
			readArea.setText(readArea.getText() + "\n" + message);
			return;
		}
		write(message);
	}

	public void write(String message) {
		readArea.setText(readArea.getText() + message);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			enterCommand();
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			sendField.setText(lastMessage);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			sendField.setText("");
		}
	}
	
	public void disposeSpectatorFrame(){
		if(spectator != null)
			spectator = null;
	}
	
	public SpectateFrame getSpectator() {
		return spectator;
	}
	
	public boolean hasSpectator() {
		if(spectator == null)
			return false;
		return true;
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		Server.shutdown();
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

	public void keyReleased(KeyEvent arg0) {
	}

	public void keyTyped(KeyEvent arg0) {
	}

}