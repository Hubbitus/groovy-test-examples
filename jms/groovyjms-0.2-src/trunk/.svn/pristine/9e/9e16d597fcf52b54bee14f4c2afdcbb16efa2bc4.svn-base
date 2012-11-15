package groovy.jms.pool;

import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import java.util.concurrent.*;
import java.util.Map;

public class JMSThreadPoolExecutor extends ThreadPoolExecutor {
    static Logger logger = Logger.getLogger(JMSThreadPoolExecutor.class.getName());
    private static final int defaultCorePoolSize = 10, defaultMaximumPoolSize = 10;
    private static final long defaultKeepAliveTime = 1000;
    private static final TimeUnit defaultUnit = TimeUnit.MILLISECONDS;
    private ThreadGroup threadGroup = new ThreadGroup(this.toString());

    public JMSThreadPoolExecutor(ConnectionFactory f, ThreadFactory tf, Map cfg) {
        super((Integer) ((cfg != null && cfg.containsKey("corePoolSize")) ? cfg.get("corePoolSize") : defaultCorePoolSize),
                (Integer) ((cfg != null && cfg.containsKey("maximumPoolSize")) ? cfg.get("maximumPoolSize") : defaultMaximumPoolSize),
                (Long) ((cfg != null && cfg.containsKey("keepAliveTime")) ? Long.parseLong("" + cfg.get("keepAliveTime")) : defaultKeepAliveTime), //TODO find a better way to parseLong
                (TimeUnit) ((cfg != null && cfg.containsKey("unit")) ? cfg.get("unit") : defaultUnit),
                new LinkedBlockingQueue(), tf);
    }

    public JMSThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public JMSThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public JMSThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public JMSThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    protected void beforeExecute(Thread t, Runnable r) { super.beforeExecute(t, r); }

    public ThreadGroup getThreadGroup() {return threadGroup;}

    protected void afterExecute(Runnable r, Throwable t) {
        if (JMSThread.jms.get() != null) JMSThread.jms.get().connect(); //flush message, TODO: review this 
        super.afterExecute(r, t);
    }

}
