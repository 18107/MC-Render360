package mod.render360;

import java.io.IOException;
import java.io.InputStream;

public class Reader {

	public static String read(String resourceIn) {
		InputStream is = Reader.class.getResourceAsStream(resourceIn);
		if (is == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		int i;
		
		try {
			i = is.read();
			while (i != -1) {
				sb.append((char) i);
				i = is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
			
		}
		
		return sb.toString();
	}
}
