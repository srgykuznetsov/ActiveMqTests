import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MainActiveMQ
{
    public static void main(String[] args) {

        String strBrokerUrl = "vm://localhost";//"tcp://0.0.0.0:61616";
        String strSrcQueue  = "SrcQueue";
        String strDstQueue1 = "DstQueue1";
        String strDstQueue2 = "DstQueue2";

        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(strBrokerUrl);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


            Destination destinationSrc = session.createQueue(strSrcQueue);

            TextMessage msgInitial = session.createTextMessage("TestMessage");
            MsgProcessor initSender = new MsgProcessor(session, destinationSrc);
            initSender.send(msgInitial);

            Destination destinationDst1 =  session.createQueue(strDstQueue1);
            Destination destinationDst2 =  session.createQueue(strDstQueue2);


            MsgProcessor receiver = new MsgProcessor(session, destinationSrc);
            Message msg = receiver.receive();

            validateMessage(msg);

            MsgProcessor sender1 = new MsgProcessor(session, destinationDst1);
            sender1.send(msg);

            MsgProcessor sender2 = new MsgProcessor(session, destinationDst2);
            sender1.send(msg);

            System.in.read();

            // Clean up
            session.close();
            connection.close();
        }
        catch (Exception e)
        {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }


    }

    static void validateMessage(Message message) throws JMSException
    {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            if (text == null || text.length() == 0) {
                System.out.println("Empty text message received");
            }
        }
//        else {
//
//        }
    }

    static class MsgProcessor
    {
        private final Session session;
        private final Destination destination;

        public MsgProcessor(Session session, Destination destination) {
            this.session = session;
            this.destination = destination;
        }

        public void send(Message msg) throws JMSException
        {
            MessageProducer producer = this.session.createProducer(this.destination);
            try
            {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                producer.send(msg);
            }
            finally
            {
                producer.close();
            }
        }

        public Message receive() throws JMSException
        {
            MessageConsumer consumer = this.session.createConsumer(this.destination);
            // Wait for a message
            try
            {
                Message message = consumer.receive();
                return message;
            }
            finally
            {
                consumer.close();
            }
        }
    }
}
