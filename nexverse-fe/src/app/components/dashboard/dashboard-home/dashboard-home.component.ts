import { Component } from '@angular/core';
import { CurrentUserService } from '../../../core/services/current-user.service';
import { SuperAdminDashboardComponent } from '../super-admin-dashboard/super-admin-dashboard.component';
import { OrgAdminDashboardComponent } from '../org-admin-dashboard/org-admin-dashboard.component';
import { DepManagerDashboardComponent } from '../dep-manager-dashboard/dep-manager-dashboard.component';
import { EmployeeDashboardComponent } from "../employee-dashboard/employee-dashboard.component";
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-home',
  imports: [SuperAdminDashboardComponent, OrgAdminDashboardComponent, DepManagerDashboardComponent, EmployeeDashboardComponent],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.scss'
})
export class DashboardHomeComponent{
  constructor(private currentUserService: CurrentUserService, private router: Router) {
    this.role = this.currentUserService.getCurrentUserRole();
    this.urlPath = router.url;
  }
  role: string | undefined;
  urlPath : string | undefined;
}