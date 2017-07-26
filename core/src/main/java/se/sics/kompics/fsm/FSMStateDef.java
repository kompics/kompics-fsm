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
import java.util.Map;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.handler.FSMStateChangeHandler;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMStateDef {
  
  private Optional<FSMStateChangeHandler> onEntry;
  private Optional<FSMStateChangeHandler> onExit;
  
  private Map<Class, FSMBasicEventHandler> positiveHandlers;
  private Map<Class, FSMBasicEventHandler> negativeHandlers;
  
  private Map<Class, FSMPatternEventHandler> positiveNetworkHandlers;
  private Map<Class, FSMPatternEventHandler> negativeNetworkHandlers;

  private FSMStateDef() {}
  
  public FSMStateDef setOnEntry(Optional<FSMStateChangeHandler> handler) {
    this.onEntry = handler;
    return this;
  }

  public FSMStateDef setOnExit(Optional<FSMStateChangeHandler> handler) {
    this.onExit = handler;
    return this;
  }

  public FSMStateDef setPositiveHandlers(Map<Class, FSMBasicEventHandler> positiveHandlers) {
    this.positiveHandlers = positiveHandlers;
    return this;
  }

  public FSMStateDef setNegativeHandlers(Map<Class, FSMBasicEventHandler> negativeHandlers) {
    this.negativeHandlers = negativeHandlers;
    return this;
  }

  public FSMStateDef setPositiveNetworkHandlers(Map<Class, FSMPatternEventHandler> positiveNetworkHandlers) {
    this.positiveNetworkHandlers = positiveNetworkHandlers;
    return this;
  }

  public FSMStateDef setNegativeNetworkHandlers(Map<Class, FSMPatternEventHandler> negativeNetworkHandlers) {
    this.negativeNetworkHandlers = negativeNetworkHandlers;
    return this;
  }
  
  public static FSMStateDef instance() {
    return new FSMStateDef();
  }
  
  protected FSMState build(FSMStateName state, FSMExternalState es, FSMInternalState is) throws FSMException {
    return new FSMState(state, onEntry, onExit, es, is,
      positiveHandlers, negativeHandlers, positiveNetworkHandlers, negativeNetworkHandlers);
  }
}
