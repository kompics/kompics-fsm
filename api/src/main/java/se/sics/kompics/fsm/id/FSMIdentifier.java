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

import java.util.Objects;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMIdentifier implements Identifier {
  public final Identifier fsmDefId;
  public final Identifier baseId;
  
  public FSMIdentifier(Identifier fsmDefId, Identifier baseId) {
    this.fsmDefId = fsmDefId;
    this.baseId = baseId;
  }

  @Override
  public int partition(int nrPartitions) {
    long longP = fsmDefId.partition(nrPartitions);
    int intP = (int)(longP + baseId.partition(nrPartitions)) % nrPartitions;
    return intP;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + Objects.hashCode(this.fsmDefId);
    hash = 79 * hash + Objects.hashCode(this.baseId);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FSMIdentifier other = (FSMIdentifier) obj;
    if (!Objects.equals(this.fsmDefId, other.fsmDefId)) {
      return false;
    }
    if (!Objects.equals(this.baseId, other.baseId)) {
      return false;
    }
    return true;
  }
  
  @Override
  public int compareTo(Identifier o) {
    FSMIdentifier that = (FSMIdentifier)o;
    int r = this.fsmDefId.compareTo(that.fsmDefId);
    if(r == 0) {
      r = this.baseId.compareTo(that.baseId);
    }
    return r;
  }

  @Override
  public String toString() {
    return "<def:" + fsmDefId + ",base:" + baseId + ">";
  }
}
