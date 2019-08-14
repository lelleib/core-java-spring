package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;

@RunWith(SpringRunner.class)
public class GSDPollTaskTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private GSDPollTask testingObject;

	private GatekeeperRelayClient relayClient;
	
	private final BlockingQueue<GSDPollResponseDTO> queue = new LinkedBlockingQueue<>(1);;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		ReflectionTestUtils.setField(testingObject, "relayClient", relayClient);
		ReflectionTestUtils.setField(testingObject, "queue", queue);
		ReflectionTestUtils.setField(testingObject, "session",  getTestSession());
		ReflectionTestUtils.setField(testingObject, "recipientCloudCN",  "test-cn");
		ReflectionTestUtils.setField(testingObject, "recipientCloudPublicKey",  "test-key");
		ReflectionTestUtils.setField(testingObject, "gsdPollRequestDTO",  new GSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGeneralAdvertisementResult() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(null);
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = queue.take();
		
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGatekeeperRelayResponse() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		when(relayClient.sendRequestAndReturnResponse(any(), any(), any())).thenReturn(null);
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = queue.take();
		
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(JMSException.class).when(relayClient).publishGeneralAdvertisement(any(), any(), any());
		when(relayClient.sendRequestAndReturnResponse(any(), any(), any())).thenReturn(new GatekeeperRelayResponse("session-id", "message-type", new GSDPollResponseDTO()));
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = queue.take();
		
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(JMSException.class).when(relayClient).sendRequestAndReturnResponse(any(), any(), any());
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = queue.take();
		
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	public Session getTestSession() {
		return new Session() {

			//-------------------------------------------------------------------------------------------------
			public void close() throws JMSException {}
			public Queue createQueue(final String queueName) throws JMSException { return null;	}
			public Topic createTopic(final String topicName) throws JMSException { return null;	}
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { return null; }
			public MessageProducer createProducer(final Destination destination) throws JMSException { return null;	}
			public TextMessage createTextMessage(final String text) throws JMSException { return null; }
			public BytesMessage createBytesMessage() throws JMSException { return null; }
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException { return null; }
			public StreamMessage createStreamMessage() throws JMSException { return null; }
			public TextMessage createTextMessage() throws JMSException { return null; }
			public boolean getTransacted() throws JMSException { return false; 	}
			public int getAcknowledgeMode() throws JMSException { return 0; }
			public void commit() throws JMSException {}
			public void rollback() throws JMSException {}
			public void recover() throws JMSException {}
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public void run() {}
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException { return null;	}
			public QueueBrowser createBrowser(final Queue queue) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException { return null; }
			public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }
			public TemporaryTopic createTemporaryTopic() throws JMSException { return null;	}
			public void unsubscribe(final String name) throws JMSException {}

		};
	}
	
	//-------------------------------------------------------------------------------------------------
	public MessageConsumer getTestMessageConsumer() {
		return new MessageConsumer() {

			//-------------------------------------------------------------------------------------------------
			public Message receive(final long timeout) throws JMSException { return null; }
			public void close() throws JMSException {}
			public String getMessageSelector() throws JMSException { return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receive() throws JMSException { return null; }
			public Message receiveNoWait() throws JMSException { return null; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getDummyPublicKey() {
		return new PublicKey() {
			
			//-------------------------------------------------------------------------------------------------
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}
}
