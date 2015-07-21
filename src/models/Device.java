package models;

public class Device {
	public static enum OS {
		ANDROID(0),
		IOS(1);

		private final int value;

		private OS(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	private String deviceUuid;
	private int os;
	private boolean pushAvailable;
	private String pushToken;
	private boolean pushAlert = true;
	private boolean active;

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	public int getOs() {
		return os;
	}

	public void setOs(int os) {
		this.os = os;
	}

	public boolean isPushAvailable() {
		return pushAvailable && pushToken != null;
	}

	public void setPushAvailable(boolean pushAvailable) {
		this.pushAvailable = pushAvailable;
	}

	public String getPushToken() {
		return pushToken;
	}

	public void setPushToken(String pushToken) {
		this.pushToken = pushToken;
	}

	public boolean isPushAlert() {
		return pushAlert;
	}

	public void setPushAlert(boolean pushAlert) {
		this.pushAlert = pushAlert;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
