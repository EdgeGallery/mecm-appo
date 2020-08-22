package org.edgegallery.mecm.appo.service.interfaces;

import java.util.Map;
import org.edgegallery.mecm.appo.common.AppoProcessFlowResponse;

public interface AppoProcessflowService {

    /**
     * Start processing process flow asychronously.
     *
     * @param processKey   process key
     * @param requestInput input parameters
     */
    void executeProcessAsync(String processKey, Map<String, String> requestInput);

    /**
     * Start processing process flow sychronously.
     *
     * @param processKey   process key
     * @param requestInput input parameters
     * @return workflow response
     */
    AppoProcessFlowResponse executeProcessSync(String processKey, Map<String, String> requestInput);
}