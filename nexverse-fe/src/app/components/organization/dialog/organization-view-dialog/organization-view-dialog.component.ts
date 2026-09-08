import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogTitle } from '@angular/material/dialog'
import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FileSizePipe } from '../../../../shared/pipes/file-size.pipe';
import { OrganizationDetail } from '../../../../core/models/organization-detail.model';
import { environment } from '../../../../../environments/environment.development';

@Component({
  selector: 'app-organization-view-dialog',
  imports: [MATERIAL_IMPORTS, NgClass, FileSizePipe, DatePipe, DecimalPipe],
  templateUrl: './organization-view-dialog.component.html',
  styleUrl: './organization-view-dialog.component.scss'
})
export class OrganizationViewDialogComponent implements OnInit{
  totalStorgeLimits = 10 * 1024 * 1024 *1024
  constructor(@Inject(MAT_DIALOG_DATA) public organization : OrganizationDetail){} // what is Inject here and Mat dialog data
  ngOnInit(): void {
    this.orgLogoUrl = `${environment.apiUrl}/organization/${this.organization.id}/logo`;
  }
  orgLogoUrl: string|undefined 
}