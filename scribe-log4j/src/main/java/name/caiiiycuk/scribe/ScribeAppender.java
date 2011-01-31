package name.caiiiycuk.scribe;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import scribe.thrift.LogEntry;
import scribe.thrift.scribe.Client;

/**
 * Scribe log4j appender
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
public class ScribeAppender extends AppenderSkeleton {

	private String 	hostname;
	private String 	scribeHost;
	private int 	scribePort;
	private String 	scribeCategory;
	private boolean printExceptionStack;

	// NOTE: logEntries, client, and transport are all protected by a lock on
	// 'this.'

	// The Scribe interface for sending log messages accepts a list. This list
	// is created
	// once and cleared and appended when new logs are created. The list is
	// always size 1.
	private List<LogEntry> logEntries;

	private Client client;
	private TFramedTransport transport;

	private boolean valid;

	public ScribeAppender() {
		scribeHost 			= null;
		scribePort 			= 0;
		scribeCategory 		= null;
		printExceptionStack = false;
		valid 				= false;
	}

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

	/*
	 * Activates this Appender by opening a transport to the Scribe server.
	 */
	@Override
	public void activateOptions() {
	}

	private boolean connect() {
		if (!isConfigurationValid()) {
			if (printExceptionStack) {
				System.out.println("Worn configuration for name.caiiiycuk.scribe.ScribeAppender");
				
				System.out.println(StringUtils.join(new Object[] {
						"hostname: ", hostname, "; scribeHost: ", scribeHost, "; scribePort: ", scribePort, "; scribeCategory: ", scribeCategory, "; printExceptionStack: ", printExceptionStack
				}, ""));
				
			}
			
			
			return false;
		}
		
		try {
			// Thrift boilerplate code
			logEntries = new ArrayList<LogEntry>(1);
			TSocket sock = new TSocket(new Socket(scribeHost, scribePort));
			transport = new TFramedTransport(sock);
			TBinaryProtocol protocol = new TBinaryProtocol(transport,
					false, false);
			client = new Client(protocol, protocol);
			// This is commented out because it was throwing Exceptions for
			// no good reason.
			
			valid = true;
			
			return true;
		} catch (Throwable t) {
			if (printExceptionStack) {
				t.printStackTrace();
			}
		}
		
		return false;
	}

	/*
	 * Appends a log message to Scribe
	 */
	@Override
	public void append(LoggingEvent event) {
		synchronized (this) {
			if (!valid) {
				if (!connect()) {
					return;
				}
			}
			
			try {
				String message = String.format("[%s] %s", hostname, layout
						.format(event));
				LogEntry entry = new LogEntry(scribeCategory, message);

				logEntries.add(entry);
				client.Log(logEntries);
			} catch (Throwable t) {
				if (printExceptionStack) {
					t.printStackTrace();
				}
			} finally {
				logEntries.clear();
			}
		}
	}

	@Override
	public void close() {
		if (transport != null) {
			transport.close();
		}
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	protected boolean isConfigurationValid() {
		return StringUtils.isNotBlank(hostname)
				&& StringUtils.isNotBlank(scribeHost)
				&& StringUtils.isNotBlank(scribeCategory) && scribePort != 0;
	}

}