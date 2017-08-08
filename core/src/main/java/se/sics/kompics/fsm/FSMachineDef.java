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

import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import org.javatuples.Pair;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.id.FSMIdentifier;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.id.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMachineDef {

  private final FSMIdentifierFactory fsmIdFactory;
  final Identifier fsmDefId;
  final String fsmName;
  private final Map<FSMStateName, FSMStateDef> stateDefs;
  private final Table<FSMStateName, FSMStateName, Boolean> transitionTable;
  private final FSMBasicEventHandler fallbackEventHandler;
  private final FSMPatternEventHandler fallbackMsgHandler;
  private final Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents;
  private final Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents;
  private final Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackPositivePatternEvents;
  private final Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackNegativePatternEvents;

  private FSMachineDef(FSMIdentifierFactory fsmIdFactory, String fsmName, Map<FSMStateName, FSMStateDef> stateDefs,
    Table<FSMStateName, FSMStateName, Boolean> transitionTable, 
    FSMBasicEventHandler defaultFallbackBasicEvent, FSMPatternEventHandler defaultFallbackPatternEvent,
    Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents, 
    Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents,
    Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackPositivePatternEvents, 
    Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackNegativePatternEvents) {
    this.fsmIdFactory = fsmIdFactory;
    this.fsmName = fsmName;
    try {
      this.fsmDefId = fsmIdFactory.getFSMDefId(fsmName);
    } catch (FSMException ex) {
      throw new RuntimeException(ex);
    }
    this.stateDefs = stateDefs;
    this.transitionTable = transitionTable;
    this.fallbackEventHandler = defaultFallbackBasicEvent;
    this.fallbackMsgHandler = defaultFallbackPatternEvent;
    this.fallbackPositiveBasicEvents = fallbackPositiveBasicEvents;
    this.fallbackNegativeBasicEvents = fallbackNegativeBasicEvents;
    this.fallbackPositivePatternEvents = fallbackPositivePatternEvents;
    this.fallbackNegativePatternEvents = fallbackNegativePatternEvents;
  }

  public FSMachine build(Identifier baseId, FSMOnKillAction oka, FSMExternalState es, FSMInternalState is)
    throws FSMException {

    Map<FSMStateName, FSMState> states = new HashMap<>();
    for (Map.Entry<FSMStateName, FSMStateDef> e : stateDefs.entrySet()) {
      FSMState state = e.getValue().build(e.getKey(), es, is);
      states.put(e.getKey(), state);
    }

    return new FSMachine(fsmIdFactory.getFSMId(fsmDefId, baseId), oka, states, transitionTable, fallbackEventHandler, fallbackMsgHandler,
      fallbackPositiveBasicEvents,fallbackNegativeBasicEvents, fallbackPositivePatternEvents, fallbackNegativePatternEvents);
  }
  
  public static FSMachineDef definition(FSMIdentifierFactory fsmIdFactory, String fsmName, 
    Map<FSMStateName, FSMStateDef> stateDefs, Table<FSMStateName, FSMStateName, Boolean> transitionTable, 
    FSMBasicEventHandler defaultFallbackBasicEvent, FSMPatternEventHandler defaultFallbackPatternEvent,
    Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents, 
    Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents,
    Map<Pair<Class, Class>, FSMPatternEventHandler> fallbackPositivePatternEvents, 
    Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackNegativePatternEvents) {
    return new FSMachineDef(fsmIdFactory, fsmName, stateDefs, transitionTable, 
      defaultFallbackBasicEvent, defaultFallbackPatternEvent,
      fallbackPositiveBasicEvents, fallbackNegativeBasicEvents, 
      fallbackPositivePatternEvents, fallbackNegativePatternEvents);
  }
  
  public static FSMachineDef definition(FSMIdentifierFactory fsmIdFactory, String fsmName,  
    Map<FSMStateName, FSMStateDef> stateDefs, Table<FSMStateName, FSMStateName, Boolean> transitionTable, 
    Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents, 
    Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents,
    Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackPositivePatternEvents, 
    Map<Pair<Class, Class>, FSMPatternEventHandler>  fallbackNegativePatternEvents) {
    return new FSMachineDef(fsmIdFactory, fsmName, stateDefs, transitionTable, 
      FSMachine.defaultFallbackBasicEvent, FSMachine.defaultFallbackPatternEvent,
      fallbackPositiveBasicEvents, fallbackNegativeBasicEvents, 
      fallbackPositivePatternEvents, fallbackNegativePatternEvents);
  }
  
  public FSMIdentifier getFsmId(Identifier baseId) {
    return fsmIdFactory.getFSMId(fsmDefId, baseId);
  }
}
