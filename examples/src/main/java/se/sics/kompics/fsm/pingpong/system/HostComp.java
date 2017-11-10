/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.pingpong.system;

import java.util.List;
import org.javatuples.Pair;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.fsm.core.PingPort;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HostComp extends ComponentDefinition {
  private final Component pingComp;
  private final Component pongComp;
  
  public HostComp(Init init) {
    pingComp = create(PingComp.class, new PingComp.Init(init.pingPongPairs));
    pongComp = create(PongComp.class, Init.NONE);
    connect(pingComp.getNegative(PingPort.class), pongComp.getPositive(PingPort.class), Channel.TWO_WAY);
  }
  
  public static class Init extends se.sics.kompics.Init<HostComp> {
    public final List<Pair<Identifier, Identifier>> pingPongPairs;
    
    public Init(List<Pair<Identifier, Identifier>> pingPongPairs) {
      this.pingPongPairs = pingPongPairs;
    }
  }
}
