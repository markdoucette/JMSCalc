package ca.bcit.comp4655.jms.connectionutil;

import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ConnectionUtil 
{
	private static String JBOSS_INITIAL_CONTEXT_FACTORY= "org.jboss.naming.remote.client.InitialContextFactory";
	private static String JBOSS_URL_PKG_PREFIXES = "org.jboss.naming:org.jnp.interfaces";
	private static String PROVIDER_URL = "remote://localhost:4447";
	private static String USER_NAME = "javaStudent";
	private static String PASSWORD = "java";
	
	
	private Destination dest;
	private Session session;
	private QueueConnection queueConnection;
	private TopicConnection topicConnection;
	private MessageConsumer consumer;
	private final Context context;
	
	public QueueConnection getQueueConnection() 
	{
		return queueConnection;
	}

	public TopicConnection getTopicConnection() 
	{
		return topicConnection;
	}
	
	public ConnectionUtil( ConnectionElements conElements ) throws NamingException, JMSException 
	{
		
		
	/*	
		
		Object tmp = iniCtx.lookup("jms/RemoteConnectionFactory");
		QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
		conn = qcf.createQueueConnection("javaStudent", "java" );
		que = (Queue) iniCtx.lookup("jms/queue/test");
		session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE );
		QueueReceiver queueReceiver = session.createReceiver(que);
		queueReceiver.setMessageListener( this );
		conn.start();
		
	*/	
	
		Properties props = new Properties();
		props.setProperty( Context.INITIAL_CONTEXT_FACTORY,JBOSS_INITIAL_CONTEXT_FACTORY );
		props.setProperty( Context.URL_PKG_PREFIXES, JBOSS_URL_PKG_PREFIXES );
		props.setProperty( Context.PROVIDER_URL, PROVIDER_URL );
		props.setProperty( Context.SECURITY_PRINCIPAL, USER_NAME );
		props.setProperty(Context.SECURITY_CREDENTIALS, PASSWORD );
		this.context = new InitialContext( props );
	
		switch ( conElements.getType() )
		{
		case QUEUE:
			QueueConnectionFactory queueFactory = ( QueueConnectionFactory)context.lookup( "jms/RemoteConnectionFactory" );
			queueConnection = queueFactory.createQueueConnection( USER_NAME, PASSWORD );
			dest = (Queue) context.lookup( conElements.getDestinationName() );
			session = queueConnection.createQueueSession( conElements.isTransactedSession(), conElements.getSessionMode() );
			break;
		case TOPIC:
			TopicConnectionFactory topicFactory = ( TopicConnectionFactory )context.lookup( "ConnectionFactory" );
			topicConnection = topicFactory.createTopicConnection();
			session = topicConnection.createTopicSession( conElements.isTransactedSession(), conElements.getSessionMode() );
			dest = (Topic) context.lookup( conElements.getDestinationName() );
			
			break;
		default:
			break;
		}
		
	}
	
	public void start() throws JMSException 
	{
		if ( queueConnection!=null )
		{
			queueConnection.start();
		}
		if ( topicConnection!=null )
		{
			topicConnection.start();
		}
	}

	public Session getSession()
	{
		return session;
	}
	
	public MessageConsumer getConsumer()
	{
		return consumer;
	}
	
	public Destination getDestination()
	{
		return dest;
	}
	
	public void disconnect() throws JMSException 
	{
		
		if ( topicConnection != null) 
		{
			topicConnection.stop();
		}
		if ( queueConnection !=null )
		{
			queueConnection.stop();
		}
		if (session != null) 
		{
			session.close();
		}
		if (topicConnection != null) 
		{
			topicConnection.close();
		}
		if ( queueConnection!=null )
		{
			queueConnection.close();
		}
	}
	
}
