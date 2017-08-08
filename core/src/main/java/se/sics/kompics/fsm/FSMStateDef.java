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
import org.javatuples.Pair;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.handler.FSMStateChangeHandler;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMStateDef {
  
  private Optional<FSMStateChangeHandler> onEntry;
  private Optional<FSMStateChangeHandler> onExit;
  
  private Map<Class, FSMBasicEventHandler> positiveBasicHandlers;
  private Map<Class, FSMBasicEventHandler> negativeBasicHandlers;
  
  private Map<Pair<Class, Class>, FSMPatternEventHandler> positivePatternHandlers;
  private Map<Pair<Class, Class>, FSMPatternEventHandler> negativePatternHandlers;

  private FSMStateDef() {}
  
  public FSMStateDef setOnEntry(Optional<FSMStateChangeHandler> handler) {
    this.onEntry = handler;
    return this;
  }

  public FSMStateDef setOnExit(Optional<FSMStateChangeHandler> handler) {
    this.onExit = handler;
    return this;
  }

  public FSMStateDef setPositiveBasicHandlers(Map<Class, FSMBasicEventHandler> positiveHandlers) {
    this.positiveBasicHandlers = positiveHandlers;
    return this;
  }

  public FSMStateDef setNegativeBasicHandlers(Map<Class, FSMBasicEventHandler> negativeHandlers) {
    this.negativeBasicHandlers = negativeHandlers;
    return this;
  }

  public FSMStateDef setPositivePatternHandlers(Map<Pair<Class, Class>, FSMPatternEventHandler> positivePatternHandlers) {
    this.positivePatternHandlers = positivePatternHandlers;
    return this;
  }

  public FSMStateDef setNegativePatternHandlers(Map<Pair<Class, Class>, FSMPatternEventHandler> negativePatternHandlers) {
    this.negativePatternHandlers = negativePatternHandlers;
    return this;
  }
  
  public static FSMStateDef instance() {
    return new FSMStateDef();
  }
  
  protected FSMState build(FSMStateName state, FSMExternalState es, FSMInternalState is) throws FSMException {
    return new FSMState(state, onEntry, onExit, es, is,
      positiveBasicHandlers, negativeBasicHandlers, positivePatternHandlers, negativePatternHandlers);
  }
}
