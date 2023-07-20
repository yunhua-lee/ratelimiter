package biz;

import java.util.concurrent.*;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class Requester {
	private final Integer count;

	private final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

	public Requester(Integer count){
		this. count = count;
	}

	public void start(final BlockingQueue<Long> queue){
		Runnable task = new Runnable() {
			@Override
			public void run() {

				for(int i=0; i< count; i++){
					queue.add(System.currentTimeMillis());
				}
			}
		};

		pool.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
	}
}
