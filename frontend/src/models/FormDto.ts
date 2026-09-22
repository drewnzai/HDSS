import type { FormCategory, FormStatus, FormTarget } from "./types/FormTypes";

export interface FormDto{
    id: number;
    name: string;
    title: string;
    category: FormCategory;
    target: FormTarget;
    version: number;
    description: string;
    active: boolean;
    status: FormStatus;
}