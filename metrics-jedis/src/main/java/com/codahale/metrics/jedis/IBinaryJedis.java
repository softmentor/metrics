package com.codahale.metrics.jedis;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.ZParams;

/**
 * @author softmentor
 *
 */
public interface IBinaryJedis {

	String ping();

	/**
	 * Ask the server to silently close the connection.
	 */
	String quit();

	/**
	 * Remove the specified keys. If a given key does not exist no operation is
	 * performed for this key. The command returns the number of keys removed.
	 * 
	 * Time complexity: O(1)
	 * 
	 * @param keys
	 * @return Integer reply, specifically: an integer greater than 0 if one or
	 *         more keys were removed 0 if none of the specified key existed
	 */
	Long del(byte[]... keys);

	/**
	 * Delete all the keys of the currently selected DB. This command never
	 * fails.
	 * 
	 * @return Status code reply
	 */
	String flushDB();

	/**
	 * Returns all the keys matching the glob-style pattern as space separated
	 * strings. For example if you have in the database the keys "foo" and
	 * "foobar" the command "KEYS foo*" will return "foo foobar".
	 * <p>
	 * Note that while the time complexity for this operation is O(n) the
	 * constant times are pretty low. For example Redis running on an entry
	 * level laptop can scan a 1 million keys database in 40 milliseconds.
	 * <b>Still it's better to consider this one of the slow commands that may
	 * ruin the DB performance if not used with care.</b>
	 * <p>
	 * In other words this command is intended only for debugging and special
	 * operations like creating a script to change the DB schema. Don't use it
	 * in your normal code. Use Redis Sets in order to group together a subset
	 * of objects.
	 * <p>
	 * Glob style patterns examples:
	 * <ul>
	 * <li>h?llo will match hello hallo hhllo
	 * <li>h*llo will match hllo heeeello
	 * <li>h[ae]llo will match hello and hallo, but not hillo
	 * </ul>
	 * <p>
	 * Use \ to escape special chars if you want to match them verbatim.
	 * <p>
	 * Time complexity: O(n) (with n being the number of keys in the DB, and
	 * assuming keys and pattern of limited length)
	 * 
	 * @param pattern
	 * @return Multi bulk reply
	 */
	Set<byte[]> keys(byte[] pattern);

	/**
	 * Return a randomly selected key from the currently selected DB.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @return Singe line reply, specifically the randomly selected key or an
	 *         empty string is the database is empty
	 */
	byte[] randomBinaryKey();

	/**
	 * Atomically renames the key oldkey to newkey. If the source and
	 * destination name are the same an error is returned. If newkey already
	 * exists it is overwritten.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @param oldkey
	 * @param newkey
	 * @return Status code repy
	 */
	String rename(byte[] oldkey, byte[] newkey);

	/**
	 * Rename oldkey into newkey but fails if the destination key newkey already
	 * exists.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @param oldkey
	 * @param newkey
	 * @return Integer reply, specifically: 1 if the key was renamed 0 if the
	 *         target key already exist
	 */
	Long renamenx(byte[] oldkey, byte[] newkey);

	/**
	 * Return the number of keys in the currently selected database.
	 * 
	 * @return Integer reply
	 */
	Long dbSize();

	/**
	 * Select the DB with having the specified zero-based numeric index. For
	 * default every new client connection is automatically selected to DB 0.
	 * 
	 * @param index
	 * @return Status code reply
	 */
	String select(int index);

	/**
	 * Move the specified key from the currently selected DB to the specified
	 * destination DB. Note that this command returns 1 only if the key was
	 * successfully moved, and 0 if the target key was already there or if the
	 * source key was not found at all, so it is possible to use MOVE as a
	 * locking primitive.
	 * 
	 * @param key
	 * @param dbIndex
	 * @return Integer reply, specifically: 1 if the key was moved 0 if the key
	 *         was not moved because already present on the target DB or was not
	 *         found in the current DB.
	 */
	Long move(byte[] key, int dbIndex);

	/**
	 * Delete all the keys of all the existing databases, not just the currently
	 * selected one. This command never fails.
	 * 
	 * @return Status code reply
	 */
	String flushAll();

	/**
	 * Get the values of all the specified keys. If one or more keys dont exist
	 * or is not of type String, a 'nil' value is returned instead of the value
	 * of the specified key, but the operation never fails.
	 * <p>
	 * Time complexity: O(1) for every key
	 * 
	 * @param keys
	 * @return Multi bulk reply
	 */
	List<byte[]> mget(byte[]... keys);

