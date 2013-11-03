package com.codahale.metrics.jedis;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

/**
 * Inheriting all the methods that need to be proxied. This interface would not
 * be required if there are cleaner interface definitions for all jedis
 * commands.
 * <p>
 * Current we observe that some commands are present in {@link Jedis}this
 * inherits from {@link JedisCommands} however there are still some commands not
 * inherited. Same is the case for {@link BinaryJedis} this inherits from
 * {@link BinaryJedisCommands} however there are still some commands not
 * inherited.
 * 
 * @author softmentor
 * 
 */
public interface IJedisProxy extends IJedis, IBinaryJedis, BinaryJedisCommands,
		JedisCommands {

}
