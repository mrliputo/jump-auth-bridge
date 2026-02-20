export type AuthContext = {
  /** Host package that serves the data (e.g. com.btpns.acquisition) */
  issuerPackage: string;

  userId: string;
  displayName?: string;
  roles?: string[];

  accessToken: string;
  expiresAtEpochMs: number;
};

export type GetAuthContextOptions = {
  /** If true, host may re-read from storage/refresh in-process before returning. */
  forceRefresh?: boolean;
};

export type ConfigureOptions = {
  hostPackage: string;
  /** Fully qualified service class in host app. Defaults to com.btpns.jump.JumpAuthService */
  hostServiceClass?: string;
  timeoutMs?: number;
};

export type JumpAuthErrorCode =
  | 'E_NOT_CONFIGURED'
  | 'E_BIND_FAILED'
  | 'E_TIMEOUT'
  | 'E_PERMISSION'
  | 'E_REMOTE'
  | 'E_INVALID_RESPONSE'
  | 'E_UNAVAILABLE';

export class JumpAuthError extends Error {
  code: JumpAuthErrorCode;
  cause?: unknown;
  constructor(code: JumpAuthErrorCode, message: string, cause?: unknown) {
    super(message);
    this.name = 'JumpAuthError';
    this.code = code;
    this.cause = cause;
  }
}

