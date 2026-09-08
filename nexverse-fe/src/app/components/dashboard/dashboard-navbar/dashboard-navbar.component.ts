import { Component, Input, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { CurrentUser } from '../../../core/models/current-user.model';
import { environment } from '../../../../environments/environment.development';
import { AuthService } from '../../../core/services/auth.service';
import { CurrentUserService } from '../../../core/services/current-user.service';

@Component({
  selector: 'app-dashboard-navbar',
  imports: [MATERIAL_IMPORTS, RouterLink],
  templateUrl: './dashboard-navbar.component.html',
  styleUrl: './dashboard-navbar.component.scss'
})
export class DashboardNavbarComponent implements OnInit{
  profileImageUrl : string | undefined;
  @Input({ required: true })
  currentUser!: CurrentUser;
  companyLogoUrl: string = `${environment.apiUrl}/user/organization/logo`;
  constructor(private authService: AuthService, private router: Router, private currentUserService: CurrentUserService) { }
  ngOnInit(): void {
    this.profileImageUrl = `${environment.apiUrl}/user/profile-image`
  }
  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigateByUrl("/login");
        this.currentUserService.clear();
      }
    });
  }
}