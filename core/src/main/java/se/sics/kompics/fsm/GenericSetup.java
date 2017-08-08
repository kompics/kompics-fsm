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

import java.util.List;
import org.javatuples.Pair;
import org.slf4j.Logger;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.PortCore;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class GenericSetup {

  public static void handledEvents(Logger LOG, ComponentProxy proxy,
    List<Pair<Class, List<Handler>>> positiveBasicEvents,
    List<Pair<Class, List<Handler>>> negativeBasicEvents,
    List<Pair<Class, List<ClassMatchedHandler>>> positivePatternEvents,
    List<Pair<Class, List<ClassMatchedHandler>>> negativePatternEvents) {

    setupBasicEvent(LOG, proxy, true, positiveBasicEvents);
    setupBasicEvent(LOG, proxy, false, negativeBasicEvents);
    setupPatternEvent(LOG, proxy, true, positivePatternEvents);
    setupPatternEvent(LOG, proxy, false, negativePatternEvents);
  }

  private static void setupBasicEvent(Logger LOG, ComponentProxy proxy, boolean portCharge,
    List<Pair<Class, List<Handler>>> basicEvents) {
    for (Pair<Class, List<Handler>> p : basicEvents) {
      PortCore port;
      if (portCharge) {
        try {
          port = proxy.getNegative(p.getValue0()).getPair();
        } catch (Exception ex) {
          port = (PortCore) proxy.requires(p.getValue0());
        }
      } else {
        try {
          port = proxy.getPositive(p.getValue0()).getPair();
        } catch (Exception ex) {
          port = (PortCore) proxy.provides(p.getValue0());
        }
      }
      for (final Handler h : p.getValue1()) {
        LOG.info("{} port:{} event:{}",
          new Object[]{(portCharge ? "positive" : "negative"), p.getValue0(), h.getEventType()});
        proxy.subscribe(h, port);
      }
    }
  }

  private static <C extends KompicsEvent & PatternExtractor<Class, FSMEvent>> void setupPatternEvent(Logger LOG,
    ComponentProxy proxy, boolean portCharge, List<Pair<Class, List<ClassMatchedHandler>>> patternEvents) {
    for (Pair<Class, List<ClassMatchedHandler>> p : patternEvents) {
      PortCore port;
      if (portCharge) {
        try {
          port = proxy.getNegative(p.getValue0()).getPair();
        } catch (Exception ex) {
          port = (PortCore) proxy.requires(p.getValue0());
        }
      } else {
        try {
          port = proxy.getPositive(p.getValue0()).getPair();
        } catch (Exception ex) {
          port = (PortCore) proxy.provides(p.getValue0());
        }
      }
      for (final ClassMatchedHandler e : p.getValue1()) {
        LOG.info("{} port:{} container:{} content:{}",
          new Object[]{(portCharge ? "positive" : "negative"), p.getValue0(), e.getCxtType(), e.pattern()});
        proxy.subscribe(e, port);
      }
    }
  }
}
