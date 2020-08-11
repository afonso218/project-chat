package cliente.util;

public class Tools {
	
	public static String formatUserName(String user_raw) {
		return user_raw.replace(" ", "_").toLowerCase();
	}
	
}
