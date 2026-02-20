package com.btpns.jump.ipc;

import com.btpns.jump.ipc.AuthContext;

interface IAuthListener {
  void onAuthChanged(in AuthContext context);
  void onAuthInvalidated(String reason);
}

