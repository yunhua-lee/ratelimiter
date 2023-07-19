package limiter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class FixedTimeWindow extends AbstractLimiter{
	public FixedTimeWindow(Integer interval, Integer count){
		super(interval, count);
	}

	@Override
	public boolean check() {
		Jedis client = pool.getResource();

		try{
			String timeKey = getTimeKey();
			String countKey = getCountKey();
			long currentCount = 0;

			String timeKeyValue = client.get(timeKey);
			currentCount = client.incr(countKey);

			//switch time windows
			if( timeKeyValue == null ) {
				timeKeyValue = String.valueOf(currentCount);

				//try to set new time key
				SetParams params = new SetParams();

				//important: time key expire time should be larger than interval.
				//because the applications time may not be consistent with redis or
				//each other.
				params.ex( 2 * getInterval());
				params.nx();
				String result = client.set(timeKey, timeKeyValue, params);
				if( "OK".equalsIgnoreCase(result) ) {
					System.out.println("switch time window, new window: " +
							timeKey + ", new value: " + timeKeyValue);
				} else {
					timeKeyValue = client.get(timeKey); //get latest time key value
				}
			}

			long startCount = Long.parseLong(timeKeyValue);
			long realCount = currentCount - startCount;

			if (realCount > getCount()) {
				System.out.println("rate limited, real count: " + realCount +
						", limit count: " + getCount());
				return false;
			}

			return true;
		} finally {
			client.close();
		}
	}

	@Override
	public void start() {
		//do nothing
		return;
	}

	private String getTimeKey(){
		//time key is used to set time period. Its value is the request count for
		//the beginning of a period.
		//For example: rate_limiter_time_4345631 = 128.
		long time = System.currentTimeMillis();
		long period = (time / 1000) / getInterval();

		return "rate_limiter_time_" + period;
	}

	private String getCountKey(){
		//count key is used to record the request count. The total request count for
		//a period is the value of count key minuses the value of time key.
		//For example: rate_limiter_time_4345631 = 128, rate_limiter_count = 156,
		//Then the total request count for period 4345631 is 156 - 128 = 28.
		return "rate_limiter_count";
	}
}
