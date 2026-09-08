import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from "@angular/router";
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { PlatformLogoComponent } from '../../../shared/components/platform-logo/platform-logo.component';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { AuthService } from '../../../core/services/auth.service';
import { ErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-forgot-password',
  imports: [PlatformLogoComponent, ReactiveFormsModule, MATERIAL_IMPORTS, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  emailSent: boolean = false;
  isSubmitting = false;
  forgotPasswordForm: FormGroup;
  errorMessage: string | undefined;
  constructor(private formBuilder: FormBuilder, private authService: AuthService) {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    })
  }
  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const request = this.forgotPasswordForm.getRawValue();
    this.authService.forgotPassword(request)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response) => {
          this.emailSent = true;
        },
        error: (error: HttpErrorResponse) => {
          const errorResponse = error.error as ErrorResponse;
          this.errorMessage = errorResponse.message;
        },
        complete: () => {
          this.errorMessage = undefined;
        }
      })
  }
  get email() { 
    return this.forgotPasswordForm.get('email'); 
  }
}