package com.samoondigital.yojnaplus.domain.model

/** Supported lookup strategies on the Electoral Search screen. */
enum class SearchType(val label: String) {
    MOBILE("Mobile Number"),
    EPIC("EPIC Number"),
    NAME_DOB("Name / DOB"),
}
