/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.pingpong.system;

import com.google.common.base.Optional;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.javatuples.Pair;
import se.sics.kompics.Kompics;
import se.sics.kompics.config.Config;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.core.IntId;
import se.sics.kompics.fsm.core.PingFSM;
import se.sics.kompics.fsm.core.PongFSM;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Launcher {

  public static void main(String[] args) throws IOException, FSMException {
    if (Kompics.isOn()) {
      Kompics.shutdown();
    }
    List<Pair<Identifier, Identifier>> pingPongPairs = new LinkedList<>();
    pingPongPairs.add(Pair.with((Identifier)new IntId(0), (Identifier)new IntId(1)));
    pingPongPairs.add(Pair.with((Identifier)new IntId(2), (Identifier)new IntId(3)));
    HostComp.Init init = new HostComp.Init(pingPongPairs);
    
    FSMIdentifierFactory fsmIdFactory = FSMIdentifierFactory.DEFAULT;
    fsmIdFactory.registerFSMDefId(PingFSM.NAME);
    fsmIdFactory.registerFSMDefId(PongFSM.NAME);
    
    Config.Impl config = (Config.Impl)Kompics.getConfig();
    Config.Builder builder = Kompics.getConfig().modify(UUID.randomUUID());
    builder.setValue(FSMIdentifierFactory.CONFIG_KEY, fsmIdFactory);
    config.apply(builder.finalise(), (Optional)Optional.absent());
    Kompics.setConfig(config);
    
    Kompics.createAndStart(HostComp.class, init, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
    try {
      Kompics.waitForTermination();
    } catch (InterruptedException ex) {
      System.exit(1);
    }
  }
}
