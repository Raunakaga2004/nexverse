export interface ImportEmployeeResponse {
    totalRows: number;
    successfulImports: number;
    failedImports: number;
    errors: EmployeeImportError[];
}

export interface EmployeeImportError {
    rowNumber: number;
    columnIndex: number;
    rejectedValue: string | null;
    reason: string;
}