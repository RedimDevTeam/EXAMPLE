package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.UpdateUISettingsRequest;
import com.b2bplatform.operator.dto.response.UISettingsResponse;
import com.b2bplatform.operator.model.OperatorUISettings;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.repository.OperatorUISettingsRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing operator UI settings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UISettingsService {
    
    private final OperatorUISettingsRepository uiSettingsRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    /**
     * Get UI settings for an operator. Creates default settings if none exist.
     */
    public UISettingsResponse getUISettings(Long operatorId) {
        log.info("Getting UI settings for operator {}", operatorId);
        
        OperatorUISettings settings = uiSettingsRepository.findByOperatorId(operatorId)
            .orElseGet(() -> {
                // Create default settings if none exist
                OperatorUISettings defaultSettings = createDefaultSettings(operatorId);
                return uiSettingsRepository.save(defaultSettings);
            });
        
        return mapToResponse(settings);
    }
    
    /**
     * Update UI settings for an operator.
     */
    @Transactional
    public UISettingsResponse updateUISettings(Long operatorId, UpdateUISettingsRequest request) {
        log.info("Updating UI settings for operator {}", operatorId);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        OperatorUISettings settings = uiSettingsRepository.findByOperatorId(operatorId)
            .orElseGet(() -> createDefaultSettings(operatorId));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("onlyJSMpeg", settings.getOnlyJSMpeg());
        oldValues.put("showCurrency", settings.getShowCurrency());
        oldValues.put("allowFullScreen", settings.getAllowFullScreen());
        oldValues.put("themeColor", settings.getThemeColor());
        
        // Update fields (only non-null fields)
        if (request.getOnlyJSMpeg() != null) {
            settings.setOnlyJSMpeg(request.getOnlyJSMpeg());
        }
        if (request.getAllowFullScreen() != null) {
            settings.setAllowFullScreen(request.getAllowFullScreen());
        }
        if (request.getAutoPlayVideo() != null) {
            settings.setAutoPlayVideo(request.getAutoPlayVideo());
        }
        if (request.getShowCurrency() != null) {
            settings.setShowCurrency(request.getShowCurrency());
        }
        if (request.getShowBalance() != null) {
            settings.setShowBalance(request.getShowBalance());
        }
        if (request.getShowGameHistory() != null) {
            settings.setShowGameHistory(request.getShowGameHistory());
        }
        if (request.getShowBetHistory() != null) {
            settings.setShowBetHistory(request.getShowBetHistory());
        }
        if (request.getShowStatistics() != null) {
            settings.setShowStatistics(request.getShowStatistics());
        }
        if (request.getEnableSoundEffects() != null) {
            settings.setEnableSoundEffects(request.getEnableSoundEffects());
        }
        if (request.getEnableBackgroundMusic() != null) {
            settings.setEnableBackgroundMusic(request.getEnableBackgroundMusic());
        }
        if (request.getEnableAnimations() != null) {
            settings.setEnableAnimations(request.getEnableAnimations());
        }
        if (request.getEnableTooltips() != null) {
            settings.setEnableTooltips(request.getEnableTooltips());
        }
        if (request.getShowSidePanel() != null) {
            settings.setShowSidePanel(request.getShowSidePanel());
        }
        if (request.getShowTopBar() != null) {
            settings.setShowTopBar(request.getShowTopBar());
        }
        if (request.getShowBottomBar() != null) {
            settings.setShowBottomBar(request.getShowBottomBar());
        }
        if (request.getCompactMode() != null) {
            settings.setCompactMode(request.getCompactMode());
        }
        if (request.getShowGameInfo() != null) {
            settings.setShowGameInfo(request.getShowGameInfo());
        }
        if (request.getShowRules() != null) {
            settings.setShowRules(request.getShowRules());
        }
        if (request.getShowPayoutTable() != null) {
            settings.setShowPayoutTable(request.getShowPayoutTable());
        }
        if (request.getEnableChat() != null) {
            settings.setEnableChat(request.getEnableChat());
        }
        if (request.getMobileOptimized() != null) {
            settings.setMobileOptimized(request.getMobileOptimized());
        }
        if (request.getTabletOptimized() != null) {
            settings.setTabletOptimized(request.getTabletOptimized());
        }
        if (request.getDesktopOptimized() != null) {
            settings.setDesktopOptimized(request.getDesktopOptimized());
        }
        if (request.getThemeColor() != null) {
            settings.setThemeColor(request.getThemeColor());
        }
        if (request.getBackgroundColor() != null) {
            settings.setBackgroundColor(request.getBackgroundColor());
        }
        if (request.getFontFamily() != null) {
            settings.setFontFamily(request.getFontFamily());
        }
        if (request.getFontSize() != null) {
            settings.setFontSize(request.getFontSize());
        }
        if (request.getLanguageSelectorEnabled() != null) {
            settings.setLanguageSelectorEnabled(request.getLanguageSelectorEnabled());
        }
        if (request.getCurrencySelectorEnabled() != null) {
            settings.setCurrencySelectorEnabled(request.getCurrencySelectorEnabled());
        }
        if (request.getLogoutButtonEnabled() != null) {
            settings.setLogoutButtonEnabled(request.getLogoutButtonEnabled());
        }
        
        settings.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorUISettings saved = uiSettingsRepository.save(settings);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("onlyJSMpeg", saved.getOnlyJSMpeg());
        newValues.put("showCurrency", saved.getShowCurrency());
        newValues.put("allowFullScreen", saved.getAllowFullScreen());
        newValues.put("themeColor", saved.getThemeColor());
        
        auditService.logAuditEvent(
            operatorId,
            "UI_SETTINGS_UPDATED",
            String.format("Updated UI settings for operator %d", operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    private OperatorUISettings createDefaultSettings(Long operatorId) {
        OperatorUISettings settings = new OperatorUISettings();
        settings.setOperatorId(operatorId);
        settings.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        // All defaults are set in entity
        return settings;
    }
    
    private UISettingsResponse mapToResponse(OperatorUISettings settings) {
        return UISettingsResponse.builder()
            .id(settings.getId())
            .operatorId(settings.getOperatorId())
            .onlyJSMpeg(settings.getOnlyJSMpeg())
            .allowFullScreen(settings.getAllowFullScreen())
            .autoPlayVideo(settings.getAutoPlayVideo())
            .showCurrency(settings.getShowCurrency())
            .showBalance(settings.getShowBalance())
            .showGameHistory(settings.getShowGameHistory())
            .showBetHistory(settings.getShowBetHistory())
            .showStatistics(settings.getShowStatistics())
            .enableSoundEffects(settings.getEnableSoundEffects())
            .enableBackgroundMusic(settings.getEnableBackgroundMusic())
            .enableAnimations(settings.getEnableAnimations())
            .enableTooltips(settings.getEnableTooltips())
            .showSidePanel(settings.getShowSidePanel())
            .showTopBar(settings.getShowTopBar())
            .showBottomBar(settings.getShowBottomBar())
            .compactMode(settings.getCompactMode())
            .showGameInfo(settings.getShowGameInfo())
            .showRules(settings.getShowRules())
            .showPayoutTable(settings.getShowPayoutTable())
            .enableChat(settings.getEnableChat())
            .mobileOptimized(settings.getMobileOptimized())
            .tabletOptimized(settings.getTabletOptimized())
            .desktopOptimized(settings.getDesktopOptimized())
            .themeColor(settings.getThemeColor())
            .backgroundColor(settings.getBackgroundColor())
            .fontFamily(settings.getFontFamily())
            .fontSize(settings.getFontSize())
            .languageSelectorEnabled(settings.getLanguageSelectorEnabled())
            .currencySelectorEnabled(settings.getCurrencySelectorEnabled())
            .logoutButtonEnabled(settings.getLogoutButtonEnabled())
            .createdBy(settings.getCreatedBy())
            .createdAt(settings.getCreatedAt())
            .updatedBy(settings.getUpdatedBy())
            .updatedAt(settings.getUpdatedAt())
            .build();
    }
}
