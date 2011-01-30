package name.caiiiycuk.scribe.examples;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForeverLog {

	private static Logger logger = LoggerFactory.getLogger(ForeverLog.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		while (true) {
			switch (i) {
			case 0:
				logger.error("Error log message");
				break;

			case 1:
				logger.warn("Warn log message");
				break;
				
			case 2:
				logger.warn("Info log message");
				break;
				
			case 3:
				logger.warn("Debug log message");
				break;				
				
			default:
				break;
			}

			i = (i+1) % 4;
		}
	}

}
