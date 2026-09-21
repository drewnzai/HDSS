export type FormCategory = "CORE" | "EXTRA";
export type FormTarget = "INDIVIDUAL" | "HOUSEHOLD";


export interface FormDto{
    id: number;
    name: string;
    title: string;
    category: FormCategory;
    target: FormTarget;
    version: number;
    description: string;
    active: boolean;
    locked: boolean;
}