	/**
	 * Set the the respective keys to the respective values.
	 * {@link #mset(String...) MSET} will replace old values with new values,
	 * while MSETNX will not perform any operation at all even if just a single
	 * key already exists.
	 * <p>
	 * Because of this semantic MSETNX can be used in order to set different
	 * keys representing different fields of an unique logic object in a way
	 * that ensures that either all the fields or none at all are set.
	 * <p>
	 * Both MSET and MSETNX are atomic operations. This means that for instance
	 * if the keys A and B are modified, another client talking to Redis can
	 * either see the changes to both A and B at once, or no modification at
	 * all.
	 * 
	 * @see #mset(String...)
	 * 
	 * @param keysvalues
	 * @return Integer reply, specifically: 1 if the all the keys were set 0 if
	 *         no key was set (at least one key already existed)
	 */
	Long msetnx(byte[]... keysvalues);

	/**
	 * Remove the specified field from an hash stored at key.
	 * <p>
	 * <b>Time complexity:</b> O(1)
	 * 
	 * @param key
	 * @param field
	 * @return If the field was present in the hash it is deleted and 1 is
	 *         returned, otherwise 0 is returned and no operation is performed.
	 */
	Long hdel(final byte[] key, final byte[]... fields);

	/**
	 * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
	 * stored at key. If the key does not exist an empty list is created just
	 * before the append operation. If the key exists but is not a List an error
	 * is returned.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @see BinaryJedis#lpush(String, String)
	 * 
	 * @param key
	 * @param string
	 * @return Integer reply, specifically, the number of elements inside the
	 *         list after the push operation.
	 */
	Long rpush(final byte[] key, final byte[]... strings);

	/**
	 * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
	 * stored at key. If the key does not exist an empty list is created just
	 * before the append operation. If the key exists but is not a List an error
	 * is returned.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @see BinaryJedis#rpush(String, String)
	 * 
	 * @param key
	 * @param string
	 * @return Integer reply, specifically, the number of elements inside the
	 *         list after the push operation.
	 */
	Long lpush(final byte[] key, final byte[]... strings);

	/**
	 * Atomically return and remove the last (tail) element of the srckey list,
	 * and push the element as the first (head) element of the dstkey list. For
	 * example if the source list contains the elements "a","b","c" and the
	 * destination list contains the elements "foo","bar" after an RPOPLPUSH
	 * command the content of the two lists will be "a","b" and "c","foo","bar".
	 * <p>
	 * If the key does not exist or the list is already empty the special value
	 * 'nil' is returned. If the srckey and dstkey are the same the operation is
	 * equivalent to removing the last element from the list and pusing it as
	 * first element of the list, so it's a "list rotation" command.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @param srckey
	 * @param dstkey
	 * @return Bulk reply
	 */
	byte[] rpoplpush(byte[] srckey, byte[] dstkey);

	/**
	 * Add the specified member to the set value stored at key. If member is
	 * already a member of the set no operation is performed. If key does not
	 * exist a new set with the specified member as sole member is created. If
	 * the key exists but does not hold a set value an error is returned.
	 * <p>
	 * Time complexity O(1)
	 * 
	 * @param key
	 * @param member
	 * @return Integer reply, specifically: 1 if the new element was added 0 if
	 *         the element was already a member of the set
	 */
	Long sadd(final byte[] key, final byte[]... members) ;

	/**
	 * Remove the specified member from the set value stored at key. If member
	 * was not a member of the set no operation is performed. If key does not
	 * hold a set value an error is returned.
	 * <p>
	 * Time complexity O(1)
	 * 
	 * @param key
	 * @param member
	 * @return Integer reply, specifically: 1 if the new element was removed 0
	 *         if the new element was not a member of the set
	 */
	Long srem(final byte[] key, final byte[]... member);

	/**
	 * Move the specifided member from the set at srckey to the set at dstkey.
	 * This operation is atomic, in every given moment the element will appear
	 * to be in the source or destination set for accessing clients.
	 * <p>
	 * If the source set does not exist or does not contain the specified
	 * element no operation is performed and zero is returned, otherwise the
	 * element is removed from the source set and added to the destination set.
	 * On success one is returned, even if the element was already present in
	 * the destination set.
	 * <p>
	 * An error is raised if the source or destination keys contain a non Set
	 * value.
	 * <p>
	 * Time complexity O(1)
	 * 
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return Integer reply, specifically: 1 if the element was moved 0 if the
	 *         element was not found on the first set and no operation was
	 *         performed
	 */
	Long smove(byte[] srckey, byte[] dstkey, byte[] member);

