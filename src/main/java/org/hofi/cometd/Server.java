package org.hofi.cometd;

import org.cometd.annotation.Configure;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.authorizer.GrantAuthorizer;

import java.util.Timer;
import java.util.TimerTask;


@Service("server")
public class Server {
  @Session
  private ServerSession _session;
  private int i = 0;

  @Configure("/hofi")
  protected void configureHofiChannel(ConfigurableServerChannel channel) {
    channel.setPersistent(true);
    channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
    Timer timer = new Timer(true);
    timer.schedule(new PublishMessageTask(), 30000, 30000);
  }

  class PublishMessageTask extends TimerTask
  {
    @Override
    public void run()
    {
      ClientSessionChannel mychannel = _session.getLocalSession().getChannel("/hofi/");
      mychannel.publish("hello world " + i++);
    }
  }
}