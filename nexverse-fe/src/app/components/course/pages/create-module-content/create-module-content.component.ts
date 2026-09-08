import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FileUploadComponent } from '../../../../shared/components/file-upload/file-upload.component';
import { ContentType } from '../../../../core/models/course.model';
import { CourseService } from '../../../../core/services/course.service';

@Component({
  selector: 'app-create-module-content',
  imports: [MATERIAL_IMPORTS, FileUploadComponent, ReactiveFormsModule],
  templateUrl: './create-module-content.component.html',
  styleUrl: './create-module-content.component.scss'
})
export class CreateModuleContentComponent {
  contentForm!: FormGroup<{
    title: FormControl<string>;
    description: FormControl<string>;
    contentType: FormControl<ContentType>;
    estimatedDurationMinutes: FormControl<number>;
    isMandatory: FormControl<boolean>;
    contentFile: FormControl<File | null>;
    textBody: FormControl<string | null>;
  }>;
  protected readonly contentTypes = Object.values(ContentType);
  courseId!: number;
  moduleId!: number;
  contentId!: number;
  isEditMode = false;
  loading = false;
  saving = false;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService
  ) { }

  ngOnInit(): void {
    this.initializeForm();
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
    this.moduleId = Number(this.route.snapshot.paramMap.get('moduleId'));
    const id = this.route.snapshot.paramMap.get('contentId');
    if (id) {
      this.isEditMode = true;
      this.contentId = Number(id);
      this.loadContent();
    }
    this.contentForm.controls.contentType.valueChanges.subscribe(() => {
      this.updateValidators();
    });
    this.updateValidators();
  }

  private initializeForm(): void {
    this.contentForm = this.formBuilder.group({
      title: this.formBuilder.nonNullable.control('', { validators: [Validators.required, Validators.maxLength(100)] }),
      description: this.formBuilder.nonNullable.control('', { validators: [Validators.maxLength(200)] }),
      contentType: this.formBuilder.nonNullable.control(ContentType.VIDEO, Validators.required),
      estimatedDurationMinutes: this.formBuilder.nonNullable.control(30, [Validators.required, Validators.min(0.01)]),
      isMandatory: this.formBuilder.nonNullable.control(true),
      contentFile: this.formBuilder.control<File | null>(null),
      textBody: this.formBuilder.control<string | null>(null)
    });
  }

  private loadContent(): void {
    this.loading = true;
    this.courseService
      .getContent(this.contentId)
      .subscribe({
        next: (response) => {
          const content = response.data;
          this.contentForm.patchValue({
            title: content!.title,
            description: content!.description,
            contentType: content!.contentType,
            estimatedDurationMinutes: Number((content!.estimatedDurationSeconds / 60).toFixed(2)),
            isMandatory: content!.isMandatory,
            textBody: content!.textBody ?? ''
          });
          this.updateValidators();
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private updateValidators(): void {
    const fileControl = this.contentForm.controls.contentFile;
    const textControl = this.contentForm.controls.textBody;
    fileControl.clearValidators();
    textControl.clearValidators();
    switch (this.contentForm.controls.contentType.value) {
      case ContentType.TEXT:
        textControl.setValidators([Validators.required, Validators.maxLength(50000)]);
        break;
      case ContentType.VIDEO:
      case ContentType.DOCUMENT:
        if (!this.isEditMode) {
          fileControl.setValidators([Validators.required]);
        }
        break;
    }
    fileControl.updateValueAndValidity();
    textControl.updateValueAndValidity();
  }

  onContentFileSelected(file: File | null): void {
    if (!file) {
      return;
    }
    this.contentForm.controls.contentFile.setValue(file);
    if (this.contentForm.controls.contentType.value === ContentType.VIDEO) {
      this.populateVideoDuration(file);
    }
  }

  private populateVideoDuration(file: File): void {
    const video = document.createElement('video');
    video.preload = 'metadata';
    video.onloadedmetadata = () => {
      URL.revokeObjectURL(video.src);
      const duration = Math.round(video.duration);
      this.contentForm.controls.estimatedDurationMinutes.setValue(Number((duration / 60).toFixed(2)));
    };
    video.onerror = () => {
      URL.revokeObjectURL(video.src);
    };
    video.src = URL.createObjectURL(file);
  }

  save(): void {
    if (this.contentForm.invalid) {
      this.contentForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    const contentFormValue = this.contentForm.getRawValue();
    const request = {
      ...contentFormValue,
      estimatedDurationSeconds:contentFormValue.estimatedDurationMinutes * 60,
      contentFile: contentFormValue.contentFile ?? undefined,
      textBody: contentFormValue.textBody ?? undefined
    }
    const operation = this.isEditMode
      ? this.courseService.updateContent(this.contentId, request)
      : this.courseService.createContent(this.moduleId, request);
    operation.subscribe({
      next: () => {
        this.saving =false;
        this.navigateBack();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.navigateBack();
  }

  navigateBack(): void {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${this.moduleId}/contents`])
  }
}
