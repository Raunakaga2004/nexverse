export interface PageResponse<T> {
    totalElements : number;
    totalPages : number;
    number : number;
    first : boolean;
    last : boolean;
    size : number;
    content : T[];
    empty : boolean;
}