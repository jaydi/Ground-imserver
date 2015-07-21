package models;

public class IMessage {
	private long id;
	private long matchId;
	private int teamId;
	private String teamName;
	private String teamImageUrl;
	private String msg;
	private boolean ack;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getTeamImageUrl() {
		return teamImageUrl;
	}

	public void setTeamImageUrl(String teamImageUrl) {
		this.teamImageUrl = teamImageUrl;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public static IMessage createAck(long id) {
		IMessage ackMessage = new IMessage();
		ackMessage.setId(id);
		ackMessage.setAck(true);
		
		return ackMessage;
	}

}
