export interface CurrentUser {
    firstName: string;
    lastName: string;
    role: "SUPER_ADMIN" | "ADMIN" | "MANAGER" | "EMPLOYEE";
    profileImageUrl?: string;
    departmentId? : number;
    departmentName? : string;
    organizationId?: number;
    organizationName?: string;
    organizationLogoUrl?: string;
}