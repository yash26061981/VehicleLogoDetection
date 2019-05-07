package com.avanseus.avr.broker;

/**
 * Created by hemanth on 18/5/16.
 */
import com.avanseus.ann.ANN;
import com.avanseus.ann.ANNController;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Server implements MessageListener {
    private static String messageBrokerUrl;
    private static String messageQueueName;
    private static int ackMode;

    private Session session;
    private boolean transacted = false;
    private MessageProducer replyProducer;
    //private MessageProtocol messageProtocol; // to be replace with ANN.
    private ANNController annController;
    static {
        messageBrokerUrl = "tcp://localhost:61616";
        messageQueueName = "client.messages";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    public Server() {
        try {
            //This message broker is embedded
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector(messageBrokerUrl);
            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Delegating the handling of messages to another class, instantiate it before setting up JMS so it
        //is ready to handle messages
        //messageProtocol = new MessageProtocol();
        annController = new ANNController();
        annController.performTraining();
        setupMessageQueueConsumer();
    }

    private void setupMessageQueueConsumer() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(transacted, ackMode);
            Destination adminQueue = session.createQueue(messageQueueName);

            //Set up a consumer to consume messages off of the admin queue
            MessageConsumer consumer = session.createConsumer(adminQueue);
            consumer.setMessageListener(this);

            //Setup a message producer to respond to messages from clients, we will get the destination
            //to send to from the JMSReplyTo header field from a Message
            replyProducer = session.createProducer(null);
            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message message) {
        try {
            TextMessage response = session.createTextMessage();
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageTextReceivedFromClient = txtMsg.getText();
                //String result = messageProtocol.handleProtocolMessage(messageTextReceivedFromClient);
                String result = annController.testNetworkForSinceInstance(messageTextReceivedFromClient);
                response.setText(result);
            }

            //Set the correlation ID from the received message to be the correlation id of the response message
            //this lets the client identify which message this is a response to if it has more than
            //one outstanding message to the server
            response.setJMSCorrelationID(message.getJMSCorrelationID());

            //Send the response to the Destination specified by the JMSReplyTo field of the received message,
            //this is presumably a temporary queue created by the client
            replyProducer.send(message.getJMSReplyTo(), response);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}

