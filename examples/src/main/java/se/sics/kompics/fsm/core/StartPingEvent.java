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
public class StartPingEvent implements PingFSM.Event {
  public final Identifier pingBaseFSMId;
  public final Identifier pongBaseFSMId;
  
  public StartPingEvent(Identifier pingBaseFSMId, Identifier pongBaseFSMId) {
    this.pingBaseFSMId = pingBaseFSMId;
    this.pongBaseFSMId = pingBaseFSMId;
  }
  
  @Override
  public Identifier getPingBaseFSMId() {
    return pingBaseFSMId;
  }

  @Override
  public String toString() {
    return "StartPingEvent<" + pingBaseFSMId + "," + pongBaseFSMId + ">";
  }
}
