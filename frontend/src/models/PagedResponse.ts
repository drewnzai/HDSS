export interface PagedResponse<T> {
    size: number;
    page: number;
    totalElements: number;
    totalPages: number;
    data: T[];
}