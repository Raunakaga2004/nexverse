import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { SnackbarService } from '../../../../core/services/snackbar.service';
import { OrganizationService } from '../../../../core/services/organization.service';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { FileUploadComponent } from "../../../../shared/components/file-upload/file-upload.component";
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-organization-form',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule, RouterLink, FileUploadComponent, CommonModule],
  templateUrl: './organization-form.component.html',
  styleUrl: './organization-form.component.scss'
})
export class OrganizationFormComponent implements OnInit {
  constructor(private formBuilder: FormBuilder, private router: Router, private route: ActivatedRoute, private organizationService: OrganizationService, private snackBarService: SnackbarService) { }

  isSubmitting = false;
  isEdit = false;
  organizationId!: number;

  organizationForm!: FormGroup;
  adminForm!: FormGroup;

  logoFile: File | null = null;

  ngOnInit() {
    this.buildForm();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.organizationId = +id;
      this.loadOrganization();
    }
  }
  private buildForm(): void {
    this.organizationForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
      address: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      country: ['', Validators.required],
      zipCode: ['', Validators.required],
      logo: [null, Validators.required]
    });
    if (!this.isEdit) {
      this.adminForm = this.formBuilder.group({
        firstName: ['', Validators.required],
        lastName: [''],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
      })
    }
  }

  private loadOrganization(): void {
    this.organizationService.getOrganization(this.organizationId).subscribe(response => {
      const org = response.data;
      this.organizationForm.patchValue({
        name: org!.name,
        email: org!.email,
        phone: org!.phone,
        address: org!.address,
        city: org!.city,
        state: org!.state,
        country: org!.country,
        zipCode: org!.zipCode,
      })
      this.organizationService.getLogo(org!.id).subscribe({
        next: (file) => {
          this.organizationForm.get('logo')?.setValue(file);
          const extension = file.type.split('/')[1]
          this.logoFile = new File(
            [file],
            `logo.${extension}`,
            { type: file.type }
          );
        }
      });
      if (!this.isEdit) {
        this.adminForm.patchValue({
          firstName: org!.orgAdmin.firstName,
          lastName: org!.orgAdmin.lastName,
          email: org!.orgAdmin.email,
          phone: org!.orgAdmin.phone,
        })
      }

    })
  }

  submit(): void {
    if (this.organizationForm.invalid || (!this.isEdit && this.adminForm.invalid)) {
      this.organizationForm.markAllAsTouched();
      this.adminForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const request = {
      name: this.organizationForm.value.name,
      email: this.organizationForm.value.email,
      phone: this.organizationForm.value.phone,
      address: this.organizationForm.value.address,
      city: this.organizationForm.value.city,
      state: this.organizationForm.value.state,
      country: this.organizationForm.value.country,
      zipCode: this.organizationForm.value.zipCode,
      orgAdminFirstName: this.adminForm.value.firstName,
      orgAdminLastName: this.adminForm.value.lastName,
      orgAdminEmail: this.adminForm.value.email,
      orgAdminPhone: this.adminForm.value.phone
    };
    const formData = new FormData();
    formData.append(
      'organization',
      new Blob(
        [JSON.stringify(request)],
        {
          type: 'application/json'
        }
      )
    );
    if (this.logoFile) {
      formData.append(
        'logoUrl',
        this.logoFile,
        this.logoFile.name
      );
    }
    if (this.isEdit) {
      this.organizationService.editOrganization(this.organizationId, formData)
        .pipe(
          finalize(() => this.isSubmitting = false)
        )
        .subscribe({
          next: (response: ApiResponse<void>) => {
            this.router.navigateByUrl('/dashboard/organizations')
            this.snackBarService.success(response.message)
            this.isSubmitting = false;
          }
        }
        );
    }
    else {
      this.organizationService.createOrganization(formData)
        .pipe(
          finalize(() => this.isSubmitting = false)
        )
        .subscribe({
          next: (response: ApiResponse<void>) => {
            this.router.navigateByUrl('/dashboard/organizations')
            this.snackBarService.success(response.message)
            this.isSubmitting = false;
          }
        });
    }
  }

  oncancel(): void {
    this.router.navigateByUrl('/dashboard/organizations')
  }

  onLogoSelected(file: File | null): void {
    if (file == null) {
      this.logoFile = null;
      return;
    }
    const allowedTypes = [
      'image/png',
      'image/jpeg',
      'image/jpg',
      'image/svg+xml'
    ];
    if (!allowedTypes.includes(file.type)) {
      this.logoFile = null;
      this.snackBarService.error(
        'Only PNG, JPG, JPEG and SVG files are allowed.'
      );
      return;
    }
    this.logoFile = file;
    this.organizationForm.get('logo')?.setValue(file);
    this.organizationForm.markAsTouched();
  }

  getOrganizationLocation() {
    return `${this.organizationForm.get('address')?.value}, ${this.organizationForm.get('zipCode')?.value}
    ${this.organizationForm.get('city')?.value}, ${this.organizationForm.get('state')?.value}, ${this.organizationForm.get('country')?.value}
    `
  }
}