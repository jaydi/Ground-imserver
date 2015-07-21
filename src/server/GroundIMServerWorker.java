package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import models.Device;
import models.IMessage;
import models.SocketKey;
import utils.ProtocolCodec;
import utils.PushManager;

public class GroundIMServerWorker implements Runnable {
	public static final String url = "jdbc:mysql://127.0.0.1/anb_development?useUnicode=true&characterEncoding=UTF-8";
	public static final String user = "root";
	public static final String pswd = "dmscjsground";

	private String key;
	private Socket socket;
	private ConcurrentHashMap<String, PrintWriter> pairMap;
	private BufferedReader reader;

	private Connection conn;

	public GroundIMServerWorker(Socket socket, ConcurrentHashMap<String, PrintWriter> pairMap) {
		this.socket = socket;
		this.pairMap = pairMap;
		init();
		initDB();
	}

	private void initDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, pswd);

			if (conn == null)
				throw new SQLException();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			key = reader.readLine();
			if (key != null)
				synchronized (pairMap) {
					pairMap.put(key, new PrintWriter(new OutputStreamWriter(socket.getOutputStream())));
				}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("client(" + key + ") on socket : " + socket.toString());

		String line = null;
		try {
			if (key == null)
				return;

			while ((line = reader.readLine()) != null) {
				System.out.println("line(" + line + ") read from : " + key);
				deliverMessage(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();

				if (reader != null)
					reader.close();

				if (socket != null)
					socket.close();

				if (key != null)
					synchronized (pairMap) {
						pairMap.remove(key);
						System.out.println("socket(" + key + ") removed");
					}

				System.out.println("client(" + key + ") closed");

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void deliverMessage(String line) throws SQLException {
		IMessage message = ProtocolCodec.decode(line, IMessage.class);

		List<SocketKey> socketKeys = getSocketKeys(message.getMatchId());
		socketKeys = fixDuplication(socketKeys, message.getTeamId());
		send(socketKeys, message);

		saveMessage(message);
		sendBackAck(message.getId());
	}

	private void sendBackAck(long id) {
		synchronized (pairMap) {
			PrintWriter writer = pairMap.get(key);
			writer.println(ProtocolCodec.encode(IMessage.createAck(id)));
			writer.flush();
		}
	}

	private List<SocketKey> getSocketKeys(long matchId) throws SQLException {
		List<SocketKey> socketKeys = new ArrayList<SocketKey>();
		Statement stmt = conn.createStatement();

		String query = "SELECT tm.team_id, d.device_uuid FROM matches m, team_members tm, devices d WHERE (m.home_team_id = tm.team_id OR m.away_team_id = tm.team_id) "
				+ "AND tm.user_id = d.user_id AND tm.status = 1 AND m.id = " + matchId;

		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			socketKeys.add(new SocketKey(rs.getInt("team_id"), rs.getString("device_uuid")));
		}

		rs.close();
		stmt.close();

		return socketKeys;
	}

	private List<SocketKey> fixDuplication(List<SocketKey> socketKeys, int teamId) {
		List<SocketKey> dupKeys = new ArrayList<SocketKey>();
		for (SocketKey socketKey : socketKeys)
			if (socketKey.getSocketKey().equals(key))
				dupKeys.add(socketKey);

		for (SocketKey dupKey : dupKeys)
			socketKeys.remove(dupKey);

		return socketKeys;
	}

	public void send(List<SocketKey> socketKeys, IMessage message) throws SQLException {
		synchronized (pairMap) {
			PrintWriter writer;

			for (SocketKey socketKey : socketKeys) {
				writer = pairMap.get(socketKey.getSocketKey());

				String line = ProtocolCodec.encode(message);
				System.out.println("line (" + line + ") sending to key : " + socketKey.getSocketKey());

				if (writer != null) {
					writer.println(line);
					writer.flush();
				} else {
					pushIMessage(socketKey, message);
				}
			}
		}
	}

	private void pushIMessage(SocketKey socketKey, IMessage message) throws SQLException {
		Device device = new Device();
		device.setDeviceUuid(socketKey.getSocketKey());
		Statement stmt = conn.createStatement();

		String query = String.format("SELECT os, push_alert, push_token, active FROM devices WHERE device_uuid = '%s'", device.getDeviceUuid());

		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			device.setOs(rs.getInt("os"));
			device.setPushAvailable(rs.getBoolean("push_alert"));
			device.setPushToken(rs.getString("push_token"));
			device.setActive(rs.getBoolean("active"));
		}

		rs.close();
		stmt.close();

		if (device.isPushAvailable())
			PushManager.sendMessage(device, message.getMatchId(), message.getTeamId(), message.getTeamName(), message.getTeamImageUrl(), message.getMsg(), socketKey.getTeamId());
	}

	private void saveMessage(IMessage message) throws SQLException {
		Statement stmt = conn.createStatement();

		String query = String.format("INSERT INTO messages(match_id, team_id, message) values(%s, %s, '%s')", message.getMatchId(), message.getTeamId(), message.getMsg());

		stmt.execute(query);
		stmt.close();
	}
}
