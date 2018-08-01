package org.thoughtcrime.securesms.util;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.logging.Log;

public class SafeAsyncTask {

  private static final String TAG = SafeAsyncTask.class.getSimpleName();

  /**
   * Executes a task on the background and forwards the result to a task on the main thread. Does
   * not run the {@code foreground} task if the lifecycle isn't in an active state.
   *
   * @param lifecycleOwner Owner of the lifecycle that will be respected.
   * @param background Task to be executed on a background thread.
   * @param foreground Task to be executed on the main thread with the result of the background operation
   * @param <T> The type of result that is computed in the background task.
   */
  public static <T> void execute(@NonNull LifecycleOwner lifecycleOwner, @NonNull SafeCallable<T> background, @NonNull SafeRunnable<T> foreground) {
    final Lifecycle lifecycle = lifecycleOwner.getLifecycle();

    AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
      T result = background.call();
      Util.runOnMain(() -> {
        if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
          foreground.run(result);
        } else {
          Log.d(TAG, "Skipping task execution. Inactive lifecycle.");
        }
      });
    });
  }

  public interface SafeCallable<T> {
    T call();
  }

  public interface SafeRunnable<T> {
    void run(T arg);
  }
}
