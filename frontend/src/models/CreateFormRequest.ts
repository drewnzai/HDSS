import type { FormCategory, FormTarget } from "./types/FormTypes";


export interface CreateFormRequest{
    name: string;
    title: string;
    category: FormCategory;
    target: FormTarget;
    description: string; 
}