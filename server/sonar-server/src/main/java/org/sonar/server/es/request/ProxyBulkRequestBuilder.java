/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.es.request;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.sonar.core.profiling.Profiling;
import org.sonar.core.profiling.StopWatch;

import java.util.Set;

public class ProxyBulkRequestBuilder extends BulkRequestBuilder {

  private final Profiling profiling;

  public ProxyBulkRequestBuilder(Client client, Profiling profiling) {
    super(client);
    this.profiling = profiling;
  }

  @Override
  public BulkResponse get() {
    StopWatch fullProfile = profiling.start("bulk", Profiling.Level.FULL);
    try {
      return super.execute().actionGet();
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Fail to execute %s", toString()), e);
    } finally {
      if (profiling.isProfilingEnabled(Profiling.Level.BASIC)) {
        fullProfile.stop("%s", toString());
      }
    }
  }

  @Override
  public BulkResponse get(TimeValue timeout) {
    throw unsupported();
  }

  @Override
  public BulkResponse get(String timeout) {
    // easy to implement if needed (copy get())
    throw unsupported();
  }

  @Override
  public ListenableActionFuture<BulkResponse> execute() {
    throw new UnsupportedOperationException("execute() should not be called as it's used for asynchronous");
  }

  private UnsupportedOperationException unsupported() {
    throw new UnsupportedOperationException("See " + ProxyBulkRequestBuilder.class.getName());
  }

  @Override
  public String toString() {
    StringBuilder message = new StringBuilder();
    message.append("Bulk[");
    HashMultiset<BulkRequestKey> groupedRequests = HashMultiset.create();
    for (int i=0 ; i<request.requests().size() ; i++) {
      ActionRequest item = request.requests().get(i);
      String requestType, index, docType;
      if (item instanceof IndexRequest) {
        IndexRequest request = (IndexRequest) item;
        requestType = "index";
        index = request.index();
        docType = request.type();
      } else if (item instanceof UpdateRequest) {
        UpdateRequest request = (UpdateRequest) item;
        requestType = "update";
        index = request.index();
        docType = request.type();
      } else if (item instanceof DeleteRequest) {
        DeleteRequest request = (DeleteRequest) item;
        requestType = "delete";
        index = request.index();
        docType = request.type();
      } else {
        // Cannot happen, not allowed by BulkRequest's contract
        throw new IllegalStateException("Unsupported bulk request type: " + item.getClass());
      }
      groupedRequests.add(new BulkRequestKey(requestType, index, docType));
    }

    Set<Entry<BulkRequestKey>> entrySet = groupedRequests.entrySet();
    int size = entrySet.size();
    int current = 0;
    for (Entry<BulkRequestKey> requestEntry : entrySet) {
      message.append(requestEntry.getCount()).append(" ").append(requestEntry.getElement().toString());
      current++;
      if (current < size) {
        message.append(", ");
      }
    }

    message.append("]");
    return message.toString();
  }

  private static class BulkRequestKey {
    private String requestType;
    private String index;
    private String docType;

    private BulkRequestKey(String requestType, String index, String docType) {
      this.requestType = requestType;
      this.index = index;
      this.docType = docType;
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
      return String.format("%s request(s) on index %s and type %s", requestType, index, docType);
    }
  }
}