	/**
	 * Return the members of a set resulting from the intersection of all the
	 * sets hold at the specified keys. Like in
	 * {@link #lrange(String, int, int) LRANGE} the result is sent to the client
	 * as a multi-bulk reply (see the protocol specification for more
	 * information). If just a single key is specified, then this command
	 * produces the same result as {@link #smembers(String) SMEMBERS}. Actually
	 * SMEMBERS is just syntax sugar for SINTER.
	 * <p>
	 * Non existing keys are considered like empty sets, so if one of the keys
	 * is missing an empty set is returned (since the intersection with an empty
	 * set always is an empty set).
	 * <p>
	 * Time complexity O(N*M) worst case where N is the cardinality of the
	 * smallest set and M the number of sets
	 * 
	 * @param keys
	 * @return Multi bulk reply, specifically the list of common elements.
	 */
	Set<byte[]> sinter(byte[]... keys);

	/**
	 * This commnad works exactly like {@link #sinter(String...) SINTER} but
	 * instead of being returned the resulting set is sotred as dstkey.
	 * <p>
	 * Time complexity O(N*M) worst case where N is the cardinality of the
	 * smallest set and M the number of sets
	 * 
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	Long sinterstore(byte[] dstkey, byte[]... keys);

	/**
	 * Return the members of a set resulting from the union of all the sets hold
	 * at the specified keys. Like in {@link #lrange(String, int, int) LRANGE}
	 * the result is sent to the client as a multi-bulk reply (see the protocol
	 * specification for more information). If just a single key is specified,
	 * then this command produces the same result as {@link #smembers(String)
	 * SMEMBERS}.
	 * <p>
	 * Non existing keys are considered like empty sets.
	 * <p>
	 * Time complexity O(N) where N is the total number of elements in all the
	 * provided sets
	 * 
	 * @param keys
	 * @return Multi bulk reply, specifically the list of common elements.
	 */
	Set<byte[]> sunion(byte[]... keys);

	/**
	 * This command works exactly like {@link #sunion(String...) SUNION} but
	 * instead of being returned the resulting set is stored as dstkey. Any
	 * existing value in dstkey will be over-written.
	 * <p>
	 * Time complexity O(N) where N is the total number of elements in all the
	 * provided sets
	 * 
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	Long sunionstore(byte[] dstkey, byte[]... keys);

	/**
	 * Return the difference between the Set stored at key1 and all the Sets
	 * key2, ..., keyN
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * key1 = [x, a, b, c]
	 * key2 = [c]
	 * key3 = [a, d]
	 * SDIFF key1,key2,key3 => [x, b]
	 * </pre>
	 * 
	 * Non existing keys are considered like empty sets.
	 * <p>
	 * <b>Time complexity:</b>
	 * <p>
	 * O(N) with N being the total number of elements of all the sets
	 * 
	 * @param keys
	 * @return Return the members of a set resulting from the difference between
	 *         the first set provided and all the successive sets.
	 */
	Set<byte[]> sdiff(byte[]... keys);

	/**
	 * This command works exactly like {@link #sdiff(String...) SDIFF} but
	 * instead of being returned the resulting set is stored in dstkey.
	 * 
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	Long sdiffstore(byte[] dstkey, byte[]... keys);

	/**
	 * Remove the specified member from the sorted set value stored at key. If
	 * member was not a member of the set no operation is performed. If key does
	 * not not hold a set value an error is returned.
	 * <p>
	 * Time complexity O(log(N)) with N being the number of elements in the
	 * sorted set
	 * 
	 * 
	 * 
	 * @param key
	 * @param member
	 * @return Integer reply, specifically: 1 if the new element was removed 0
	 *         if the new element was not a member of the set
	 */
	Long zrem(final byte[] key, final byte[]... members) ;

	Transaction multi();

	List<Object> multi(TransactionBlock jedisTransaction);

	void connect();

	void disconnect();

	String watch(byte[]... keys);

	String unwatch();

