/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.pingpong.system;

import java.util.List;
import org.javatuples.Pair;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.MultiFSM;
import se.sics.kompics.fsm.core.PingCtrlPort;
import se.sics.kompics.fsm.core.PingFSM;
import se.sics.kompics.fsm.core.PingPort;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class PingComp extends ComponentDefinition {

  private final Positive<PingPort> pingPort = requires(PingPort.class);
  private final Negative<PingCtrlPort> pingCtrlPort = provides(PingCtrlPort.class);
  private final PingFSM.ExternalState pingES;
  private MultiFSM pingFSM;
  private List<Pair<Identifier, Identifier>> baseFSMId;

  public PingComp(Init init) {
    pingES = new PingFSM.ExternalState();
    pingES.setProxy(proxy);
    try {
      FSMIdentifierFactory fsmIdFactory = config().getValue(FSMIdentifierFactory.CONFIG_KEY, FSMIdentifierFactory.class);
      pingFSM = PingFSM.multifsm(fsmIdFactory, pingES);
      pingFSM.setupHandlers();
    } catch (FSMException ex) {
      throw new RuntimeException(ex);
    }
    baseFSMId = init.baseFSMId;

    subscribe(handleStart, control);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      for (Pair<Identifier, Identifier> e : baseFSMId) {
        PingFSM.start(pingES, e.getValue0(), e.getValue1());
      }
    }
  };

  public static class Init extends se.sics.kompics.Init<PingComp> {

    public final List<Pair<Identifier, Identifier>> baseFSMId;

    public Init(List<Pair<Identifier, Identifier>> baseFSMId) {
      this.baseFSMId = baseFSMId;
    }
  }
}
