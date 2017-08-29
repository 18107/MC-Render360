package core.render360.coretransform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CLTLog
{
	public static final Logger logger = LogManager.getLogger("Render360-CLT");

	public static void info(String s)
	{
		logger.info(s);
	}
	
	public static <T> void info(T s) {
		logger.info(s.toString());
	}
}
