import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from "@angular/router";
import { finalize } from 'rxjs';
import { PlatformLogoComponent } from '../../../shared/components/platform-logo/platform-logo.component';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/login.model';
import { ApiResponse } from '../../../core/models/api-response.model';
import { ErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-login',
  imports: [
    PlatformLogoComponent,
    MATERIAL_IMPORTS,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  isSubmitting = false;
  loginForm: FormGroup;
  errorMessage: string | undefined;
  constructor(private formBuilder: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    })
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const loginFormValues: LoginRequest = this.loginForm.getRawValue();
    this.authService.login(loginFormValues)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response: ApiResponse<void>) => {
          this.router.navigateByUrl("/dashboard");
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
    return this.loginForm.get('email'); 
  }
  get password() { 
    return this.loginForm.get('password'); 
  }
}