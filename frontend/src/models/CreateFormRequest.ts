import type { FormCategory, FormTarget } from "./FormDto";

export interface CreateFormRequest{
    name: string;
    title: string;
    category: FormCategory;
    target: FormTarget;
    description: string; 
}