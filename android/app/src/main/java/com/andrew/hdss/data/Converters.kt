package com.andrew.hdss.data

import androidx.room.TypeConverter
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.HouseholdStatus
import com.andrew.hdss.data.models.enums.LocationType
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.data.models.enums.Sex
import com.andrew.hdss.data.models.enums.VisitStatus
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromSex(value: Sex?): String? = value?.name

    @TypeConverter
    fun toSex(value: String?): Sex? = value?.let { Sex.valueOf(it) }

    @TypeConverter
    fun fromHouseholdStatus(value: HouseholdStatus?): String? = value?.name

    @TypeConverter
    fun toHouseholdStatus(value: String?): HouseholdStatus? = value?.let { HouseholdStatus.valueOf(it) }

    @TypeConverter
    fun fromMembershipStartType(value: MembershipStartType?): String? = value?.name

    @TypeConverter
    fun toMembershipStartType(value: String?): MembershipStartType? = value?.let { MembershipStartType.valueOf(it) }

    @TypeConverter
    fun fromMembershipEndType(value: MembershipEndType?): String? = value?.name

    @TypeConverter
    fun toMembershipEndType(value: String?): MembershipEndType? = value?.let { MembershipEndType.valueOf(it) }

    @TypeConverter
    fun fromRelationshipToHead(value: RelationshipToHead?): String? = value?.name

    @TypeConverter
    fun toRelationshipToHead(value: String?): RelationshipToHead? = value?.let { RelationshipToHead.valueOf(it) }

    @TypeConverter
    fun fromLocationType(value: LocationType?): String? = value?.name

    @TypeConverter
    fun toLocationType(value: String?): LocationType? = value?.let { LocationType.valueOf(it) }

    @TypeConverter
    fun fromVisitStatus(value: VisitStatus?): String? = value?.name

    @TypeConverter
    fun toVisitStates(value: String?): VisitStatus? = value?.let{ VisitStatus.valueOf(it) }

    @TypeConverter
    fun fromFormCategory(value: FormCategory): String =
        value.name

    @TypeConverter
    fun toFormCategory(value: String): FormCategory =
        FormCategory.valueOf(value)

    @TypeConverter
    fun fromFormTarget(value: FormTarget): String =
        value.name

    @TypeConverter
    fun toFormTarget(value: String): FormTarget =
        FormTarget.valueOf(value)

    @TypeConverter
    fun fromFormStatus(value: FormStatus): String =
        value.name

    @TypeConverter
    fun toFormStatus(value: String): FormStatus =
        FormStatus.valueOf(value)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? =
        value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let(LocalDateTime::parse)
}