	/**
	 * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
	 * commands as blocking versions of LPOP and RPOP able to block if the
	 * specified keys don't exist or contain empty lists.
	 * <p>
	 * The following is a description of the exact semantic. We describe BLPOP
	 * but the two commands are identical, the only difference is that BLPOP
	 * pops the element from the left (head) of the list, and BRPOP pops from
	 * the right (tail).
	 * <p>
	 * <b>Non blocking behavior</b>
	 * <p>
	 * When BLPOP is called, if at least one of the specified keys contain a non
	 * empty list, an element is popped from the head of the list and returned
	 * to the caller together with the name of the key (BLPOP returns a two
	 * elements array, the first element is the key, the second the popped
	 * value).
	 * <p>
	 * Keys are scanned from left to right, so for instance if you issue BLPOP
	 * list1 list2 list3 0 against a dataset where list1 does not exist but
	 * list2 and list3 contain non empty lists, BLPOP guarantees to return an
	 * element from the list stored at list2 (since it is the first non empty
	 * list starting from the left).
	 * <p>
	 * <b>Blocking behavior</b>
	 * <p>
	 * If none of the specified keys exist or contain non empty lists, BLPOP
	 * blocks until some other client performs a LPUSH or an RPUSH operation
	 * against one of the lists.
	 * <p>
	 * Once new data is present on one of the lists, the client finally returns
	 * with the name of the key unblocking it and the popped value.
	 * <p>
	 * When blocking, if a non-zero timeout is specified, the client will
	 * unblock returning a nil special value if the specified amount of seconds
	 * passed without a push operation against at least one of the specified
	 * keys.
	 * <p>
	 * The timeout argument is interpreted as an integer value. A timeout of
	 * zero means instead to block forever.
	 * <p>
	 * <b>Multiple clients blocking for the same keys</b>
	 * <p>
	 * Multiple clients can block for the same key. They are put into a queue,
	 * so the first to be served will be the one that started to wait earlier,
	 * in a first-blpopping first-served fashion.
	 * <p>
	 * <b>blocking POP inside a MULTI/EXEC transaction</b>
	 * <p>
	 * BLPOP and BRPOP can be used with pipelining (sending multiple commands
	 * and reading the replies in batch), but it does not make sense to use
	 * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
	 * <p>
	 * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
	 * return a multi-bulk nil reply, exactly what happens when the timeout is
	 * reached. If you like science fiction, think at it like if inside
	 * MULTI/EXEC the time will flow at infinite speed :)
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @see #brpop(int, String...)
	 * 
	 * @param timeout
	 * @param keys
	 * @return BLPOP returns a two-elements array via a multi bulk reply in
	 *         order to return both the unblocking key and the popped value.
	 *         <p>
	 *         When a non-zero timeout is specified, and the BLPOP operation
	 *         timed out, the return value is a nil multi bulk reply. Most
	 *         client values will return false or nil accordingly to the
	 *         programming language used.
	 */
	List<byte[]> blpop(int timeout, byte[]... keys);

