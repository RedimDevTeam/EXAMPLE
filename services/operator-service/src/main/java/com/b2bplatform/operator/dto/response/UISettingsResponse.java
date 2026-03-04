package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UISettingsResponse {
    
    private Long id;
    private Long operatorId;
    
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
    private String themeColor;
    private String backgroundColor;
    private String fontFamily;
    private String fontSize;
    
    // Other Settings
    private Boolean languageSelectorEnabled;
    private Boolean currencySelectorEnabled;
    private Boolean logoutButtonEnabled;
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
