/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * KompicsToolbox is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.kompics.fsm;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.genericsetup.GenericSetup;
import se.sics.kompics.fsm.genericsetup.OnFSMExceptionAction;
import se.sics.kompics.fsm.id.FSMDefId;
import se.sics.kompics.fsm.id.FSMId;
import se.sics.kompics.fsm.id.FSMIds;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class MultiFSM {

  private static final Logger LOG = LoggerFactory.getLogger(MultiFSM.class);
  private String logPrefix = "";

  private final FSMachineDef fsmDef;
  private final OnFSMExceptionAction oexa;
  private final FSMIdExtractor fsmIdExtractor;
  private final Map<FSMId, FSMachine> fsms = new HashMap<>();
  private final FSMExternalState es;
  private final FSMInternalStateBuilders isb;
  private final Map<Class, Set<Class>> positiveBasicEvents;
  private final Map<Class, Set<Class>> negativeBasicEvents;
  private final Map<Class, Set<Class>> positivePatternEvents;
  private final Map<Class, Set<Class>> negativePatternEvents;

  private final FSMOnKillAction oka = new FSMOnKillAction() {
    @Override
    public void kill(FSMId fsmId) {
      LOG.info("{}removing fsm:{}", logPrefix, fsmId);
      fsms.remove(fsmId);
    }
  };

  private Optional<FSMachine> getFSM(FSMEvent event) throws FSMException {
    Optional<FSMId> optFsmId = fsmIdExtractor.fromEvent(fsmDef.id, event);
    if (!optFsmId.isPresent()) {
      LOG.warn("{}fsm did not handle event:{}", new Object[]{logPrefix, event});
      return Optional.absent();
    }
    FSMId fsmId = optFsmId.get();
    FSMachine fsm = fsms.get(fsmId);
    if (fsm == null) {
      fsm = fsmDef.build(fsmId.baseId, oka, es, isb.newInternalState(fsmId));
      fsms.put(fsmId, fsm);
    }
    return Optional.of(fsm);
  }
  private final Handler obeapos = new Handler<FSMEvent>() {
    @Override
    public void handle(FSMEvent event) {
      try {
        Optional<FSMachine> fsm = getFSM(event);
        if (fsm.isPresent()) {
          fsm.get().handlePositive(event);
        }
      } catch (FSMException ex) {
        oexa.handle(ex);
      }
    }
  };

  private final Handler obeaneg = new Handler<FSMEvent>() {
    @Override
    public void handle(FSMEvent event) {
      try {
        Optional<FSMachine> fsm = getFSM(event);
        if (fsm.isPresent()) {
          fsm.get().handleNegative(event);
        }
      } catch (FSMException ex) {
        oexa.handle(ex);
      }
    }
  };

  private <B extends PatternExtractor<Class, FSMEvent>> ClassMatchedHandler opeapos() {
    return new ClassMatchedHandler<FSMEvent, B>() {
      @Override
      public void handle(FSMEvent payload, B container) {
        try {
          Optional<FSMachine> fsm = getFSM(payload);
          if (fsm.isPresent()) {
            fsm.get().handlePositive(payload, container);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        }
      }
    };
  }

  private <B extends PatternExtractor<Class, FSMEvent>> ClassMatchedHandler opeaneg() {
    return new ClassMatchedHandler<FSMEvent, B>() {
      @Override
      public void handle(FSMEvent payload, B container) {
        try {
          Optional<FSMachine> fsm = getFSM(payload);
          if (fsm.isPresent()) {
            fsm.get().handleNegative(payload, container);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        }
      }
    };
  }

  // Class1 - ? extends PortType , Class2 - ? extends FSMEvent(KompicsEvent)
  public MultiFSM(FSMachineDef fsmDef, OnFSMExceptionAction oexa, FSMIdExtractor fsmIdExtractor,
    FSMExternalState es, FSMInternalStateBuilders isb,
    Map<Class, Set<Class>> positivePorts, Map<Class, Set<Class>> negativePorts,
    Map<Class, Set<Class>> positiveNetworkMsgs, Map<Class, Set<Class>> negativeNetworkMsgs) {
    this.fsmDef = fsmDef;
    this.oexa = oexa;
    this.fsmIdExtractor = fsmIdExtractor;
    this.es = es;
    this.isb = isb;
    this.positiveBasicEvents = positivePorts;
    this.negativeBasicEvents = negativePorts;
    this.positivePatternEvents = positiveNetworkMsgs;
    this.negativePatternEvents = negativeNetworkMsgs;
  }

  public void setProxy(ComponentProxy proxy) {
    this.es.setProxy(proxy);
  }

  public void setupPortsAndHandlers() {
    Pair<List, List> ports = prepareBasicEvents();
    Pair<List, List> networkPorts = preparePatternEvents();
    GenericSetup.portsAndHandledEvents(es.getProxy(), ports.getValue0(), ports.getValue1(),
      networkPorts.getValue0(), networkPorts.getValue1());
  }

  public void setupHandlers() {
    Pair<List, List> basicEvents = prepareBasicEvents();
    Pair<List, List> patternEvents = preparePatternEvents();
    GenericSetup.handledEvents(es.getProxy(), basicEvents.getValue0(), basicEvents.getValue1(),
      patternEvents.getValue0(), patternEvents.getValue1());
  }

  private Pair<List, List> prepareBasicEvents() {
    List pPorts = new LinkedList<>();
    List nPorts = new LinkedList<>();

    for (Map.Entry<Class, Set<Class>> e : positiveBasicEvents.entrySet()) {
      List<Pair<Handler, Class>> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(Pair.with(obeapos, c));
      }
      pPorts.add(Pair.with(e.getKey(), events));
    }
    for (Map.Entry<Class, Set<Class>> e : negativeBasicEvents.entrySet()) {
      List<Pair<Handler, Class>> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(Pair.with(obeaneg, c));
      }
      nPorts.add(Pair.with(e.getKey(), events));
    }
    return Pair.with(pPorts, nPorts);
  }

  private Pair<List, List> preparePatternEvents() {
    List pPorts = new LinkedList<>();
    List nPorts = new LinkedList<>();
    for (Map.Entry<Class, Set<Class>> e : positivePatternEvents.entrySet()) {
      List<Pair<ClassMatchedHandler, Class>> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(Pair.with(opeapos(), c));
      }
      pPorts.add(Pair.with(e.getKey(), events));
    }
    for (Map.Entry<Class, Set<Class>> e : negativePatternEvents.entrySet()) {
      List<Pair<ClassMatchedHandler, Class>> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(Pair.with(opeaneg(), c));
      }
      nPorts.add(Pair.with(e.getKey(), events));
    }
    return Pair.with(pPorts, nPorts);
  }

  public FSMStateName getFSMState(String fsmName, Identifier baseId) {
    FSMDefId fsmDefId = FSMIds.getDefId(fsmName);
    FSMId fsmId = fsmDefId.getFSMId(baseId);
    FSMachine fsm = fsms.get(fsmId);
    return fsm.getState();
  }

  public boolean isEmpty() {
    return fsms.isEmpty();
  }

  public int size() {
    return fsms.size();
  }
}
