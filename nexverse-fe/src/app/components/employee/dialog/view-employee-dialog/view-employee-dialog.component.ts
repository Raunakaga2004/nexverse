import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DatePipe, LowerCasePipe, NgClass, TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Employee } from '../../../../core/models/employee.model';
import { environment } from '../../../../../environments/environment.development';

@Component({
  selector: 'app-view-employee-dialog',
  imports: [MATERIAL_IMPORTS, DatePipe, NgClass, TitleCasePipe, LowerCasePipe],
  templateUrl: './view-employee-dialog.component.html',
  styleUrl: './view-employee-dialog.component.scss'
})
export class ViewEmployeeDialogComponent implements OnInit{
  profileImageUrl : string | undefined;
  constructor(
    public dialogRef: MatDialogRef<ViewEmployeeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public employee: Employee
  ) { }
  ngOnInit(): void {
    console.log(this.employee)
    this.profileImageUrl = `${environment.apiUrl}/employee/${this.employee.id}/profile-image`
  }

  get fullName(): string {
    return `${this.employee.firstName} ${this.employee.lastName ?? ""}`;
  }

  get initials(): string {
    return (
      (this.employee.firstName?.charAt(0))
    ).toUpperCase();
  }

  close(): void {
    this.dialogRef.close();
  }
}
