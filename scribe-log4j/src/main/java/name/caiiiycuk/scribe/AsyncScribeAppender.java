package name.caiiiycuk.scribe;

import org.apache.log4j.AsyncAppender;

/**
 * An asynchronous version of {@link ScribeAppender}, which extends
 * Log4j's AsyncAppender.
 * 
 * Example configuration:
 * 
 * <pre>
 * 	log4j.rootLogger=DEBUG, scribe
 *
 *	log4j.appender.scribe=name.caiiiycuk.scribe.ScribeAppender
 *	log4j.appender.scribe.hostname=my-app.ru
 *	log4j.appender.scribe.scribeHost=127.0.0.1
 *	log4j.appender.scribe.scribePort=1463
 *	log4j.appender.scribe.scribeCategory=my-app
 *	log4j.appender.scribe.printExceptionStack=true
 *	log4j.appender.scribe.layout=org.apache.log4j.PatternLayout
 *	log4j.appender.scribe.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n
 * </pre> 
 * 
 * 
 * @author caiiiycuk
 */
public class AsyncScribeAppender extends AsyncAppender {

	private String 	hostname;
	private String 	scribeHost;
	private int 	scribePort;
	private String 	scribeCategory;
	private boolean printExceptionStack;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getScribeHost() {
		return scribeHost;
	}

	public void setScribeHost(String scribeHost) {
		this.scribeHost = scribeHost;
	}

	public int getScribePort() {
		return scribePort;
	}

	public void setScribePort(int scribePort) {
		this.scribePort = scribePort;
	}

	public String getScribeCategory() {
		return scribeCategory;
	}

	public void setScribeCategory(String scribeCategory) {
		this.scribeCategory = scribeCategory;
	}
	
	public void setPrintExceptionStack(boolean printExceptionStack) {
		this.printExceptionStack = printExceptionStack;
	}

	@Override
	public void activateOptions() {
		super.activateOptions();
		synchronized (this) {
			ScribeAppender scribeAppender = new ScribeAppender();
			scribeAppender.setLayout(getLayout());
			scribeAppender.setHostname(getHostname());
			scribeAppender.setScribeHost(getScribeHost());
			scribeAppender.setScribePort(getScribePort());
			scribeAppender.setScribeCategory(getScribeCategory());
			scribeAppender.setPrintExceptionStack(printExceptionStack);
			scribeAppender.activateOptions();
			addAppender(scribeAppender);
		}
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

}