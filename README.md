# CustomCompletionService

ExecutorCompletionService的一个扩展；<br> 
这个类可以完成这样的逻辑：需要对多个线程池的结果做统一处理，先完成的先处理。<br> 
实现的关键：静态代理模式、内部类；<br> 
这是一个代理两个线程池的简单例子，还可以做更好的封装，适应代理更多线程池的变化。<br> 

ExecutorCompletionService源代码中有两个例子，可以参考这两个例子使用CustomCompletionService。

```java
//从完成队列中取出结果并处理
void solve(Executor e,
             Collection<Callable<Result>> solvers)
      throws InterruptedException, ExecutionException {
      CompletionService<Result> ecs
          = new ExecutorCompletionService<Result>(e);
      for (Callable<Result> s : solvers)
          ecs.submit(s);
      int n = solvers.size();
      for (int i = 0; i < n; ++i) {
          Result r = ecs.take().get();
          if (r != null)
              use(r);
      }
  }}
  ```
  
  ```java
    //取到第一个符合条件的任务后，取消其他任务
    void solve(Executor e,
             Collection<Callable<Result>> solvers)
      throws InterruptedException {
      CompletionService<Result> ecs
          = new ExecutorCompletionService<Result>(e);
      int n = solvers.size();
      List<Future<Result>> futures
          = new ArrayList<Future<Result>>(n);
      Result result = null;
      try {
          for (Callable<Result> s : solvers)
              futures.add(ecs.submit(s));
          for (int i = 0; i < n; ++i) {
              try {
                  Result r = ecs.take().get();
                  if (r != null) {
                      result = r;
                      break;
                  }
              } catch (ExecutionException ignore) {}
          }
      }
      finally {
          for (Future<Result> f : futures)
              f.cancel(true);
      }
 
      if (result != null)
          use(result);
  }}
 ```
