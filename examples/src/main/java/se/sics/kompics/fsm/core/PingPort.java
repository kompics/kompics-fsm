/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import se.sics.kompics.PortType;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class PingPort extends PortType {
  {
    request(BasicEvent.Ping.class);
    indication(BasicEvent.Pong.class);
    request(PatternEvent.class);
    indication(PatternEvent.class);
  }
}