	/**
	 * Sort a Set or a List accordingly to the specified parameters and store
	 * the result at dstkey.
	 * 
	 * @see #sort(String, SortingParams)
	 * @see #sort(String)
	 * @see #sort(String, String)
	 * 
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return The number of elements of the list at dstkey.
	 */
	Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey);

	/**
	 * Sort a Set or a List and Store the Result at dstkey.
	 * <p>
	 * Sort the elements contained in the List, Set, or Sorted Set value at key
	 * and store the result at dstkey. By default sorting is numeric with
	 * elements being compared as double precision floating point numbers. This
	 * is the simplest form of SORT.
	 * 
	 * @see #sort(String)
	 * @see #sort(String, SortingParams)
	 * @see #sort(String, SortingParams, String)
	 * 
	 * @param key
	 * @param dstkey
	 * @return The number of elements of the list at dstkey.
	 */
	Long sort(byte[] key, byte[] dstkey);

	/**
	 * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
	 * commands as blocking versions of LPOP and RPOP able to block if the
	 * specified keys don't exist or contain empty lists.
	 * <p>
	 * The following is a description of the exact semantic. We describe BLPOP
	 * but the two commands are identical, the only difference is that BLPOP
	 * pops the element from the left (head) of the list, and BRPOP pops from
	 * the right (tail).
	 * <p>
	 * <b>Non blocking behavior</b>
	 * <p>
	 * When BLPOP is called, if at least one of the specified keys contain a non
	 * empty list, an element is popped from the head of the list and returned
	 * to the caller together with the name of the key (BLPOP returns a two
	 * elements array, the first element is the key, the second the popped
	 * value).
	 * <p>
	 * Keys are scanned from left to right, so for instance if you issue BLPOP
	 * list1 list2 list3 0 against a dataset where list1 does not exist but
	 * list2 and list3 contain non empty lists, BLPOP guarantees to return an
	 * element from the list stored at list2 (since it is the first non empty
	 * list starting from the left).
	 * <p>
	 * <b>Blocking behavior</b>
	 * <p>
	 * If none of the specified keys exist or contain non empty lists, BLPOP
	 * blocks until some other client performs a LPUSH or an RPUSH operation
	 * against one of the lists.
	 * <p>
	 * Once new data is present on one of the lists, the client finally returns
	 * with the name of the key unblocking it and the popped value.
	 * <p>
	 * When blocking, if a non-zero timeout is specified, the client will
	 * unblock returning a nil special value if the specified amount of seconds
	 * passed without a push operation against at least one of the specified
	 * keys.
	 * <p>
	 * The timeout argument is interpreted as an integer value. A timeout of
	 * zero means instead to block forever.
	 * <p>
	 * <b>Multiple clients blocking for the same keys</b>
	 * <p>
	 * Multiple clients can block for the same key. They are put into a queue,
	 * so the first to be served will be the one that started to wait earlier,
	 * in a first-blpopping first-served fashion.
	 * <p>
	 * <b>blocking POP inside a MULTI/EXEC transaction</b>
	 * <p>
	 * BLPOP and BRPOP can be used with pipelining (sending multiple commands
	 * and reading the replies in batch), but it does not make sense to use
	 * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
	 * <p>
	 * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
	 * return a multi-bulk nil reply, exactly what happens when the timeout is
	 * reached. If you like science fiction, think at it like if inside
	 * MULTI/EXEC the time will flow at infinite speed :)
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @see #blpop(int, String...)
	 * 
	 * @param timeout
	 * @param keys
	 * @return BLPOP returns a two-elements array via a multi bulk reply in
	 *         order to return both the unblocking key and the popped value.
	 *         <p>
	 *         When a non-zero timeout is specified, and the BLPOP operation
	 *         timed out, the return value is a nil multi bulk reply. Most
	 *         client values will return false or nil accordingly to the
	 *         programming language used.
	 */
	List<byte[]> brpop(int timeout, byte[]... keys);

	/**
	 * Request for authentication in a password protected Redis server. A Redis
	 * server can be instructed to require a password before to allow clients to
	 * issue commands. This is done using the requirepass directive in the Redis
	 * configuration file. If the password given by the client is correct the
	 * server replies with an OK status code reply and starts accepting commands
	 * from the client. Otherwise an error is returned and the clients needs to
	 * try a new password. Note that for the high performance nature of Redis it
	 * is possible to try a lot of passwords in parallel in very short time, so
	 * make sure to generate a strong and very long password so that this attack
	 * is infeasible.
	 * 
	 * @param password
	 * @return Status code reply
	 */
	String auth(String password);

	/**
	 * Starts a pipeline, which is a very efficient way to send lots of command
	 * and read all the responses when you finish sending them. Try to avoid
	 * this version and use pipelined() when possible as it will give better
	 * performance.
	 * 
	 * @param jedisPipeline
	 * @return The results of the command in the same order you've run them.
	 */
	List<Object> pipelined(PipelineBlock jedisPipeline);

	Pipeline pipelined();

	void subscribe(JedisPubSub jedisPubSub, String... channels);

	Long publish(String channel, String message);

	void psubscribe(JedisPubSub jedisPubSub, String... patterns);

	Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max);

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through
	 * kN, and stores it at dstkey. It is mandatory to provide the number of
	 * input keys N, before passing the input keys and the other (optional)
	 * arguments.
	 * <p>
	 * As the terms imply, the {@link #zinterstore(String, String...)
	 * ZINTERSTORE} command requires an element to be present in each of the
	 * given inputs to be inserted in the result. The
	 * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
	 * elements across all inputs.
	 * <p>
	 * Using the WEIGHTS option, it is possible to add weight to each input
	 * sorted set. This means that the score of each element in the sorted set
	 * is first multiplied by this weight before being passed to the
	 * aggregation. When this option is not given, all weights default to 1.
	 * <p>
	 * With the AGGREGATE option, it's possible to specify how the results of
	 * the union or intersection are aggregated. This option defaults to SUM,
	 * where the score of an element is summed across the inputs where it
	 * exists. When this option is set to be either MIN or MAX, the resulting
	 * set will contain the minimum or maximum score of an element across the
	 * inputs where it exists.
	 * <p>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
	 * sizes of the input sorted sets, and M being the number of elements in the
	 * resulting sorted set
	 * 
	 * @see #zunionstore(String, String...)
	 * @see #zunionstore(String, ZParams, String...)
	 * @see #zinterstore(String, String...)
	 * @see #zinterstore(String, ZParams, String...)
	 * 
	 * @param dstkey
	 * @param sets
	 * @return Integer reply, specifically the number of elements in the sorted
	 *         set at dstkey
	 */
	Long zunionstore(byte[] dstkey, byte[]... sets);

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through
	 * kN, and stores it at dstkey. It is mandatory to provide the number of
	 * input keys N, before passing the input keys and the other (optional)
	 * arguments.
	 * <p>
	 * As the terms imply, the {@link #zinterstore(String, String...)
	 * ZINTERSTORE} command requires an element to be present in each of the
	 * given inputs to be inserted in the result. The
	 * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
	 * elements across all inputs.
	 * <p>
	 * Using the WEIGHTS option, it is possible to add weight to each input
	 * sorted set. This means that the score of each element in the sorted set
	 * is first multiplied by this weight before being passed to the
	 * aggregation. When this option is not given, all weights default to 1.
	 * <p>
	 * With the AGGREGATE option, it's possible to specify how the results of
	 * the union or intersection are aggregated. This option defaults to SUM,
	 * where the score of an element is summed across the inputs where it
	 * exists. When this option is set to be either MIN or MAX, the resulting
	 * set will contain the minimum or maximum score of an element across the
	 * inputs where it exists.
	 * <p>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
	 * sizes of the input sorted sets, and M being the number of elements in the
	 * resulting sorted set
	 * 
	 * @see #zunionstore(String, String...)
	 * @see #zunionstore(String, ZParams, String...)
	 * @see #zinterstore(String, String...)
	 * @see #zinterstore(String, ZParams, String...)
	 * 
	 * @param dstkey
	 * @param sets
	 * @param params
	 * @return Integer reply, specifically the number of elements in the sorted
	 *         set at dstkey
	 */
	Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets);

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through
	 * kN, and stores it at dstkey. It is mandatory to provide the number of
	 * input keys N, before passing the input keys and the other (optional)
	 * arguments.
	 * <p>
	 * As the terms imply, the {@link #zinterstore(String, String...)
	 * ZINTERSTORE} command requires an element to be present in each of the
	 * given inputs to be inserted in the result. The
	 * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
	 * elements across all inputs.
	 * <p>
	 * Using the WEIGHTS option, it is possible to add weight to each input
	 * sorted set. This means that the score of each element in the sorted set
	 * is first multiplied by this weight before being passed to the
	 * aggregation. When this option is not given, all weights default to 1.
	 * <p>
	 * With the AGGREGATE option, it's possible to specify how the results of
	 * the union or intersection are aggregated. This option defaults to SUM,
	 * where the score of an element is summed across the inputs where it
	 * exists. When this option is set to be either MIN or MAX, the resulting
	 * set will contain the minimum or maximum score of an element across the
	 * inputs where it exists.
	 * <p>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
	 * sizes of the input sorted sets, and M being the number of elements in the
	 * resulting sorted set
	 * 
	 * @see #zunionstore(String, String...)
	 * @see #zunionstore(String, ZParams, String...)
	 * @see #zinterstore(String, String...)
	 * @see #zinterstore(String, ZParams, String...)
	 * 
	 * @param dstkey
	 * @param sets
	 * @return Integer reply, specifically the number of elements in the sorted
	 *         set at dstkey
	 */
	Long zinterstore(byte[] dstkey, byte[]... sets);

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through
	 * kN, and stores it at dstkey. It is mandatory to provide the number of
	 * input keys N, before passing the input keys and the other (optional)
	 * arguments.
	 * <p>
	 * As the terms imply, the {@link #zinterstore(String, String...)
	 * ZINTERSTORE} command requires an element to be present in each of the
	 * given inputs to be inserted in the result. The
	 * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
	 * elements across all inputs.
	 * <p>
	 * Using the WEIGHTS option, it is possible to add weight to each input
	 * sorted set. This means that the score of each element in the sorted set
	 * is first multiplied by this weight before being passed to the
	 * aggregation. When this option is not given, all weights default to 1.
	 * <p>
	 * With the AGGREGATE option, it's possible to specify how the results of
	 * the union or intersection are aggregated. This option defaults to SUM,
	 * where the score of an element is summed across the inputs where it
	 * exists. When this option is set to be either MIN or MAX, the resulting
	 * set will contain the minimum or maximum score of an element across the
	 * inputs where it exists.
	 * <p>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
	 * sizes of the input sorted sets, and M being the number of elements in the
	 * resulting sorted set
	 * 
	 * @see #zunionstore(String, String...)
	 * @see #zunionstore(String, ZParams, String...)
	 * @see #zinterstore(String, String...)
	 * @see #zinterstore(String, ZParams, String...)
	 * 
	 * @param dstkey
	 * @param sets
	 * @param params
	 * @return Integer reply, specifically the number of elements in the sorted
	 *         set at dstkey
	 */
	Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets);

	/**
	 * Synchronously save the DB on disk.
	 * <p>
	 * Save the whole dataset on disk (this means that all the databases are
	 * saved, as well as keys with an EXPIRE set (the expire is preserved). The
	 * server hangs while the saving is not completed, no connection is served
	 * in the meanwhile. An OK code is returned when the DB was fully stored in
	 * disk.
	 * <p>
	 * The background variant of this command is {@link #bgsave() BGSAVE} that
	 * is able to perform the saving in the background while the server
	 * continues serving other clients.
	 * <p>
	 * 
	 * @return Status code reply
	 */
	String save();

	/**
	 * Asynchronously save the DB on disk.
	 * <p>
	 * Save the DB in background. The OK code is immediately returned. Redis
	 * forks, the parent continues to server the clients, the child saves the DB
	 * on disk then exit. A client my be able to check if the operation
	 * succeeded using the LASTSAVE command.
	 * 
	 * @return Status code reply
	 */
	String bgsave();

	/**
	 * Rewrite the append only file in background when it gets too big. Please
	 * for detailed information about the Redis Append Only File check the <a
	 * href="http://code.google.com/p/redis/wiki/AppendOnlyFileHowto">Append
	 * Only File Howto</a>.
	 * <p>
	 * BGREWRITEAOF rewrites the Append Only File in background when it gets too
	 * big. The Redis Append Only File is a Journal, so every operation
	 * modifying the dataset is logged in the Append Only File (and replayed at
	 * startup). This means that the Append Only File always grows. In order to
	 * rebuild its content the BGREWRITEAOF creates a new version of the append
	 * only file starting directly form the dataset in memory in order to
	 * guarantee the generation of the minimal number of commands needed to
	 * rebuild the database.
	 * <p>
	 * 
	 * @return Status code reply
	 */
	String bgrewriteaof();

	/**
	 * Return the UNIX time stamp of the last successfully saving of the dataset
	 * on disk.
	 * <p>
	 * Return the UNIX TIME of the last DB save executed with success. A client
	 * may check if a {@link #bgsave() BGSAVE} command succeeded reading the
	 * LASTSAVE value, then issuing a BGSAVE command and checking at regular
	 * intervals every N seconds if LASTSAVE changed.
	 * 
	 * @return Integer reply, specifically an UNIX time stamp.
	 */
	Long lastsave();

	/**
	 * Synchronously save the DB on disk, then shutdown the server.
	 * <p>
	 * Stop all the clients, save the DB, then quit the server. This commands
	 * makes sure that the DB is switched off without the lost of any data. This
	 * is not guaranteed if the client uses simply {@link #save() SAVE} and then
	 * {@link #quit() QUIT} because other clients may alter the DB data between
	 * the two commands.
	 * 
	 * @return Status code reply on error. On success nothing is returned since
	 *         the server quits and the connection is closed.
	 */
	String shutdown();

	/**
	 * Provide information and statistics about the server.
	 * <p>
	 * The info command returns different information and statistics about the
	 * server in an format that's simple to parse by computers and easy to read
	 * by humans.
	 * <p>
	 * <b>Format of the returned String:</b>
	 * <p>
	 * All the fields are in the form field:value
	 * 
	 * <pre>
	 * edis_version:0.07
	 * connected_clients:1
	 * connected_slaves:0
	 * used_memory:3187
	 * changes_since_last_save:0
	 * last_save_time:1237655729
	 * total_connections_received:1
	 * total_commands_processed:1
	 * uptime_in_seconds:25
	 * uptime_in_days:0
	 * </pre>
	 * 
	 * <b>Notes</b>
	 * <p>
	 * used_memory is returned in bytes, and is the total number of bytes
	 * allocated by the program using malloc.
	 * <p>
	 * uptime_in_days is redundant since the uptime in seconds contains already
	 * the full uptime information, this field is only mainly present for
	 * humans.
	 * <p>
	 * changes_since_last_save does not refer to the number of key changes, but
	 * to the number of operations that produced some kind of change in the
	 * dataset.
	 * <p>
	 * 
	 * @return Bulk reply
	 */
	String info();

	/**
	 * Dump all the received requests in real time.
	 * <p>
	 * MONITOR is a debugging command that outputs the whole sequence of
	 * commands received by the Redis server. is very handy in order to
	 * understand what is happening into the database. This command is used
	 * directly via telnet.
	 * 
	 * @param jedisMonitor
	 */
	void monitor(JedisMonitor jedisMonitor);

	/**
	 * Change the replication settings.
	 * <p>
	 * The SLAVEOF command can change the replication settings of a slave on the
	 * fly. If a Redis server is arleady acting as slave, the command SLAVEOF NO
	 * ONE will turn off the replicaiton turning the Redis server into a MASTER.
	 * In the proper form SLAVEOF hostname port will make the server a slave of
	 * the specific server listening at the specified hostname and port.
	 * <p>
	 * If a server is already a slave of some master, SLAVEOF hostname port will
	 * stop the replication against the old server and start the
	 * synchrnonization against the new one discarding the old dataset.
	 * <p>
	 * The form SLAVEOF no one will stop replication turning the server into a
	 * MASTER but will not discard the replication. So if the old master stop
	 * working it is possible to turn the slave into a master and set the
	 * application to use the new master in read/write. Later when the other
	 * Redis server will be fixed it can be configured in order to work as
	 * slave.
	 * <p>
	 * 
	 * @param host
	 * @param port
	 * @return Status code reply
	 */
	String slaveof(String host, int port);

	String slaveofNoOne();

	/**
	 * Retrieve the configuration of a running Redis server. Not all the
	 * configuration parameters are supported.
	 * <p>
	 * CONFIG GET returns the current configuration parameters. This sub command
	 * only accepts a single argument, that is glob style pattern. All the
	 * configuration parameters matching this parameter are reported as a list
	 * of key-value pairs.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * $ redis-cli config get '*'
	 * 1. "dbfilename"
	 * 2. "dump.rdb"
	 * 3. "requirepass"
	 * 4. (nil)
	 * 5. "masterauth"
	 * 6. (nil)
	 * 7. "maxmemory"
	 * 8. "0\n"
	 * 9. "appendfsync"
	 * 10. "everysec"
	 * 11. "save"
	 * 12. "3600 1 300 100 60 10000"
	 * 
	 * $ redis-cli config get 'm*'
	 * 1. "masterauth"
	 * 2. (nil)
	 * 3. "maxmemory"
	 * 4. "0\n"
	 * </pre>
	 * 
	 * @param pattern
	 * @return Bulk reply.
	 */
	List<String> configGet(String pattern);

	/**
	 * Reset the stats returned by INFO
	 * 
	 * @return
	 */
	String configResetStat();

	/**
	 * Alter the configuration of a running Redis server. Not all the
	 * configuration parameters are supported.
	 * <p>
	 * The list of configuration parameters supported by CONFIG SET can be
	 * obtained issuing a {@link #configGet(String) CONFIG GET *} command.
	 * <p>
	 * The configuration set using CONFIG SET is immediately loaded by the Redis
	 * server that will start acting as specified starting from the next
	 * command.
	 * <p>
	 * 
	 * <b>Parameters value format</b>
	 * <p>
	 * The value of the configuration parameter is the same as the one of the
	 * same parameter in the Redis configuration file, with the following
	 * exceptions:
	 * <p>
	 * <ul>
	 * <li>The save paramter is a list of space-separated integers. Every pair
	 * of integers specify the time and number of changes limit to trigger a
	 * save. For instance the command CONFIG SET save "3600 10 60 10000" will
	 * configure the server to issue a background saving of the RDB file every
	 * 3600 seconds if there are at least 10 changes in the dataset, and every
	 * 60 seconds if there are at least 10000 changes. To completely disable
	 * automatic snapshots just set the parameter as an empty string.
	 * <li>All the integer parameters representing memory are returned and
	 * accepted only using bytes as unit.
	 * </ul>
	 * 
	 * @param parameter
	 * @param value
	 * @return Status code reply
	 */
	String configSet(String parameter, String value);

	boolean isConnected();

	Long strlen(byte[] key);

	void sync();

	/**
	 * Undo a {@link #expire(String, int) expire} at turning the expire key into
	 * a normal key.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @param key
	 * @return Integer reply, specifically: 1: the key is now persist. 0: the
	 *         key is not persist (only happens when key not set).
	 */
	Long persist(byte[] key);

	byte[] echo(byte[] string);

	String debug(DebugParams params);

	Client getClient();

	/**
	 * Pop a value from a list, push it to another list and return it; or block
	 * until one is available
	 * 
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return the element
	 */
	byte[] brpoplpush(byte[] source, byte[] destination, int timeout);

	/**
	 * Sets or clears the bit at offset in the string value stored at key
	 * 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	Boolean setbit(byte[] key, long offset, byte[] value);

	/**
	 * Returns the bit value at offset in the string value stored at key
	 * 
	 * @param key
	 * @param offset
	 * @return
	 */
	Boolean getbit(byte[] key, long offset);

	Long setrange(byte[] key, long offset, byte[] value);

	String getrange(byte[] key, long startOffset, long endOffset);

	Long publish(byte[] channel, byte[] message);

	void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels);

	void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns);

	Long getDB();

}