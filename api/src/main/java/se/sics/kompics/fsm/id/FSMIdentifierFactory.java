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
package se.sics.kompics.fsm.id;

import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.util.ByteIdentifier;
import se.sics.kompics.util.Identifier;
import se.sics.kompics.util.IdentifierFactory;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public interface FSMIdentifierFactory extends IdentifierFactory {
  public static final String CONFIG_KEY = "fsm_identifier_factory";
  public static final FSMIdentifierFactory DEFAULT = new FSMIdentifierFactory() {
    private byte fsmId = (byte) 0;
    private final Map<String, Identifier> reservedFSMDIds = new HashMap<>();

    @Override
    public synchronized Identifier registerFSMDefId(String fsmName) throws FSMException {
      if (fsmId >= 255) {
        throw new FSMException("out of id space");
      }
      Identifier fsmDefId = new ByteIdentifier(fsmId++);
      if (reservedFSMDIds.containsKey(fsmName)) {
        throw new FSMException("clash - owner name:" + fsmName + " is registered with:" + reservedFSMDIds.get(fsmName));
      }
      if (reservedFSMDIds.values().contains(fsmDefId)) {
        throw new FSMException("clash - fsmdDefId:" + fsmDefId + " belongs to owner:" + fsmName);
      }
      reservedFSMDIds.put(fsmName, fsmDefId);
      return fsmDefId;
    }

    @Override
    public synchronized Identifier getFSMDefId(String fsmName) throws FSMException {
      Identifier fsmDefId = reservedFSMDIds.get(fsmName);
      if (fsmDefId == null) {
        throw new FSMException("owner is not registered");
      }
      return fsmDefId;
    }

    @Override
    public FSMIdentifier getFSMId(Identifier fsmDefId, Identifier baseId) {
      return new FSMIdentifier(fsmDefId, baseId);
    }

    @Override
    public Identifier getFSMDefId(Identifier fsmId) {
      FSMIdentifier id = (FSMIdentifier) fsmId;
      return id.fsmDefId;
    }
  };

  public Identifier registerFSMDefId(String fsmName) throws FSMException;

  public Identifier getFSMDefId(String fsmName) throws FSMException;

  public FSMIdentifier getFSMId(Identifier fsmDefId, Identifier baseId);
  
  public Identifier getFSMDefId(Identifier fsmId);
}
