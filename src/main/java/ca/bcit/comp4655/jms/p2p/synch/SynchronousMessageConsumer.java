package ca.bcit.comp4655.jms.p2p.synch;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SynchronousMessageConsumer implements MessageListener {

	private static QueueConnection conn;
	private static QueueSession session;
	private static Queue que;

	public SynchronousMessageConsumer() 
	{
		try 
		{
			
			setupPTP();
			
			System.out.print( "waiting for messages" );
			
			for ( int i = 0; i < 100; i++ ) 
			{
				Thread.sleep(1000);
				System.out.print( "." );
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) 
	{
		new SynchronousMessageConsumer();
		
	}

	@Override
	public void onMessage(Message msg) {
		TextMessage tm = (TextMessage) msg;
		try {
			System.out.println("onMessage, recv text=" + tm.getText());
		
		} catch (JMSException j) {
			j.printStackTrace();
		}

	}
	
	/*
	 * In JBoss 7, you can either by pass the security by turning that off:
	 * standalone-full.xml:
	 * <hornetq-server>
				<security-enabled>false</security-enabled>
				.....
		</hornetq-server>
	 * OR you can add user by running JBOSS_HOME/bin/add-user.bat (or .sh) script.
	 * 	
	 */
	private void setupPTP() throws JMSException, NamingException 
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
		QueueReceiver queueReceiver = session.createReceiver(que);
		queueReceiver.setMessageListener( this );
		conn.start();
	}
}
