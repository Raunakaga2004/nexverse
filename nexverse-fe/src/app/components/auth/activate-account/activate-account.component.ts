import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { SetPasswordComponent } from '../../../shared/components/set-password/set-password.component';
import { AuthService } from '../../../core/services/auth.service';
import { SetPasswordRequest } from '../../../core/models/set-password.model';
import { ApiResponse } from '../../../core/models/api-response.model';
import { ErrorResponse } from '../../../core/models/error-response.model';

@Component({
    selector: 'app-activate-account',
    imports: [SetPasswordComponent],
    templateUrl: './activate-account.component.html'
})
export class ActivateAccountComponent implements OnInit {
    token!: string;
    isSubmitting = false;
    errorMessage: string | undefined;
    accountActivated: boolean = false;
    accountActivatedMessage: string = "";
    constructor(private authService: AuthService, private route: ActivatedRoute) { }
    ngOnInit(): void {
        this.token = this.route.snapshot.paramMap.get('token') || "";
    }
    activateAccount(newPassword: string): void {
        this.isSubmitting = true;
        const activateAccount: SetPasswordRequest = {
            newPassword,
            token: this.token
        }
        this.authService.activateAccount(activateAccount)
            .pipe(finalize(() => this.isSubmitting = false))
            .subscribe({
                next: (response: ApiResponse<void>) => {
                    this.accountActivated = true;
                    this.accountActivatedMessage = response.message;
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
}