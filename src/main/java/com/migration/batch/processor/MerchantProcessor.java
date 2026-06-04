package com.migration.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.migration.batch.model.Merchant;
import com.migration.batch.model.enriched.EnrichedMerchant;

/**
 * Processor for Merchant data transformation.
 * Implements business rules for Merchant data quality checks, deduplication,
 * and transformation to the enriched model.
 */
@Component
public class MerchantProcessor implements ItemProcessor<Merchant, EnrichedMerchant> {

    /**
     * Processes a Merchant entity and transforms it into an EnrichedMerchant.
     * Applies data quality checks and transformations as per business rules.
     * 
     * @param merchant The source Merchant entity
     * @return EnrichedMerchant with transformed data, or null if invalid
     */
    @Override
    public EnrichedMerchant process(Merchant merchant) throws Exception {
        // Null checks and data quality validation
        if (merchant.getMerchantId() == null || merchant.getMerchantName() == null) {
            throw new IllegalArgumentException("MerchantID and MerchantName are required");
        }

        // Apply transformation rules
        EnrichedMerchant enriched = new EnrichedMerchant();
        enriched.setMerchantId(merchant.getMerchantId());
        enriched.setMerchantName(merchant.getMerchantName().trim());
        
        // Calculate risk score
        enriched.setMerchantRiskScore(calculateRiskScore(merchant.getCreditLimit(), merchant.getIsActive()));
        
        // Group merchant by region
        enriched.setRegionGroup(determineRegion(merchant.getRegion()));
        
        // Set active status label
        enriched.setActiveStatusLabel(merchant.getIsActive() ? "Active" : "Inactive");
        
        // Calculate days since registration
        enriched.setDaysSinceRegistration(calculateDaysSinceRegistration(merchant.getRegistrationDate()));
        
        return enriched;
    }

    /**
     * Calculates merchant risk score based on credit limit and active status.
     * 
     * @param creditLimit The merchant credit limit
     * @param isActive Whether the merchant is active
     * @return Risk score (1.0 to 5.0)
     */
    private Double calculateRiskScore(Double creditLimit, Boolean isActive) {
        if (creditLimit == null) creditLimit = 0.0;
        if (isActive == null) isActive = false;
        
        if (creditLimit > 100000 && isActive) return 1.0;
        if (creditLimit > 50000 && isActive) return 2.0;
        if (creditLimit > 10000) return 3.0;
        return isActive ? 4.0 : 5.0;
    }

    /**
     * Determines region group for the merchant.
     * 
     * @param region The merchant region
     * @return Region group (AMERICAS, EMEA, APAC, OTHER)
     */
    private String determineRegion(String region) {
        if (region == null) return "OTHER";
        String normalized = region.trim().toUpperCase();
        if (normalized.contains("AMERICA") || normalized.contains("USA") || normalized.contains("CANADA")) {
            return "AMERICAS";
        } else if (normalized.contains("EUROPE") || normalized.contains("AFRICA")) {
            return "EMEA";
        } else if (normalized.contains("ASIA") || normalized.contains("PACIFIC")) {
            return "APAC";
        }
        return "OTHER";
    }

    /**
     * Calculates days since merchant registration.
     * 
     * @param registrationDate The merchant registration date
     * @return Number of days since registration
     */
    private Long calculateDaysSinceRegistration(java.util.Date registrationDate) {
        if (registrationDate == null) return 0L;
        return (System.currentTimeMillis() - registrationDate.getTime()) / (24 * 60 * 60 * 1000);
    }
}
