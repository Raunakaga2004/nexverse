import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { CourseModule } from '../../../../core/models/course.model';

@Component({
  selector: 'app-create-module-dialog',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS],
  templateUrl: './create-module-dialog.component.html',
  styleUrl: './create-module-dialog.component.scss'
})
export class CreateModuleDialogComponent implements OnInit {
  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CreateModuleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CourseModule | null
  ) { }
  ngOnInit(): void {
    this.moduleForm = this.formBuilder.group({
      title: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],
      description: [
        '',
        Validators.maxLength(1000)
      ],
      isMandatory: [true]
    });

    if (!this.data) {
      return;
    }

    this.moduleForm.patchValue({
      title: this.data.title,
      description: this.data.description,
      isMandatory: this.data.isMandatory
    });
  }

  moduleForm!: FormGroup<{
    title: FormControl<string | null>,
    description: FormControl<string | null>,
    isMandatory: FormControl<boolean | null>
  }>


  save(): void {

    if (this.moduleForm.invalid) {
      this.moduleForm.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.moduleForm.value);

  }
}
