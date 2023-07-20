package limiter;

import redis.clients.jedis.Jedis;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class SimpleFixedTimeWindow extends AbstractLimiter{
	public SimpleFixedTimeWindow(Integer interval, Integer count){
		super(interval, count);
	}

	@Override
	public boolean check() {
		Jedis client = jedisPool.getResource();

		try{
			//It is extremely simpler than FixedTimeWindow.
			//Only one Redis key and two Redis commands.
			Long realCount = client.incr(getCountKey());
			if( realCount == 1 ){
				System.out.println("switch time window.");

				//expired time may not be so precise because key is already
				//created on the previous operation "client.incr(getCountKey())"
				client.expire( getCountKey(), getInterval() );
			}

			if( realCount > getLimitCount() ){
				System.out.println("rate limited(fixed time window with one key), " +
						"real count: " + realCount + ", limit count: " + getLimitCount());
				return false;
			} else {
				return true;
			}
		} finally {
			//close client and it will be returned to pool
			client.close();
		}
	}

	@Override
	public void start() {
		//do nothing
		return;
	}

	private String getCountKey(){

		return "rate_limiter_count_simple";
	}
}
