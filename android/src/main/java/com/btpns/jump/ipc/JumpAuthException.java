package com.btpns.jump.ipc;

import androidx.annotation.Nullable;

public class JumpAuthException extends Exception {
  public enum Code {
    NOT_CONFIGURED,
    BIND_FAILED,
    TIMEOUT,
    PERMISSION,
    REMOTE,
    INVALID_RESPONSE
  }

  public final Code code;

  public JumpAuthException(Code code, String message) {
    super(message);
    this.code = code;
  }

  public JumpAuthException(Code code, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.code = code;
  }
}

