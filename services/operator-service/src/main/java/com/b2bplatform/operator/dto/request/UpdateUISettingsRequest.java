package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for updating operator UI settings.
 * All fields are optional - only provided fields will be updated.
 */
@Data
public class UpdateUISettingsRequest {
    
    // Video/Streaming Settings
    private Boolean onlyJSMpeg;
    private Boolean allowFullScreen;
    private Boolean autoPlayVideo;
    
    // Display Settings
    private Boolean showCurrency;
    private Boolean showBalance;
    private Boolean showGameHistory;
    private Boolean showBetHistory;
    private Boolean showStatistics;
    
    // UI Behavior Settings
    private Boolean enableSoundEffects;
    private Boolean enableBackgroundMusic;
    private Boolean enableAnimations;
    private Boolean enableTooltips;
    
    // Layout Settings
    private Boolean showSidePanel;
    private Boolean showTopBar;
    private Boolean showBottomBar;
    private Boolean compactMode;
    
    // Game Settings
    private Boolean showGameInfo;
    private Boolean showRules;
    private Boolean showPayoutTable;
    private Boolean enableChat;
    
    // Responsive Settings
    private Boolean mobileOptimized;
    private Boolean tabletOptimized;
    private Boolean desktopOptimized;
    
    // Theme Settings
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Theme color must be a valid hex color code (e.g., #FF5733)")
    private String themeColor;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Background color must be a valid hex color code (e.g., #FFFFFF)")
    private String backgroundColor;
    
    private String fontFamily;
    private String fontSize;
    
    // Other Settings
    private Boolean languageSelectorEnabled;
    private Boolean currencySelectorEnabled;
    private Boolean logoutButtonEnabled;
}
