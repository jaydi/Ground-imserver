package models;

public class SocketKey {
	private int teamId;
	private String socketKey;

	public SocketKey(int teamId, String socketKey) {
		super();
		this.teamId = teamId;
		this.socketKey = socketKey;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public String getSocketKey() {
		return socketKey;
	}

	public void setSocketKey(String socketKey) {
		this.socketKey = socketKey;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		SocketKey target = null;
		try {
			target = (SocketKey) o;
		} catch (ClassCastException e) {
			return false;
		} catch (Exception e) {
			return false;
		}

		if (target.getTeamId() == teamId && target.getSocketKey().equals(socketKey))
			return true;
		else
			return false;
	}
}
