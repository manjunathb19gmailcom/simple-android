package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

data class FacilityChangeClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Facility Change:Facility Clicked"
}

data class FacilityChangeSearchQueryChanged(val query: String): UiEvent {
  override val analyticsName = "Facility Change:Facility Search Query Changed"
}
