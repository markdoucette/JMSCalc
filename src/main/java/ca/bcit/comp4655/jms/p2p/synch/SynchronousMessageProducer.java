package ca.bcit.comp4655.jms.p2p.synch;

import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SynchronousMessageProducer
{
	private static QueueConnection conn;
	private static QueueSession session;
	private static Queue que;

	public static void main(String[] args) 
	{
		System.out.println("Start sending message ... ");
		try 
		{
			setupPTP();
			send( "Hello from SynchronousMessageProducer" );
		}
		catch (JMSException | NamingException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Message sent.");
	}

	

	private static void send( String text ) throws JMSException, NamingException 
	{	
		
		QueueSender msgSender = session.createSender( que );
		msgSender.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
		
		TextMessage txtMsg = session.createTextMessage( text );
		if ( text!=null )
		{
			msgSender.send( txtMsg );
		}
		else
		{
			//Sends an empty control message to indicate the end of the message stream:
			msgSender.send (session.createTextMessage() );
		}
	}
	
	
	private static void setupPTP() throws JMSException, NamingException 
	{
		Properties props = new Properties();
		props.setProperty( Context.INITIAL_CONTEXT_FACTORY,"org.jboss.naming.remote.client.InitialContextFactory" );
		props.setProperty( Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
		props.setProperty( Context.PROVIDER_URL, "remote://localhost:4447" );
		props.setProperty( Context.SECURITY_PRINCIPAL, "javaStudent" );
		props.setProperty(Context.SECURITY_CREDENTIALS, "java" );
		InitialContext iniCtx = new InitialContext(props);
		Object tmp = iniCtx.lookup("jms/RemoteConnectionFactory");
		QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
		conn = qcf.createQueueConnection("javaStudent", "java" );
		que = (Queue) iniCtx.lookup("jms/queue/test");
		session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE );
		conn.start();
	}
}
