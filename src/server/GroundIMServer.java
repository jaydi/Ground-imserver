package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroundIMServer {
	private static final int IM_PORT = 8999;
	
	private static ServerSocket listener;
	private static ConcurrentHashMap<String, PrintWriter> pairMap;
	
	private static ExecutorService executor = Executors.newCachedThreadPool();

	public static void main(String args[]) {
		try {
			listener = new ServerSocket(IM_PORT);
			pairMap = new ConcurrentHashMap<String, PrintWriter>();
			System.out.println("waiting first connection...");

			while (true) {
				Socket socket = listener.accept();
				executor.execute(new GroundIMServerWorker(socket, pairMap));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
