package org.hofi.cometd;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;

public class Client {
  public static void main(String[] args) throws Exception {
    Client client = new Client();
    client.run();
  }

  private volatile BayeuxClient client;
  private final MessageListener messageListener = new MessageListener();

  private void run() throws Exception {
    String url = "http://localhost:8080/cometd/cometd";

    HttpClient httpClient = new HttpClient();
    httpClient.start();

    client = new BayeuxClient(url, new LongPollingTransport(null, httpClient));
    client.getChannel(Channel.META_HANDSHAKE).addListener(new InitializerListener());
    client.handshake();
    boolean success = client.waitFor(1000, BayeuxClient.State.CONNECTED);
    if (!success) {
      System.err.printf("Could not handshake with server at %s%n", url);
    }
  }

  private void initialize() {
    client.batch(new Runnable() {
      @Override
      public void run() {
        ClientSessionChannel chatChannel = client.getChannel("/hofi");
        chatChannel.subscribe(messageListener);
      }
    });
  }

  private class InitializerListener implements ClientSessionChannel.MessageListener {
    @Override
    public void onMessage(ClientSessionChannel channel, Message message) {
      if (message.isSuccessful()) {
        initialize();
      }
    }
  }

  private class MessageListener implements ClientSessionChannel.MessageListener {
    @Override
    public void onMessage(ClientSessionChannel channel, Message message) {
      System.err.println("MessageListener.onMessage");
      Object data = message.getData();
      String mymessage = data instanceof String ? ((String)data) : data.toString();
      System.err.println("Message: " + mymessage);
    }
  }
}