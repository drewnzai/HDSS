import type { LocationType } from "./Location";

export interface CreateLocationRequest {
    name: string;
    type: LocationType;
    parentId: number | null;
    code: string;
}