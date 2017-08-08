/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import se.sics.kompics.id.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class BasicEvent {

  public static class Ping implements PingFSM.Event, PongFSM.Event {

    private final Identifier pingBaseFSMId;
    private final Identifier pongBaseFSMId;

    public Ping(Identifier pingBaseFSMId, Identifier pongBaseFSMId) {
      this.pingBaseFSMId = pingBaseFSMId;
      this.pongBaseFSMId = pongBaseFSMId;
    }

    @Override
    public Identifier getPingBaseFSMId() {
      return pingBaseFSMId;
    }

    @Override
    public Identifier getPongBaseFSMId() {
      return pongBaseFSMId;
    }

    public Pong pong() {
      return new Pong(pingBaseFSMId, pongBaseFSMId);
    }

    @Override
    public String toString() {
      return "Ping<" + pingBaseFSMId + "," + pongBaseFSMId + ">";
    }
  }

  public static class Pong implements PingFSM.Event, PongFSM.Event {

    private final Identifier pingBaseFSMId;
    private final Identifier pongBaseFSMId;

    public Pong(Identifier pingBaseFSMId, Identifier pongBaseFSMId) {
      this.pingBaseFSMId = pingBaseFSMId;
      this.pongBaseFSMId = pongBaseFSMId;
    }

    @Override
    public Identifier getPingBaseFSMId() {
      return pingBaseFSMId;
    }

    @Override
    public Identifier getPongBaseFSMId() {
      return pongBaseFSMId;
    }
    
    @Override
    public String toString() {
      return "Pong<" + pingBaseFSMId + "," + pongBaseFSMId + ">";
    }
  }
}
