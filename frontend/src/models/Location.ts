export type LocationType =
    | "COUNTRY"
    | "COUNTY"
    | "SUB_COUNTY"
    | "DIVISION"
    | "LOCATION"
    | "SUB_LOCATION";

export const LOCATION_TYPE_ORDER: LocationType[] = [
    "COUNTRY",
    "COUNTY",
    "SUB_COUNTY",
    "DIVISION",
    "LOCATION",
    "SUB_LOCATION"
];

export const LOCATION_TYPE_LABELS: Record<LocationType, string> = {
    COUNTRY: "Country",
    COUNTY: "County",
    SUB_COUNTY: "Sub-County",
    DIVISION: "Division",
    LOCATION: "Location",
    SUB_LOCATION: "Sub-Location"
};

export function childTypeOf(type: LocationType): LocationType | null {
    const idx = LOCATION_TYPE_ORDER.indexOf(type);
    if (idx === -1 || idx === LOCATION_TYPE_ORDER.length - 1) return null;
    return LOCATION_TYPE_ORDER[idx + 1];
}

export interface LocationDto {
    id: number;
    name: string;
    type: LocationType;
    parentId: number | null;
    code: string | null;
}