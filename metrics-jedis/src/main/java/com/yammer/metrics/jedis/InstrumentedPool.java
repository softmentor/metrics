package com.yammer.metrics.jedis;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.yammer.metrics.jedis.InstrumentedJedisPool;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * This class is present to just make the instance variable
 * {@link GenericObjectPool} available to {@link InstrumentedJedisPool}. Since
 * {@link Pool} currently has this defined as private.
 * <p>
 * The Jedis version 2.2.x onwards this is not required. Would deprecate this
 * class once we have 2.2.0 released.
 * 
 * Refer:
 * https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients
 * /util/Pool.java
 * 
 * @author softmentor
 * 
 */
public class InstrumentedPool<T> {

	protected GenericObjectPool internalPool;

	protected InstrumentedPool() {
		this.internalPool = null;
	}

	public InstrumentedPool(final GenericObjectPool.Config poolConfig,
			PoolableObjectFactory factory) {
		this.internalPool = new GenericObjectPool(factory, poolConfig);
	}

	@SuppressWarnings("unchecked")
	public T getResource() {
		try {
			return (T) internalPool.borrowObject();
		} catch (Exception e) {
			throw new JedisConnectionException(
					"Could not get a resource from the pool", e);
		}
	}

	public void returnResourceObject(final Object resource) {
		try {
			internalPool.returnObject(resource);
		} catch (Exception e) {
			throw new JedisException(
					"Could not return the resource to the pool", e);
		}
	}

	public void returnBrokenResource(final T resource) {
		returnBrokenResourceObject(resource);
	}

	public void returnResource(final T resource) {
		returnResourceObject(resource);
	}

	protected void returnBrokenResourceObject(final Object resource) {
		try {
			internalPool.invalidateObject(resource);
		} catch (Exception e) {
			throw new JedisException(
					"Could not return the resource to the pool", e);
		}
	}

	public void destroy() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new JedisException("Could not destroy the pool", e);
		}
	}

}
