package com.avgon.portfolio.domain;

import static com.avgon.common.domain.DecimalPolicy.positivePrice;
import static com.avgon.common.domain.DecimalPolicy.positiveQuantity;

import java.math.BigDecimal;

public record PurchaseAmount(BigDecimal price, BigDecimal quantity) {

    public PurchaseAmount {
        price = positivePrice(price, "매수가");
        quantity = positiveQuantity(quantity, "매수 수량");
    }
}
