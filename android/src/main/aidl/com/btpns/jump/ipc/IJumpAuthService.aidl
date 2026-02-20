package com.btpns.jump.ipc;

import android.os.Bundle;
import com.btpns.jump.ipc.AuthContext;
import com.btpns.jump.ipc.IAuthListener;

interface IJumpAuthService {
  boolean ping();
  AuthContext getAuthContext(String callerPackage, in Bundle options);
  void registerListener(IAuthListener listener);
  void unregisterListener(IAuthListener listener);
}

