import { Component, Inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';

@Component({
  selector: 'app-department-create-dialog',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS],
  templateUrl: './department-create-dialog.component.html',
  styleUrl: './department-create-dialog.component.scss'
})
export class DepartmentCreateDialogComponent {
  loading = false;
  form!: FormGroup<{
    name: FormControl<string>;
  }>;


  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly dialogRef: MatDialogRef<DepartmentCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      mode: 'create' | 'edit';
      department?: {
        id: string;
        name: string;
      };
    }
  ) { }

  ngOnInit(): void {
    this.form = this.formBuilder.nonNullable.group({
      name: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ]
    });
    if (this.data.mode === 'edit' && this.data.department) {
      this.form.patchValue({
        name: this.data.department.name
      });
    }
  }

  get title(): string {
    return this.data.mode === 'create'
      ? 'Create Department'
      : 'Edit Department';
  }

  get actionLabel(): string {
    return this.data.mode === 'create'
      ? 'Create'
      : 'Save Changes';
  }

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close({
      name: this.form.controls.name.value.trim()
    });
  }
}
