package com.b2bplatform.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for casino launch / getLaunchUrl.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameLaunchUrlResponse {

    /** Full URL to launch the game (base URL + token). */
    private String url;
    /** Session token (sessionId) for the client. */
    private String token;
}
