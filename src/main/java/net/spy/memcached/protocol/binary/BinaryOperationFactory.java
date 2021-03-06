/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached.protocol.binary;

import net.spy.memcached.metrics.MetricCollector;
import net.spy.memcached.metrics.MetricType;
import net.spy.memcached.ops.BaseOperationFactory;
import net.spy.memcached.ops.CASOperation;
import net.spy.memcached.ops.ConcatenationOperation;
import net.spy.memcached.ops.ConcatenationType;
import net.spy.memcached.ops.DeleteOperation;
import net.spy.memcached.ops.FlushOperation;
import net.spy.memcached.ops.GetAndTouchOperation;
import net.spy.memcached.ops.GetOperation;
import net.spy.memcached.ops.GetOperation.Callback;
import net.spy.memcached.ops.GetlOperation;
import net.spy.memcached.ops.GetsOperation;
import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.MultiGetOperationCallback;
import net.spy.memcached.ops.MultiGetsOperationCallback;
import net.spy.memcached.ops.MultiReplicaGetOperationCallback;
import net.spy.memcached.ops.Mutator;
import net.spy.memcached.ops.MutatorOperation;
import net.spy.memcached.ops.NoopOperation;
import net.spy.memcached.ops.ObserveOperation;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.ReplicaGetOperation;
import net.spy.memcached.ops.ReplicaGetsOperation;
import net.spy.memcached.ops.SASLAuthOperation;
import net.spy.memcached.ops.SASLMechsOperation;
import net.spy.memcached.ops.SASLStepOperation;
import net.spy.memcached.ops.StatsOperation;
import net.spy.memcached.ops.StoreOperation;
import net.spy.memcached.ops.StoreType;
import net.spy.memcached.ops.TapOperation;
import net.spy.memcached.ops.TouchOperation;
import net.spy.memcached.ops.UnlockOperation;
import net.spy.memcached.ops.VersionOperation;
import net.spy.memcached.tapmessage.RequestMessage;
import net.spy.memcached.tapmessage.TapOpcode;

import javax.security.auth.callback.CallbackHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Factory for binary operations.
 */
public class BinaryOperationFactory extends BaseOperationFactory {

  public BinaryOperationFactory(MetricCollector metricCollector, MetricType metricType) {
    super(metricCollector, metricType);
  }

  @Override
  protected DeleteOperation createDelete(String key, DeleteOperation.Callback operationCallback) {
    return new DeleteOperationImpl(key, operationCallback);
  }

  @Override
  protected DeleteOperation createDelete(String key, long cas,
    DeleteOperation.Callback operationCallback) {
    return new DeleteOperationImpl(key, cas, operationCallback);
  }

  @Override
  protected UnlockOperation createUnlock(String key, long casId,
          OperationCallback cb) {
    return new UnlockOperationImpl(key, casId, cb);
  }

  @Override
  protected ObserveOperation createObserve(String key, long casId, int index,
          ObserveOperation.Callback cb) {
    return new ObserveOperationImpl(key, casId, index, cb);
  }

  @Override
  protected FlushOperation createFlush(int delay, OperationCallback cb) {
    return new FlushOperationImpl(cb);
  }

  @Override
  protected GetAndTouchOperation createGetAndTouch(String key, int expiration,
      GetAndTouchOperation.Callback cb) {
    return new GetAndTouchOperationImpl(key, expiration, cb);
  }

  @Override
  protected GetOperation createGet(String key, Callback callback) {
    return new GetOperationImpl(key, callback);
  }

  @Override
  protected ReplicaGetOperation createReplicaGet(String key, int index,
    ReplicaGetOperation.Callback callback) {
    return new ReplicaGetOperationImpl(key, index, callback);
  }

  @Override
  protected ReplicaGetsOperation createReplicaGets(String key, int index,
    ReplicaGetsOperation.Callback callback) {
    return new ReplicaGetsOperationImpl(key, index, callback);
  }

  @Override
  protected GetOperation createGet(Collection<String> value, Callback cb) {
    return new MultiGetOperationImpl(value, cb);
  }

  @Override
  protected GetlOperation createGetl(String key, int exp, GetlOperation.Callback cb) {
    return new GetlOperationImpl(key, exp, cb);
  }

  @Override
  protected GetsOperation createGets(String key, GetsOperation.Callback cb) {
    return new GetsOperationImpl(key, cb);
  }

