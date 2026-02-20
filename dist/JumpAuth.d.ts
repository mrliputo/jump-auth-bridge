import { AuthContext, ConfigureOptions, GetAuthContextOptions } from './types';
export declare const JumpAuth: {
    configure(options: ConfigureOptions): Promise<void>;
    getAuthContext(options?: GetAuthContextOptions): Promise<AuthContext>;
    /**
     * Subscribe to auth changes coming from host (optional; requires host to push updates).
     */
    subscribeAuthChanges(listener: (context: AuthContext | null, reason?: string) => void): () => void;
};
