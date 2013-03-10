package com.yammer.metrics.jedis;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.SortingParams;
import redis.clients.jedis.ZParams;
import redis.clients.util.Slowlog;

/**
 * @author softmentor
 *
 */
public interface IJedis {

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
	Long del(String... keys);

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
	Set<String> keys(String pattern);

	/**
	 * Return a randomly selected key from the currently selected DB.
	 * <p>
	 * Time complexity: O(1)
	 * 
	 * @return Singe line reply, specifically the randomly selected key or an
	 *         empty string is the database is empty
	 */
	String randomKey();

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
	String rename(String oldkey, String newkey);

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
	Long renamenx(String oldkey, String newkey);

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
	Long move(String key, int dbIndex);

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
	List<String> mget(String... keys);

	/**
	 * Set the the respective keys to the respective values. MSET will replace
	 * old values with new values, while {@link #msetnx(String...) MSETNX} will
	 * not perform any operation at all even if just a single key already
	 * exists.
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
	 * @see #msetnx(String...)
	 * 
	 * @param keysvalues
	 * @return Status code reply Basically +OK as MSET can't fail
	 */
	String mset(String... keysvalues);

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
	Long msetnx(String... keysvalues);

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
	String rpoplpush(String srckey, String dstkey);

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
	Long smove(String srckey, String dstkey, String member);

	/**
	 * Return the members of a set resulting from the intersection of all the
	 * sets hold at the specified keys. Like in
	 * {@link #lrange(String, long, long) LRANGE} the result is sent to the
	 * client as a multi-bulk reply (see the protocol specification for more
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
	Set<String> sinter(String... keys);

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
	Long sinterstore(String dstkey, String... keys);

	/**
	 * Return the members of a set resulting from the union of all the sets hold
	 * at the specified keys. Like in {@link #lrange(String, long, long) LRANGE}
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
	Set<String> sunion(String... keys);

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
	Long sunionstore(String dstkey, String... keys);

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
	Set<String> sdiff(String... keys);

	/**
	 * This command works exactly like {@link #sdiff(String...) SDIFF} but
	 * instead of being returned the resulting set is stored in dstkey.
	 * 
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	Long sdiffstore(String dstkey, String... keys);

	String watch(String... keys);

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
	List<String> blpop(int timeout, String... keys);

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
	Long sort(String key, SortingParams sortingParameters, String dstkey);

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
	Long sort(String key, String dstkey);

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
	List<String> brpop(int timeout, String... keys);

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
	Long zunionstore(String dstkey, String... sets);

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
	Long zunionstore(String dstkey, ZParams params, String... sets);

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
	Long zinterstore(String dstkey, String... sets);

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
	Long zinterstore(String dstkey, ZParams params, String... sets);

	Long strlen(String key);

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
	Long persist(String key);

	String echo(String string);

	/**
	 * Pop a value from a list, push it to another list and return it; or block
	 * until one is available
	 * 
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return the element
	 */
	String brpoplpush(String source, String destination, int timeout);

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

	Object eval(String script, int keyCount, String... params);

	Object eval(String script, List<String> keys, List<String> args);

	Object eval(String script);

	Object evalsha(String script);

	Object evalsha(String sha1, List<String> keys, List<String> args);

	Object evalsha(String sha1, int keyCount, String... params);

	Boolean scriptExists(String sha1);

	List<Boolean> scriptExists(String... sha1);

	String scriptLoad(String script);

	List<Slowlog> slowlogGet();

	List<Slowlog> slowlogGet(long entries);

	Long objectRefcount(String string);

	String objectEncoding(String string);

	Long objectIdletime(String string);

}