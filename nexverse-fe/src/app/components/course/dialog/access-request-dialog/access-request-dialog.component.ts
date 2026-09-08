import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';

@Component({
  selector: 'app-access-request-dialog',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule],
  templateUrl: './access-request-dialog.component.html',
  styleUrl: './access-request-dialog.component.scss'
})
export class AccessRequestDialogComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly form = this.formBuilder.group({
    reason: ['']
  });

  constructor(
    private dialogRef: MatDialogRef<AccessRequestDialogComponent>
  ) { }

  submit(): void {
    const reason = this.form.get('reason')?.value?.trim() ?? '';
    this.dialogRef.close(reason);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
