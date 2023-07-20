package biz;

import limiter.Limiter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class Processor {
	private final Limiter limiter;

	public Processor(Limiter limiter){
		this.limiter = limiter;
	}

	public void start(final BlockingQueue<Long> queue){
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {
					try {
						Long request = queue.poll(100, TimeUnit.MILLISECONDS);
						if( request == null){
							continue;
						}

						if( limiter.check() ) {
							processRequest(request);
						} else {
							rejectRequest(request);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		thread.start();
	}

	void processRequest(Long request){
		//Just for extension
		return;
	}

	void rejectRequest(Long request){
		//Just for extension
		return;
	}
}
