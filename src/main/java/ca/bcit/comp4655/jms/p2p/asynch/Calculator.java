package ca.bcit.comp4655.jms.p2p.asynch;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import ca.bcit.comp4655.jms.connectionutil.ConnectionElements;
import ca.bcit.comp4655.jms.connectionutil.ConnectionUtil;
import ca.bcit.comp4655.jms.connectionutil.MessagingModelType;

public class Calculator implements MessageListener
{

	private ConnectionUtil request;
	private QueueSession session;

	public Calculator() throws NamingException, JMSException
	{
		request = new ConnectionUtil(getConnectionElemenets("jms/queue/calcRequest"));
		
		session = (QueueSession) request.getSession();
		QueueReceiver queueReceiver = session.createReceiver( (Queue) request.getDestination());
		queueReceiver.setMessageListener(this);
		request.start();
		
		System.out.println( "Calculator is listening for messages..." );
		for ( int i = 0; i < 100; i++ ) 
		{
			try
			{
				Thread.sleep(1000);
				System.out.print( "." );
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void onMessage(Message msg )
	{
		
		try
		{
			if ( msg != null &&  msg instanceof MapMessage )
			{
				MapMessage message = (MapMessage) msg;
				System.out.println("Received Message. Operand 1: " + message.getString("operand1"));
				System.out.println("Received Message. Operand 2: " + message.getString("operand2"));
				TextMessage outgoingMsg = session.createTextMessage("" + getResults(message));

				ConnectionUtil response = new ConnectionUtil(getConnectionElemenets("jms/queue/calcResponse"));
				Queue responseQueue = (Queue) response.getDestination();
				MessageProducer sender = response.getSession().createProducer(responseQueue);
				sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				sender.send(outgoingMsg);

			}
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
		

	}

	public int getResults(Message message) throws JMSException
	{
		MapMessage calc = (MapMessage) message;
		OperationType type = null;
		Enumeration<String> e = calc.getMapNames();
		List<Integer> numbers = new ArrayList<Integer>();
		while (e.hasMoreElements())
		{
			String item = e.nextElement();
			if (item.startsWith("operand"))
			{
				numbers.add(calc.getInt(item));
			}
			else if ("operaionName".equals(item))
			{
				type = OperationType.valueOf(calc.getString(item));
			}
		}
		switch (type)
		{
		case Addition:
			return add(numbers);
		default:
			return -1;
		}
	}

	private int add(List<Integer> numbers)
	{
		int total = 0;
		for (Integer num : numbers)
		{
			total = total + num;
		}
		return total;
	}

	public static void main(String[] args)
	{
		try
		{
			new Calculator();
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	synchronized void waitForever()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000);
				System.out.print(".");
			}
			catch (InterruptedException ex)
			{}
		}
	}

	private enum OperationType
	{
		Addition, Subtraction, Multiplication, Division
	}

	private static ConnectionElements getConnectionElemenets(String queueName)
	{
		ConnectionElements elements = new ConnectionElements();
		elements.setDestinationName(queueName);
		elements.setSessionMode(Session.DUPS_OK_ACKNOWLEDGE);
		elements.setTransactedSession(false);
		elements.setType(MessagingModelType.QUEUE);
		return elements;
	}

}
