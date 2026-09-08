import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { SkillService } from '../../../../core/services/skill.service';
import { SnackbarService } from '../../../../core/services/snackbar.service';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { FileUploadComponent } from "../../../../shared/components/file-upload/file-upload.component";

@Component({
  selector: 'app-skill-create-dialog',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, FileUploadComponent],
  templateUrl: './skill-create-dialog.component.html',
  styleUrl: './skill-create-dialog.component.scss'
})
export class SkillCreateDialogComponent implements OnInit{
  loading = false;
  form!: FormGroup;

  icon: File | null = null;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly dialogRef: MatDialogRef<SkillCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      mode: 'create' | 'edit';
      skill?: {
        id: number;
        name: string;
        description : string;
      };
    },
    private skillService: SkillService,
    private snackBarService : SnackbarService
  ) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      name: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],
      description : [''],
      icon: [null, Validators.required]
    });
    if (this.data.mode === 'edit' && this.data.skill) {
      this.skillService.getIcon(this.data.skill.id).subscribe({
        next: (file) => {
          this.form.get('icon')?.setValue(file);
          const extension = file.type.split('/')[1]
          this.icon = new File(
            [file],
            `logo.${extension}`,
            { type: file.type }
          );
        }
      })
      this.form.patchValue({
        name: this.data.skill.name,
        description : this.data.skill.description
      });
    }
  }

  get title(): string {
    return this.data.mode === 'create'
      ? 'Create Skill'
      : 'Edit Skill';
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
    this.loading = true;
    const request = {
      name: this.form.value.name,
      description : this.form.value.description
    }
    const formData = new FormData();
    formData.append(
      'skill',
      new Blob(
        [JSON.stringify(request)],
        {
          type: 'application/json'
        }
      )
    )
    if (this.icon) {
      formData.append(
        'icon',
        this.icon,
        this.icon.name
      );
    }

    if (this.data.mode === "edit") {
      this.skillService.editSkill(this.data.skill!.id, formData)
        .pipe(
          finalize(() => this.loading = false)
        )
        .subscribe({
          next: (response: ApiResponse<void>) => {
            this.dialogRef.close();
          }
        }
        );
    }
    else {
      this.skillService.createSkill(formData)
        .pipe(
          finalize(() => this.loading = false)
        )
        .subscribe({
          next: (response: ApiResponse<void>) => {
            this.dialogRef.close();
          }
        }
        );
    }
  }

  onIconSelect(file: File | null): void {
    if (file == null) {
      this.icon = null;
      return;
    }
    const allowedTypes = [
      'image/png',
      'image/svg+xml'
    ];
    if (!allowedTypes.includes(file.type)) {
      this.icon = null;
      this.snackBarService.error(
        'Only PNG and SVG files are allowed.'
      );
      return;
    }
    this.icon = file;
    this.form.get('icon')?.setValue(file);
    this.form.markAsTouched();
  }
}
