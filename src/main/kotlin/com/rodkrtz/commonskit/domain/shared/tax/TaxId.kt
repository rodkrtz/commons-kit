package com.rodkrtz.commonskit.domain.shared.tax

import com.rodkrtz.commonskit.domain.shared.region.Country

public sealed interface TaxId {
    public val value: String
    public val country: Country
}