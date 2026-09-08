export interface Employee {
    id : number;
    firstName : string;
    lastName : string | null;
    role : string;
    status : string;
    email : string;
    phoneNumber : string | null;
    employeeCode : string | null;
    jobTitle: string | null;
    profileImageUrl : string | null;
    departmentName : string;
    createdAt : string;
    updatedAt : string;
    enabled : boolean;
}