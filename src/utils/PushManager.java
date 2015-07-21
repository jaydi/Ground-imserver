package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Device;
import models.Device.OS;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class PushManager implements Runnable {
	public static enum LocKey {
		IMESSAGE;
	};

	private static final String GCM_API_KEY = "AIzaSyBUlCTuXTgn4H_oRP_YkdwEU-KcnmwhShQ";
	private static final String IOS_CERTIFICATE_PATH = "/home/anb/ground/server/cert/APNS_push.p12";
	private static final String IOS_CERTIFICATE_PASSWORD = "GroundIOS0513";
	private static Sender sender = new Sender(GCM_API_KEY);
	private static ApnsService service = APNS.newService().withCert(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASSWORD).withSandboxDestination().build();

	private static ExecutorService executorService = Executors.newCachedThreadPool();
	
	public static void sendMessage(Device device, long matchId, int teamId, String teamName, String teamImageUrl, String message, int targetTeamId) {
		executorService.execute(new PushManager(device, matchId, teamId, teamName, teamImageUrl, message, targetTeamId));
	}
	
	private Device device;
	private long matchId;
	private int teamId;
	private String teamName;
	private String teamImageUrl;
	private String message;
	private int targetTeamId;
	
	public PushManager(Device device, long matchId, int teamId, String teamName, String teamImageUrl, String message, int targetTeamId) {
		super();
		this.device = device;
		this.matchId = matchId;
		this.teamId = teamId;
		this.teamName = teamName;
		this.teamImageUrl = teamImageUrl;
		this.message = message;
		this.targetTeamId = targetTeamId;
	}

	@Override
	public void run() {
		if (device == null || device.getPushToken() == null || device.getPushToken().isEmpty() || !device.isActive()) {
			return;
		}

		if (!device.isPushAlert()) {
			return;
		}

		String pushToken = device.getPushToken();

		if (device.getOs() == OS.ANDROID.value()) {
			Message.Builder mb = new Message.Builder();
			mb.delayWhileIdle(false);
			mb.addData("anb.ground.extra.pushKey", LocKey.IMESSAGE.name());
			mb.addData("anb.ground.extra.pushParams", ProtocolCodec.encode(Collections.EMPTY_LIST));
			mb.addData("anb.ground.extra.message", message);
			mb.addData("anb.ground.extra.matchId", String.valueOf(matchId));
			mb.addData("anb.ground.extra.teamId", String.valueOf(teamId));
			mb.addData("anb.ground.extra.teamName", teamName);
			mb.addData("anb.ground.extra.teamImageUrl", (teamImageUrl == null) ? "" : teamImageUrl);
			mb.addData("anb.ground.extra.targetTeamId", String.valueOf(targetTeamId));

			try {
				Result result = sender.send(mb.build(), pushToken, 1);
				if (result.getMessageId() == null) {
					String error = result.getErrorCodeName();
					System.out.println(error);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (device.getOs() == 1) {
			ArrayList<String> locArgs = new ArrayList<String>();
			locArgs.add(teamName);
			locArgs.add(StringUtils.truncate(message));

			String payload = APNS.newPayload().localizedKey(LocKey.IMESSAGE.name()).localizedArguments(locArgs).sound("default").customField("teamId", targetTeamId)
					.customField("matchId", matchId).build();
			service.push(pushToken, payload);
		}
	}
}
