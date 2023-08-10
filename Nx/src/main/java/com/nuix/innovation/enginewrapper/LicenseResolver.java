package com.nuix.innovation.enginewrapper;

import lombok.NonNull;
import nuix.engine.Engine;

/***
 * An interface for providing {@link NuixEngine} license resolution.
 * @author Jason Wells
 */
public interface LicenseResolver {
    /***
     * Attempts to license the provided Engine instance using resolution and filtering configuration of this instance.
     * @param engine The engine instance to attempt to license.
     * @return True if a license was obtained, false if not.
     * @throws Exception Exceptions thrown by any of the methods working to obtain a license will be uncaught and allowed
     * to bubble up for caller to respond to.
     */
    boolean resolveLicense(@NonNull Engine engine) throws Exception;
}
