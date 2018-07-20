package org.simple.clinic.patient

enum class PatientEntryValidationError {
  PERSONAL_DETAILS_EMPTY,
  FULL_NAME_EMPTY,
  PHONE_NUMBER_NON_NULL_BUT_BLANK,
  PHONE_NUMBER_EMPTY,
  BOTH_DATEOFBIRTH_AND_AGE_ABSENT,
  BOTH_DATEOFBIRTH_AND_AGE_PRESENT,
  INVALID_DATE_OF_BIRTH,
  DATE_OF_BIRTH_IN_FUTURE,
  MISSING_GENDER,

  EMPTY_ADDRESS_DETAILS,
  COLONY_OR_VILLAGE_EMPTY,
  COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK,
  DISTRICT_EMPTY,
  STATE_EMPTY
}
