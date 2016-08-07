package pw.hellojava.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorCompletionService的一个扩展；
 * 这个类可以完成这样的逻辑：需要对多个线程池的结果做统一处理。
 * 实现的关键：静态代理模式、内部类；
 * 这是一个代理两个线程池的简单例子，还可以做更好的封装，适应代理更多线程池的变化。
 * @author cpw
 *
 * @param <V>
 */
public class CustomCompletionService<V> {
	
    private final Executor exe1;
    private final Executor exe2;

    /**
     * 完成队列
     */
    private final BlockingQueue<Future<V>> completionQueue;

    /**
     * 实现futuretask中的钩子done方法，futuretask中任务结束（正常结束、异常结束、取消）都会调用这个done方法
     * 在done方法中将任务结果提交到completionQueue，内部类可以访问外部类field、method
     */
    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }
        protected void done() { completionQueue.add(task); }
        private final Future<V> task;
    }

    /**
     * 用于包装任务
     * @param task
     * @return
     */
    private RunnableFuture<V> newTaskFor(Callable<V> task) {
            return new FutureTask<V>(task);
    }

    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
            return new FutureTask<V>(task, result);
    }

    /**
     * 代理两个线程池
     * @param exe1
     * @param exe2
     */
    public CustomCompletionService(Executor exe1,Executor exe2) {
        if (exe1 == null || exe2 == null )
            throw new NullPointerException();
        
        this.exe1 = exe1;
        this.exe2 = exe2;
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    public Future<V> submit(Callable<V> task, boolean chooseExe) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task);
        if(chooseExe)
        	exe1.execute(new QueueingFuture(f));
        else
        	exe2.execute(new QueueingFuture(f));
        
        return f;
    }

    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    public Future<V> poll() {
        return completionQueue.poll();
    }

    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
