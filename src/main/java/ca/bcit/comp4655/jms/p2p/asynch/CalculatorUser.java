package ca.bcit.comp4655.jms.p2p.asynch;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import ca.bcit.comp4655.jms.connectionutil.ConnectionElements;
import ca.bcit.comp4655.jms.connectionutil.ConnectionUtil;
import ca.bcit.comp4655.jms.connectionutil.MessagingModelType;

public class CalculatorUser implements MessageListener
{

	public CalculatorUser()
	{
		ConnectionUtil response;
		try
		{
			
			/*
			 * request = new ConnectionUtil(getConnectionElemenets("jms/queue/calcRequest"));
		
		session = (QueueSession) request.getSession();
		QueueReceiver queueReceiver = session.createReceiver( (Queue) request.getDestination());
		queueReceiver.setMessageListener(this);
		request.start();
			 */
			response = new ConnectionUtil( getConnectionElemenets( "jms/queue/calcResponse" ) );
			QueueSession session = (QueueSession) response.getSession();
			
			QueueReceiver receiver = session.createReceiver( (Queue) response.getDestination()); 
			receiver.setMessageListener(this);
			response.start();
		}
		catch (NamingException | JMSException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)
	{
		try
		{
			ConnectionUtil requestUtil = new ConnectionUtil( getConnectionElemenets( "jms/queue/calcRequest" ) );
			Queue requestQueue =( Queue ) requestUtil.getDestination();
			QueueSender sender = ((QueueSession)requestUtil.getSession()).createSender(requestQueue);
			sender.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
			
			MapMessage message = requestUtil.getSession().createMapMessage();
			message.setInt( "operand1", 2 );
			message.setInt( "operand2", 3 );
			message.setString( "operaionName", "Addition" );
			sender.send( message );
			System.out.println ( "Sent " + message.getString("operand1" ) + "+" + message.getString("operand2") );
			//requestUtil.disconnect();
			
			new CalculatorUser();
			for ( int i = 0; i < 100; i++ ) 
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
			}
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	
	}

	@Override
	public void onMessage(Message message )
	{
		try {
			if ( message!=null && message instanceof TextMessage )
			{
				TextMessage txt = (TextMessage)message;
				
				System.out.println( "Result: " + txt.getText() );
			}
			else
			{
				System.err.println ( "Calc client timed out!" );
			}
		}
		
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	
		}

		private static ConnectionElements getConnectionElemenets( String queueName ) 
		{
			ConnectionElements elements = new ConnectionElements();
			elements.setDestinationName( queueName );
			elements.setSessionMode( Session.DUPS_OK_ACKNOWLEDGE );
			elements.setTransactedSession( false );
			elements.setType( MessagingModelType.QUEUE );
			return elements;
		}
}
