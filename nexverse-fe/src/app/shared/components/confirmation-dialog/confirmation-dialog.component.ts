import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { ConfirmationDialogData } from '../../../core/models/confirmation-dialog.model';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';

@Component({
  selector: 'app-confirmation-dialog',
  imports: [MATERIAL_IMPORTS],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent {
  constructor(private dialogRef : MatDialogRef<ConfirmationDialogComponent>, @Inject(MAT_DIALOG_DATA) public data : ConfirmationDialogData){}

  cancel() : void {
    this.dialogRef.close(false);
  }

  confirm() : void {
    this.dialogRef.close(true);
  }
}