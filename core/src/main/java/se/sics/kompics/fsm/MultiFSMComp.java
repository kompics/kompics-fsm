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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Start;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class MultiFSMComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(MultiFSMComp.class);
  private String logPrefix = "";

  private final MultiFSM fsm;
  
  public MultiFSMComp(Init init) {
    fsm = init.fsm;
    fsm.setProxy(proxy);

    subscribe(handleStart, control);
    fsm.setupPortsAndHandlers();
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);
    }
  };
  
  @Override
  public void tearDown() {
    //TODO Alex - maybe introduce a CLEANUP message to properly clean things before tear down;
    LOG.warn("{}multi fsm tear down is iffy at best atm - externaly stop all machines");
  }

  public static class Init extends se.sics.kompics.Init<MultiFSMComp> {

    public final MultiFSM fsm;
    
    public Init(MultiFSM fsm) {
      this.fsm = fsm;
    }
  }
}
