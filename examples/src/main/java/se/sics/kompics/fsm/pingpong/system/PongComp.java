/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.pingpong.system;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.MultiFSM;
import se.sics.kompics.fsm.core.PingPort;
import se.sics.kompics.fsm.core.PongFSM;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class PongComp extends ComponentDefinition {
  private final Negative<PingPort> pingPort = provides(PingPort.class);
  private MultiFSM pongFSM;

  public PongComp() {
    PongFSM.ExternalState pongES = new PongFSM.ExternalState();
    pongES.setProxy(proxy);
    try {
      FSMIdentifierFactory fsmIdFactory = config().getValue(FSMIdentifierFactory.CONFIG_KEY, FSMIdentifierFactory.class);
      pongFSM = PongFSM.multifsm(fsmIdFactory, pongES);
      pongFSM.setupHandlers();
    } catch (FSMException ex) {
      throw new RuntimeException(ex);
    }
  }
}
