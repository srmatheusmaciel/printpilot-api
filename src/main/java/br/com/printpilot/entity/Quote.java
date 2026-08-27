package br.com.printpilot.entity;

import br.com.printpilot.enums.PricingType;
import br.com.printpilot.enums.QuoteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com produto (referência viva)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Relacionamento com material (referência viva)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    // Snapshots — imutáveis ao longo do tempo
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "material_name", nullable = false, length = 150)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false, length = 30)
    private PricingType pricingType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 4)
    private BigDecimal width;

    @Column(precision = 12, scale = 4)
    private BigDecimal height;

    @Column(name = "unit_area", precision = 12, scale = 4)
    private BigDecimal unitArea;

    @Column(name = "total_area", precision = 12, scale = 4)
    private BigDecimal totalArea;

    @Column(name = "units_per_sheet")
    private Integer unitsPerSheet;

    @Column(name = "required_sheets")
    private Integer requiredSheets;

    // Componentes financeiros calculados pelo Pricing Engine
    @Column(name = "material_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal materialCost;

    @Column(name = "printing_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal printingCost;

    @Column(name = "finishing_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal finishingCost;

    @Column(name = "waste_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal wasteCost;

    @Column(name = "labor_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "margin_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal marginPercentage;

    @Column(name = "suggested_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal suggestedPrice;

    // Preço que pode ser ajustado manualmente no futuro
    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuoteStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = QuoteStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
