import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PlatformLogoComponent } from '../platform-logo/platform-logo.component';
import { RouterLink } from '@angular/router';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';

@Component({
  selector: 'app-set-password',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, PlatformLogoComponent, RouterLink],
  templateUrl: './set-password.component.html',
  styleUrl: './set-password.component.scss'
})
export class SetPasswordComponent {
  @Input()
  errorMessage: string | undefined;

  @Input()
  title!: string;

  @Input()
  buttonText!: string;

  @Input()
  isSubmitting!: boolean;

  @Input()
  passwordSetted!: boolean;

  @Input()
  passwordSettedMessage!: string;

  @Output()
  passwordSubmitted = new EventEmitter<string>();

  setPasswordForm: FormGroup;
  constructor(private formBuilder: FormBuilder) {
    this.setPasswordForm = this.formBuilder.group({
      newPassword: ['', [
        Validators.required,
        Validators.pattern(
          /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/
        )
      ]],
      confirmPassword: ['', [Validators.required]]
    })
  }
  onSubmit(): void {
    if (this.setPasswordForm.invalid) {
      this.setPasswordForm.markAllAsTouched();
      return;
    }
    const formValues = this.setPasswordForm.getRawValue();
    if (formValues.confirmPassword !== formValues.newPassword) {
      this.setPasswordForm.get('confirmPassword')?.setErrors({ passwordMismatch: true })
      return;
    }
    this.passwordSubmitted.emit(
      formValues.newPassword
    )
  }

  get newPassword() { 
    return this.setPasswordForm.get('newPassword'); 
  }

  get confirmPassword() { 
    return this.setPasswordForm.get('confirmPassword'); 
  }
}