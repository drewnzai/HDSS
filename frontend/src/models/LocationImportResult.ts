import type { LocationType } from "./Location";

export interface LocationImportResult {
    rowsProcessed: number;
    created: Partial<Record<LocationType, number>>;
    reused: Partial<Record<LocationType, number>>;
    errors: string[];
}