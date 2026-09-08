export interface OrganizationDetail {
    id : number;
    name : string;
    logoUrl : string;
    email : string;
    phone : string;
    address : string;
    city : string;
    state : string;
    zipCode : string;
    country: string;
    status: string;
    createdAt : string;
    updatedAt : string;
    enabled : boolean;
    orgAdmin : Admin;
    totalStorageBytes : number;
}

export interface Admin {
    firstName : string;
    lastName : string;
    email : string;
    phone : string;
    status : string;
}