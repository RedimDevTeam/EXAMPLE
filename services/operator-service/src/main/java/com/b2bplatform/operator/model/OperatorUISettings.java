package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for operator-specific UI configuration settings.
 */
@Entity
@Table(name = "operator_ui_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = "operator_id"))
@Data
public class OperatorUISettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false, unique = true)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    // Video/Streaming Settings
    @Column(name = "only_jsmpeg")
    private Boolean onlyJSMpeg = false;
    
    @Column(name = "allow_full_screen")
    private Boolean allowFullScreen = true;
    
    @Column(name = "auto_play_video")
    private Boolean autoPlayVideo = false;
    
    // Display Settings
    @Column(name = "show_currency")
    private Boolean showCurrency = true;
    
    @Column(name = "show_balance")
    private Boolean showBalance = true;
    
    @Column(name = "show_game_history")
    private Boolean showGameHistory = true;
    
    @Column(name = "show_bet_history")
    private Boolean showBetHistory = true;
    
    @Column(name = "show_statistics")
    private Boolean showStatistics = false;
    
    // UI Behavior Settings
    @Column(name = "enable_sound_effects")
    private Boolean enableSoundEffects = true;
    
    @Column(name = "enable_background_music")
    private Boolean enableBackgroundMusic = false;
    
    @Column(name = "enable_animations")
    private Boolean enableAnimations = true;
    
    @Column(name = "enable_tooltips")
    private Boolean enableTooltips = true;
    
    // Layout Settings
    @Column(name = "show_side_panel")
    private Boolean showSidePanel = true;
    
    @Column(name = "show_top_bar")
    private Boolean showTopBar = true;
    
    @Column(name = "show_bottom_bar")
    private Boolean showBottomBar = true;
    
    @Column(name = "compact_mode")
    private Boolean compactMode = false;
    
    // Game Settings
    @Column(name = "show_game_info")
    private Boolean showGameInfo = true;
    
    @Column(name = "show_rules")
    private Boolean showRules = true;
    
    @Column(name = "show_payout_table")
    private Boolean showPayoutTable = true;
    
    @Column(name = "enable_chat")
    private Boolean enableChat = false;
    
    // Responsive Settings
    @Column(name = "mobile_optimized")
    private Boolean mobileOptimized = true;
    
    @Column(name = "tablet_optimized")
    private Boolean tabletOptimized = true;
    
    @Column(name = "desktop_optimized")
    private Boolean desktopOptimized = true;
    
    // Theme Settings
    @Column(name = "theme_color", length = 50)
    private String themeColor; // Hex color code
    
    @Column(name = "background_color", length = 50)
    private String backgroundColor; // Hex color code
    
    @Column(name = "font_family", length = 100)
    private String fontFamily;
    
    @Column(name = "font_size", length = 20)
    private String fontSize; // e.g., "14px", "medium"
    
    // Other Settings
    @Column(name = "language_selector_enabled")
    private Boolean languageSelectorEnabled = true;
    
    @Column(name = "currency_selector_enabled")
    private Boolean currencySelectorEnabled = true;
    
    @Column(name = "logout_button_enabled")
    private Boolean logoutButtonEnabled = true;
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
