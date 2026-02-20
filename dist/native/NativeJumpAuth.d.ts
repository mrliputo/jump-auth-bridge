export type NativeAuthContext = {
    issuerPackage: string;
    userId: string;
    displayName?: string;
    roles?: string[];
    accessToken: string;
    expiresAtEpochMs: number;
};
export type NativeConfigureOptions = {
    hostPackage: string;
    hostServiceClass?: string;
    timeoutMs?: number;
};
export type NativeGetAuthContextOptions = {
    forceRefresh?: boolean;
};
export type AuthChangedEvent = {
    context?: NativeAuthContext;
    reason?: string;
};
type NativeJumpAuthModule = {
    configure(options: NativeConfigureOptions): Promise<void>;
    getAuthContext(options?: NativeGetAuthContextOptions): Promise<NativeAuthContext>;
};
export declare const NativeJumpAuth: NativeJumpAuthModule;
export {};
