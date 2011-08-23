package name.caiiiycuk.scribe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

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
 *  log4j.appender.scribe.addStackTraceToMessage=true
 *  log4j.appender.scribe.timeToWaitBeforeRetry=6000
 *  log4j.appender.scribe.localStoreForwardClassName=my.domain.logging.ILocalStoreForwardImpl
 *	log4j.appender.scribe.layout=org.apache.log4j.PatternLayout
 *	log4j.appender.scribe.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n
 * </pre> 
 * 
 * 
 * @author caiiiycuk
 */
public class ScribeAppender extends AppenderSkeleton {

	private String 	hostname;//dynamically set

	private String 	scribeHost;
	private int 	scribePort;
	private String 	scribeCategory;
	private boolean printExceptionStack;

    private boolean addStackTraceToMessage;
    private long timeToWaitBeforeRetry;
    private String localStoreForwardClassName;

    private int sizeOfInMemoryStoreForward;

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

    private long connectionFailureTimeStamp = 0;

    private ILocalStoreForward localStoreForwardInstance;

	public ScribeAppender() {
		scribeHost 			= null;
		scribePort 			= 0;
		scribeCategory 		= null;
		printExceptionStack = false;
		valid 				= false;

        addStackTraceToMessage = true;
        timeToWaitBeforeRetry = 5*1000;//5 seconds
        localStoreForwardClassName = null;

        localStoreForwardInstance = null;
	}

    private void instantiateILocalStoreForward(){

        if (this.localStoreForwardClassName != null && localStoreForwardInstance == null){
            try {
                Class clazz = Class.forName(this.localStoreForwardClassName);
                this.localStoreForwardInstance = (ILocalStoreForward) clazz.newInstance();
            } catch (Exception e) {
                if (printExceptionStack) {
                    System.err.println ("Error instantiating instance of " + ILocalStoreForward.class.getName() + " for given class: " + this.localStoreForwardClassName);
                    System.exit(1);
                }
            }
        }
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

    public boolean isAddStackTraceToMessage() {
        return addStackTraceToMessage;
    }

    public void setAddStackTraceToMessage(boolean addStackTraceToMessage) {
        this.addStackTraceToMessage = addStackTraceToMessage;
    }

    public long getTimeToWaitBeforeRetry() {
        return timeToWaitBeforeRetry;
    }

    public void setTimeToWaitBeforeRetry(long timeToWaitBeforeRetry) {
        this.timeToWaitBeforeRetry = timeToWaitBeforeRetry;
    }

    public String getLocalStoreForwardClassName() {
        return localStoreForwardClassName;
    }

    public void setLocalStoreForwardClassName(String localStoreForwardClassName) {
        this.localStoreForwardClassName = localStoreForwardClassName;
    }

    public int getSizeOfInMemoryStoreForward() {
        return sizeOfInMemoryStoreForward;
    }

    public void setSizeOfInMemoryStoreForward(int sizeOfInMemoryStoreForward) {
        this.sizeOfInMemoryStoreForward = sizeOfInMemoryStoreForward;
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

        Socket socket = null;
		try {
            System.out.println("Attempting to connect to scribe server: " + this.scribeHost);
			// Thrift boilerplate code
			logEntries = new ArrayList<LogEntry>(1);

            socket = new Socket();
            socket.connect(new InetSocketAddress(scribeHost, scribePort), 1000);

			TSocket sock = new TSocket(socket);

			transport = new TFramedTransport(sock);
			TBinaryProtocol protocol = new TBinaryProtocol(transport, false, false);

			client = new Client(protocol, protocol);
			
			valid = true;

            //start store forward
            if (this.localStoreForwardInstance != null){//local store forward is provided in config
                LogEntry logEntry = null;
                while ((logEntry = this.localStoreForwardInstance.getLogEntry()) != null){//read from local store forward here
                    this.sendLogEntry(logEntry);
                }
            }
            //end store forward

			return true;
		}
        catch (Throwable t) {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    if (printExceptionStack) {
                        t.printStackTrace();
                    }
                }
            }
            this.handleConnectionFailure();
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
                long now = System.currentTimeMillis();
				if ((now - this.connectionFailureTimeStamp) > this.timeToWaitBeforeRetry) {//if 5 sec has passed since last connection failure
					//attempt to connect again
                    connect();
				}
			}
			
			try {

                if (valid || (this.localStoreForwardInstance != null)){//only process if the connection is valid or if a local store forward provider exists
                    String message = String.format("[%s] %s", hostname, layout
                            .format(event));

                    if (event.getThrowableInformation() != null){
                        if (addStackTraceToMessage){
                            String[] throwableStrRep = event.getThrowableInformation().getThrowableStrRep();
                            if (throwableStrRep != null){
                                StringBuffer sb = new StringBuffer();
                                for (String throwableLine: throwableStrRep){
                                    sb.append(throwableLine).append("\n");
                                }
                                message = message + "\n" + sb.toString();
                            }
                        }
                    }

                    LogEntry entry = new LogEntry(scribeCategory, message);

                    if (client != null && valid){//either send to scribe
                        this.sendLogEntry(entry);
                    }
                    else if (this.localStoreForwardInstance != null){//or store forward there is a provider
                        this.localStoreForwardInstance.putLogEntry(entry);
                    }
                }
			}
            catch (TTransportException t){
                this.handleConnectionFailure();
                if (printExceptionStack) {
					t.printStackTrace();
				}
            }
            catch (Throwable t) {
				if (printExceptionStack) {
					t.printStackTrace();
				}
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
        if (this.sizeOfInMemoryStoreForward > 0){
            this.localStoreForwardClassName = InMemoryQueueLocalStoreForward.class.getName();
        }
        this.instantiateILocalStoreForward();

        this.hostname = this.getHostname();

		return StringUtils.isNotBlank(hostname)
				&& StringUtils.isNotBlank(scribeHost)
				&& StringUtils.isNotBlank(scribeCategory) && scribePort != 0;
	}

    public static String getStringStackTrace(Throwable t) {
        if (t == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        //sb.append(t).append("\n");
        StackTraceElement[] trace = t.getStackTrace();
        for (StackTraceElement ste : trace) {
            sb.append(ste).append("\n");
        }

        Throwable ourCause = t.getCause();
        if (ourCause != null) {
            addCause(ourCause, trace, sb);
        }

        return sb.toString();
    }

    private static void addCause(Throwable ourCause, StackTraceElement[] causedTrace, StringBuilder sb) {
        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = ourCause.getStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        sb.append("Caused by: ").append(ourCause).append("\n");
        for (int i=0; i <= m; i++)
            sb.append(trace[i]).append("\n");
        if (framesInCommon != 0)
            sb.append(framesInCommon).append(" more").append("\n");

        // Recurse if we have a cause
        Throwable ourCause1 = ourCause.getCause();
        if (ourCause1 != null) {
            addCause(ourCause1, trace, sb);
        }
    }

    private void handleConnectionFailure (){
        this.close();
        this.client = null;
        this.transport = null;
        this.valid = false;
        this.connectionFailureTimeStamp = System.currentTimeMillis();
    }

    private void sendLogEntry (LogEntry entry) throws TException{
        try{
            logEntries.add(entry);
            client.Log(logEntries);
        }
        finally{
            logEntries.clear();
        }
    }

    private String getHostname (){

        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            //byte[] ipAddr = addr.getAddress();

            // Get hostname
            String hostname = addr.getHostName();

            return hostname;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown";
        }

    }


}