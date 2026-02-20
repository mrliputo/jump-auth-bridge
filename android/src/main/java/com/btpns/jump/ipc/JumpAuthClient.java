package com.btpns.jump.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client-side binder helper. Binds explicitly to host package/service and calls AIDL.
 */
public class JumpAuthClient {

  public static class Config {
    public final String hostPackage;
    public final String hostServiceClass;
    public final long timeoutMs;

    public Config(String hostPackage, @Nullable String hostServiceClass, long timeoutMs) {
      this.hostPackage = hostPackage;
      this.hostServiceClass = hostServiceClass != null ? hostServiceClass : "com.btpns.jump.JumpAuthService";
      this.timeoutMs = timeoutMs;
    }
  }

  private final Context appContext;
  private Config config;

  public JumpAuthClient(@NonNull Context context) {
    this.appContext = context.getApplicationContext();
  }

  public void configure(@NonNull Config config) {
    this.config = config;
  }

  public AuthContext getAuthContext(@NonNull String callerPackage, @Nullable Bundle options)
      throws JumpAuthException {
    if (config == null) throw new JumpAuthException(JumpAuthException.Code.NOT_CONFIGURED, "Not configured");

    final AtomicReference<IJumpAuthService> serviceRef = new AtomicReference<>();
    final AtomicReference<Exception> errorRef = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);

    Intent intent = new Intent();
    intent.setComponent(new ComponentName(config.hostPackage, config.hostServiceClass));

    ServiceConnection conn = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder binder) {
        serviceRef.set(IJumpAuthService.Stub.asInterface(binder));
        latch.countDown();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        // Ignore; call will fail if disconnected.
      }

      @Override
      public void onNullBinding(ComponentName name) {
        errorRef.set(new JumpAuthException(JumpAuthException.Code.BIND_FAILED, "Null binding from host"));
        latch.countDown();
      }

      @Override
      public void onBindingDied(ComponentName name) {
        errorRef.set(new JumpAuthException(JumpAuthException.Code.BIND_FAILED, "Binding died"));
        latch.countDown();
      }
    };

    boolean bound;
    try {
      bound = appContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    } catch (SecurityException se) {
      throw new JumpAuthException(JumpAuthException.Code.PERMISSION, "Permission denied binding to host", se);
    } catch (Exception e) {
      throw new JumpAuthException(JumpAuthException.Code.BIND_FAILED, "Failed to bind to host", e);
    }

    if (!bound) {
      throw new JumpAuthException(JumpAuthException.Code.BIND_FAILED, "bindService returned false");
    }

    try {
      boolean ok = latch.await(config.timeoutMs, TimeUnit.MILLISECONDS);
      if (!ok) {
        throw new JumpAuthException(JumpAuthException.Code.TIMEOUT, "Timeout binding to host");
      }
      if (errorRef.get() != null) {
        Exception ex = errorRef.get();
        if (ex instanceof JumpAuthException) throw (JumpAuthException) ex;
        throw new JumpAuthException(JumpAuthException.Code.BIND_FAILED, ex.getMessage(), ex);
      }

      IJumpAuthService service = serviceRef.get();
      if (service == null) {
        throw new JumpAuthException(JumpAuthException.Code.BIND_FAILED, "Service is null after bind");
      }

      try {
        return service.getAuthContext(callerPackage, options);
      } catch (SecurityException se) {
        throw new JumpAuthException(JumpAuthException.Code.PERMISSION, "Permission denied calling host", se);
      } catch (RemoteException re) {
        throw new JumpAuthException(JumpAuthException.Code.REMOTE, "Remote exception", re);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new JumpAuthException(JumpAuthException.Code.TIMEOUT, "Interrupted", ie);
    } finally {
      // Unbind on main thread to avoid StrictMode issues.
      Handler main = new Handler(Looper.getMainLooper());
      main.post(() -> {
        try {
          appContext.unbindService(conn);
        } catch (Exception ignored) {
        }
      });
    }
  }
}

