package com.b2bplatform.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for casino-site login (no password).
 * Used when players come from operator/casino site; we validate operator (API key), language, currency, IP, table.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasinoLoginRequest {

    private String type;
    @NotBlank(message = "Username is required")
    private String username;
    private Integer mode;
    private String currencyid;
    private String firstName;
    private String lastName;
    private String agentid;
    private Integer playerLevel;
    private Short playerType;
    private String tableId;
    private String host;
    private String dirPath;
    /** Optional: for internal/testing flow when using same endpoint with password */
    private String password;
    private String oldPassword;
    private String newPassword;
}
