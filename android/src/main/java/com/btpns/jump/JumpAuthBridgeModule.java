package com.btpns.jump;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.btpns.jump.ipc.AuthContext;
import com.btpns.jump.ipc.JumpAuthClient;
import com.btpns.jump.ipc.JumpAuthException;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

public class JumpAuthBridgeModule extends ReactContextBaseJavaModule {

  public static final String NAME = "JumpAuthBridge";

  private final JumpAuthClient client;

  public JumpAuthBridgeModule(ReactApplicationContext reactContext) {
    super(reactContext);
    client = new JumpAuthClient(reactContext);
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void configure(ReadableMap options, Promise promise) {
    try {
      String hostPackage = options.hasKey("hostPackage") ? options.getString("hostPackage") : null;
      String hostServiceClass = options.hasKey("hostServiceClass") ? options.getString("hostServiceClass") : null;
      int timeoutMs = options.hasKey("timeoutMs") ? options.getInt("timeoutMs") : 3000;

      if (hostPackage == null || hostPackage.isEmpty()) {
        promise.reject("E_INVALID_RESPONSE", "hostPackage is required");
        return;
      }

      client.configure(new JumpAuthClient.Config(hostPackage, hostServiceClass, timeoutMs));
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("E_INVALID_RESPONSE", e);
    }
  }

  @ReactMethod
  public void getAuthContext(ReadableMap options, Promise promise) {
    try {
      Bundle bundle = new Bundle();
      if (options != null && options.hasKey("forceRefresh")) {
        bundle.putBoolean("forceRefresh", options.getBoolean("forceRefresh"));
      }

      String callerPackage = getReactApplicationContext().getPackageName();
      AuthContext ctx = client.getAuthContext(callerPackage, bundle);
      if (ctx == null || ctx.accessToken == null || ctx.userId == null || ctx.issuerPackage == null) {
        promise.reject("E_INVALID_RESPONSE", "Invalid AuthContext");
        return;
      }
      promise.resolve(toWritableMap(ctx));
    } catch (JumpAuthException jae) {
      promise.reject(mapCode(jae.code), jae.getMessage(), jae);
    } catch (Exception e) {
      promise.reject("E_REMOTE", e);
    }
  }

  private static String mapCode(JumpAuthException.Code code) {
    switch (code) {
      case NOT_CONFIGURED:
        return "E_NOT_CONFIGURED";
      case BIND_FAILED:
        return "E_BIND_FAILED";
      case TIMEOUT:
        return "E_TIMEOUT";
      case PERMISSION:
        return "E_PERMISSION";
      case INVALID_RESPONSE:
        return "E_INVALID_RESPONSE";
      case REMOTE:
      default:
        return "E_REMOTE";
    }
  }

  private static WritableMap toWritableMap(AuthContext ctx) {
    WritableMap map = Arguments.createMap();
    map.putString("issuerPackage", ctx.issuerPackage);
    map.putString("userId", ctx.userId);
    map.putString("displayName", ctx.displayName);
    map.putString("accessToken", ctx.accessToken);
    map.putDouble("expiresAtEpochMs", (double) ctx.expiresAtEpochMs);

    List<String> roles = ctx.roles;
    if (roles != null) {
      WritableArray arr = Arguments.createArray();
      for (String r : roles) arr.pushString(r);
      map.putArray("roles", arr);
    }
    return map;
  }
}

