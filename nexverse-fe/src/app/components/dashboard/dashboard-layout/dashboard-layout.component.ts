import { Component, OnInit } from '@angular/core';
import { DashboardSidebarComponent } from "../dashboard-sidebar/dashboard-sidebar.component";
import { DashboardNavbarComponent } from "../dashboard-navbar/dashboard-navbar.component";
import { RouterOutlet } from '@angular/router';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { UserService } from '../../../core/services/user.service';
import { CurrentUserService } from '../../../core/services/current-user.service';
import { CurrentUser } from '../../../core/models/current-user.model';
import { ApiResponse } from '../../../core/models/api-response.model';


@Component({
  selector: 'app-dashboard-layout',
  imports: [MATERIAL_IMPORTS, RouterOutlet, DashboardSidebarComponent, DashboardNavbarComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss'
})
export class DashboardLayoutComponent implements OnInit {
  constructor(private userService: UserService, private currentUserService: CurrentUserService) { }
  currentUser?: CurrentUser;
  loading: boolean = true;
  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (response: ApiResponse<CurrentUser>) => {
        this.currentUser = response.data;
        if (this.currentUser)
          this.currentUserService.setCurrentUser(this.currentUser);
        this.loading = false;
      }
    });
  }
}