  @Override
  protected StatsOperation createKeyStats(String key, StatsOperation.Callback cb) {
    return new KeyStatsOperationImpl(key, cb);
  }

  @Override
  protected MutatorOperation createMutate(Mutator m, String key, long by, long def,
      int exp, OperationCallback cb) {
    return new MutatorOperationImpl(m, key, by, def, exp, cb);
  }

  @Override
  protected StatsOperation createStats(String arg,
      net.spy.memcached.ops.StatsOperation.Callback cb) {
    return new StatsOperationImpl(arg, cb);
  }

  @Override
  protected StoreOperation createStore(StoreType storeType, String key, int flags,
      int exp, byte[] data, StoreOperation.Callback cb) {
    return new StoreOperationImpl(storeType, key, flags, exp, data, 0, cb);
  }

  @Override
  protected TouchOperation createTouch(String key, int expiration,
      OperationCallback cb) {
    return new TouchOperationImpl(key, expiration, cb);
  }

  @Override
  protected VersionOperation createVersion(OperationCallback cb) {
    return new VersionOperationImpl(cb);
  }

  @Override
  protected NoopOperation createNoop(OperationCallback cb) {
    return new NoopOperationImpl(cb);
  }

  @Override
  protected CASOperation createCas(StoreType type, String key, long casId, int flags,
      int exp, byte[] data, StoreOperation.Callback cb) {
    return new StoreOperationImpl(type, key, flags, exp, data, casId, cb);
  }

  @Override
  protected ConcatenationOperation createCat(ConcatenationType catType, long casId,
      String key, byte[] data, OperationCallback cb) {
    return new ConcatenationOperationImpl(catType, key, data, casId, cb);
  }

  @Override
  protected SASLAuthOperation createSaslAuth(String[] mech, String serverName,
      Map<String, ?> props, CallbackHandler cbh, OperationCallback cb) {
    return new SASLAuthOperationImpl(mech, serverName, props, cbh, cb);
  }

  @Override
  protected SASLMechsOperation createSaslMechs(OperationCallback cb) {
    return new SASLMechsOperationImpl(cb);
  }

  @Override
  protected SASLStepOperation createSaslStep(String[] mech, byte[] challenge,
      String serverName, Map<String, ?> props, CallbackHandler cbh,
      OperationCallback cb) {
    return new SASLStepOperationImpl(mech, challenge, serverName, props, cbh,
        cb);
  }

  @Override
  protected TapOperation createTapBackfill(String id, long date, OperationCallback cb) {
    return new TapBackfillOperationImpl(id, date, cb);
  }

  @Override
  protected TapOperation createTapCustom(String id, RequestMessage message,
      OperationCallback cb) {
    return new TapCustomOperationImpl(id, message, cb);
  }

  @Override
  protected TapOperation createTapAck(TapOpcode opcode, int opaque, OperationCallback cb) {
    return new TapAckOperationImpl(opcode, opaque, cb);
  }

  @Override
  protected TapOperation createTapDump(String id, OperationCallback cb) {
    return new TapDumpOperationImpl(id, cb);
  }

  @Override
  protected Collection<? extends Operation> cloneGet(KeyedOperation op) {
    Collection<Operation> rv = new ArrayList<Operation>();
    GetOperation.Callback getCb = null;
    GetsOperation.Callback getsCb = null;
    ReplicaGetOperation.Callback replicaGetCb = null;
    if (op.getCallback() instanceof GetOperation.Callback) {
      getCb =
          new MultiGetOperationCallback(op.getCallback(), op.getKeys().size());
    } else if(op.getCallback() instanceof ReplicaGetOperation.Callback) {
      replicaGetCb =
       new MultiReplicaGetOperationCallback(op.getCallback(), op.getKeys().size());
    } else {
      getsCb =
          new MultiGetsOperationCallback(op.getCallback(), op.getKeys().size());
    }
    for (String k : op.getKeys()) {
      if(getCb != null) {
        rv.add(get(k, getCb));
      } else if(getsCb != null) {
        rv.add(gets(k, getsCb));
      } else {
        rv.add(replicaGet(k, ((ReplicaGetOperationImpl)op).getReplicaIndex() ,replicaGetCb));
      }
    }
    return rv;
  }
}
