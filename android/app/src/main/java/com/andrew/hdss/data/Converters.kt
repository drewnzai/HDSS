package com.andrew.hdss.data

import androidx.room.TypeConverter
import com.andrew.hdss.data.models.enums.HouseholdStatus
import com.andrew.hdss.data.models.enums.LocationType
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.data.models.enums.Sex
import java.time.LocalDate

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
}