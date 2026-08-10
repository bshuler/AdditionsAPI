package us.fihgu.toolbox.resourcepack;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

import com.chrismin13.additionsapi.AdditionsAPI;
import com.chrismin13.additionsapi.files.ConfigFile;
import com.chrismin13.additionsapi.utils.Debug;

import us.fihgu.toolbox.http.FileContext;
import us.fihgu.toolbox.http.HTTPServer;
import us.fihgu.toolbox.http.StaticContextGenerator;
import us.fihgu.toolbox.network.NetworkUtils;

public class ResourcePackServer {
	public static String localhost;
	public static String host;
	public static int port;

	public static String path;

	private static HTTPServer server;

	public static void startServer() {
		try {
			localhost = AdditionsAPI.getInstance().getConfig().getString("http.localhost",
					InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			Debug.sayError(
					"Something just went terribly wrong. Please contact the plugin developer on Spigot and tell him that the plugin couldn't acquire the Localhost IP. If the plugin doesn't work, try adding in the Config, under http, localhost: 0.0.0.0");
			localhost = "127.0.0.1";
		}
		Debug.saySuper("Current Localhost: " + localhost);
		host = AdditionsAPI.getInstance().getConfig().getString("http.host", NetworkUtils.getExternalIP());
		port = AdditionsAPI.getInstance().getConfig().getInt("http.port");
		int numReadThread = AdditionsAPI.getInstance().getConfig().getInt("http.numReadThread");
		int numWriteThread = AdditionsAPI.getInstance().getConfig().getInt("http.numWriteThread");

		int build = AdditionsAPI.getInstance().getConfig().getInt("resource-pack.build", 1);
		path = "/resourcepack" + build + ".zip";

		InetSocketAddress address = new InetSocketAddress(localhost, port);
		server = new HTTPServer(address);
		server.numReadThread = numReadThread;
		server.numWriteThread = numWriteThread;
		server.putContextGenerator(path, new StaticContextGenerator(new FileContext(
				Paths.get(AdditionsAPI.getInstance().getDataFolder() + "/resource-pack/resource.zip"))));

		if (ConfigFile.getInstance().getConfig().getBoolean("resource-pack.send-to-player")) {
			if (ConfigFile.getInstance().getConfig().getBoolean("resource-pack.use-minepack")) {
				// Minepack (minepack.net) was a third-party resource pack hosting service
				// that this plugin used to upload to via HTML-scraping (Selenium/HtmlUnit).
				// The service is defunct; that integration has been removed (see
				// legacy-hooks/MinePackInitializationMethod.java.txt) and the built-in
				// HTTP file server below is always used regardless of this setting.
				Debug.sayError(
						"resource-pack.use-minepack is enabled in config.yml, but the Minepack integration has been removed (the service is defunct). Falling back to the built-in HTTP server. Set use-minepack to false to silence this message.");
			}
			server.startServer();
		}
	}

	public static void stopServer() {
		if (server != null) {
			server.stopServer();
		}
	}
}
